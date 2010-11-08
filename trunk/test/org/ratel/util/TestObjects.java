package org.ratel.util;

import static org.ratel.util.Asserts.*;
import static org.ratel.util.Objects.*;
import junit.framework.*;
public class TestObjects extends TestCase {

    public void testNonemptyBombsOnEmptyLists() throws Exception {
        nonEmpty(list(2));
        try {
            nonEmpty(rest(list(1)));
            fail();
        } catch (RuntimeException e) {
            assertMatches("empty passed to nonempty", e);
        }
    }
    
    public void testRestReturnsNonFirstElements() throws Exception {
        assertEquals(list(2, 3), rest(1, 2, 3));
        assertEquals(list(2), rest(1, 2));
        assertEquals(list(), rest(1));
        assertEquals(list(2, 3), rest(list(1, 2, 3)));
        assertEquals(list(2), rest(list(1, 2)));
        assertEquals(list(), rest(list(1)));
        try {
            rest(list());
            fail("can't take the rest of empty list!");
        } catch (RuntimeException success) {
        }
    }

    
}
