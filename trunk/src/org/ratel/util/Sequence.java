package org.ratel.util;

import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Index.*;

import java.util.*;

public class Sequence implements Iterable<Integer> {

    private static final Sequence EMPTY_SEQUENCE = new Sequence(0, -1) {
        @Override public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                @Override public boolean hasNext() {
                    return false;
                }
                @Override public Integer next() {
                    throw bomb("no next on empty sequence!");
                }
                @Override public void remove() {} 
                
            };
        }  
    };
    private final int start;
    private final int end;

    private Sequence(int start, int end) { 
        this.start = start;
        this.end = end;
    }
    
    public static Sequence oneTo(int n) {
        return sequence(1, n);
    }
    
    public Sequence reverse() {
        return sequence(end, start);
    }
    
    public static Sequence sequence(int start, int end) {
        return new Sequence(start, end);
    }
    
    public static Sequence zeroTo(long length) {
        if(length == 0) return EMPTY_SEQUENCE;
        bombIf(length > Integer.MAX_VALUE, "can't make sequence longer than Integer.MAX_VALUE");
        return sequence(0, (int)length - 1);
    }

    public static <T> Sequence along(Collection<T> c) {
        if(c.isEmpty()) return EMPTY_SEQUENCE;
        return new Sequence(0, c.size() - 1);
    }
    
    public static <T> Sequence along(T[] ts) {
        return along(list(ts));
    }
    
    public static Sequence along(double[] ts) {
        return along(list(ts));
    }
    
    @Override public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int current = start;
            @Override public boolean hasNext() {
                return current != (end > start ? end + 1 : end - 1);
            }

            @Override public Integer next() {
                return end > start ? current++ : current--;
            }

            @Override public void remove() {
                bomb("unimplemented");
            } 
            
        };
    }
    
    public static void requireParallel(Collection<?> ... lists) {
        int length = first(lists).size();
        for (Index<Collection<?>> objects : indexing(rest(lists))) { 
            int thisLength = objects.value.size();
            bombUnless(thisLength == length, 
                "list " + objects.num + " did not have same length as first, " + thisLength + " != " + length);
        }
    }


}
