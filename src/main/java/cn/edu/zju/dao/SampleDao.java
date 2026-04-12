package cn.edu.zju.dao;

import cn.edu.zju.bean.Sample;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SampleDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(SampleDao.class);

    public int save(String uploadedBy) {
        AtomicInteger key = new AtomicInteger();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into sample(created_at, uploaded_by) values (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setTimestamp(1, new Timestamp(new Date().getTime()));
                preparedStatement.setString(2, uploadedBy);
                preparedStatement.executeUpdate();
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    while (generatedKeys.next()) {
                        key.set(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to save sample for uploadedBy={}", uploadedBy, e);
            }
        });
        return key.get();
    }

    public List<Sample> findAll() {
        List<Sample> samples = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from sample");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int sampleId = resultSet.getInt("id");
                    Date createdAt = new Date(resultSet.getTimestamp("created_at").getTime());
                    String uploadedBy = resultSet.getString("uploaded_by");
                    Sample sample = new Sample(sampleId, createdAt, uploadedBy);
                    samples.add(sample);
                }
            } catch (SQLException e) {
                log.error("Failed to load all samples", e);
            }
        });
        return samples;
    }

    public int count() {
        AtomicInteger count = new AtomicInteger();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select count(*) from sample");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count.set(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                log.error("Failed to count samples", e);
            }
        });
        return count.get();
    }

    public List<Sample> findRecent(int limit) {
        List<Sample> samples = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "select id, created_at, uploaded_by from sample order by id desc limit ?")) {
                preparedStatement.setInt(1, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int sampleId = resultSet.getInt("id");
                        Date createdAt = new Date(resultSet.getTimestamp("created_at").getTime());
                        String uploadedBy = resultSet.getString("uploaded_by");
                        samples.add(new Sample(sampleId, createdAt, uploadedBy));
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to load recent samples with limit={}", limit, e);
            }
        });
        return samples;
    }

    public Sample findById(int id) {
        AtomicReference<Sample> sample = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select id, created_at, uploaded_by from sample where id = ?")) {
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int sampleId = resultSet.getInt("id");
                        Date createdAt = new Date(resultSet.getTimestamp("created_at").getTime());
                        String uploadedBy = resultSet.getString("uploaded_by");
                        sample.set(new Sample(sampleId, createdAt, uploadedBy));
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to find sample by id={}", id, e);
            }
        });
        return sample.get();
    }
}
