package org.ratel.util;

import java.util.*;

import static org.ratel.r.Util.*;

public class SampleRatelClass implements Comparable<SampleRatelClass> {
    private final String s;

    public static final SampleRatelClass HELLO = new SampleRatelClass();
    public static final SampleRatelClass GOODBYE = new SampleRatelClass("goodbye");

    // part of test - do not remove
    static {
    }

    SampleRatelClass() {
        this("hello");
    }

    public SampleRatelClass(String s) {
        this.s = s;
    }

    public SampleRatelClass(int i) {
        this(String.valueOf(i));
    }

    @Override public String toString() {
        return world(3).toString();
    }

    public String world() {
        return s + ", world!";
    }

    private List<String> world(int n) {
        return Collections.nCopies(n, world());
    }

    public static void doNothing() {}

    @Override public int compareTo(SampleRatelClass o) {
        return 0;
    }

    public static String squish(String...args) {
        return join("", args);
    }

    public static int sum(int...args) {
        int result = 0;
        for (int i : args)
            result += i;
        return result ;
    }

    public static double sum(double...args) {
        double result = 0;
        for (double i : args)
            result += i;
        return result ;
    }

    public SampleRatelClass[] copies(int n) {
        SampleRatelClass[] result = new SampleRatelClass[n];
        for(int i = 0; i < n; i++) result[i] = this;
        return result;
    }

    public String[] stringCopies(int n) {
        String[] result = new String[n];
        for(int i = 0; i < n; i++) result[i] = s;
        return result;
    }

    public static int[] intCopies(int n) {
        int[] result = new int[n];
        for(int i = 0; i < n; i++) result[i] = n;
        return result;
    }

    public static double[] doubleCopies(double n) {
        double[] result = new double[5];
        for(int i = 0; i < 5; i++) result[i] = n;
        return result;
    }

    public static boolean allSame(SampleRatelClass...args) {
        if (args.length == 0) return true;
        SampleRatelClass first = first(args);
        for (SampleRatelClass sampleRatelClass : args)
            if (!sampleRatelClass.equals(first)) return false;
        return true;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((s == null) ? 0 : s.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final SampleRatelClass other = (SampleRatelClass) obj;
        if (s == null) {
            if (other.s != null) return false;
        } else if (!s.equals(other.s)) return false;
        return true;
    }


}

