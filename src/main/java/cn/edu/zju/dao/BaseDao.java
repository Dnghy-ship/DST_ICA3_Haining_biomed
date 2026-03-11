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

    /**
     * 统计指定表的总记录数
     */
    public int countAll(String tableName) {
        AtomicInteger count = new AtomicInteger(0);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        String.format("select count(*) from %s", tableName));
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count.set(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return count.get();
    }

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
}