package com.fftw.util;

import java.math.BigDecimal;

import junit.framework.TestCase;

import com.fftw.bloomberg.util.FixedWidthFormatter;

public class TestFixedWidthFormatter extends TestCase
{

    public void testNumberLength1() {
        
        String fn = FixedWidthFormatter.formatNumber(1, 1);
        
        assertEquals(1, fn.length());
    }
    
    public void testNumberDouble() {
        String db = FixedWidthFormatter.formatNumber(1234.098, 8,7);
        assertEquals("000012340980000", db);
        assertEquals(15, db.length());
        
        db = FixedWidthFormatter.formatNumber(0.0055, 8,7);
        assertEquals("000000000055000", db);
        assertEquals(15, db.length());
    }
    
    public void testNumberDoubleParse() {
        double restult = FixedWidthFormatter.parseNumber("00055", 4);
        
        assertEquals(0.0055, restult);
    }
    public void testNumberBigDecimal() {
        String db = FixedWidthFormatter.formatNumber(new BigDecimal(1234.098), 8,7);
        assertEquals("000012340980000", db);
        assertEquals(15, db.length());
        
        db = FixedWidthFormatter.formatNumber(new BigDecimal(0.0055), 8,7);
        assertEquals("000000000055000", db);
        assertEquals(15, db.length());
    }
    
}
