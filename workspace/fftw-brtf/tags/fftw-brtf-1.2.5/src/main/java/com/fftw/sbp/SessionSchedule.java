package com.fftw.sbp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Properties;

/**
 *
 */
public class SessionSchedule {

    private static final String TIME = "HH:mm:ss";

    private DateTime startTime;

    private DateTime endTime;

    private Interval currentInterval;

    private boolean dailySchedule;

    private DateTimeZone defaultTimeZone = DateTimeZone.forID("UTC");

    /**
     * Default daily schedule
     */
    public SessionSchedule() {
        startTime = new LocalDate().toDateTime(new LocalTime(0, 0, 0, 0));
        endTime = new LocalDate().toDateTime(new LocalTime(23, 59, 59, 999));

        currentInterval = new Interval(startTime.getMillis(), endTime.getMillis());
    }

    public SessionSchedule(Properties props) {
        // Check for timezone first
        defaultTimeZone = extractTimeZone(props, "session.timeZone");

        LocalDate startDate = null;
        LocalDate endDate = null;

        // Do we have a weekly schedule?
        String startDay = props.getProperty("session.startDay");
        String endDay = props.getProperty("session.endDay");
        if (startDay != null && endDay != null) {
            // Extract the day of the week and set the date
            LocalDate day = new LocalDate();
            LocalDate.Property dp = day.property(DateTimeFieldType.dayOfWeek());

            startDate = dp.setCopy(startDay);
            endDate = dp.setCopy(endDay);
            dailySchedule = false;
        } else if (startDay == null && endDay == null) {
            // Default to a daily schedule
            startDate = new LocalDate();
            endDate = new LocalDate();
            dailySchedule = true;
        } else {
            throw new IllegalArgumentException("Must specify both startDay and endDay");
        }

        // Daily and weekly will have this
        String startTimeStr = props.getProperty("session.startTime");
        String endTimeStr = props.getProperty("session.endTime");

        if (startTimeStr != null && endTimeStr != null) {
            startTime = extractTime(startDate, startTimeStr);
            endTime = extractTime(endDate, endTimeStr);
        } else {
            throw new IllegalArgumentException("Must specify both startTime and endTime");
        }

        setInterval(startTime, endTime);
    }

    private void setInterval(DateTime startTime, DateTime endTime) {
        currentInterval = new Interval(startTime, endTime);
    }

    private DateTime extractTime(LocalDate day, String startTimeStr) {
        String[] startTimeParts = startTimeStr.split(" "); // extract time zone

        DateTimeZone currentTimeZone = defaultTimeZone;

        if (startTimeParts.length > 1) {
            currentTimeZone = DateTimeZone.forID(startTimeParts[1]);
        }

        DateTimeFormatter fmt = DateTimeFormat.forPattern(TIME);
        fmt.withZone(currentTimeZone);

        DateTime time = fmt.parseDateTime(startTimeParts[0]);

        return combineDateTime(day, time, time.getZone());
    }

    private DateTime combineDateTime(LocalDate day, DateTime time, DateTimeZone timeZone) {
        DateTime combined = new DateTime(day.getYear(), day.getMonthOfYear(), day.getDayOfMonth(), time.getHourOfDay(),
                time.getMinuteOfHour(), time.getSecondOfMinute(), time.getMillisOfSecond(), timeZone);

        return combined;
    }

    private DateTimeZone extractTimeZone(Properties props, String timeZone) {
        String timeZoneStr = props.getProperty(timeZone);

        if (timeZoneStr != null) {
            return DateTimeZone.forID(timeZoneStr);
        } else {
            return DateTimeZone.forID("UTC");
        }
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public boolean isSessionActive() {
        // if we are not active, is NOW after the current interval or before?
        // If after, we need to recalculate the interval
        boolean active = currentInterval.containsNow();
        if (!active) {
            recalculate();
            active = currentInterval.containsNow();
        }
        return active;
    }

    public boolean isDailySchedule() {
        return dailySchedule;
    }

    /**
     * Recalculate the schedule.
     * <p/>
     * When we pass the end of the schedule, we need to recalcuate the schedule
     *
     * @return false - if the schedule was <b>not</b> recalcuated
     *         true - if the schedule was recalculated
     */
    public boolean recalculate() {
        if (currentInterval.containsNow()) {
            return false;
        } else {
            if (isDailySchedule()) {
                // This can be called multiple times a day without getting ahead
                LocalDate today = new LocalDate();
                startTime = combineDateTime(today, startTime, startTime.getZone());
                endTime = combineDateTime(today, endTime, endTime.getZone());
            } else {
                // We have to be careful when we call this
                DateTime newStartTime = startTime.plusWeeks(1);
                DateTime newEndTime = endTime.plusWeeks(1);

                if (endTime.isBeforeNow() && newStartTime.isAfterNow()) {
                    startTime = newStartTime;
                    endTime = newEndTime;
                } else {
                    // do not change the period
                    return false;
                }
            }
            setInterval(startTime, endTime);
            return true;
        }
    }
}
