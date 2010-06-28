package org.ratel.r;

import java.util.*;

import static java.util.Arrays.*;

public class Util {
    public static String join(String delim, String ... s) {
        return join(delim, list(s));
    }

    public static String join(String delim, Collection<String> strings) {
        StringBuilder b = new StringBuilder();
        for(Iterator<String> i = strings.iterator(); i.hasNext(); ) {
            b.append(i.next());
            if (i.hasNext()) b.append(delim);
        }
        return b.toString();
    }

    public static String commaSep(Collection<String> s) {
        return join(", ", s);
    }

    public static String paren(String s) {
        return wrap("(", s, ")");
    }

    public static <T> List<String> strings(T ... ts) {
        return strings(list(ts));
    }

    public static <T> List<String> strings(Collection<T> ts) {
        List<String> result = empty();
        for (T t : ts)
            result.add(t == null ? "null" : t.toString());
        return result;
    }

    public static <T> List<T> empty() {
        return new ArrayList<T>();
    }


    public static <T> List<T> list(T ... ts) {
        return asList(ts);
    }

    public static <T> List<T> list(Collection<T> ts) {
        return new ArrayList<T>(ts);
    }

    public static String dQuote(String s) {
        return wrap(s, "\"");
    }

    private static String wrap(String s, String wrapping) {
        return wrap(wrapping, s, wrapping);
    }

    private static String wrap(String left, String s, String right) {
        bombNull(left, "no left!");
        bombNull(left, "no right!");
        return left + bombNull(s, "no s!") + right;
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

    public static RuntimeException bomb(Throwable t) {
        throw new RuntimeException(t);
    }

    public static RuntimeException bomb(String s, Throwable t) {
        throw new RuntimeException(s, t);
    }

    public static RuntimeException bomb(String s) {
        throw new RuntimeException(s);
    }

    public static <T> T[] array(T ... ts) {
        return ts;
    }

    public static <T> List<T> copy(List<T> list) {
        return new ArrayList<T>(list);
    }

        public static <T> T the(T ... ts) {
        return the(list(ts));
    }

    public static <T> T the(Collection<T> ts) {
        bombNull(ts, "null list in the");
        Iterator<T> i = ts.iterator();
        bombUnless(i.hasNext(), "empty list passed to the");
        T result = i.next();
        if(i.hasNext())
            bomb("tried to take the of multiple elements in " + ts);
        return result;
    }

    public static <T> T first(T ... ts) {
        bombNull(ts, "null ts!");
        return first(list(ts));
    }

    public static <T> T first(Collection<T> ts) {
        bombNull(ts, "null ts!");
        bombIf(ts.isEmpty(), "no first element!");
        return ts.iterator().next();
    }
    
    public static <T> T second(Collection<T> ts) {
        return nth(ts, 2);
    }

    public static <T> T nth(Collection<T> ts, int n) {
        bombNull(ts, "null ts!");
        bombUnless(ts.size() >= n, "no element for " + n + " in " + ts);
        Iterator<T> it = ts.iterator();
        for(int i = 0; i < n - 1; i++) it.next();
        return it.next();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }

    public static <T> Set<T> emptySet() {
        return new HashSet<T>();
    }


}
