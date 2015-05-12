package com.fftw.util;

/**
 * This object knows how to add the values of the specified instance to itself to create a new instance.
 */
public interface Aggregatable<T> {

    T aggregate(T addend);

    boolean aggregatable(T other);
}
