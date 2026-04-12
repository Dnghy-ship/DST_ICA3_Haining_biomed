package cn.edu.zju.dao;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.MatchedDrugLabel;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MatchingResultDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(MatchingResultDao.class);

    public void saveResults(int sampleId, List<MatchedDrugLabel> results) {
        if (results == null) {
            return;
        }
        DBUtils.execSQL(connection -> {
            try (PreparedStatement del = connection.prepareStatement(
                    "delete from matching_result where sample_id = ?");
                 PreparedStatement ins = connection.prepareStatement(
                         "insert into matching_result (sample_id, drug_label_id, score, recommendation_level, matched_genes, created_at) values (?,?,?,?,?,?)")) {
                connection.setAutoCommit(false);
                del.setInt(1, sampleId);
                del.executeUpdate();

                for (MatchedDrugLabel label : results) {
                    if (label == null || label.getId() == null || label.getId().isBlank()) {
                        continue;
                    }
                    ins.setInt(1, sampleId);
                    ins.setString(2, label.getId());
                    ins.setInt(3, label.getScore());
                    ins.setString(4, label.getRecommendationLevel());
                    List<String> matchedGenes = label.getMatchedGenes() == null ? new ArrayList<>() : label.getMatchedGenes();
                    ins.setString(5, String.join(",", matchedGenes));
                    ins.setTimestamp(6, new Timestamp(new Date().getTime()));
                    ins.addBatch();
                }
                ins.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.warn("Rollback failed for saving matching_result sample {}", sampleId, rollbackEx);
                }
                log.warn("Failed to save matching_result snapshot for sample {}", sampleId, e);
            }
        });
    }

    public List<MatchedDrugLabel> findBySampleId(int sampleId) {
        List<MatchedDrugLabel> results = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                        "select mr.score, mr.recommendation_level, mr.matched_genes, " +
                        "dl.id, dl.name, dl.obj_cls, dl.alternate_drug_available, dl.dosing_information, " +
                        "dl.prescribing_markdown, dl.source, dl.text_markdown, dl.summary_markdown, dl.raw, dl.drug_id " +
                        "from matching_result mr " +
                        "join drug_label dl on mr.drug_label_id = dl.id " +
                        "where mr.sample_id = ? order by mr.score desc")) {
                ps.setInt(1, sampleId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int score = rs.getInt("score");
                        String recLevel = rs.getString("recommendation_level");
                        String genesStr = rs.getString("matched_genes");
                        List<String> genes = (genesStr != null && !genesStr.isEmpty())
                                ? Arrays.asList(genesStr.split(",")) : new ArrayList<>();

                        DrugLabel dl = new DrugLabel(
                                rs.getString("id"), rs.getString("name"), rs.getString("obj_cls"),
                                rs.getBoolean("alternate_drug_available"), rs.getBoolean("dosing_information"),
                                rs.getString("prescribing_markdown"), rs.getString("source"),
                                rs.getString("text_markdown"), rs.getString("summary_markdown"),
                                rs.getString("raw"), rs.getString("drug_id"));
                        results.add(new MatchedDrugLabel(dl, score, recLevel, genes));
                    }
                }
            } catch (SQLException e) {
                log.warn("Failed to read matching_result snapshot for sample {}", sampleId, e);
            }
        });
        return results;
    }

    public boolean hasResults(int sampleId) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select 1 from matching_result where sample_id = ? limit 1")) {
                ps.setInt(1, sampleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        exists.set(true);
                    }
                }
            } catch (SQLException e) {
                log.warn("Failed to check matching_result existence for sample {}", sampleId, e);
            }
        });
        return exists.get();
    }

    public Set<Integer> findSampleIdsWithResults() {
        Set<Integer> ids = new HashSet<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select distinct sample_id from matching_result");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            } catch (SQLException e) {
                log.warn("Failed to list sample IDs from matching_result", e);
            }
        });
        return ids;
    }
}
