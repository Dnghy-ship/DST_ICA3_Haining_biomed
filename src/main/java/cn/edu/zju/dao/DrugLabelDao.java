package cn.edu.zju.dao;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DrugLabelDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DrugLabelDao.class);

    public boolean existsById(String id) {
        return super.existsById(id, "drug_label");
    }

    public void saveDrugLabel(DrugLabel drugLabel) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into drug_label (id,name,obj_cls,alternate_drug_available,dosing_information,prescribing_markdown,source,text_markdown,summary_markdown,raw,drug_id) values (?,?,?,?,?,?,?,?,?,?,?)");
                preparedStatement.setString(1, drugLabel.getId());
                preparedStatement.setString(2, drugLabel.getName());
                preparedStatement.setString(3, drugLabel.getObjCls());
                preparedStatement.setBoolean(4, drugLabel.isAlternateDrugAvailable());
                preparedStatement.setBoolean(5, drugLabel.isDosingInformation());
                preparedStatement.setString(6, drugLabel.getPrescribingMarkdown());
                preparedStatement.setString(7, drugLabel.getSource());
                preparedStatement.setString(8, drugLabel.getTextMarkdown());
                preparedStatement.setString(9, drugLabel.getSummaryMarkdown());
                preparedStatement.setString(10, drugLabel.getRaw());
                preparedStatement.setString(11, drugLabel.getDrugId());
                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });

    }

    public List<DrugLabel> findAll() {
        List<DrugLabel> drugLabels = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select id, name, obj_cls, alternate_drug_available, dosing_information, prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id from drug_label");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    String obj_cls = resultSet.getString("obj_cls");
                    boolean alternate_drug_available = resultSet.getBoolean("alternate_drug_available");
                    boolean dosing_information = resultSet.getBoolean("dosing_information");
                    String prescribing_markdown = resultSet.getString("prescribing_markdown");
                    String source = resultSet.getString("source");
                    String text_markdown = resultSet.getString("text_markdown");
                    String summary_markdown = resultSet.getString("summary_markdown");
                    String raw = resultSet.getString("raw");
                    String drug_id = resultSet.getString("drug_id");
                    DrugLabel drugLabel = new DrugLabel(id, name, obj_cls, alternate_drug_available, dosing_information, prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id);
                    drugLabels.add(drugLabel);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugLabels;
    }

    /**
     * Count drug labels matching the optional search term (matches source or summary_markdown).
     */
    public int count(String q) {
        AtomicInteger total = new AtomicInteger(0);
        String pattern = buildLikePattern(q);
        DBUtils.execSQL(connection -> {
            try {
                String sql = (pattern != null)
                        ? "select count(*) from drug_label where lower(source) like lower(?) or lower(summary_markdown) like lower(?)"
                        : "select count(*) from drug_label";
                PreparedStatement ps = connection.prepareStatement(sql);
                if (pattern != null) {
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    total.set(rs.getInt(1));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return total.get();
    }

    /**
     * Return a page of drug labels.
     *
     * @param q      optional search term (partial match on source or summary_markdown)
     * @param offset 0-based row offset
     * @param limit  max rows to return
     */
    public List<DrugLabel> findPage(String q, int offset, int limit) {
        List<DrugLabel> drugLabels = new ArrayList<>();
        String pattern = buildLikePattern(q);
        String whereClause = (pattern != null)
                ? "WHERE lower(source) like lower(?) or lower(summary_markdown) like lower(?)"
                : "";
        String sql = "select id, name, obj_cls, alternate_drug_available, dosing_information,"
                + " prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id"
                + " from drug_label " + whereClause + " ORDER BY id LIMIT ?,?";
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                int idx = 1;
                if (pattern != null) {
                    ps.setString(idx++, pattern);
                    ps.setString(idx++, pattern);
                }
                ps.setInt(idx++, offset);
                ps.setInt(idx, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String objCls = rs.getString("obj_cls");
                    boolean alternDrug = rs.getBoolean("alternate_drug_available");
                    boolean dosing = rs.getBoolean("dosing_information");
                    String prescribing = rs.getString("prescribing_markdown");
                    String source = rs.getString("source");
                    String text = rs.getString("text_markdown");
                    String summary = rs.getString("summary_markdown");
                    String raw = rs.getString("raw");
                    String drugId = rs.getString("drug_id");
                    drugLabels.add(new DrugLabel(id, name, objCls, alternDrug, dosing, prescribing, source, text, summary, raw, drugId));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugLabels;
    }

}

