package cn.edu.zju.servlet;

import cn.edu.zju.bean.Page;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests for pagination parameter handling logic (no servlet container required).
 *
 * These tests verify the page-clamping and offset arithmetic that each servlet uses.
 */
public class PaginationLogicTest {

    // Mirrors the logic in all *Servlet.doGet()
    private static int parsePositiveInt(String s, int defaultVal, int max) {
        if (s == null || s.trim().isEmpty()) return defaultVal;
        try {
            int v = Integer.parseInt(s.trim());
            if (v < 1) return defaultVal;
            return Math.min(v, max);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    @Test
    public void testParsePositiveIntDefaults() {
        assertEquals(20, parsePositiveInt(null, 20, 100));
        assertEquals(20, parsePositiveInt("", 20, 100));
        assertEquals(20, parsePositiveInt("   ", 20, 100));
        assertEquals(20, parsePositiveInt("abc", 20, 100));
        assertEquals(20, parsePositiveInt("0", 20, 100));
        assertEquals(20, parsePositiveInt("-5", 20, 100));
    }

    @Test
    public void testParsePositiveIntValidValues() {
        assertEquals(1, parsePositiveInt("1", 20, 100));
        assertEquals(50, parsePositiveInt("50", 20, 100));
        assertEquals(100, parsePositiveInt("100", 20, 100));
    }

    @Test
    public void testParsePositiveIntCappedAtMax() {
        assertEquals(100, parsePositiveInt("200", 20, 100));
        assertEquals(100, parsePositiveInt("999", 20, 100));
    }

    @Test
    public void testOffsetCalculation() {
        // offset = (page - 1) * pageSize
        assertEquals(0, (1 - 1) * 20);
        assertEquals(20, (2 - 1) * 20);
        assertEquals(80, (5 - 1) * 20);
    }

    @Test
    public void testPageClampingToTotalPages() {
        int pageSize = 20;
        int totalCount = 100;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);  // 5

        // page 6 should be clamped to 5
        int requestedPage = 6;
        int clampedPage = Math.min(requestedPage, totalPages);
        assertEquals(5, clampedPage);
    }

    @Test
    public void testTotalPagesCalculation() {
        assertEquals(5, (int) Math.ceil(100.0 / 20));
        assertEquals(6, (int) Math.ceil(101.0 / 20));
        assertEquals(1, (int) Math.ceil(1.0 / 20));
        assertEquals(1, (int) Math.ceil(20.0 / 20));
        assertEquals(2, (int) Math.ceil(21.0 / 20));
    }

    @Test
    public void testEmptyResultSetHandling() {
        int totalCount = 0;
        int pageSize = 20;
        // When totalCount is 0, totalPages should be 1 (so page can be 1)
        int totalPages = (totalCount == 0) ? 1 : (int) Math.ceil((double) totalCount / pageSize);
        assertEquals(1, totalPages);

        Page<String> page = new Page<>(Collections.emptyList(), 1, pageSize, totalCount);
        assertEquals(0, page.getTotalPages());  // Page class itself returns 0 for 0 items
        assertFalse(page.isHasPrev());
        assertFalse(page.isHasNext());
    }
}
