package cn.edu.zju.dao;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for DrugDao parameter validation helpers.
 * These tests do not require a database connection.
 */
public class DrugDaoHelperTest {

    // ---- buildLikePattern tests ----

    @Test
    public void testBuildLikePatternNullReturnsNull() {
        assertNull(DrugDao.buildLikePattern(null));
    }

    @Test
    public void testBuildLikePatternEmptyReturnsNull() {
        assertNull(DrugDao.buildLikePattern(""));
        assertNull(DrugDao.buildLikePattern("   "));
    }

    @Test
    public void testBuildLikePatternNormalTerm() {
        assertEquals("%warfarin%", DrugDao.buildLikePattern("warfarin"));
    }

    @Test
    public void testBuildLikePatternTrimsWhitespace() {
        assertEquals("%warfarin%", DrugDao.buildLikePattern("  warfarin  "));
    }

    @Test
    public void testBuildLikePatternEscapesPercent() {
        assertEquals("%100\\%%", DrugDao.buildLikePattern("100%"));
    }

    @Test
    public void testBuildLikePatternEscapesUnderscore() {
        assertEquals("%CYP2C\\_9%", DrugDao.buildLikePattern("CYP2C_9"));
    }

    // ---- validateSortColumn tests ----

    @Test
    public void testValidateSortColumnDefault() {
        assertEquals("name", DrugDao.validateSortColumn(null));
        assertEquals("name", DrugDao.validateSortColumn(""));
        assertEquals("name", DrugDao.validateSortColumn("invalid"));
        assertEquals("name", DrugDao.validateSortColumn("id"));  // not allowed
    }

    @Test
    public void testValidateSortColumnName() {
        assertEquals("name", DrugDao.validateSortColumn("name"));
        assertEquals("name", DrugDao.validateSortColumn("NAME"));
    }

    @Test
    public void testValidateSortColumnBiomarker() {
        assertEquals("biomarker", DrugDao.validateSortColumn("biomarker"));
        assertEquals("biomarker", DrugDao.validateSortColumn("BIOMARKER"));
    }

    // ---- validateSortDir tests ----

    @Test
    public void testValidateSortDirDefault() {
        assertEquals("ASC", DrugDao.validateSortDir(null));
        assertEquals("ASC", DrugDao.validateSortDir(""));
        assertEquals("ASC", DrugDao.validateSortDir("invalid"));
    }

    @Test
    public void testValidateSortDirAsc() {
        assertEquals("ASC", DrugDao.validateSortDir("asc"));
        assertEquals("ASC", DrugDao.validateSortDir("ASC"));
    }

    @Test
    public void testValidateSortDirDesc() {
        assertEquals("DESC", DrugDao.validateSortDir("desc"));
        assertEquals("DESC", DrugDao.validateSortDir("DESC"));
    }
}
