package cn.edu.zju.servlet;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the URL-sanitisation helper in DrugServlet.
 * Pure-Java, no database or servlet container required.
 */
public class DrugServletTest {

    @Test
    public void testSanitiseUrlNull() {
        assertNull(DrugServlet.sanitiseUrl(null));
    }

    @Test
    public void testSanitiseUrlEmpty() {
        assertNull(DrugServlet.sanitiseUrl(""));
        assertNull(DrugServlet.sanitiseUrl("  "));
    }

    @Test
    public void testSanitiseUrlHttpsIsUnchanged() {
        String url = "https://www.pharmgkb.org/drug/PA448710";
        assertEquals(url, DrugServlet.sanitiseUrl(url));
    }

    @Test
    public void testSanitiseUrlHttpPromotedToHttps() {
        assertEquals(
                "https://www.pharmgkb.org/drug/PA448710",
                DrugServlet.sanitiseUrl("http://www.pharmgkb.org/drug/PA448710")
        );
    }

    @Test
    public void testSanitiseUrlJavascriptRejected() {
        assertNull(DrugServlet.sanitiseUrl("javascript:alert(1)"));
    }

    @Test
    public void testSanitiseUrlDataUriRejected() {
        assertNull(DrugServlet.sanitiseUrl("data:text/html,<script>alert(1)</script>"));
    }

    @Test
    public void testSanitiseUrlFtpRejected() {
        assertNull(DrugServlet.sanitiseUrl("ftp://example.com/file"));
    }
}
