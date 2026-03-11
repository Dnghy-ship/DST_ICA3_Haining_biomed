package cn.edu.zju.dao;

import cn.edu.zju.bean.Sample;
import cn.edu.zju.dbutils.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SampleDao extends BaseDao {

    public int save(String uploadedBy) {
        AtomicInteger key = new AtomicInteger();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into sample(created_at, uploaded_by) values (?,?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setTimestamp(1, new Timestamp(new Date().getTime()));
                preparedStatement.setString(2, uploadedBy);
                key.set(preparedStatement.executeUpdate());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return key.get();
    }

    public List<Sample> findAll() {
        List<Sample> samples = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select s.id, s.created_at, s.uploaded_by, count(a.sample_id) as variant_count"
                        + " from sample s left join annovar a on s.id = a.sample_id"
                        + " group by s.id, s.created_at, s.uploaded_by");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int sampleId = resultSet.getInt("id");
                    Date createdAt = new Date(resultSet.getTimestamp("created_at").getTime());
                    String uploadedBy = resultSet.getString("uploaded_by");
                    int variantCount = resultSet.getInt("variant_count");
                    samples.add(new Sample(sampleId, createdAt, uploadedBy, variantCount));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return samples;
    }

    public Sample findById(int id) {
        AtomicReference<Sample> sample = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("select id, created_at, uploaded_by from sample where id = ?");
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int sampleId = resultSet.getInt("id");
                    Date createdAt = new Date(resultSet.getTimestamp("created_at").getTime());
                    String uploadedBy = resultSet.getString("uploaded_by");
                    sample.set(new Sample(sampleId, createdAt, uploadedBy));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return sample.get();
    }

    /**
     * Count samples, optionally filtered by uploadedBy.
     *
     * @param uploadedByQ optional search term for uploaded_by (partial match); null/empty means no filter
     */
    public int count(String uploadedByQ) {
        AtomicInteger total = new AtomicInteger(0);
        String pattern = buildLikePattern(uploadedByQ);
        DBUtils.execSQL(connection -> {
            try {
                String sql = (pattern != null)
                        ? "select count(*) from sample where lower(uploaded_by) like lower(?)"
                        : "select count(*) from sample";
                PreparedStatement ps = connection.prepareStatement(sql);
                if (pattern != null) {
                    ps.setString(1, pattern);
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    total.set(rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return total.get();
    }

    /**
     * Return a page of samples with variant counts.
     *
     * @param uploadedByQ optional search term for uploaded_by
     * @param offset      0-based row offset
     * @param limit       max rows to return
     */
    public List<Sample> findPage(String uploadedByQ, int offset, int limit) {
        List<Sample> samples = new ArrayList<>();
        String pattern = buildLikePattern(uploadedByQ);
        String whereClause = (pattern != null) ? "WHERE lower(s.uploaded_by) like lower(?)" : "";
        String sql = "select s.id, s.created_at, s.uploaded_by, count(a.sample_id) as variant_count"
                + " from sample s left join annovar a on s.id = a.sample_id "
                + whereClause
                + " group by s.id, s.created_at, s.uploaded_by"
                + " ORDER BY s.id DESC LIMIT ?,?";
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
                    int sampleId = rs.getInt("id");
                    Date createdAt = new Date(rs.getTimestamp("created_at").getTime());
                    String uploadedBy = rs.getString("uploaded_by");
                    int variantCount = rs.getInt("variant_count");
                    samples.add(new Sample(sampleId, createdAt, uploadedBy, variantCount));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return samples;
    }

}

