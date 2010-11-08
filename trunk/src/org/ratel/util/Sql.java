package org.ratel.util;

public class Sql {
    public static String quote(String s) {
        s = s.replaceAll("'", "''");
        String quoted = '\'' + s + '\'';
        return quoted;
    }
}
