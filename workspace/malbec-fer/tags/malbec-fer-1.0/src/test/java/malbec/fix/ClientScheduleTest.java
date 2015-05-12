package malbec.fix;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import malbec.fix.ClientSchedule;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 * Test the schedule for <code>FixClient</code>s.
 * 
 * A FIX Session has a schedule that deals with the resetting of sequence numbers, this schedule is to deal
 * with when the client should be connected.
 * 
 */
public class ClientScheduleTest {

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

    @Test(groups = { "unittest" })
    public void testConfigure() {
        ClientSchedule cs = new ClientSchedule();
        cs.setStartDayTime("Monday", "05:30:00");
        cs.setEndDayTime("Friday", "20:30:00");

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
        cs.setStartDayTime("Monday", "00:00:00");
        cs.setEndDayTime("Saturday", "00:00:00");

        // get the times so we can test
        DateTime st = cs.getStartTime();
        DateTime et = cs.getEndTime();

        assertTrue(cs.isWithinSchedule(st), "Failed to include start time within schedule");
        assertTrue(cs.isWithinSchedule(et), "Failed to include the end time within schedule");

        // test invalid times
        DateTime mondayMorning = new DateTime(st.getYear(), st.getMonthOfYear(), st.getDayOfMonth(), 0, 0, 0,
                0, st.getZone());

        // this should work unless it is the first
        DateTime fridayNight = new DateTime(et.getYear(), et.getMonthOfYear(), et.getDayOfMonth()-1, 23, 59, 59,
                999, st.getZone());

        assertTrue(cs.isWithinSchedule(mondayMorning), "Monday morning is excluded");
        assertTrue(cs.isWithinSchedule(fridayNight), "Friday night is excluded");
    }
}
