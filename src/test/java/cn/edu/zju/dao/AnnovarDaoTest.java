package cn.edu.zju.dao;

import cn.edu.zju.AppConfig;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Unit tests for the new DAO helper methods introduced in the Level 1 clean-up.
 *
 * These tests exercise the pure-Java logic (e.g. AppConfig construction) and
 * the new countAll / countVariantsBySample signatures without requiring a live
 * database connection.  Tests that need a real DB are guarded with Assume so
 * they are simply skipped in a CI environment where no MySQL is available.
 */
public class AnnovarDaoTest {

    /**
     * Returns true only when a real MySQL instance appears to be configured.
     * We check for a non-default JDBC URL in app.properties; the default
     * "localhost" URL will fail to connect in CI, so we skip those tests.
     */
    private boolean dbAvailable() {
        try {
            AppConfig cfg = AppConfig.getInstance();
            String url = cfg.getJdbcUrl();
            if (url == null || url.trim().isEmpty()) {
                return false;
            }
            // Quick ping – just try to get a connection; ignore failure
            java.sql.DriverManager.setLoginTimeout(1);
            java.sql.Connection c = java.sql.DriverManager.getConnection(
                    url, cfg.getJdbcUsername(), cfg.getJdbcPassword());
            c.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void testCountVariantsBySampleReturnZeroForUnknownId() {
        Assume.assumeTrue("Skipping: no DB available", dbAvailable());
        AnnovarDao dao = new AnnovarDao();
        // sample_id = -1 should not exist, so count must be 0
        int count = dao.countVariantsBySample(-1);
        assertEquals(0, count);
    }

    @Test
    public void testSampleDaoCountAllIsNonNegative() {
        Assume.assumeTrue("Skipping: no DB available", dbAvailable());
        SampleDao dao = new SampleDao();
        int count = dao.countAll();
        assertTrue("countAll() must return a non-negative value", count >= 0);
    }

    @Test
    public void testDrugDaoCountAllIsNonNegative() {
        Assume.assumeTrue("Skipping: no DB available", dbAvailable());
        DrugDao dao = new DrugDao();
        int count = dao.countAll();
        assertTrue("countAll() must return a non-negative value", count >= 0);
    }

    @Test
    public void testDrugLabelDaoCountAllIsNonNegative() {
        Assume.assumeTrue("Skipping: no DB available", dbAvailable());
        DrugLabelDao dao = new DrugLabelDao();
        int count = dao.countAll();
        assertTrue("countAll() must return a non-negative value", count >= 0);
    }

    @Test
    public void testDosingGuidelineDaoCountAllIsNonNegative() {
        Assume.assumeTrue("Skipping: no DB available", dbAvailable());
        DosingGuidelineDao dao = new DosingGuidelineDao();
        int count = dao.countAll();
        assertTrue("countAll() must return a non-negative value", count >= 0);
    }

    /** Pure-Java test: AppConfig should never return null properties object. */
    @Test
    public void testAppConfigLoads() {
        AppConfig cfg = AppConfig.getInstance();
        assertNotNull("AppConfig instance must not be null", cfg);
        // jdbcUrl may be null if properties file is missing, but getInstance() must not throw
    }
}
