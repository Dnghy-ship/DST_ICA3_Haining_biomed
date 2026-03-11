package cn.edu.zju.bean;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for Sample bean including the variantCount field.
 */
public class SampleTest {

    @Test
    public void testDefaultConstructor() {
        Sample sample = new Sample();
        assertEquals(0, sample.getId());
        assertNull(sample.getCreatedAt());
        assertNull(sample.getUploadedBy());
        assertEquals(0, sample.getVariantCount());
    }

    @Test
    public void testThreeArgConstructor() {
        Date now = new Date();
        Sample sample = new Sample(42, now, "testUser");
        assertEquals(42, sample.getId());
        assertEquals(now, sample.getCreatedAt());
        assertEquals("testUser", sample.getUploadedBy());
        assertEquals(0, sample.getVariantCount());  // defaults to 0
    }

    @Test
    public void testFourArgConstructor() {
        Date now = new Date();
        Sample sample = new Sample(1, now, "alice", 500);
        assertEquals(1, sample.getId());
        assertEquals(now, sample.getCreatedAt());
        assertEquals("alice", sample.getUploadedBy());
        assertEquals(500, sample.getVariantCount());
    }

    @Test
    public void testSetters() {
        Sample sample = new Sample();
        Date now = new Date();
        sample.setId(7);
        sample.setCreatedAt(now);
        sample.setUploadedBy("bob");
        sample.setVariantCount(1234);

        assertEquals(7, sample.getId());
        assertEquals(now, sample.getCreatedAt());
        assertEquals("bob", sample.getUploadedBy());
        assertEquals(1234, sample.getVariantCount());
    }
}
