package cn.edu.zju.dbutils;

import cn.edu.zju.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

public class DBUtils {

    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    public static Connection getConnection() {
        Connection connection = null;
        AppConfig appConfig = AppConfig.getInstance();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("MySQL JDBC driver not found", e);
        }
        try {
            connection = DriverManager.getConnection(appConfig.getJdbcUrl()
                    , appConfig.getJdbcUsername()
                    , appConfig.getJdbcPassword());
        } catch (SQLException e) {
            log.error("Failed to get database connection", e);
        }
        return connection;
    }

    public static void execSQL(Consumer<Connection> consumer) {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                log.warn("Skipping SQL execution because database connection is null");
                return;
            }
            consumer.accept(connection);
        } catch (SQLException e) {
            log.warn("Failed to close database connection", e);
        }
    }
}
