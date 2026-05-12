package cn.edu.zju.controller;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.MatchedDrugLabel;
import cn.edu.zju.bean.PatientProfile;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.bean.WarfarinDoseSummary;
import cn.edu.zju.bean.VariantAnnotation;
import cn.edu.zju.bean.VariantBioDetails;
import cn.edu.zju.bean.VariantCore;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.MatchingResultDao;
import cn.edu.zju.dao.PatientProfileDao;
import cn.edu.zju.dao.SampleDao;
import cn.edu.zju.service.DosageCalculatorService;
import cn.edu.zju.servlet.DispatchServlet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);
    private static final Pattern EVIDENCE_LEVEL_1A = Pattern.compile("\\b(LEVEL\\s*1A|1A)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVIDENCE_LEVEL_1B = Pattern.compile("\\b(LEVEL\\s*1B|1B)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVIDENCE_LEVEL_2A = Pattern.compile("\\b(LEVEL\\s*2A|2A)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVIDENCE_LEVEL_2B = Pattern.compile("\\b(LEVEL\\s*2B|2B)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVIDENCE_LEVEL_3 = Pattern.compile("\\bLEVEL\\s*3\\b", Pattern.CASE_INSENSITIVE);
    private static final String BENIGN_SYNONYMOUS_SNV = "synonymous snv";

    private SampleDao sampleDao = new SampleDao();
    private AnnovarDao annovarDao = new AnnovarDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private MatchingResultDao matchingResultDao = new MatchingResultDao();
    private PatientProfileDao patientProfileDao = new PatientProfileDao();
    private DosageCalculatorService dosageCalculatorService = new DosageCalculatorService();

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
        List<VariantCore> variants = annovarDao.findAnnotationsBySampleId(sampleId);
        Set<String> patientGenes = collectPatientGenesExcludingBenignVariants(variants);
        if (patientGenes.isEmpty()) {
            response.sendRedirect("samples");
            return;
        }
        List<DrugLabel> drugLabels = drugLabelDao.findAll();
        List<MatchedDrugLabel> matched = doMatch(drugLabels, patientGenes);
        PatientProfile profile = patientProfileDao.findBySampleId(sampleId);
        boolean warfarinMatched = applyWarfarinDose(profile, matched, variants);
        WarfarinDoseSummary doseSummary = dosageCalculatorService.buildWarfarinDoseSummary(profile, variants, warfarinMatched);
        matched.sort(Comparator.comparingInt(MatchedDrugLabel::getScore).reversed());
        try {
            matchingResultDao.saveResults(sampleId, matched);
        } catch (Exception e) {
            log.warn("Could not save matching results (matching_result table may not exist yet)", e);
        }
        request.setAttribute("matched", matched);
        request.setAttribute("sample", sampleDao.findById(sampleId));
        request.setAttribute("patientProfile", profile);
        request.setAttribute("warfarinDoseSummary", doseSummary);
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
        List<VariantCore> variants = annovarDao.findAnnotationsBySampleId(sampleId);
        PatientProfile profile = patientProfileDao.findBySampleId(sampleId);
        boolean warfarinMatched = applyWarfarinDose(profile, matched, variants);
        WarfarinDoseSummary doseSummary = dosageCalculatorService.buildWarfarinDoseSummary(profile, variants, warfarinMatched);
        request.setAttribute("matched", matched);
        request.setAttribute("sample", sampleDao.findById(sampleId));
        request.setAttribute("patientProfile", profile);
        request.setAttribute("warfarinDoseSummary", doseSummary);
        request.getRequestDispatcher("/views/matching_result.jsp").forward(request, response);
    }

    private List<MatchedDrugLabel> doMatch(List<DrugLabel> drugLabels, Set<String> patientGenes) {
        List<MatchedDrugLabel> matchedLabels = new ArrayList<>();
        if (drugLabels == null || drugLabels.isEmpty() || patientGenes == null || patientGenes.isEmpty()) {
            return matchedLabels;
        }
        for (DrugLabel drugLabel : drugLabels) {
            if (drugLabel == null) {
                continue;
            }
            Set<String> labelGenes = extractGenesFromLabelSummary(drugLabel);
            if (labelGenes.isEmpty()) {
                continue;
            }
            List<String> matchedGenes = new ArrayList<>();
            for (String patientGene : patientGenes) {
                if (labelGenes.contains(patientGene)) {
                    matchedGenes.add(patientGene);
                }
            }
            if (!matchedGenes.isEmpty()) {
                int score = calculateScore(drugLabel, patientGenes);
                String recLevel = getRecommendationLevel(score);
                matchedLabels.add(new MatchedDrugLabel(drugLabel, score, recLevel, matchedGenes));
            }
        }
        return matchedLabels;
    }

    private int calculateScore(DrugLabel label, Set<String> patientGenes) {
        if (label == null || patientGenes == null || patientGenes.isEmpty()) {
            return 0;
        }
        String summary = Optional.ofNullable(label.getSummaryMarkdown()).orElse("");
        int evidenceScore = 0;
        if (EVIDENCE_LEVEL_1A.matcher(summary).find()) {
            evidenceScore = 10;
        } else if (EVIDENCE_LEVEL_1B.matcher(summary).find()) {
            evidenceScore = 8;
        } else if (EVIDENCE_LEVEL_2A.matcher(summary).find()) {
            evidenceScore = 5;
        } else if (EVIDENCE_LEVEL_2B.matcher(summary).find()) {
            evidenceScore = 3;
        } else if (EVIDENCE_LEVEL_3.matcher(summary).find()) {
            evidenceScore = 1;
        }

        if (label.isDosingInformation()) {
            evidenceScore += 4;
        }
        return Math.max(evidenceScore, 1);
    }

    private String getRecommendationLevel(int score) {
        if (score >= 8) return "Strong";
        if (score >= 4) return "Moderate";
        return "Optional";
    }

    private Set<String> collectPatientGenesExcludingBenignVariants(List<VariantCore> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> genes = new LinkedHashSet<>();
        for (VariantCore variant : variants) {
            if (variant == null || variant.getAnnotation() == null) {
                continue;
            }
            if (isBenignSynonymousVariant(variant)) {
                continue;
            }
            String geneSymbol = Optional.ofNullable(variant.getAnnotation())
                    .map(VariantAnnotation::getGeneSymbol)
                    .orElse(null);
            if (geneSymbol == null || geneSymbol.isBlank()) {
                continue;
            }
            String[] splitGenes = geneSymbol.split("[,;]");
            for (String splitGene : splitGenes) {
                String normalizedGene = normalizeGene(splitGene);
                if (normalizedGene != null) {
                    genes.add(normalizedGene);
                }
            }
        }
        return genes;
    }

    private Set<String> extractGenesFromLabelSummary(DrugLabel label) {
        String summary = Optional.ofNullable(label)
                .map(DrugLabel::getSummaryMarkdown)
                .orElse("");
        Set<String> summaryTokens = new LinkedHashSet<>();
        String[] split = summary.toUpperCase(Locale.ROOT).split("[^A-Z0-9_-]+");
        for (String token : split) {
            if (!token.isBlank()) {
                summaryTokens.add(token);
            }
        }
        return summaryTokens;
    }

    private String normalizeGene(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isBenignSynonymousVariant(VariantCore variant) {
        if (variant == null) {
            return false;
        }
        VariantCore loadedVariant = annovarDao.loadBioDetailsIfNeeded(variant);
        if (loadedVariant == null) {
            return false;
        }
        VariantBioDetails bioDetails = loadedVariant.getBioDetails();
        if (bioDetails == null || bioDetails.getRawDetails() == null || bioDetails.getRawDetails().isBlank()) {
            return false;
        }
        try {
            JsonElement root = JsonParser.parseString(bioDetails.getRawDetails());
            if (!root.isJsonObject()) {
                return false;
            }
            JsonObject jsonObject = root.getAsJsonObject();
            if (!jsonObject.has("annovar_col_9")) {
                return false;
            }
            JsonElement exonicFunc = jsonObject.get("annovar_col_9");
            String exonicFuncValue = exonicFunc != null && !exonicFunc.isJsonNull()
                    ? exonicFunc.getAsString()
                    : "";
            String normalizedExonicFunc = exonicFuncValue.trim()
                    .replace("_", " ")
                    .replaceAll("\\s+", " ")
                    .toLowerCase(Locale.ROOT);
            return BENIGN_SYNONYMOUS_SNV.equals(normalizedExonicFunc);
        } catch (Exception e) {
            log.debug("Could not parse variant_bio_details JSON for variant {}", variant.getId(), e);
            return false;
        }
    }

    public void uploadAnnovarOutput(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uploadedBy = request.getParameter("uploaded_by");
        if (uploadedBy == null || uploadedBy.isBlank()) {
            request.setAttribute("validateError", "Uploaded by can not be blank");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        Integer age = parseInteger(request.getParameter("age"));
        BigDecimal height = parsePositiveDecimal(request.getParameter("height"));
        BigDecimal weight = parsePositiveDecimal(request.getParameter("weight"));
        String gender = normalizeText(request.getParameter("gender"));
        if (age == null || age <= 0) {
            request.setAttribute("validateError", "Age is required and must be a positive integer");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        if (height == null) {
            request.setAttribute("validateError", "Height is required and must be a positive number");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        if (weight == null) {
            request.setAttribute("validateError", "Weight is required and must be a positive number");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        if (gender == null) {
            request.setAttribute("validateError", "Gender cannot be blank");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        Part requestPart = request.getPart("annovar");
        if (requestPart == null || requestPart.getSize() <= 0) {
            request.setAttribute("validateError", "annovar output file can not be blank");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        String content;
        try (InputStream inputStream = requestPart.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                request.setAttribute("validateError", "annovar output file can not be blank");
                request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
                return;
            }
            content = new String(bytes, StandardCharsets.UTF_8);
        }
        int sampleId = sampleDao.save(uploadedBy);
        if (sampleId <= 0) {
            request.setAttribute("validateError", "Failed to create sample record");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        PatientProfile profile = new PatientProfile();
        profile.setSampleId(sampleId);
        profile.setAge(age);
        profile.setHeight(height);
        profile.setWeight(weight);
        profile.setGender(gender);
        patientProfileDao.save(profile);
        try {
            boolean saved = annovarDao.save(sampleId, content);
            if (!saved) {
                request.setAttribute("validateError", "Failed to save variant data for this sample");
                request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            request.setAttribute("validateError", "annovar output file is invalid");
            request.getRequestDispatcher("/views/matching_index_error.jsp").forward(request, response);
            return;
        }
        response.sendRedirect("matching?sampleId=" + sampleId);
    }

    private boolean applyWarfarinDose(PatientProfile profile, List<MatchedDrugLabel> matched, List<VariantCore> variants) {
        if (matched == null || matched.isEmpty()) {
            return false;
        }
        boolean warfarinMatched = false;
        for (MatchedDrugLabel item : matched) {
            if (item == null || item.getName() == null) {
                continue;
            }
            if (!item.getName().toLowerCase(Locale.ROOT).contains("warfarin")) {
                continue;
            }
            warfarinMatched = true;
            Double dose = dosageCalculatorService.calculateWarfarinDose(profile, variants);
            item.setCalculatedDose(dose);
        }
        return warfarinMatched;
    }

    private Integer parseInteger(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parsePositiveDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
