package malbec.fer;

import static malbec.fer.FerretState.*;
import static org.testng.Assert.*;

import java.util.Properties;

import malbec.AbstractBaseTest;
import malbec.util.DateTimeUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.testng.annotations.Test;

public class FerretScheduleTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testActiveSchedule() {
        DateTimeUtil.freezeTime("2009/03/16 10:00:00");

        FerretSchedule fs = new FerretSchedule();

        // Should always default to inactive
        assertEquals(fs.currentScheduleState(), Inactive);

        // initialize and go active
        fs.setActiveStartEnd("Monday", new LocalTime(4, 0, 0, 0), "Friday", new LocalTime(16, 30, 0, 0));
        assertEquals(fs.getActiveStart(), new DateTime(2009, 03, 16, 4, 0, 0, 0));
        assertEquals(fs.getActiveEnd(), new DateTime(2009, 03, 20, 16, 30, 0, 0));
        // We only need manual advancement for stage->ticket->dma all others are automatic
        assertTrue(fs.isWithinActiveSchedule());
        assertEquals(fs.currentScheduleState(), Active);

        // Check some ranges
        DateTimeUtil.freezeTime("2009/03/16 03:00:00");
        assertFalse(fs.isWithinActiveSchedule());
        DateTimeUtil.freezeTime("2009/03/16 05:00:00");
        assertTrue(fs.isWithinActiveSchedule());
        DateTimeUtil.freezeTime("2009/03/20 16:00:00");
        assertTrue(fs.isWithinActiveSchedule());
        DateTimeUtil.freezeTime("2009/03/20 16:31:00");
        assertFalse(fs.isWithinActiveSchedule());

        // Test the roll-over to more restricted state
        assertEquals(fs.currentScheduleState(), Inactive);

        // Test advance of schedule
        fs.setStateActive();
        DateTimeUtil.freezeTime("2009/03/20 16:00:00");
        assertTrue(fs.isWithinActiveSchedule());
        assertFalse(fs.advanceActiveSchedule());
        assertEquals(fs.currentScheduleState(), Active);

        DateTimeUtil.freezeTime("2009/03/21 03:59:00");
        assertTrue(fs.advanceActiveSchedule());
        assertEquals(fs.getActiveStart(), new DateTime(2009, 03, 23, 4, 0, 0, 0));
        assertEquals(fs.getActiveEnd(), new DateTime(2009, 03, 27, 16, 30, 0, 0));

        DateTimeUtil.freezeTime("2009/03/23 04:32:00");
        assertEquals(fs.currentScheduleState(), Active);
    }

    @Test(groups = { "unittest" })
    public void testStateScheduleCalculated() {
        String frozenTime = "2009/03/16 10:00:00";
        DateTimeUtil.freezeTime(frozenTime);

        // initialize
        FerretSchedule fs = createTestSchedule();

        // assertTrue(fs.advanceActiveState());
        assertEquals(fs.getRejectStart(), new DateTime(2009, 03, 16, 0, 0, 0, 0));
        assertEquals(fs.getRejectEnd(), new DateTime(2009, 03, 16, 23, 59, 59, 0));
        assertEquals(fs.getStageStart(), new DateTime(2009, 03, 16, 4, 30, 0, 0));
        assertEquals(fs.getStageEnd(), new DateTime(2009, 03, 16, 20, 0, 0, 0));
        assertEquals(fs.getTicketStart(), new DateTime(2009, 03, 16, 6, 0, 0, 0));
        assertEquals(fs.getTicketEnd(), new DateTime(2009, 03, 16, 16, 30, 0, 0));
        assertEquals(fs.getDmaStart(), new DateTime(2009, 03, 16, 7, 0, 0, 0));
        assertEquals(fs.getDmaEnd(), new DateTime(2009, 03, 16, 16, 0, 0, 0));

        // Test the calculated time for one day
        DateTimeUtil.freezeTime("2009/03/16 04:00:00");
        checkWithinRejectTime(fs);
        assertEquals(fs.getCalculatedState(), Reject);

        DateTimeUtil.freezeTime("2009/03/16 04:30:00");
        checkWithinStageTime(fs);
        assertEquals(fs.getCalculatedState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.getCalculatedState(), Ticket);

        DateTimeUtil.freezeTime("2009/03/16 07:00:00");
        checkWithinDmaTime(fs);
        assertEquals(fs.getCalculatedState(), DMA);

        // Go back up
        DateTimeUtil.freezeTime("2009/03/16 16:01:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.getCalculatedState(), Ticket);

        DateTimeUtil.freezeTime("2009/03/16 16:31:00");
        checkWithinStageTime(fs);
        assertEquals(fs.getCalculatedState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 20:01:00");
        assertTrue(fs.isWithinActiveSchedule());
        checkWithinRejectTime(fs);
        assertEquals(fs.getCalculatedState(), Reject);
    }

    @Test(groups = { "unittest" })
    public void testStateScheduleManualAdvance() {
        String frozenTime = "2009/03/16 10:00:00"; // DMA time
        DateTimeUtil.freezeTime(frozenTime);

        // initialize
        FerretSchedule fs = createTestSchedule();

        // assertTrue(fs.advanceActiveState());
        assertEquals(fs.currentScheduleState(), Stage);

        // Test the calculated time for one day
        DateTimeUtil.freezeTime("2009/03/16 04:00:00");
        checkWithinRejectTime(fs);
        assertEquals(fs.currentScheduleState(), Reject);

        // going from Reject to Stage is automatic
        DateTimeUtil.freezeTime("2009/03/16 04:30:00");
        checkWithinStageTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToTicket(), Ticket);
        assertEquals(fs.currentScheduleState(), Ticket);

        DateTimeUtil.freezeTime("2009/03/16 07:00:00");
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Ticket);
        assertEquals(fs.setStateToDma(), DMA);
        assertEquals(fs.currentScheduleState(), DMA);

        // Go back up - these transitions are automatic
        DateTimeUtil.freezeTime("2009/03/16 16:01:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Ticket);

        DateTimeUtil.freezeTime("2009/03/16 16:31:00");
        checkWithinStageTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 20:01:00");
        checkWithinRejectTime(fs); // HERE
        assertEquals(fs.currentScheduleState(), Reject);

        // test the next day (kinda)
        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToTicket(), Ticket);
        assertEquals(fs.currentScheduleState(), Ticket);

        // test skipping Ticket : Stage->DMA
        DateTimeUtil.freezeTime("2009/03/16 08:00:00");
        fs.resetState();
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToDma(), Stage);
        assertEquals(fs.currentScheduleState(), Stage);
    }

    @Test(groups = { "unittest" })
    public void testStateScheduleManualNoAdvance() {
        String frozenTime = "2009/03/16 10:00:00"; // DMA time
        DateTimeUtil.freezeTime(frozenTime);

        // initialize
        FerretSchedule fs = createTestSchedule();

        assertEquals(fs.currentScheduleState(), Stage);

        // Test the calculated time for one day
        DateTimeUtil.freezeTime("2009/03/16 04:00:00");
        checkWithinRejectTime(fs);
        assertEquals(fs.currentScheduleState(), Reject);

        // going from Reject to Stage is automatic
        DateTimeUtil.freezeTime("2009/03/16 04:30:00");
        checkWithinStageTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 07:00:00");
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        // Go back up - these transitions are automatic
        DateTimeUtil.freezeTime("2009/03/16 16:01:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 16:31:00");
        checkWithinStageTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 20:01:00");
        checkWithinRejectTime(fs);
        assertEquals(fs.currentScheduleState(), Reject);

        // test the next day (kinda)
        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToTicket(), Ticket);
        assertEquals(fs.currentScheduleState(), Ticket);

        // test skipping Ticket : Stage->DMA
        DateTimeUtil.freezeTime("2009/03/16 08:00:00");
        fs.resetState();
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToDma(), Stage);
        assertEquals(fs.currentScheduleState(), Stage);
    }

    @Test(groups = { "unittest" })
    public void testStateScheduleManualOneAdvance() {
        String frozenTime = "2009/03/16 10:00:00"; // DMA time
        DateTimeUtil.freezeTime(frozenTime);

        // initialize
        FerretSchedule fs = createTestSchedule();

        // assertTrue(fs.advanceActiveState());
        assertEquals(fs.currentScheduleState(), Stage);

        // Test the calculated time for one day
        DateTimeUtil.freezeTime("2009/03/16 04:00:00");
        checkWithinRejectTime(fs);
        assertEquals(fs.currentScheduleState(), Reject);

        // going from Reject to Stage is automatic
        DateTimeUtil.freezeTime("2009/03/16 04:30:00");
        checkWithinStageTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToTicket(), Ticket);
        assertEquals(fs.currentScheduleState(), Ticket);

        DateTimeUtil.freezeTime("2009/03/16 07:00:00");
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Ticket);

        // Go back up - these transitions are automatic
        DateTimeUtil.freezeTime("2009/03/16 16:01:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Ticket);

        DateTimeUtil.freezeTime("2009/03/16 16:31:00");
        checkWithinStageTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);

        DateTimeUtil.freezeTime("2009/03/16 20:01:00");
        checkWithinRejectTime(fs); // HERE
        assertEquals(fs.currentScheduleState(), Reject);

        // test the next day (kinda)
        DateTimeUtil.freezeTime("2009/03/16 06:00:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToTicket(), Ticket);
        assertEquals(fs.currentScheduleState(), Ticket);

        // test skipping Ticket : Stage->DMA
        DateTimeUtil.freezeTime("2009/03/16 08:00:00");
        fs.resetState();
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToDma(), Stage);
        assertEquals(fs.currentScheduleState(), Stage);
    }

    @Test(groups = { "unittest" })
    public void testRollToNextDay() {
        String frozenTime = "2009/03/16 10:00:00"; // DMA time
        DateTimeUtil.freezeTime(frozenTime);

        // initialize
        FerretSchedule fs = createTestSchedule();

        checkWithinDmaTime(fs);
        // really test the next day
        assertFalse(fs.advanceSchedule());
        DateTimeUtil.freezeTime("2009/03/17 06:00:00");
        assertTrue(fs.advanceSchedule());
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Stage);
        assertEquals(fs.setStateToTicket(), Ticket);
        assertEquals(fs.currentScheduleState(), Ticket);

        // really test the next day
        DateTimeUtil.freezeTime("2009/03/17 08:00:00");
        checkWithinDmaTime(fs);
        assertEquals(fs.currentScheduleState(), Ticket);
        assertFalse(fs.advanceSchedule());

        DateTimeUtil.freezeTime("2009/03/17 16:01:00");
        checkWithinTicketTime(fs);
        assertEquals(fs.currentScheduleState(), Ticket);
        assertTrue(fs.advanceSchedule());

        // DMA should be next day
        assertEquals(fs.getDmaEnd(), new DateTime(2009, 03, 18, 16, 0, 0, 0));
        assertEquals(fs.currentScheduleState(), Ticket);
    }

    @Test(groups = { "unittest" })
    public void testStateScheduleWeekend() {
        String frozenTime = "2009/03/16 10:00:00"; // DMA time
        DateTimeUtil.freezeTime(frozenTime);

        // initialize
        FerretSchedule fs = createTestSchedule();
        assertEquals(fs.currentScheduleState(), Stage);

        // Test the calculated time for one day
        DateTimeUtil.freezeTime("2009/03/20 17:00:00");
        assertFalse(fs.isWithinActiveSchedule());
        assertEquals(fs.currentScheduleState(), Inactive);

        // Test Monday morning
        DateTimeUtil.freezeTime("2009/03/23 00:01:00");

        assertTrue(fs.advanceSchedule());
        while (fs.advanceSchedule())
            ;
        assertEquals(fs.currentScheduleState(), Inactive);

        DateTimeUtil.freezeTime("2009/03/23 05:00:00");
        assertFalse(fs.advanceSchedule());
        assertEquals(fs.currentScheduleState(), Stage);
    }

    @Test(groups = { "unittest" })
    public void testPropertyConstructor() {
        DateTimeUtil.freezeTime("2009/03/20 11:50:00");
        Properties props = new Properties();

        props.setProperty("ActiveModeStart", "Sunday 18:00");
        props.setProperty("ActiveModeEnd", "Friday 20:00");

        props.setProperty("StagedModeStart", "04:30");
        props.setProperty("StagedModeEnd", "20:00");
        props.setProperty("TicketModeStart", "06:00");
        props.setProperty("TicketModeEnd", "16:30:00");
        props.setProperty("DMAModeStart", "07:00");
        props.setProperty("DMAModeEnd", "16:00");
        FerretSchedule fs = new FerretSchedule(props);

        assertEquals(fs.getActiveStart(), new DateTime(2009, 3, 16, 4, 30, 0, 0));
        assertEquals(fs.getActiveEnd(), new DateTime(2009, 3, 20, 20, 0, 0, 0));

        assertEquals(fs.getRejectStart(), new DateTime(2009, 3, 20, 0, 0, 0, 0));
        assertEquals(fs.getRejectEnd(), new DateTime(2009, 3, 20, 23, 59, 59, 999));

        assertEquals(fs.getTicketStart(), new DateTime(2009, 3, 20, 6, 0, 0, 0));
        assertEquals(fs.getTicketEnd(), new DateTime(2009, 3, 20, 16, 30, 0, 0));

        assertEquals(fs.getDmaStart(), new DateTime(2009, 3, 20, 7, 0, 0, 0));
        assertEquals(fs.getDmaEnd(), new DateTime(2009, 3, 20, 16, 0, 0, 0));

    }

    @Test(groups = { "unittest", "development" })
    public void testRejectEmailSchedule() {
        DateTimeUtil.freezeTime("2009/03/16 20:01:00");

        // initialize
        FerretSchedule fs = createTestSchedule();

        assertEquals(fs.getStageEnd(), new DateTime(2009, 3, 16, 20, 0, 0, 0));
        assertEquals(fs.currentScheduleState(), Reject);
        
        assertTrue(fs.isWithinRejectEmailSchedule());

    }

    public static FerretSchedule createTestSchedule() {
        FerretSchedule fs = new FerretSchedule();
        // initialize
        fs.setActiveStartEnd("Monday", new LocalTime(4, 00, 0, 0), "Friday", new LocalTime(16, 30, 0, 0));
        fs.setRejectStartEnd(new LocalTime(0, 0, 0, 0), new LocalTime(23, 59, 59, 0));
        fs.setStageStartEnd(new LocalTime(4, 30, 0, 0), new LocalTime(20, 0, 0, 0));
        fs.setTicketStartEnd(new LocalTime(6, 0, 0, 0), new LocalTime(16, 30, 0, 0));
        fs.setDmaStartEnd(new LocalTime(7, 0, 0, 0), new LocalTime(16, 0, 0, 0));
        fs.setRejectEmailStartEnd(fs.getStageEnd(), fs.getStageStart().plusDays(1));

        return fs;
    }

    private void checkWithinDmaTime(FerretSchedule fs) {
        assertTrue(fs.isWithinActiveSchedule());
        assertTrue(fs.isWithinRejectSchedule());
        assertTrue(fs.isWithinStageSchedule());
        assertTrue(fs.isWithinTicketSchedule());
        assertTrue(fs.isWithinDmaSchedule());
    }

    private void checkWithinTicketTime(FerretSchedule fs) {
        assertTrue(fs.isWithinActiveSchedule());
        assertTrue(fs.isWithinRejectSchedule());
        assertTrue(fs.isWithinStageSchedule());
        assertTrue(fs.isWithinTicketSchedule());
        assertFalse(fs.isWithinDmaSchedule());
    }

    private void checkWithinStageTime(FerretSchedule fs) {
        assertTrue(fs.isWithinActiveSchedule());
        assertTrue(fs.isWithinRejectSchedule());
        assertTrue(fs.isWithinStageSchedule());
        assertFalse(fs.isWithinTicketSchedule());
        assertFalse(fs.isWithinDmaSchedule());
    }

    private void checkWithinRejectTime(FerretSchedule fs) {
        assertTrue(fs.isWithinActiveSchedule());
        assertTrue(fs.isWithinRejectSchedule());
        assertFalse(fs.isWithinStageSchedule());
        assertFalse(fs.isWithinTicketSchedule());
        assertFalse(fs.isWithinDmaSchedule());
    }

}
