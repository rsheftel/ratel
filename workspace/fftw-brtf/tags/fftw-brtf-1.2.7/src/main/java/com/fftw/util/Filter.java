package com.fftw.util;

/**
 * Base an a solution for Sun bug 6350345 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6350345
 */
public interface Filter<T> {
    /**
     * Compare this item with the specified filter criteria.
     *
     * @param item
     * @return
     */
    boolean accept(T item);
}
