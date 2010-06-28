package org.ratel.util;

import java.util.*;

import junit.framework.*;

import static org.ratel.r.Util.*;

public abstract class Asserts extends TestCase {

    public static void assertMatches(String regex, Throwable success) {
        try {
            assertMatches(regex, success.getMessage());
        } catch (AssertionFailedError failure) {
            failure.initCause(success);
            throw failure;
        }
    }

    public static <T> void assertMatches(T expected, T actual) {
        assertEquals(expected, actual);
    }

    public static void assertMatches(String regex, String message) {
        bombNull(regex, "no regex provided!");
        bombNull(message, "no message provided!");
        assertTrue(
                "message must match \n" + regex + "\n\tis\n" + message + "\n",
                message.matches("(?s).*" + regex + ".*")
        );
    }

    public static void assertNotMatches(String regex, String message) {
        assertFalse(
            "message must not match \n" + regex + "\n\tis\n" + message + "\n",
            message.matches("(?s).*" + regex + ".*")
        );
    }

    public static <T> void assertSetEquals(Collection<T> expected, Collection<T> actual) {
        assertEquals(new HashSet<T>(expected), new HashSet<T>(actual));
    }

    public static <T> void requireEquals(T left, T right) {
        assertEquals(left, right);
    }


    public static <K, V> Map<K, V> assertSize(int expected, Map<K, V> data) {
        assertSize(expected, data.keySet());
        return data;
    }

    public static <T> Collection<T> assertSize(int expected, Collection<T> ts) {
        bombNull(ts, "ts was null");
        assertEquals("length of " + ts + " did not match " + expected, expected, ts.size());
        return ts;
    }

    public static <T> void assertEmpty(Collection<T> ts) {
        bombNull(ts, "ts was null");
        assertTrue(ts + " \n was not empty...", ts.isEmpty());
    }

    public static <T> void assertContains(T item, Collection<T> container) {
        if (container.contains(item)) return; // do this first to prevent toString from realizing if unnecessary
        bomb("container does not contain \n" + item + "\n does contain\n" + container);
    }

    public static <T> void assertDoesNotContain(T item, Collection<T> container) {
        if (!container.contains(item)) return; // do this first to prevent toString from realizing if unnecessary
        bomb("container contains \n" + item + "\n and shouldn't. contents:\n" + container);
    }



}
