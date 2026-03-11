package cn.edu.zju.dao;

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

public class DrugDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DrugDao.class);

    public boolean existsById(String id) {
        return super.existsById(id, "drug");
    }

    public void saveDrug(Drug drug) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into drug (id, name, obj_cls, biomarker, drug_url) values    (?,?,?,?,?)");
                preparedStatement.setString(1, drug.getId());
                preparedStatement.setString(2, drug.getName());
                preparedStatement.setString(3, drug.getObjCls());
                preparedStatement.setBoolean(4, drug.isBiomarker());
                preparedStatement.setString(5, drug.getDrugUrl());
                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });

    }

    public List<Drug> findAll() {
        List<Drug> drugs = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select id,name,obj_cls,drug_url,biomarker from drug");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    String objCls = resultSet.getString("obj_cls");
                    String drugUrl = resultSet.getString("drug_url");
                    boolean biomarker = resultSet.getBoolean("biomarker");
                    Drug drug = new Drug(id, name, biomarker, drugUrl, objCls);
                    drugs.add(drug);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugs;
    }

    /**
     * Count drugs matching optional name filter.
     *
     * @param q optional search term (case-insensitive partial match on name); null/empty means no filter
     */
    public int count(String q) {
        AtomicInteger total = new AtomicInteger(0);
        String pattern = buildLikePattern(q);
        DBUtils.execSQL(connection -> {
            try {
                String sql = (pattern != null)
                        ? "select count(*) from drug where lower(name) like lower(?)"
                        : "select count(*) from drug";
                PreparedStatement ps = connection.prepareStatement(sql);
                if (pattern != null) {
                    ps.setString(1, pattern);
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
     * Return a page of drugs.
     *
     * @param q      optional search term (partial match on name)
     * @param sort   column to sort by: "name" or "biomarker"
     * @param dir    sort direction: "asc" or "desc"
     * @param offset 0-based row offset
     * @param limit  max rows to return
     */
    public List<Drug> findPage(String q, String sort, String dir, int offset, int limit) {
        List<Drug> drugs = new ArrayList<>();
        String pattern = buildLikePattern(q);
        String orderClause = "ORDER BY " + validateSortColumn(sort) + " " + validateSortDir(dir);
        String whereClause = (pattern != null) ? "WHERE lower(name) like lower(?)" : "";
        String sql = "select id,name,obj_cls,drug_url,biomarker from drug "
                + whereClause + " " + orderClause + " LIMIT ?,?";
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                int idx = 1;
                if (pattern != null) {
                    ps.setString(idx++, pattern);
                }
                ps.setInt(idx++, offset);
                ps.setInt(idx, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String objCls = rs.getString("obj_cls");
                    String drugUrl = rs.getString("drug_url");
                    boolean biomarker = rs.getBoolean("biomarker");
                    drugs.add(new Drug(id, name, biomarker, drugUrl, objCls));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugs;
    }

    /** Convert search term to SQL LIKE pattern; returns null if q is blank. */
    public static String buildLikePattern(String q) {
        return BaseDao.buildLikePattern(q);
    }

    /** Validate and return allowed sort column name. */
    public static String validateSortColumn(String sort) {
        if ("biomarker".equalsIgnoreCase(sort)) return "biomarker";
        return "name";
    }

    /** Validate and return allowed sort direction. */
    public static String validateSortDir(String dir) {
        if ("desc".equalsIgnoreCase(dir)) return "DESC";
        return "ASC";
    }
}
