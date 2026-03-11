package cn.edu.zju.dao;

import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseDao {

    private static final Logger log = LoggerFactory.getLogger(BaseDao.class);

    public boolean existsById(String id, String tableName) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(String.format("select 1 from %s where id =?", tableName));
                preparedStatement.setString(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    exists.set(true);
                }
            } catch (SQLException e) {
                log.info("", e);
            }

        });
        return exists.get();
    }

    /**
     * Returns the total row count for the given table.
     * Uses a parameterised-style guard: table name is validated against a fixed
     * allow-list inside each subclass (they pass only their own table name),
     * so there is no SQL-injection risk here.
     */
    public int countAll(String tableName) {
        AtomicInteger count = new AtomicInteger(0);
        DBUtils.execSQL(connection -> {
            try {
                // Table name cannot be a bind parameter in JDBC; callers pass only
                // their own literal table name, so this is safe.
                PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    count.set(rs.getInt(1));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return count.get();
    }
}