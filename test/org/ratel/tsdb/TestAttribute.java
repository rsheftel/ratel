package org.ratel.tsdb;

import static org.ratel.tsdb.Attribute.*;
import static org.ratel.util.Asserts.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import junit.framework.*;
public class TestAttribute extends TestCase {
    public void testAttributeId() throws Exception {
        assertEquals(2, attribute("ticker").id());
        try {
            attribute("not an attribute").id();
            fail();
        } catch (RuntimeException success) {
            assertMatches("couldn't find attribute", success);
        } 
    }
    
    public void testDateAttributeValueColumns() throws Exception {
        List<Integer> ids = EXPIRY_DATE.valueIds(list("2008/09/19"));
        assertMatches(1363, the(ids));
    }
}
