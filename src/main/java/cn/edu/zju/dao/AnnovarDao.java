package cn.edu.zju.dao;

import cn.edu.zju.bean.VariantAnnotation;
import cn.edu.zju.bean.VariantBioDetails;
import cn.edu.zju.bean.VariantCore;
import cn.edu.zju.dbutils.DBUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AnnovarDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(AnnovarDao.class.getSimpleName());
    private static final Gson gson = new Gson();
    private static final int CHR_INDEX = 0;
    private static final int START_POS_INDEX = 1;
    private static final int END_POS_INDEX = 2;
    private static final int REF_ALLELE_INDEX = 3;
    private static final int ALT_ALLELE_INDEX = 4;
    private static final int GENE_SYMBOL_INDEX = 6;
    private static final int ACMG_CLASSIFICATION_INDEX = 125;
    private static final int BATCH_SIZE = 1000;

    public boolean save(int sampleId, String content) {
        String[] lines = content.split("\\r|\\n");
        AtomicBoolean success = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            String insertCore = "INSERT INTO variant_core (sample_id, chr, start_pos, end_pos, ref_allele, alt_allele) VALUES (?, ?, ?, ?, ?, ?)";
            String insertAnnotation = "INSERT INTO variant_annotation (variant_id, gene_symbol, acmg_classification) VALUES (?, ?, ?)";
            String insertBioDetails = "INSERT INTO variant_bio_details (variant_id, raw_details) VALUES (?, ?)";
            try (PreparedStatement corePs = connection.prepareStatement(insertCore, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement annotationPs = connection.prepareStatement(insertAnnotation);
                 PreparedStatement bioDetailsPs = connection.prepareStatement(insertBioDetails)) {
                connection.setAutoCommit(false);
                int count = 0;
                for (String line : lines) {
                    if (line == null || line.isBlank()) {
                        continue;
                    }
                    String[] split = line.split("\\t");
                    if (split.length < 6) {
                        throw new ArrayIndexOutOfBoundsException("Invalid annovar row, expected at least 6 core columns: chr, start_pos, end_pos, ref_allele, alt_allele");
                    }
                    corePs.setInt(1, sampleId);
                    corePs.setString(2, safeGet(split, CHR_INDEX));
                    corePs.setString(3, safeGet(split, START_POS_INDEX));
                    corePs.setString(4, safeGet(split, END_POS_INDEX));
                    corePs.setString(5, safeGet(split, REF_ALLELE_INDEX));
                    corePs.setString(6, safeGet(split, ALT_ALLELE_INDEX));
                    corePs.executeUpdate();

                    int variantId;
                    try (ResultSet generatedKeys = corePs.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Failed to get generated key from variant_core insert");
                        }
                        variantId = generatedKeys.getInt(1);
                    }

                    annotationPs.setInt(1, variantId);
                    annotationPs.setString(2, nullableTrim(safeGet(split, GENE_SYMBOL_INDEX)));
                    annotationPs.setString(3, nullableTrim(safeGet(split, ACMG_CLASSIFICATION_INDEX)));
                    annotationPs.addBatch();

                    bioDetailsPs.setInt(1, variantId);
                    bioDetailsPs.setString(2, buildRawDetailsJson(split));
                    bioDetailsPs.addBatch();

                    count++;
                    if (count % BATCH_SIZE == 0) {
                        annotationPs.executeBatch();
                        bioDetailsPs.executeBatch();
                    }
                }
                annotationPs.executeBatch();
                bioDetailsPs.executeBatch();
                connection.commit();
                success.set(true);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    log.error("Rollback failed when saving variant data for sample {}", sampleId, rollbackException);
                }
                log.error("Failed to save variant data for sample {}", sampleId, e);
            } catch (RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    log.error("Rollback failed when saving variant data for sample {}", sampleId, rollbackException);
                }
                throw e;
            }
        });
        return success.get();
    }

    public long countAll() {
        AtomicLong count = new AtomicLong();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("select count(*) from variant_core");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count.set(rs.getLong(1));
                }
            } catch (SQLException e) {
                log.error("Failed to count variants from variant_core", e);
            }
        });
        return count.get();
    }

    public List<VariantCore> findAnnotationsBySampleId(int sampleId) {
        String sql = "select vc.id, vc.sample_id, va.gene_symbol, va.acmg_classification, vbd.raw_details " +
                "from variant_core vc " +
                "join variant_annotation va on va.variant_id = vc.id " +
                "left join variant_bio_details vbd on vbd.variant_id = vc.id " +
                "where vc.sample_id = ? and va.gene_symbol is not null and va.gene_symbol <> ''";
        List<VariantCore> variants = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, sampleId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        VariantCore core = new VariantCore();
                        core.setId(rs.getInt("id"));
                        core.setSampleId(rs.getInt("sample_id"));

                        VariantAnnotation annotation = new VariantAnnotation();
                        annotation.setVariantId(rs.getInt("id"));
                        annotation.setGeneSymbol(nullableTrim(rs.getString("gene_symbol")));
                        annotation.setAcmgClassification(nullableTrim(rs.getString("acmg_classification")));
                        core.setAnnotation(annotation);

                        String rawDetails = rs.getString("raw_details");
                        if (rawDetails != null) {
                            core.setBioDetails(new VariantBioDetails(rs.getInt("id"), rawDetails));
                        }
                        variants.add(core);
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to find annotations by sample id {}", sampleId, e);
            }
        });
        return variants;
    }

    public List<VariantAnnotation> findAnnotationsByGeneSymbolExact(String geneSymbol) {
        String sql = "select variant_id, gene_symbol, acmg_classification " +
                "from variant_annotation " +
                "where gene_symbol = ?";
        List<VariantAnnotation> annotations = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, geneSymbol);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        annotations.add(new VariantAnnotation(
                                rs.getInt("variant_id"),
                                nullableTrim(rs.getString("gene_symbol")),
                                nullableTrim(rs.getString("acmg_classification"))
                        ));
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to find annotations for gene symbol {}", geneSymbol, e);
            }
        });
        return annotations;
    }

    public VariantBioDetails getBioDetails(int variantId) {
        String sql = "select variant_id, raw_details from variant_bio_details where variant_id = ?";
        VariantBioDetails[] holder = new VariantBioDetails[1];
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, variantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        holder[0] = new VariantBioDetails(
                                rs.getInt("variant_id"),
                                rs.getString("raw_details")
                        );
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to load bio details for variant {}", variantId, e);
            }
        });
        return holder[0];
    }

    public VariantCore loadBioDetailsIfNeeded(VariantCore core) {
        if (core == null) {
            return null;
        }
        if (core.getBioDetails() == null) {
            core.setBioDetails(getBioDetails(core.getId()));
        }
        return core;
    }

    public List<String> getRefGenes(int sampleId) {
        String sql = "select distinct va.gene_symbol " +
                "from variant_core vc " +
                "join variant_annotation va on va.variant_id = vc.id " +
                "where vc.sample_id = ? and va.gene_symbol is not null and va.gene_symbol <> ''";
        List<String> geneSymbolFields = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, sampleId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String gene = nullableTrim(rs.getString(1));
                        if (gene != null) {
                            geneSymbolFields.add(gene);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to load ref genes for sample {}", sampleId, e);
            }
        });

        LinkedHashSet<String> genes = new LinkedHashSet<>();
        for (String geneColumn : geneSymbolFields) {
            String[] splitGenes = geneColumn.split("[,;]");
            for (String gene : splitGenes) {
                String trimmed = nullableTrim(gene);
                if (trimmed != null) {
                    genes.add(trimmed);
                }
            }
        }
        return new ArrayList<>(genes);
    }

    private static String safeGet(String[] split, int index) {
        if (index < 0 || index >= split.length) {
            return null;
        }
        return split[index];
    }

    private static String nullableTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String buildRawDetailsJson(String[] split) {
        Map<String, Object> details = new LinkedHashMap<>();
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("column_key_format", "annovar_col_<1-based-original-column-index>");
        meta.put("scope", "stores long-tail columns only (from original column 6 onward), excluding gene_symbol/acmg_classification");
        details.put("_meta", meta);
        // i starts at 5, which is original annovar column 6 in 1-based indexing.
        for (int i = 5; i < split.length; i++) {
            if (i == GENE_SYMBOL_INDEX || i == ACMG_CLASSIFICATION_INDEX) {
                continue;
            }
            details.put("annovar_col_" + (i + 1), split[i]);
        }
        return gson.toJson(details);
    }
}
