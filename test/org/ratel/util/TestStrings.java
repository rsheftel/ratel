package org.ratel.util;

import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;
public class TestStrings extends Asserts {

    
    public void testLeftSpacePad() throws Exception {
        assertEquals("  a", leftSpacePad(3, "a"));
        assertEquals("aaa", leftSpacePad(3, "aaa"));
        assertEquals("aaaa", leftSpacePad(3, "aaaa"));
    }
    
    public void testLeftZeroPad() throws Exception {
        assertEquals("005", leftZeroPad(5, 3));
        assertEquals("0005", leftZeroPad(5, 4));
        assertEquals("00005", leftZeroPad(5, 5));
        assertEquals("-00005", leftZeroPad(-5, 5));
        assertEquals("123456", leftZeroPad(123456, 3));
        assertEquals("123456", leftZeroPad(123456, 6));
        
    }
    
    public void testChompWorksKindOfLikePerl() throws Exception {
        assertEquals("a", chomp("a"));
        assertEquals("a\na", chomp("a\na"));
        assertEquals("a", chomp("a\n"));
        assertEquals("a\r\na", chomp("a\r\na"));
        assertEquals("a", chomp("a\r\n"));
    }
    public void testJoin() throws Exception {
        assertEquals("join", join("", "jo", "in"));
        assertEquals("jo.in", join(".", "jo", "in"));
        assertEquals(", jo, in", join(", ", "", "jo", "in"));
    }
    
    public void testSplitWorksLikeYouWouldExpect() throws Exception {
        assertEquals(list(""), split(",", ""));
        assertEquals(list("a", "b"), split(",", "a,b"));
        assertEquals(list("a", "b", "c"), split(":||:", "a:||:b:||:c"));
        assertEquals(list("", "b"), split(",", ",b"));
        assertEquals(list("a", ""), split(",", "a,"));
        assertEquals(list("", ""), split(",", ","));
    }
    
    public void testJavaClassify() throws Exception {
        assertEquals("IntIdentity", javaClassify("int() identity"));
        assertEquals("Int", javaClassify("int"));
        assertEquals("Numeric", javaClassify("numeric()"));
        assertEquals("Numeric", javaClassify("numeric"));
        assertEquals("NumericIdentity", javaClassify("numeric() identity"));
        assertEquals("DataSource", javaClassify("data_source"));
        assertEquals("AlreadyCorrect", javaClassify("AlreadyCorrect"));
        assertEquals("ATest", javaClassify("aTest"));
        assertEquals("TestWith1234Numbers", javaClassify("test_with_1234_numbers"));
        assertEquals("TestWith1234Numbers", javaClassify("test_with_1234numbers"));
        assertEquals("testWith1234Numbers", javaIdentifier("test_with_1234numbers"));
        assertEquals("", javaIdentifier(""));
        assertEquals("", javaClassify(""));
        assertEquals("a", javaIdentifier("A"));
        assertEquals("B", javaClassify("b"));
    }
    
    public void testIsEmpty() throws Exception {
        assertTrue(isEmpty((String)null));
        assertTrue(isEmpty(""));
        assertTrue(isEmpty("  "));
        assertFalse(isEmpty("asdf"));
        assertTrue(hasContent("asdf"));
        assertFalse(hasContent(""));
    }
    
    public void testNDecimals() throws Exception {
        assertEquals("1.0", nDecimals(1, 1.0));
        assertEquals("1.0", nDecimals(1, 1.01));
        assertEquals("0.3", nDecimals(1, 0.25));
        assertEquals("1.1", nDecimals(1, 1.05000000));
        assertEquals("1.1", nDecimals(1, 1.05000001));
        assertEquals("1.01", nDecimals(2, 1.01));
        assertEquals("Inf", nDecimals(2, Double.POSITIVE_INFINITY));
        assertEquals("-Inf", nDecimals(2, Double.NEGATIVE_INFINITY));
        assertEquals("NaN", nDecimals(2, Double.NaN));
    }
    
}
