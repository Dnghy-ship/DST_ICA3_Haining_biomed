package cn.edu.zju.controller;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.MatchedDrugLabel;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.MatchingResultDao;
import cn.edu.zju.dao.SampleDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private SampleDao sampleDao = new SampleDao();
    private AnnovarDao annovarDao = new AnnovarDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private MatchingResultDao matchingResultDao = new MatchingResultDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerPostMapping("/upload", this::uploadAnnovarOutput);
        dispatcher.registerGetMapping("/matchingIndex", this::matchingIndex);
        dispatcher.registerGetMapping("/matching", this::matching);
        dispatcher.registerGetMapping("/matchingResult", this::viewMatchingResult);
        dispatcher.registerGetMapping("/samples", this::samples);
    }

    public void matchingIndex(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("/views/matching_index.jsp").forward(request, response);
    }

    public void samples(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<Sample> samples = sampleDao.findAll();
        request.setAttribute("samples", samples);
        try {
            Set<Integer> savedResultIds = matchingResultDao.findSampleIdsWithResults();
            request.setAttribute("samplesWithResults", savedResultIds);
        } catch (Exception e) {
            log.warn("Could not load saved result IDs (matching_result table may not exist yet)", e);
        }
        request.getRequestDispatcher("/views/samples.jsp").forward(request, response);
    }

    public void matching(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String sampleIdParameter = request.getParameter("sampleId");
        if (sampleIdParameter == null) {
            request.getRequestDispatcher("/views/samples.jsp").forward(request, response);
            return;
        }
        Integer sampleId = null;
        try {
            sampleId = Integer.valueOf(sampleIdParameter);
        } catch (NumberFormatException e) {
            response.sendRedirect("samples");
            return;
        }
        List<String> refGenes = annovarDao.getRefGenes(sampleId);
        if (refGenes.isEmpty()) {
            response.sendRedirect("samples");
            return;
        }
        List<DrugLabel> drugLabels = drugLabelDao.findAll();
        List<MatchedDrugLabel> matched = doMatch(refGenes, drugLabels);
        matched.sort(Comparator.comparingInt(MatchedDrugLabel::getScore).reversed());
        try {
            matchingResultDao.saveResults(sampleId, matched);
        } catch (Exception e) {
            log.warn("Could not save matching results (matching_result table may not exist yet)", e);
        }
        request.setAttribute("matched", matched);
        request.setAttribute("sample", sampleDao.findById(sampleId));
        request.getRequestDispatcher("/views/matching_index_search.jsp").forward(request, response);
    }

    public void viewMatchingResult(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String sampleIdParameter = request.getParameter("sampleId");
        if (sampleIdParameter == null) {
            response.sendRedirect("samples");
            return;
        }
        Integer sampleId = null;
        try {
            sampleId = Integer.valueOf(sampleIdParameter);
        } catch (NumberFormatException e) {
            response.sendRedirect("samples");
            return;
        }
        List<MatchedDrugLabel> matched = matchingResultDao.findBySampleId(sampleId);
        if (matched.isEmpty()) {
            response.sendRedirect("matching?sampleId=" + sampleId);
            return;
        }
        request.setAttribute("matched", matched);
        request.setAttribute("sample", sampleDao.findById(sampleId));
        request.getRequestDispatcher("/views/matching_result.jsp").forward(request, response);
    }

    private List<MatchedDrugLabel> doMatch(List<String> refGenes, List<DrugLabel> drugLabels) {
        List<MatchedDrugLabel> matchedLabels = new ArrayList<>();
        for (DrugLabel drugLabel : drugLabels) {
            List<String> matchedGenes = new ArrayList<>();
            String summary = drugLabel.getSummaryMarkdown() != null ? drugLabel.getSummaryMarkdown() : "";
            for (String gene : refGenes) {
                if (summary.contains(gene)) {
                    matchedGenes.add(gene);
                }
            }
            if (!matchedGenes.isEmpty()) {
                int score = calculateScore(drugLabel);
                String recLevel = getRecommendationLevel(score);
                matchedLabels.add(new MatchedDrugLabel(drugLabel, score, recLevel, matchedGenes));
            }
        }
        return matchedLabels;
    }

    private int calculateScore(DrugLabel label) {
        String summary = label.getSummaryMarkdown() != null ? label.getSummaryMarkdown() : "";
        if (summary.contains("1A") || summary.contains("Level 1A")) return 10;
        if (summary.contains("1B") || summary.contains("Level 1B")) return 8;
        if (summary.contains("2A") || summary.contains("Level 2A")) return 5;
        if (summary.contains("2B") || summary.contains("Level 2B")) return 3;
        if (summary.contains("Level 3")) return 1;
        if (label.isDosingInformation()) return 4;
        return 1;
    }

    private String getRecommendationLevel(int score) {
        if (score >= 8) return "Strong";
        if (score >= 4) return "Moderate";
        return "Optional";
    }

    public void uploadAnnovarOutput(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uploadedBy = request.getParameter("uploaded_by");
        if (uploadedBy == null || uploadedBy.isBlank()) {
            request.setAttribute("validateError", "Uploaded by can not be blank");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        Part requestPart = request.getPart("annovar");
        if (requestPart == null) {
            request.setAttribute("validateError", "annovar output file can not be blank");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        InputStream inputStream = requestPart.getInputStream();
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes);
        int sampleId = sampleDao.save(uploadedBy);
        try {
            annovarDao.save(sampleId, content);
        } catch (ArrayIndexOutOfBoundsException e) {
            request.setAttribute("validateError", "annovar output file is invalid");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        response.sendRedirect("matching?sampleId=" + sampleId);
    }
}
