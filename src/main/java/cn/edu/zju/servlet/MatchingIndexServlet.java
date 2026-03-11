package cn.edu.zju.servlet;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.SampleDao;
import cn.edu.zju.filter.AuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the ANNOVAR file upload and matching workflow.
 *
 * GET  /matchingIndex  - show upload form and guidance
 * POST /matchingIndex  - process uploaded ANNOVAR TSV, run drug matching
 */
@WebServlet(name = "MatchingIndexServlet", urlPatterns = "/matchingIndex")
@MultipartConfig(maxFileSize = 50 * 1024 * 1024)  // 50 MB max
public class MatchingIndexServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/matching_index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String uploadedBy = request.getSession().getAttribute(AuthenticationFilter.USERNAME) != null
                ? (String) request.getSession().getAttribute(AuthenticationFilter.USERNAME)
                : "anonymous";

        Part filePart = request.getPart("annovarFile");
        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Please select an ANNOVAR TSV file to upload.");
            request.getRequestDispatcher("/views/matching_index.jsp").forward(request, response);
            return;
        }

        // Read file content
        String content;
        try (InputStream is = filePart.getInputStream()) {
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int len;
            while ((len = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, len);
            }
            content = buffer.toString(StandardCharsets.UTF_8.name());
        }

        if (content.trim().isEmpty()) {
            request.setAttribute("error", "The uploaded file is empty.");
            request.getRequestDispatcher("/views/matching_index.jsp").forward(request, response);
            return;
        }

        // Save sample record
        SampleDao sampleDao = new SampleDao();
        int newKey = sampleDao.save(uploadedBy);
        Sample sample = sampleDao.findById(newKey);
        int sampleId = (sample != null) ? sample.getId() : newKey;

        // Save annovar rows
        AnnovarDao annovarDao = new AnnovarDao();
        annovarDao.save(sampleId, content);

        // Run matching: get non-synonymous ref genes and find matching drug labels
        List<String> refGenes = annovarDao.getRefGenes(sampleId);
        DrugLabelDao drugLabelDao = new DrugLabelDao();
        List<DrugLabel> allLabels = drugLabelDao.findAll();
        List<DrugLabel> matchedLabels = new ArrayList<>();
        for (DrugLabel label : allLabels) {
            if (label.getSummaryMarkdown() != null) {
                for (String gene : refGenes) {
                    if (label.getSummaryMarkdown().contains(gene)) {
                        matchedLabels.add(label);
                        break;
                    }
                }
            }
        }

        request.setAttribute("sampleId", sampleId);
        request.setAttribute("refGenes", refGenes);
        request.setAttribute("matchedLabels", matchedLabels);
        request.getRequestDispatcher("/views/matching_index.jsp").forward(request, response);
    }
}
