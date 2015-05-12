package systemdb.metadata;

import static systemdb.metadata.MarketHistoryTable.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import systemdb.data.*;

import db.*;

public class MarketHistoryTableTest extends DbTestCase {

    private static final Symbol MARKET = new Symbol("TEST.SP.1C");

    public void testStartsAndDoesNotEnd() throws Exception {
        assertFalse(MARKET.hasPeriods());
        assertSize(0, MARKET.activePeriods());
        MARKET.addPeriod("2008/04/30", "2008/05/01");
        assertTrue(MARKET.hasPeriods());
        assertSize(1, MARKET.activePeriods());
        MARKET.addPeriod(date("2008/05/31"), null);
        assertSize(2, MARKET.activePeriods());
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertSize(2, inactive);
        MarketPeriod first = first(inactive);
        assertFalse(first.hasStart());
        assertTrue(first.hasEnd());
        assertEquals(date("2008/04/29"), first.end());
        assertEquals(date("2008/05/02"), second(inactive).start());
        assertEquals(date("2008/05/30"), second(inactive).end());
    }
    
    public void testStartsWayEarlyAndEnds() throws Exception {
        MARKET.addPeriod(null, date("2008/04/20"));
        MARKET.addPeriod("2008/04/30", "2008/05/20");
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertSize(2, inactive);
        assertPeriod(first(inactive), "2008/04/21", "2008/04/29");
        assertPeriod(second(inactive), "2008/05/21", null);
    }
    
    public void testStartsWayEarlyAndDoesNotEnd() throws Exception {
        MARKET.addPeriod(null, date("2008/04/20"));
        MARKET.addPeriod(date("2008/04/25"), date("2008/04/27"));
        MARKET.addPeriod(date("2008/04/29"), null);
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertSize(2, inactive);
        assertPeriod(first(inactive), "2008/04/21", "2008/04/24");
        assertPeriod(second(inactive), "2008/04/28", "2008/04/28");
    }
    
    public void testStartsAndEnds() throws Exception {
        MARKET.addPeriod(date("2008/03/15"), date("2008/04/20"));
        MARKET.addPeriod(date("2008/04/25"), date("2008/05/15"));
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertSize(3, inactive);
        assertPeriod(first(inactive), null, "2008/03/14");
        assertPeriod(second(inactive), "2008/04/21", "2008/04/24");
        assertPeriod(third(inactive), "2008/05/16", null);
    }
    
    private void assertPeriod(MarketPeriod period, String start, String end) {
        assertEquals(new MarketPeriod(MARKET, dateMaybe(start), dateMaybe(end)), period);
    }
    
    public void testCantAddPeriodWithMisorderedDates() throws Exception {
        try {
            MARKET.addPeriod(date("2008/03/15"), date("2008/03/14"));
        } catch (RuntimeException e) {
            assertMatches("misordered dates", e);
        }
    }
    
    public void testBombsWhenAddingOrReadingOverlappingPeriods() { 
        MARKET.addPeriod("2008/05/15", null);
        MARKET.addPeriod("2008/04/15", "2008/04/17");
        MARKET.addPeriod(null, "2008/03/15");
        
        overlappingPeriodFails(null, "2008/03/14");
        overlappingPeriodFails("2008/03/15", "2008/03/16");
        overlappingPeriodFails("2008/03/14", "2008/03/16");
        overlappingPeriodFails("2008/03/15", "2008/03/15");
    }

    private void overlappingPeriodFails(String start, String end) {
        try {
            MARKET.addPeriod(dateMaybe(start), dateMaybe(end));
            fail("didn't fail");
        } catch (RuntimeException e) {
            assertMatches("period exists", e);
        }
        MARKET_HISTORY.insertUnchecked(dateMaybe(start), dateMaybe(end), MARKET.name());
        try {
            MARKET.activePeriods();
            fail("didn't fail");
        } catch (RuntimeException e) {
            assertMatches("overlap detected", e);
        }
        MARKET_HISTORY.delete(dateMaybe(start), dateMaybe(end), MARKET.name());
    }

    public void testInactivePeriodsWithOnePeriodUntil() throws Exception {
        MARKET.addPeriod(null, "2008/03/15");
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertPeriod(first(inactive), "2008/03/16", null);
    }

    public void testInactivePeriodsWithOnePeriodBefore() throws Exception {
        MARKET.addPeriod("2008/03/15", null);
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertPeriod(first(inactive), null, "2008/03/14");
    }
    
    public void testInactivePeriodsWithOnePeriodBetween() throws Exception {
        MARKET.addPeriod("2008/03/15", "2008/03/18");
        List<MarketPeriod> inactive = MARKET.inactivePeriods();
        assertPeriod(first(inactive), null, "2008/03/14");
        assertPeriod(second(inactive), "2008/03/19", null);
    }
    
}
