package org.ratel.r;

import static org.ratel.r.R.*;

import java.io.*;

import org.ratel.util.*;

public class TestR extends Asserts {

    public void testRQuote() throws Exception {
        assertEquals("I\\'m a string", rQuote("I'm a string", '\''));
        assertEquals("I'm a string", rQuote("I'm a string", '"'));
        assertEquals("\"I\\'m a string\"", rQuote("\"I'm a string\"", '\''));
        assertEquals("\\\\foo", rQuote("\\foo", '\''));
        assertEquals("\\\\\\'foo", rQuote("\\'foo", '\''));
    }


    public void testRString() throws Exception {
        assertEquals("Hello, world!", rString("'Hello, world!'"));
        assertEquals("Hello, world!", rString("squish('Hello, ', 'world!')"));
    }

    public void testR() throws Exception {
        assertEquals(3.0, rDouble("1+2"));
        assertEquals(3, r("as.integer(1+2)").asInt());
        try {
            assertEquals(3, rInt("1+2"));
            fail();
        } catch (Exception e) {
            assertMatches("was not INT", e);
        }
        assertEquals(3.0, rDouble("1+2"));
    }

    public void testLogger() throws Exception {
        StringWriter sw = new StringWriter();
        LogWriter log = new LogWriter(sw);
        r("1");
        R.setLogger(log);
        r("cat('Hello, world!\n')");
        assertEquals("Hello, world!\n", sw.toString());
        R.clearLogger();
        r("cat('Hello, world!\n')");
        assertEquals("Hello, world!\n", sw.toString());
    }
}
