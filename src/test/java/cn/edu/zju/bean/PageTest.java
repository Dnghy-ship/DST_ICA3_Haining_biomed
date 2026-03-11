package cn.edu.zju.bean;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PageTest {

    @Test
    public void testBasicPage() {
        List<String> items = Arrays.asList("a", "b", "c");
        Page<String> page = new Page<>(items, 1, 20, 100);

        assertEquals(1, page.getPage());
        assertEquals(20, page.getPageSize());
        assertEquals(100, page.getTotalCount());
        assertEquals(5, page.getTotalPages());  // ceil(100/20)
        assertFalse(page.isHasPrev());
        assertTrue(page.isHasNext());
        assertEquals(items, page.getItems());
    }

    @Test
    public void testLastPage() {
        List<String> items = Collections.singletonList("z");
        Page<String> page = new Page<>(items, 5, 20, 100);

        assertEquals(5, page.getPage());
        assertEquals(5, page.getTotalPages());
        assertTrue(page.isHasPrev());
        assertFalse(page.isHasNext());
    }

    @Test
    public void testMiddlePage() {
        List<String> items = Arrays.asList("x", "y");
        Page<String> page = new Page<>(items, 3, 20, 100);

        assertEquals(3, page.getPage());
        assertTrue(page.isHasPrev());
        assertTrue(page.isHasNext());
    }

    @Test
    public void testEmptyResult() {
        Page<String> page = new Page<>(Collections.emptyList(), 1, 20, 0);

        assertEquals(0, page.getTotalCount());
        assertEquals(0, page.getTotalPages());
        assertFalse(page.isHasPrev());
        assertFalse(page.isHasNext());
        assertTrue(page.getItems().isEmpty());
    }

    @Test
    public void testSinglePage() {
        List<String> items = Arrays.asList("a", "b");
        Page<String> page = new Page<>(items, 1, 20, 2);

        assertEquals(1, page.getTotalPages());
        assertFalse(page.isHasPrev());
        assertFalse(page.isHasNext());
    }

    @Test
    public void testPageSizeExactlyDivides() {
        List<String> items = Collections.emptyList();
        Page<String> page = new Page<>(items, 2, 10, 20);

        assertEquals(2, page.getTotalPages());
    }

    @Test
    public void testPageSizeNotExactlyDivides() {
        List<String> items = Collections.emptyList();
        Page<String> page = new Page<>(items, 1, 10, 21);

        assertEquals(3, page.getTotalPages());  // ceil(21/10)
    }
}
