package malbec.util;

import static org.testng.Assert.*;
import malbec.AbstractBaseTest;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

public class DailyRangeTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testRange() {

        DailyRange dr = new DailyRange(new DateTime(2009, 3, 16, 3, 0, 0, 0), new DateTime(2009, 3, 16, 23,
            0, 0, 0));

        assertTrue(dr.afterStart(new DateTime(2009, 3, 16, 3, 6, 0, 0)));
        assertFalse(dr.afterStart(new DateTime(2009, 3, 16, 2, 6, 0, 0)));

        assertTrue(dr.beforeEnd(new DateTime(2009, 3, 16, 20, 0, 0, 0)));
        assertFalse(dr.beforeEnd(new DateTime(2009, 3, 16, 23, 5, 0, 0)));

        assertTrue(dr.isWithinRange(new DateTime(2009, 3, 16, 10, 5, 0, 0)));

        assertNull(dr.advanceRange(new DateTime(2009, 3, 16, 10, 5, 0, 0)));
        assertNotNull(dr.advanceRange(new DateTime(2009, 3, 17, 10, 5, 0, 0)));

        assertTrue(dr.isWithinRange(new DateTime(2009, 3, 16, 4, 0, 0, 0), new DateTime(2009, 03, 16, 10, 0,
            0, 0)));

        assertFalse(dr.isWithinRange(new DateTime(2009, 3, 16, 2, 0, 0, 0), new DateTime(2009, 03, 16, 10,
            0, 0, 0)));

        assertFalse(dr.isWithinRange(new DateTime(2009, 3, 15, 9, 0, 0, 0), new DateTime(2009, 03, 17, 10,
            0, 0, 0)));
    }

    @Test(groups = { "unittest" })
    public void testOvernightRange() {

        DailyRange dr = new DailyRange(new DateTime(2009, 3, 16, 20, 0, 0, 0), new DateTime(2009, 3, 17, 4,
            0, 0, 0));

        assertTrue(dr.isWithinRange(new DateTime(2009, 3, 16, 23, 59, 0, 0)));
        assertTrue(dr.isWithinRange(new DateTime(2009, 3, 17, 0, 0, 0, 0)));
        DailyRange newDR = dr.advanceRange(dr.getEnd().plusHours(1));
        
        assertNotNull(newDR);
        assertEquals(newDR.getStart(), new DateTime(2009, 3, 17, 20, 0,0,0));
        assertEquals(newDR.getEnd(), new DateTime(2009, 3, 18, 4, 0,0,0));
    }
}
