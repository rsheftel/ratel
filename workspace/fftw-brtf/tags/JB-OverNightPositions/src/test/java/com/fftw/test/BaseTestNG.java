package com.fftw.test;

public class BaseTestNG {

    @SuppressWarnings("unchecked")
    protected void assertComparable(Comparable actual, Comparable expected, String message) {
        if (actual.compareTo(expected) != 0) {
            assert false : message + " expected:<" + expected + "> but was:<" + actual + ">";
        }
    }
}
