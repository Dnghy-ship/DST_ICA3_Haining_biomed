package cn.edu.zju;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final AppConfig instance = new AppConfig();

    public static AppConfig getInstance() {
        return instance;
    }

    public AppConfig() {
        try (InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("app.properties")) {
            if (resourceAsStream == null) {
                log.warn("app.properties not found on classpath");
                return;
            }
            Properties properties = new Properties();
            try {
                properties.load(resourceAsStream);
                this.jdbcUrl = properties.getProperty("jdbc.url");
                this.jdbcUsername = properties.getProperty("jdbc.username");
                this.jdbcPassword = properties.getProperty("jdbc.password");
            } catch (IOException e) {
                log.warn("Failed to load app.properties", e);
            }
        } catch (IOException e) {
            log.warn("Failed to close app.properties stream", e);
        }
    }

    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public void setJdbcUsername(String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }
}
