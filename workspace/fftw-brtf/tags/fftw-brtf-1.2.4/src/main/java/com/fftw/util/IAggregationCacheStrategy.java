package com.fftw.util;


public interface IAggregationCacheStrategy<K,T> {

    boolean canAggregate(T augend, T addend);
    
    T aggregate(T augend, T addend);
 
    K getAggregateKeyForItem(T item);
    
    K getAggregateKeyForKey(K key);
    
    T convertToAggregate(T item);
}
