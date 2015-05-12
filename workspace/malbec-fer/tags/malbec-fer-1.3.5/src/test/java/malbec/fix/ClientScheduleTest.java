package malbec.fix;

import static malbec.util.DateTimeUtil.determineWeeklyRange;
import static org.testng.Assert.*;
import malbec.AbstractBaseTest;
import malbec.util.DateTimeUtil;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 * Test the schedule for <code>FixClient</code>s.
 * 
 * A FIX Session has a schedule that deals with the resetting of sequence numbers, this schedule is to deal
 * with when the client should be connected.
 * 
 */
public class ClientScheduleTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testScheduleTime() {
        ClientSchedule cs = new ClientSchedule();
        DateTime startTime = new DateTime();
        DateTime endTime = startTime.plusDays(5);

        cs.setStartTime(startTime);
        cs.setEndTime(endTime);

        // test valid times
        DateTime startOfSchedule = new DateTime(startTime);
        DateTime endOfSchedule = new DateTime(endTime);
        long middle = (endTime.getMillis() - startTime.getMillis()) / 2;
        DateTime middleOfSchedule = startTime.plus(middle);

        assertTrue(cs.isWithinSchedule(startOfSchedule), "Failed to include start time within schedule");
        assertTrue(cs.isWithinSchedule(endOfSchedule), "Failed to include the end time within schedule");
        assertTrue(cs.isWithinSchedule(middleOfSchedule), "Failed to include within schdule");

        // test invalid times
        DateTime beforeSchedule = startTime.minusMillis(1);
        DateTime afterSchedule = endTime.plusMillis(1);

        assertFalse(cs.isWithinSchedule(beforeSchedule), "Included time when it is before schdule");
        assertFalse(cs.isWithinSchedule(afterSchedule), "Included time when it is after schdule");
    }

    @Test(groups = { "unittest" })
    public void testNextPeriod() {
        ClientSchedule cs = new ClientSchedule();
        DateTime startTime = new DateTime();
        DateTime endTime = startTime.plusDays(5);

        cs.setStartTime(startTime);
        cs.setEndTime(endTime);

        assertFalse(cs.advanceSchedule(), "Incorrectly advanced schedule");

        DateTime prevStartTime = startTime.minusWeeks(1);
        DateTime prevEndTime = endTime.minusWeeks(1);

        cs.setStartTime(prevStartTime);
        cs.setEndTime(prevEndTime);

        assertTrue(cs.advanceSchedule(), "Failed to advance schdule");

        assertTrue(cs.isWithinSchedule(startTime), "Failed to include start time within schedule");
        assertTrue(cs.isWithinSchedule(endTime), "Failed to include the end time within schedule");
    }
    
    @Test(groups = { "unittest-debug", "unittest" })
    public void testWeekendStartup() {
        ClientSchedule cs = new ClientSchedule();

        // Saturday before schedule starts
        DateTimeUtil.freezeTime("2009/04/11 08:59:33");
        createWeekly(cs);
        assertEquals(cs.getStartTime(), new DateTime(2009, 4, 5, 18, 5, 0, 0));
        assertEquals(cs.getEndTime(), new DateTime(2009, 4, 10, 17,28,0,0));
        assertFalse(cs.isWithinSchedule());
        assertTrue(cs.advanceSchedule(), "Failed to advance schedule");

        // Sunday before schedule starts
        DateTimeUtil.freezeTime("2009/04/12 08:59:33");
        createWeekly(cs);
        assertEquals(cs.getStartTime(), new DateTime(2009, 4, 5, 18, 5, 0, 0));
        assertEquals(cs.getEndTime(), new DateTime(2009, 4, 10, 17,28,0,0));
        assertFalse(cs.isWithinSchedule());
        assertTrue(cs.advanceSchedule(), "Failed to advance schedule");

        // Monday within schedule
        DateTimeUtil.freezeTime("2009/04/13 08:59:33");
        createWeekly(cs);
        weeklyWithinTest(cs);
        
        // Tuesday within schedule
        DateTimeUtil.freezeTime("2009/04/14 08:59:33");
        createWeekly(cs);
        weeklyWithinTest(cs);
        
        // Wednesday within schedule
        DateTimeUtil.freezeTime("2009/04/15 08:59:33");
        createWeekly(cs);
        weeklyWithinTest(cs);;
        
        // Thursday within schedule
        DateTimeUtil.freezeTime("2009/04/16 08:59:33");
        createWeekly(cs);
        weeklyWithinTest(cs);
        
        // Friday within schedule
        DateTimeUtil.freezeTime("2009/04/17 08:59:33");
        createWeekly(cs);
        weeklyWithinTest(cs);
        
        // Saturday after schedule
        DateTimeUtil.freezeTime("2009/04/18 08:59:33");
        DateTime[] range = determineWeeklyRange("Sunday", "18:05:00", "Friday", "17:28:00");
        cs.setStartTime(range[0]);
        cs.setEndTime(range[1]);

        assertFalse(cs.isWithinSchedule());
        assertTrue(cs.advanceSchedule(), "Failed to advanced schedule");
       
        assertEquals(cs.getStartTime(), new DateTime(2009, 4, 19, 18,5,0,0));
        assertEquals(cs.getEndTime(), new DateTime(2009, 4, 24, 17,28,0,0));
        assertFalse(cs.isWithinSchedule());
        assertFalse(cs.advanceSchedule(), "Incorrectly advanced schedule");
        
    }

    private void createWeekly(ClientSchedule cs) {
        DateTime[] range = determineWeeklyRange("Sunday", "18:05:00", "Friday", "17:28:00");
        cs.setStartTime(range[0]);
        cs.setEndTime(range[1]);
    }
    
    private void weeklyWithinTest(ClientSchedule cs) {
        assertEquals(cs.getStartTime(), new DateTime(2009, 4, 12, 18,5,0,0));
        assertEquals(cs.getEndTime(), new DateTime(2009, 4, 17, 17,28,0,0));
        assertTrue(cs.isWithinSchedule());
        assertFalse(cs.advanceSchedule(), "Incorrectly advanced schedule");
    }

    @Test(groups = { "unittest" })
    public void testConfigure() {
        ClientSchedule cs = new ClientSchedule();
        DateTime[] range = determineWeeklyRange("Monday", "05:30:00", "Friday", "20:30:00");
        cs.setStartTime(range[0]);
        cs.setEndTime(range[1]);
        
        // get the times so we can test
        DateTime st = cs.getStartTime();
        DateTime et = cs.getEndTime();

        assertTrue(cs.isWithinSchedule(st), "Failed to include start time within schedule");
        assertTrue(cs.isWithinSchedule(et), "Failed to include the end time within schedule");

        // test invalid times
        DateTime beforeSchedule = st.minusMillis(1);
        DateTime afterSchedule = et.plusMillis(1);

        assertFalse(cs.isWithinSchedule(beforeSchedule), "Included time when it is before schdule");
        assertFalse(cs.isWithinSchedule(afterSchedule), "Included time when it is after schdule");
    }

    @Test(groups = { "unittest" })
    public void testConfigureNoSession() {
        ClientSchedule cs = new ClientSchedule();
        // We want this to be Monday 12:00 AM to Friday 11:59 PM
        DateTime[] range = determineWeeklyRange("Monday", "00:00:00", "Saturday", "00:00:00");
        cs.setStartTime(range[0]);
        cs.setEndTime(range[1]);

        // get the times so we can test
        DateTime st = cs.getStartTime();
        DateTime et = cs.getEndTime();

        assertTrue(cs.isWithinSchedule(st), "Failed to include start time within schedule");
        assertTrue(cs.isWithinSchedule(et), "Failed to include the end time within schedule");

        // test invalid times
        DateTime mondayMorning = new DateTime(st.getYear(), st.getMonthOfYear(), st.getDayOfMonth(), 0, 0, 0,
                0, st.getZone());

        // this should work unless it is the first (FIXED)
        DateTime temp = et.minusDays(1);
        
        DateTime fridayNight = new DateTime(temp.getYear(), temp.getMonthOfYear(), temp.getDayOfMonth(), 23, 59, 59,
                999, st.getZone());

        assertTrue(cs.isWithinSchedule(mondayMorning), "Monday morning is excluded");
        assertTrue(cs.isWithinSchedule(fridayNight), "Friday night is excluded");
    }
}
