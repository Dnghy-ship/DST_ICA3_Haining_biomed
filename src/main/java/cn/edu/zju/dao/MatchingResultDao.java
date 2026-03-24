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
        DBUtils.execSQL(connection -> {
            try {
                connection.setAutoCommit(false);
                PreparedStatement del = connection.prepareStatement(
                        "delete from matching_result where sample_id = ?");
                del.setInt(1, sampleId);
                del.executeUpdate();

                PreparedStatement ins = connection.prepareStatement(
                        "insert into matching_result (sample_id, drug_label_id, score, recommendation_level, matched_genes, created_at) values (?,?,?,?,?,?)");
                for (MatchedDrugLabel label : results) {
                    ins.setInt(1, sampleId);
                    ins.setString(2, label.getId());
                    ins.setInt(3, label.getScore());
                    ins.setString(4, label.getRecommendationLevel());
                    ins.setString(5, String.join(",", label.getMatchedGenes()));
                    ins.setTimestamp(6, new Timestamp(new Date().getTime()));
                    ins.addBatch();
                }
                ins.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public List<MatchedDrugLabel> findBySampleId(int sampleId) {
        List<MatchedDrugLabel> results = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "select mr.score, mr.recommendation_level, mr.matched_genes, " +
                        "dl.id, dl.name, dl.obj_cls, dl.alternate_drug_available, dl.dosing_information, " +
                        "dl.prescribing_markdown, dl.source, dl.text_markdown, dl.summary_markdown, dl.raw, dl.drug_id " +
                        "from matching_result mr " +
                        "join drug_label dl on mr.drug_label_id = dl.id " +
                        "where mr.sample_id = ? order by mr.score desc");
                ps.setInt(1, sampleId);
                ResultSet rs = ps.executeQuery();
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
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return results;
    }

    public boolean hasResults(int sampleId) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "select 1 from matching_result where sample_id = ? limit 1");
                ps.setInt(1, sampleId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    exists.set(true);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return exists.get();
    }

    public Set<Integer> findSampleIdsWithResults() {
        Set<Integer> ids = new HashSet<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "select distinct sample_id from matching_result");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return ids;
    }
}
