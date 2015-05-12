package systemdb;

import static systemdb.data.Interval.*;
import static util.Dates.*;
import static util.Range.*;
import systemdb.data.*;
import db.*;

public class TestInterval extends DbTestCase {

    public void testNextEvenInterval() {
        assertNext(HOURLY, "2007/12/31 23:59:59", "2008/01/01 00:00:00");
        assertNext(HOURLY, "2008/01/01 00:00:00", "2008/01/01 01:00:00");
        assertNext(HOURLY, "2008/01/01 01:00:01", "2008/01/01 02:00:00");

        assertNext(SECOND, "2007/12/31 23:59:59", "2008/01/01 00:00:00");
        assertNext(SECOND, "2008/01/01 00:00:00", "2008/01/01 00:00:01");
        assertNext(SECOND, "2008/01/01 01:00:01", "2008/01/01 01:00:02");

        assertNext(FIVE_MINUTES, "2007/12/31 23:59:59", "2008/01/01 00:00:00");
        assertNext(FIVE_MINUTES, "2008/01/01 00:00:00", "2008/01/01 00:05:00");
        assertNext(FIVE_MINUTES, "2008/01/01 00:05:00", "2008/01/01 00:10:00");
        assertNext(FIVE_MINUTES, "2008/01/01 01:00:01", "2008/01/01 01:05:00");
        
        assertNext(DAILY, "2007/12/31 23:59:59", "2008/01/01 00:00:00");
        assertNext(DAILY, "2008/01/01 00:00:00", "2008/01/02 00:00:00");
        assertNext(DAILY, "2008/01/01 00:00:01", "2008/01/02 00:00:00");
        
    }
    
    
    
    public void testCurrentRange() throws Exception {
        assertEquals(range("2008/01/01 00:00:00", "2008/01/01 00:05:00"), FIVE_MINUTES.range("2008/01/01 00:00:00"));
        assertEquals(range("2008/01/01 00:00:00", "2008/01/01 00:05:00"), FIVE_MINUTES.range("2008/01/01 00:00:02"));
        assertEquals(range("2008/01/01 00:00:00", "2008/01/01 00:05:00"), FIVE_MINUTES.range("2008/01/01 00:04:59"));
        assertEquals(range("2008/01/01 00:05:00", "2008/01/01 00:10:00"), FIVE_MINUTES.range("2008/01/01 00:05:00"));
        
        assertEquals(range("2007/12/31 00:00:00", "2008/01/01 00:00:00"), DAILY.range("2007/12/31 23:59:59"));
        assertEquals(range("2008/01/01 00:00:00", "2008/01/02 00:00:00"), DAILY.range("2008/01/01 00:00:00"));
        assertEquals(range("2008/01/01 00:00:00", "2008/01/02 00:00:00"), DAILY.range("2008/01/01 00:00:01"));
    }

    private void assertNext(Interval interval, String start, String expectedNext) {
        assertEquals(date(expectedNext), interval.nextBoundary(date(start)));
    }

}
