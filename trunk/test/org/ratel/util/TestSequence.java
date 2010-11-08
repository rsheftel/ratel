package org.ratel.util;

import java.util.*;

import junit.framework.*;
import static org.ratel.util.Index.*;

import static org.ratel.util.Objects.*;
import static org.ratel.util.Sequence.*;
public class TestSequence extends TestCase {

    private static final List<String> LETTERS = list("a", "b", "c");

    public void testCanCreateSequences() throws Exception {
        assertSequence(list(1,2,3,4,5,6,7,8,9,10), 1, 10);
        assertSequence(list(-4,-3,-2,-1,0), -4, 0);
        assertSequence(list(0, -1, -2, -3, -4), 0, -4);
        assertSequence(list(0), 0, 0);
    }
    
    public void testAlongWithNoElements() throws Exception {
        for(int i : along(list())) 
            fail("should not have used any  i " + i);
    }

    private void assertSequence(List<Integer> expected, int start, int end) {
        List<Integer> result = empty();
        for(int i : sequence(start,end)) 
            result.add(i);
        assertEquals(expected, result);
    }
    
    public void testAlong() throws Exception {
        List<Integer> indices = empty();
        List<String> copy = empty();
        for(int i : along(LETTERS)) {
            indices.add(i);
            copy.add(LETTERS.get(i));
        }
        assertEquals(list(0,1,2), indices);
        assertEquals(LETTERS, copy);
    }
    
    public void testOneTo() throws Exception {
        List<Integer> result = empty();
        for(int i : oneTo(3)) result.add(i);
        assertEquals(list(1,2,3), result);
    }
    
    public void testSeqLoopsHaltWhenZeroLength() throws Exception {
        List<Integer> result = empty();
        for(int i : zeroTo(0)) result.add(i);
        assertTrue(result.isEmpty());
    }
    
    public void testIndexing() throws Exception {
        List<Integer> indices = empty();
        List<String> copy = empty();
        for(Index<String> i : indexing(LETTERS)) {
            indices.add(i.num);
            copy.add(i.value);
            if (i.value.equals("c")) assertTrue(i.isLast());
            if (i.value.equals("a")) assertTrue(i.isFirst());
        }
        assertEquals(LETTERS, copy);
        assertEquals(list(0, 1, 2), indices); 

    }
    
}
