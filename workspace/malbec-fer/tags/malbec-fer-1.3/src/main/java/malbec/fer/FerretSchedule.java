package malbec.fer;

import static malbec.fer.FerretState.*;
import static malbec.util.DateTimeUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import malbec.util.DailyRange;
import malbec.util.DateTimeUtil;
import malbec.util.WeeklyRange;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedule for the Ferret.
 * 
 * The schedule tracks the state of the system with 4 different states. There is a hierarchy that states which
 * overlapping state takes precedence. The global state has the highest precedence.
 * 
 * <tt>
 * Active - Monday 04:30 - Friday 16:30 (Global state)
 * 
 * Reject - Daily 00:00 - 23:59
 * Stage - Daily 04:30 - 16:30
 * Ticket - Daily 06:00 - 16:00
 * DMA - Daily 07:00 - 16:00
 * </tt>
 */
public class FerretSchedule {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private WeeklyRange activeRange;

    private DailyRange rejectRange;

    private DailyRange stageRange;

    private DailyRange ticketRange;

    private DailyRange dmaRange;

    private DailyRange rejectEmailRange;

    private FerretState currentState_;// = Inactive;

    public FerretSchedule(WeeklyRange activeRange, DailyRange rejectRange, DailyRange stageRange,
        DailyRange ticketRange, DailyRange dmaRange) {
        super();
        this.activeRange = activeRange;
        this.rejectRange = rejectRange;
        this.stageRange = stageRange;
        this.ticketRange = ticketRange;
        this.dmaRange = dmaRange;
    }

    public FerretSchedule() {}

    public FerretSchedule(Properties props) {

        String activeRangeStart = props.getProperty("ActiveModeStart");
        String activeRangeEnd = props.getProperty("ActiveModeEnd");
        activeRange = buildActiveRange(activeRangeStart, activeRangeEnd);

        String rejectRangeStart = props.getProperty("RejectModeStart");
        String rejectRangeEnd = props.getProperty("RejectModeEnd");
        rejectRange = buildRejectRange(rejectRangeStart, rejectRangeEnd);

        String stageRangeStart = props.getProperty("StagedModeStart");
        String stageRangeEnd = props.getProperty("StagedModeEnd");

        stageRange = buildStageRange(stageRangeStart, stageRangeEnd);

        String ticketRangeStart = props.getProperty("TicketModeStart");
        String ticketRangeEnd = props.getProperty("TicketModeEnd");
        ticketRange = buildTicketRange(ticketRangeStart, ticketRangeEnd);

        String dmaRangeStart = props.getProperty("DMAModeStart");
        String dmaRangeEnd = props.getProperty("DMAModeEnd");

        dmaRange = buildDmaRange(dmaRangeStart, dmaRangeEnd);
    }

    private DailyRange buildDmaRange(String start, String end) {
        return buildRange(start, end, new LocalTime(7, 0, 0, 0), new LocalTime(16, 0, 0, 0));
    }

    private DailyRange buildTicketRange(String start, String end) {
        return buildRange(start, end, new LocalTime(6, 0, 0, 0), new LocalTime(16, 30, 0, 0));
    }

    private DailyRange buildStageRange(String start, String end) {
        return buildRange(start, end, new LocalTime(4, 30, 0, 0), new LocalTime(20, 0, 0, 0));
    }

    private DailyRange buildRejectRange(String start, String end) {
        return buildRange(start, end, new LocalTime(0, 0, 0, 0), new LocalTime(23, 59, 59, 999));
    }

    private DailyRange buildRange(String start, String end, LocalTime startTime, LocalTime endTime) {
        LocalTime tmpStart = startTime;

        if (start != null) {
            tmpStart = DateTimeUtil.guessTime(start);
            if (tmpStart == null) {
                tmpStart = startTime;
            }
        }

        LocalTime tmpEnd = endTime;

        if (end != null) {
            tmpEnd = DateTimeUtil.guessTime(end);
            if (tmpEnd == null) {
                tmpEnd = endTime;
            }
        }

        return new DailyRange(tmpStart.toDateTimeToday(), tmpEnd.toDateTimeToday());
    }

    /**
     * The string should be 'Sunday 18:00' or 'Friday 16:30'
     * 
     * @param activeRangeStart
     * @param activeRangeEnd
     * @return
     */
    private WeeklyRange buildActiveRange(String activeRangeStart, String activeRangeEnd) {

        try {
            String[] rangeStart = activeRangeStart.split(" ");
            String[] rangeEnd = activeRangeEnd.split(" ");
            DateTime[] range = determineWeeklyRange(rangeStart[0], rangeStart[1], rangeEnd[0], rangeEnd[1]);
            return new WeeklyRange(range[0], range[1]);
        } catch (Exception e) {
            log.warn("Unable to parse activeRange: " + activeRangeStart + " - " + activeRangeEnd);
        }

        DateTime[] range = determineWeeklyRange("Monday", "04:30:00", "Friday", "16:30:00");
        return new WeeklyRange(range[0], range[1]);
    }

    synchronized void setCurrentState(FerretState newState) {
        currentState_ = newState;
    }

    synchronized FerretState getCurrentState() {
        return currentState_;
    }

    public synchronized FerretState currentScheduleState() {
        FerretState calculatedState = getCalculatedState();

        DateTime now = new DateTime();

        // Ticket time before DMA
        if (isWithinTicketSchedule(now) && isDmaWithinTicket() && beforeDmaEnd(now.getMillis())) {
            if (getCurrentState() == null) {
                return Stage;
            } else {
                return getCurrentState();
            }
        }

        // DMA time
        if (isWithinDmaSchedule(now)) {
            if (getCurrentState() == null) {
                return Stage;
            } else {
                return getCurrentState();
            }
        }

        // Ticket time after DMA - still stage unless advanced manually
        if (isWithinTicketSchedule(now)) {
            if (getCurrentState() != null) {
                // we had some manual transitions, figure out where we are
                return FerretState.min(getCurrentState(), Ticket);
            } else {
                return Stage;
            }
        }

        // ensure we use the calculated method
        setCurrentState(null);

        return calculatedState;
    }

    private boolean isDmaWithinTicket() {
        return ticketRange.isWithinRange(dmaRange);
    }

    private boolean beforeDmaEnd(long timeToCheck) {
        return dmaRange.beforeEnd(timeToCheck);

    }

    public synchronized void setActiveStartEnd(String startDay, LocalTime startTime, String endDay,
        LocalTime endTime) {
        DateTime activeStart = createDateTime(startDay, startTime, true);
        DateTime activeEnd = createDateTime(endDay, endTime, false);

        activeRange = new WeeklyRange(activeStart, activeEnd);
    }

    public void setDmaStartEnd(LocalTime startTime, LocalTime endTime) {
        dmaRange = new DailyRange(startTime.toDateTimeToday(), endTime.toDateTimeToday());
    }

    public void setRejectStartEnd(LocalTime startTime, LocalTime endTime) {
        rejectRange = new DailyRange(startTime.toDateTimeToday(), endTime.toDateTimeToday());
    }

    public void setRejectEmailStartEnd(DateTime startDateTimee, DateTime endDateTime) {
        rejectEmailRange = new DailyRange(startDateTimee, endDateTime);
    }

    public void setStageStartEnd(LocalTime startTime, LocalTime endTime) {
        stageRange = new DailyRange(startTime.toDateTimeToday(), endTime.toDateTimeToday());
    }

    public void setTicketStartEnd(LocalTime startTime, LocalTime endTime) {
        ticketRange = new DailyRange(startTime.toDateTimeToday(), endTime.toDateTimeToday());
    }

    public synchronized DateTime getActiveStart() {
        if (activeRange != null) {
            return activeRange.getStart();
        }

        return null;
    }

    public synchronized DateTime getActiveEnd() {
        if (activeRange != null) {
            return activeRange.getEnd();
        }
        return null;
    }

    public DateTime getDmaStart() {
        return dmaRange.getStart();
    }

    public DateTime getDmaEnd() {
        return dmaRange.getEnd();
    }

    private boolean isWithinRange(DateTime startTime, DateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }

        long timeToCheck = nowMillis();

        boolean afterStart = startTime.isBefore(timeToCheck) || startTime.getMillis() == timeToCheck;
        boolean beforeEnd = endTime.isAfter(timeToCheck) || endTime.getMillis() == timeToCheck;

        return afterStart && beforeEnd;
    }

    public boolean isWithinActiveSchedule() {
        return isWithinRange(getActiveStart(), getActiveEnd());
    }

    public boolean advanceActiveSchedule() {
        return advanceActiveSchedule(new DateTime());
    }

    public synchronized boolean advanceActiveSchedule(DateTime now) {
        WeeklyRange newRange = activeRange.advanceRange(now);

        if (newRange != null) {
            activeRange = newRange;
            return true;
        }
        return false;
    }

    /**
     * Advance all of the schedules.
     * 
     * @return
     */
    public boolean advanceSchedule() {
        DateTime now = new DateTime();

        boolean r = advanceRejectSchedule(now);
        boolean s = advanceStageSchedule(now);
        boolean t = advanceTicketSchedule(now);
        boolean d = advanceDmaSchedule(now);
        boolean a = advanceActiveSchedule(now);
        // boolean re = advanceRejectEmailSchedule(now);

        return r || s || t || d || a;
    }

    public boolean advanceDmaSchedule(DateTime now) {
        if (dmaRange != null) {
            DailyRange newRange = dmaRange.advanceRange(now);

            if (newRange != null) {
                dmaRange = newRange;
                return true;
            }
        }
        return false;
    }

    public boolean advanceRejectSchedule(DateTime now) {
        DailyRange newRange = rejectRange.advanceRange(now);

        if (newRange != null) {
            rejectRange = newRange;
            return true;
        }
        return false;
    }

    boolean advanceRejectEmailSchedule(DateTime now) {
        DailyRange newRange = rejectEmailRange.advanceRange(now);

        if (newRange != null) {
            rejectEmailRange = newRange;
            return true;
        }
        return false;
    }

    public boolean advanceStageSchedule(DateTime now) {
        DailyRange newRange = stageRange.advanceRange(now);

        if (newRange != null) {
            stageRange = newRange;
            return true;
        }
        return false;
    }

    public boolean advanceTicketSchedule(DateTime now) {
        DailyRange newRange = ticketRange.advanceRange(now);

        if (newRange != null) {
            ticketRange = newRange;
            return true;
        }
        return false;
    }

    void resetState() {
        setCurrentState(null);
    }

    void setStateActive() {
        if (isWithinActiveSchedule()) {
            setCurrentState(Active);
        }
    }

    public FerretState setStateToDma() {
        if (isWithinDmaSchedule(new DateTime()) && Ticket == currentScheduleState()) {
            setCurrentState(DMA);
        }

        return currentScheduleState();
    }

    public FerretState setStateToTicket() {
        if (isWithinTicketSchedule(new DateTime())) {
            setCurrentState(Ticket);
        }

        return currentScheduleState();
    }

    public FerretState setStateToStage() {
        if (isWithinStageSchedule(new DateTime())) {
            setCurrentState(Stage);
        }

        return currentScheduleState();
    }

    public DateTime getRejectStart() {
        return rejectRange.getStart();
    }

    public DateTime getRejectEnd() {
        return rejectRange.getEnd();
    }

    public DateTime getStageStart() {
        return stageRange.getStart();
    }

    public DateTime getStageEnd() {
        return stageRange.getEnd();
    }

    public DateTime getTicketStart() {
        return ticketRange.getStart();
    }

    public DateTime getTicketEnd() {
        return ticketRange.getEnd();
    }

    public FerretState getCalculatedState() {
        // build the list of active states
        List<FerretState> activeStates = new ArrayList<FerretState>();

        if (isWithinActiveSchedule()) {
            activeStates.add(Active);
        } else {
            activeStates.add(Inactive);
        }

        if (isWithinRejectSchedule()) {
            activeStates.add(Reject);
        }

        if (isWithinStageSchedule()) {
            activeStates.add(Stage);
        }

        DateTime now = new DateTime();
        if (isWithinTicketSchedule(now)) {
            activeStates.add(Ticket);
        }

        if (isWithinDmaSchedule(now)) {
            activeStates.add(DMA);
        }

        return FerretState.highest(activeStates);
    }

    public boolean isWithinRejectSchedule() {
        if (rejectRange != null) {
            return rejectRange.isWithinRange(new DateTime());
        }
        return false;
    }

    public boolean isWithinStageSchedule(DateTime now) {
        if (stageRange != null) {
            return stageRange.isWithinRange(now);
        }
        return false;
    }

    public boolean isWithinStageSchedule() {
        return isWithinStageSchedule(new DateTime());
    }

    public boolean isWithinTicketSchedule() {
        return isWithinTicketSchedule(new DateTime());
    }

    public boolean isWithinTicketSchedule(DateTime now) {
        if (ticketRange != null) {
            return ticketRange.isWithinRange(now);
        }
        return false;
    }

    public boolean isWithinDmaSchedule() {
        return isWithinDmaSchedule(new DateTime());
    }

    public boolean isWithinDmaSchedule(DateTime now) {
        if (dmaRange != null) {
            return dmaRange.isWithinRange(now);
        }
        return false;
    }

    public boolean isWithinRejectEmailSchedule() {
        return isWithinRejectEmailSchedule(new DateTime());
    }

    public boolean isWithinRejectEmailSchedule(DateTime now) {
        if (rejectEmailRange != null) {
            return rejectEmailRange.isWithinRange(now);
        }
        return false;
    }

}
