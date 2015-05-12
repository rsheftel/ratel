package com.fftw.util;

public interface IAggregationStrategy<T> {
    boolean canAggregate(T augend, T addend);

    T aggregate(T augend, T addend);

    T convertToAggregate(T item);
}
