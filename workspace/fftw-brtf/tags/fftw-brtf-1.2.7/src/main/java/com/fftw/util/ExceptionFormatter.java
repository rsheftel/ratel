package com.fftw.util;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Handle printing exceptions.
 * <p/>
 * Routines to get strings of stack traces and other neat things.
 */
public class ExceptionFormatter {

    private ExceptionFormatter() {
        // prevent
    }

    /**
     * Return the stack trace as a string.
     *
     * @param t
     * @return
     */
    public static String asString(Throwable t) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

}
