package cn.edu.zju.dao;

import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * Convert a search term to a SQL LIKE pattern with {@code %} wildcards.
     * Returns {@code null} when q is blank (meaning "no filter").
     * Escapes {@code %} and {@code _} literals in the input to prevent accidental wildcards.
     */
    protected static String buildLikePattern(String q) {
        if (q == null || q.trim().isEmpty()) return null;
        return "%" + q.trim().replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
