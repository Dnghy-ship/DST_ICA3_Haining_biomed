package cn.edu.zju.dao;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DosingGuidelineDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineDao.class);

    public boolean existsById(String id) {
        return super.existsById(id, "dosing_guideline");
    }

    public void saveDosingGuideline(DosingGuideline dosingGuideline) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into dosing_guideline (id,obj_cls,name,recommendation,drug_id,source,summary_markdown,text_markdown,raw) values (?,?,?,?,?,?,?,?,?)");
                preparedStatement.setString(1, dosingGuideline.getId());
                preparedStatement.setString(2, dosingGuideline.getObjCls());
                preparedStatement.setString(3, dosingGuideline.getName());
                preparedStatement.setBoolean(4, dosingGuideline.isRecommendation());
                preparedStatement.setString(5, dosingGuideline.getDrugId());
                preparedStatement.setString(6, dosingGuideline.getSource());
                preparedStatement.setString(7, dosingGuideline.getSummaryMarkdown());
                preparedStatement.setString(8, dosingGuideline.getTextMarkdown());
                preparedStatement.setString(9, dosingGuideline.getRaw());
                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });

    }

    public List<DosingGuideline> findAll() {
        List<DosingGuideline> dosingGuidelines = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select id,obj_cls,name,recommendation,drug_id,source,summary_markdown,text_markdown,raw from dosing_guideline");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String objCls = resultSet.getString("obj_cls");
                    String name = resultSet.getString("name");
                    boolean recommendation = resultSet.getBoolean("recommendation");
                    String drugId = resultSet.getString("drug_id");
                    String source = resultSet.getString("source");
                    String summaryMarkdown = resultSet.getString("summary_markdown");
                    String textMarkdown = resultSet.getString("text_markdown");
                    String raw = resultSet.getString("raw");
                    DosingGuideline dosingGuideline = new DosingGuideline(id, objCls, name, recommendation, drugId, source, summaryMarkdown, textMarkdown, raw);
                    dosingGuidelines.add(dosingGuideline);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return dosingGuidelines;
    }

    /**
     * Count dosing guidelines matching optional search term (matches name, source, or summary_markdown).
     */
    public int count(String q) {
        AtomicInteger total = new AtomicInteger(0);
        String pattern = buildLikePattern(q);
        DBUtils.execSQL(connection -> {
            try {
                String sql = (pattern != null)
                        ? "select count(*) from dosing_guideline where lower(name) like lower(?)"
                          + " or lower(source) like lower(?)"
                          + " or lower(summary_markdown) like lower(?)"
                        : "select count(*) from dosing_guideline";
                PreparedStatement ps = connection.prepareStatement(sql);
                if (pattern != null) {
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                    ps.setString(3, pattern);
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
     * Return a page of dosing guidelines.
     *
     * @param q      optional search term (partial match on name, source, or summary_markdown)
     * @param offset 0-based row offset
     * @param limit  max rows to return
     */
    public List<DosingGuideline> findPage(String q, int offset, int limit) {
        List<DosingGuideline> dosingGuidelines = new ArrayList<>();
        String pattern = buildLikePattern(q);
        String whereClause = (pattern != null)
                ? "WHERE lower(name) like lower(?)"
                  + " or lower(source) like lower(?)"
                  + " or lower(summary_markdown) like lower(?)"
                : "";
        String sql = "select id,obj_cls,name,recommendation,drug_id,source,summary_markdown,text_markdown,raw"
                + " from dosing_guideline " + whereClause + " ORDER BY id LIMIT ?,?";
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                int idx = 1;
                if (pattern != null) {
                    ps.setString(idx++, pattern);
                    ps.setString(idx++, pattern);
                    ps.setString(idx++, pattern);
                }
                ps.setInt(idx++, offset);
                ps.setInt(idx, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String objCls = rs.getString("obj_cls");
                    String name = rs.getString("name");
                    boolean recommendation = rs.getBoolean("recommendation");
                    String drugId = rs.getString("drug_id");
                    String source = rs.getString("source");
                    String summaryMarkdown = rs.getString("summary_markdown");
                    String textMarkdown = rs.getString("text_markdown");
                    String raw = rs.getString("raw");
                    dosingGuidelines.add(new DosingGuideline(id, objCls, name, recommendation, drugId, source, summaryMarkdown, textMarkdown, raw));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return dosingGuidelines;
    }

}

