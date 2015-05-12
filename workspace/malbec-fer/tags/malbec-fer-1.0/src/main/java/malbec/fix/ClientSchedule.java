package malbec.fix;

import static malbec.util.ScheduleUnit.*;

import malbec.util.ScheduleUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ClientSchedule {

    private static final String TIME_SECOND = "HH:mm:ss";

    private DateTime scheduleStart;
    private DateTime scheduleEnd;

    private ScheduleUnit schedule = ScheduleUnit.WEEKLY;

    public synchronized void setStartTime(DateTime startTime) {
        scheduleStart = startTime;
    }

    public synchronized void setEndTime(DateTime endTime) {
        scheduleEnd = endTime;
    }

    public synchronized boolean isWithinSchedule(long timeToCheck) {
        boolean afterStart = scheduleStart.isBefore(timeToCheck) || scheduleStart.getMillis() == timeToCheck;
        boolean beforeEnd = scheduleEnd.isAfter(timeToCheck) || scheduleEnd.getMillis() == timeToCheck;

        return afterStart && beforeEnd;
    }

    /**
     * Check that the specified time is within the the scheduled period.
     * 
     * scheduleStart <= timeToCheck <= scheduleEnd
     * 
     * @param timeToCheck
     * @return
     */
    public synchronized boolean isWithinSchedule(DateTime timeToCheck) {
        return isWithinSchedule(timeToCheck.getMillis());
    }

    public synchronized boolean isWithinSchedule() {
        return isWithinSchedule(System.currentTimeMillis());
    }

    /**
     * Advance to the next schedule if we are after the current schedule
     * 
     * @return
     */
    public boolean advanceSchedule() {
        DateTime now = new DateTime();

        if (isWithinSchedule(now)) {
            return false;
        }
        synchronized (this) {
            if (scheduleEnd.isBefore(now)) {
                // roll to the next period
                if (schedule == WEEKLY) {
                    scheduleStart = scheduleStart.plusWeeks(1);
                    scheduleEnd = scheduleEnd.plusWeeks(1);
                } else {
                    // do the daily
                    scheduleStart = scheduleStart.plusDays(1);
                    scheduleEnd = scheduleEnd.plusDays(1);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized void setStartDayTime(String dayOfWeek, String timeOfDay) {
        scheduleStart =  createDateTime(dayOfWeek, timeOfDay, true);
    }

    public synchronized void setEndDayTime(String dayOfWeek, String timeOfDay) {
        scheduleEnd = createDateTime(dayOfWeek, timeOfDay, false);
    }

    private DateTime createDateTime(String dayOfWeek, String timeOfDay, boolean beforeOrSame) {
        // create a dummy record to use the parser
        LocalDate day = new LocalDate();
        LocalDate.Property dp = day.property(DateTimeFieldType.dayOfWeek());
        LocalDate createdDate = dp.setCopy(dayOfWeek);

        // Check if we went back in time.
        if (beforeOrSame && createdDate.isAfter(day)) {
            createdDate = createdDate.minusWeeks(1);
        }
        DateTimeFormatter fmt = DateTimeFormat.forPattern(TIME_SECOND);
        DateTime time = fmt.parseDateTime(timeOfDay);

        return combineDateTime(createdDate, time, time.getZone());
    }

    private DateTime combineDateTime(LocalDate d, DateTime t, DateTimeZone tz) {
        DateTime combined = new DateTime(d.getYear(), d.getMonthOfYear(), d.getDayOfMonth(),
                t.getHourOfDay(), t.getMinuteOfHour(), t.getSecondOfMinute(), t.getMillisOfSecond(), tz);

        return combined;
    }

    public static enum Config {
        SCHEDULE_TYPE;
    }

    synchronized DateTime getStartTime() {
        return new DateTime(scheduleStart);
    }

    synchronized DateTime getEndTime() {
        return new DateTime(scheduleEnd);
    }
}
