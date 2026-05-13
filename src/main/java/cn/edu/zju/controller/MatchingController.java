package cn.edu.zju.controller;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.MatchedDrugLabel;
import cn.edu.zju.bean.PatientProfile;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.bean.WarfarinDoseSummary;
import cn.edu.zju.bean.VariantAnnotation;
import cn.edu.zju.bean.VariantBioDetails;
import cn.edu.zju.bean.VariantCore;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.MatchingResultDao;
import cn.edu.zju.dao.PatientProfileDao;
import cn.edu.zju.dao.SampleDao;
import cn.edu.zju.service.DosageCalculatorService;
import cn.edu.zju.servlet.DispatchServlet;
import com.google.gson.JsonArray;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Pattern ACMG_LIKELY_PATHOGENIC = Pattern.compile("LIKELY\\s*PATHOGENIC", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACMG_PATHOGENIC = Pattern.compile("\\bPATHOGENIC\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACMG_UNCERTAIN = Pattern.compile("UNCERTAIN|VUS", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACMG_LIKELY_BENIGN = Pattern.compile("LIKELY\\s*BENIGN", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACMG_BENIGN = Pattern.compile("\\bBENIGN\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final String BENIGN_SYNONYMOUS_SNV = "synonymous snv";

    private SampleDao sampleDao = new SampleDao();
    private AnnovarDao annovarDao = new AnnovarDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
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
            samples(request, response);
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
        Map<String, Integer> guidelineScores = buildGuidelineScoreByDrugId(dosingGuidelineDao.findAll());
        Map<String, Integer> variantEvidenceScores = buildVariantEvidenceScores(variants);
        List<MatchedDrugLabel> matched = doMatch(drugLabels, patientGenes, variantEvidenceScores, guidelineScores);
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

    private List<MatchedDrugLabel> doMatch(List<DrugLabel> drugLabels,
                                           Set<String> patientGenes,
                                           Map<String, Integer> variantEvidenceScores,
                                           Map<String, Integer> guidelineScores) {
        List<MatchedDrugLabel> matchedLabels = new ArrayList<>();
        if (drugLabels == null || drugLabels.isEmpty() || patientGenes == null || patientGenes.isEmpty()) {
            return matchedLabels;
        }
        for (DrugLabel drugLabel : drugLabels) {
            if (drugLabel == null) {
                continue;
            }
            String labelSearchText = buildLabelSearchText(drugLabel);
            LabelGeneInfo geneInfo = extractLabelGeneInfo(drugLabel);
            LinkedHashSet<String> matchedGenes = new LinkedHashSet<>();
            for (String patientGene : patientGenes) {
                if (geneInfo.genes.contains(patientGene) || containsGeneToken(labelSearchText, patientGene)) {
                    matchedGenes.add(patientGene);
                }
            }
            if (!matchedGenes.isEmpty()) {
                int geneMatchScore = calculateGeneMatchScore(matchedGenes.size());
                int labelEvidenceScore = calculateLabelEvidenceScore(labelSearchText);
                int variantEvidenceScore = calculateVariantEvidenceScore(matchedGenes, variantEvidenceScores);
                int guidelineScore = calculateGuidelineScore(drugLabel, guidelineScores);
                int score = calculateTotalScore(geneMatchScore, variantEvidenceScore, labelEvidenceScore, guidelineScore);
                String recLevel = getRecommendationLevel(labelEvidenceScore);
                matchedLabels.add(new MatchedDrugLabel(drugLabel, score, recLevel, new ArrayList<>(matchedGenes)));
            }
        }
        return matchedLabels;
    }

    private int calculateLabelEvidenceScore(String evidenceText) {
        if (evidenceText == null || evidenceText.isBlank()) {
            return 0;
        }
        if (EVIDENCE_LEVEL_1A.matcher(evidenceText).find()) {
            return 10;
        }
        if (EVIDENCE_LEVEL_1B.matcher(evidenceText).find()) {
            return 8;
        }
        if (EVIDENCE_LEVEL_2A.matcher(evidenceText).find()) {
            return 5;
        }
        if (EVIDENCE_LEVEL_2B.matcher(evidenceText).find()) {
            return 3;
        }
        if (EVIDENCE_LEVEL_3.matcher(evidenceText).find()) {
            return 1;
        }
        return 0;
    }

    private int calculateGeneMatchScore(int matchedGeneCount) {
        return Math.min(matchedGeneCount, 3);
    }

    private int calculateVariantEvidenceScore(Set<String> matchedGenes, Map<String, Integer> variantEvidenceScores) {
        if (matchedGenes == null || matchedGenes.isEmpty() || variantEvidenceScores == null || variantEvidenceScores.isEmpty()) {
            return 0;
        }
        int score = 0;
        for (String gene : matchedGenes) {
            score = Math.max(score, variantEvidenceScores.getOrDefault(gene, 0));
        }
        return score;
    }

    private int calculateGuidelineScore(DrugLabel label, Map<String, Integer> guidelineScores) {
        if (label == null || guidelineScores == null || guidelineScores.isEmpty()) {
            return 0;
        }
        String drugId = label.getDrugId();
        if (drugId == null || drugId.isBlank()) {
            return 0;
        }
        return guidelineScores.getOrDefault(drugId, 0);
    }

    private int calculateTotalScore(int geneMatchScore,
                                    int variantEvidenceScore,
                                    int labelEvidenceScore,
                                    int guidelineScore) {
        return geneMatchScore + variantEvidenceScore + labelEvidenceScore + guidelineScore;
    }

    private String getRecommendationLevel(int evidenceScore) {
        if (evidenceScore >= 8) {
            return "Strong";
        }
        if (evidenceScore >= 4) {
            return "Moderate";
        }
        return "Optional";
    }

    private Map<String, Integer> buildVariantEvidenceScores(List<VariantCore> variants) {
        Map<String, Integer> scores = new HashMap<>();
        if (variants == null || variants.isEmpty()) {
            return scores;
        }
        for (VariantCore variant : variants) {
            if (variant == null || variant.getAnnotation() == null) {
                continue;
            }
            VariantAnnotation annotation = variant.getAnnotation();
            String geneSymbol = annotation.getGeneSymbol();
            if (geneSymbol == null || geneSymbol.isBlank()) {
                continue;
            }
            int score = scoreVariantClassification(annotation.getAcmgClassification());
            if (score <= 0) {
                continue;
            }
            String[] splitGenes = geneSymbol.split("[,;]");
            for (String splitGene : splitGenes) {
                String normalized = normalizeGene(splitGene);
                if (normalized != null) {
                    scores.merge(normalized, score, Math::max);
                }
            }
        }
        return scores;
    }

    private int scoreVariantClassification(String classification) {
        if (classification == null || classification.isBlank()) {
            return 0;
        }
        if (ACMG_LIKELY_PATHOGENIC.matcher(classification).find()) {
            return 2;
        }
        if (ACMG_PATHOGENIC.matcher(classification).find()) {
            return 3;
        }
        if (ACMG_UNCERTAIN.matcher(classification).find()) {
            return 1;
        }
        if (ACMG_LIKELY_BENIGN.matcher(classification).find()) {
            return 0;
        }
        if (ACMG_BENIGN.matcher(classification).find()) {
            return 0;
        }
        return 0;
    }

    private Map<String, Integer> buildGuidelineScoreByDrugId(List<DosingGuideline> guidelines) {
        Map<String, Integer> scores = new HashMap<>();
        if (guidelines == null || guidelines.isEmpty()) {
            return scores;
        }
        for (DosingGuideline guideline : guidelines) {
            if (guideline == null) {
                continue;
            }
            String drugId = guideline.getDrugId();
            if (drugId == null || drugId.isBlank()) {
                continue;
            }
            int score = guideline.isRecommendation() ? 2 : 1;
            scores.merge(drugId, score, Math::max);
        }
        return scores;
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

    private LabelGeneInfo extractLabelGeneInfo(DrugLabel label) {
        Set<String> genes = new LinkedHashSet<>();
        boolean structured = false;
        if (label == null) {
            return new LabelGeneInfo(genes, false);
        }
        String raw = label.getRaw();
        if (raw != null && !raw.isBlank()) {
            try {
                JsonElement root = JsonParser.parseString(raw);
                if (root.isJsonObject()) {
                    JsonObject obj = root.getAsJsonObject();
                    addGenesFromKey(obj, "relatedGenes", genes);
                    addGenesFromKey(obj, "genes", genes);
                    addGenesFromKey(obj, "geneSymbols", genes);
                    addGeneFromField(obj, "geneSymbol", genes);
                    if (!genes.isEmpty()) {
                        structured = true;
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse raw drug label JSON for id={}", label.getId(), e);
            }
        }
        return new LabelGeneInfo(genes, structured);
    }

    private void addGenesFromKey(JsonObject obj, String key, Set<String> genes) {
        if (obj == null || !obj.has(key)) {
            return;
        }
        addGenesFromElement(obj.get(key), genes);
    }

    private void addGeneFromField(JsonObject obj, String field, Set<String> genes) {
        if (obj == null || !obj.has(field)) {
            return;
        }
        JsonElement element = obj.get(field);
        if (element != null && element.isJsonPrimitive()) {
            addNormalizedGene(element.getAsString(), genes);
        }
    }

    private void addGenesFromElement(JsonElement element, Set<String> genes) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                addGenesFromElement(item, genes);
            }
            return;
        }
        if (element.isJsonPrimitive()) {
            addNormalizedGene(element.getAsString(), genes);
            return;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            addGeneFromField(obj, "symbol", genes);
            addGeneFromField(obj, "name", genes);
            addGeneFromField(obj, "geneSymbol", genes);
        }
    }

    private String buildLabelSearchText(DrugLabel label) {
        if (label == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        appendLabelText(builder, label.getSummaryMarkdown());
        appendLabelText(builder, label.getTextMarkdown());
        appendLabelText(builder, label.getPrescribingMarkdown());
        return stripHtml(builder.toString()).toUpperCase(Locale.ROOT);
    }

    private void appendLabelText(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value);
    }

    private String stripHtml(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String stripped = HTML_TAG.matcher(value).replaceAll(" ");
        return stripped.replace("&nbsp;", " ").replace("&amp;", "&");
    }

    private boolean containsGeneToken(String labelText, String gene) {
        if (labelText == null || labelText.isBlank() || gene == null || gene.isBlank()) {
            return false;
        }
        String haystack = labelText.toUpperCase(Locale.ROOT);
        String needle = gene.toUpperCase(Locale.ROOT);
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            boolean leftOk = index == 0 || !Character.isLetterOrDigit(haystack.charAt(index - 1));
            int end = index + needle.length();
            boolean rightOk = end >= haystack.length() || !Character.isLetterOrDigit(haystack.charAt(end));
            if (leftOk && rightOk) {
                return true;
            }
            index = haystack.indexOf(needle, end);
        }
        return false;
    }

    private String normalizeGene(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        int alleleIndex = normalized.indexOf('*');
        if (alleleIndex >= 0) {
            normalized = normalized.substring(0, alleleIndex);
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private void addNormalizedGene(String value, Set<String> genes) {
        if (value == null || value.isBlank()) {
            return;
        }
        String[] splitGenes = value.split("[,;]");
        for (String splitGene : splitGenes) {
            String normalized = normalizeGene(splitGene);
            if (normalized != null) {
                genes.add(normalized);
            }
        }
    }

    private static class LabelGeneInfo {
        private final Set<String> genes;
        private final boolean structured;

        private LabelGeneInfo(Set<String> genes, boolean structured) {
            this.genes = genes;
            this.structured = structured;
        }
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
        } catch (RuntimeException e) {
            log.error("Failed to process annovar output for sample {}", sampleId, e);
            request.setAttribute("validateError", "Failed to process annovar output file");
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
