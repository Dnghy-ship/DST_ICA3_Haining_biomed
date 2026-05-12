package cn.edu.zju.dao;

import cn.edu.zju.bean.PatientProfile;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

public class PatientProfileDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(PatientProfileDao.class);

    public int save(PatientProfile profile) {
        if (profile == null) {
            return 0;
        }
        AtomicReference<Integer> key = new AtomicReference<>(0);
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "insert into patient_profile(sample_id, age, height, weight, gender) values (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, profile.getSampleId());
                if (profile.getAge() == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, profile.getAge());
                }
                ps.setBigDecimal(3, profile.getHeight());
                ps.setBigDecimal(4, profile.getWeight());
                ps.setString(5, profile.getGender());
                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        key.set(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to save patient profile for sampleId={}", profile.getSampleId(), e);
            }
        });
        return key.get();
    }

    public PatientProfile findBySampleId(int sampleId) {
        AtomicReference<PatientProfile> holder = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select id, sample_id, age, height, weight, gender from patient_profile where sample_id = ? limit 1")) {
                ps.setInt(1, sampleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        Integer age = (Integer) rs.getObject("age");
                        BigDecimal height = rs.getBigDecimal("height");
                        BigDecimal weight = rs.getBigDecimal("weight");
                        String gender = rs.getString("gender");
                        holder.set(new PatientProfile(id, sampleId, age, height, weight, gender));
                    }
                }
            } catch (SQLException e) {
                log.error("Failed to find patient profile by sample id={}", sampleId, e);
            }
        });
        return holder.get();
    }
}
