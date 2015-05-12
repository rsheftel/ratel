package com.fftw.util;


public interface IAggregationStrategy<K,T> {

    boolean canAggregate(T augend, T addend);
    
    T aggregate(T augend, T addend);
 
    K getAggregateKeyForItem(T item);
}
