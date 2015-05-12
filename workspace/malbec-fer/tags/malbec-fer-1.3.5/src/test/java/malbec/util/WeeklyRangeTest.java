package malbec.util;

import static org.testng.Assert.*;
import malbec.AbstractBaseTest;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

public class WeeklyRangeTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testWeeklyRange() {

        DateTimeUtil.freezeTime("2009/04/11 09:00:00");
        createAndTestMondayToFridayAfter();

        DateTime[] rangeDateTime = DateTimeUtil.determineWeeklyRange("Monday", "04:30:00", "Friday",
            "16:00:00");

        assertEquals(rangeDateTime[0], new DateTime(2009, 4, 6, 4, 30, 0, 0));
        assertEquals(rangeDateTime[1], new DateTime(2009, 4, 10, 16, 0, 0, 0));

        WeeklyRange wrTime = new WeeklyRange(rangeDateTime[0], rangeDateTime[1]);
        WeeklyRange nextRangeTime = wrTime.startsAfter(new DateTime(2009, 4, 11, 9, 0, 0, 0));

        assertEquals(nextRangeTime.getStart(), new DateTime(2009, 4, 13, 4, 30, 0, 0));
        assertEquals(nextRangeTime.getEnd(), new DateTime(2009, 4, 17, 16, 0, 0, 0));
    }

    @Test(groups = { "unittest" })
    public void testWeeklyRangeEveryDay() {

        DateTimeUtil.freezeTime("2009/04/11 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();
        
        DateTimeUtil.freezeTime("2009/04/12 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();

        DateTimeUtil.freezeTime("2009/04/13 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();

        DateTimeUtil.freezeTime("2009/04/14 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();

        DateTimeUtil.freezeTime("2009/04/15 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();

        DateTimeUtil.freezeTime("2009/04/16 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();

        DateTimeUtil.freezeTime("2009/04/17 09:00:00");
        // Monday 00:00:00 - Friday 23:59:59
        createAndTestMondayToFridayCurrent();
        
        DateTime[] rangeDate = DateTimeUtil.determineWeeklyRange("Monday", "Friday");

        WeeklyRange wr = new WeeklyRange(rangeDate[0], rangeDate[1]);
        WeeklyRange nextRange = wr.currentOrNext(new DateTime(2009,4,1,0,0,0,0));
        
        assertNull(nextRange);
    }

    private void createAndTestMondayToFridayCurrent() {
        DateTime[] rangeDate = DateTimeUtil.determineWeeklyRange("Monday", "Friday");

        WeeklyRange wr = new WeeklyRange(rangeDate[0], rangeDate[1]);
        WeeklyRange nextRange = wr.currentOrNext(new DateTime());

        assertEquals(nextRange.getStart(), new DateTime(2009, 4, 13, 0, 0, 0, 0));
        assertEquals(nextRange.getEnd(), new DateTime(2009, 4, 17, 23, 59, 59, 0));
    }

    private void createAndTestMondayToFridayAfter() {
        DateTime[] rangeDate = DateTimeUtil.determineWeeklyRange("Monday", "Friday");

        assertEquals(rangeDate[0], new DateTime(2009, 4, 6, 0, 0, 0, 0));
        assertEquals(rangeDate[1], new DateTime(2009, 4, 10, 23, 59, 59, 0));

        WeeklyRange wr = new WeeklyRange(rangeDate[0], rangeDate[1]);
        WeeklyRange nextRange = wr.startsAfter(new DateTime(2009, 4, 11, 9, 0, 0, 0));

        assertEquals(nextRange.getStart(), new DateTime(2009, 4, 13, 0, 0, 0, 0));
        assertEquals(nextRange.getEnd(), new DateTime(2009, 4, 17, 23, 59, 59, 0));
    }
    
}
