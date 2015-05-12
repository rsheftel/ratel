package malbec.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringUtils {

    private StringUtils() {
    // prevent
    }

    public static String exceptionToString(Throwable t) {
        StringWriter sw = new StringWriter(1024);

        t.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    public static String upperCaseOrNull(String value) {
        if (value != null) {
            return value.toUpperCase();
        }
        return null;
    }

    /**
     * Return the interned instance of this this string.
     * 
     * @param stringToLockOn
     * @return
     */
    public static synchronized Object getLock(String stringToLockOn) {
        return stringToLockOn.intern();
    }
}
