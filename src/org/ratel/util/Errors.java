package org.ratel.util;

import static org.ratel.util.Log.*;
import static org.ratel.util.Strings.*;

import java.io.*;

public class Errors {

    public static RuntimeException bomb(Throwable t) {
        RuntimeException re = new RuntimeException(t);
        if (false) err("throw: ", re);
        throw re;
    }

    public static RuntimeException bomb(String s, Throwable t) {
        RuntimeException re = new RuntimeException(s, t);
        if (false) err("throw: " + s, re);
        throw re;
    }

    public static RuntimeException bomb(String s) {
        RuntimeException re = new RuntimeException(s);
        if (false) err("throw: " + s, re);
        throw re;
    }
    
    public static void bombUnless(boolean condition, String message) {
        bombIf(!condition, message);
    }

    public static void bombIf(boolean condition, String message) {
        if (condition) bomb(message);
    }
    
    public static <T> T bombNull(T o, String message) {
        bombIf(o == null, message);
        return o;
    }

    public static String bombEmpty(String s, String message) {
        bombIf(isEmpty(s), message);
        return s;
    }
    
    public static void bombNotNull(Object o, String message) {
        bombUnless(o == null, message);
    }

    public static String trace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    
}
