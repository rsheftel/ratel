package org.ratel.util;

import static org.ratel.util.Objects.*;

import java.util.*;

public class Index <T> {
    public final int num;
    public final T value;
    private boolean isFirst;
    private boolean isLast;
    private Index(int i, T o) { this.num = i; this.value = o; } 

    public static <T> Iterable<Index<T>> indexing(T[] ts) {
        return indexing(list(ts));
    }
    
    public static<T> Iterable<Index<T>> indexing(Iterable<T> l1) {
        List<Index<T>> result = empty();
        int i = 0;
        for (T t : l1) 
            result.add(new Index<T>(i++, t));
        if (!result.isEmpty()) { 
            first(result).beFirst(); 
            last(result).beLast(); 
        } 
        return result;
    }

    public boolean isFirst() { return isFirst; }
    public boolean isLast() { return isLast; }
    private void beFirst() { isFirst = true; }
    private void beLast() { isLast = true; }
}