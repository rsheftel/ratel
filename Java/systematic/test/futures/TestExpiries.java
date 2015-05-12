package futures;

import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import util.*;
import db.*;

public class TestExpiries extends DbTestCase {
    public void testThirdFridayNyb() throws Exception {
        insertSaintFailsFakeHoliday();
        Expiry expiry = new ThirdFridayNybModifiedFollowing();
        assertExpiry(expiry, "2008/09/19", "200809");
        assertExpiry(expiry, "2008/12/19", "200812");
        assertExpiry(expiry, "2009/03/20", "200903");
        assertExpiry(expiry, "2009/06/19", "200906");
        // inserted fake holiday above
        assertExpiry(expiry, "2009/09/17", "200909");
    }
    private void insertSaintFailsFakeHoliday() {
        series("holidays_nyb").with(FINANCIAL_CALENDAR).write("2009/09/18", 1);
    }
    public void testThirdWeds() throws Exception {
        Expiry expiry = new ThirdWedMinusTwoLondonBizDaysExpiry();
        assertExpiry(expiry, "2008/09/15", "200809");
    }
    
    public void testThirdWedsNY() throws Exception {
        Expiry expiry = new ThirdWedMinusTwoNewYorkBizDaysExpiry();
        assertExpiry(expiry, "2008/10/10", "200810");
    }
    
    public void testNymexCrudeOil() throws Exception {
        Expiry expiry = new NymexCrudeOilOptionExpiry();
        assertExpiry(expiry, "2008/12/19", "200901");
        assertExpiry(expiry, "2008/09/22", "200810");
        assertExpiry(expiry, "2008/10/21", "200811");
    }
    
    private void assertExpiry(Expiry expiry, String expected, String yearMonth) {
        assertEquals(date(expected), expiry.expiration(new YearMonth(yearMonth)));
    }
    
    public void testExpiryLookup() throws Exception {
        assertEquals(NoneExpiry.class, Expiry.lookup("None").getClass());
        assertEquals(NoneExpiry.class, Expiry.lookup("futures.NoneExpiry").getClass());
    }
}
