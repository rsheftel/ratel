package com.fftw.util;

import org.quartz.Calendar;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.quartz.impl.calendar.DailyCalendar;
import org.joda.time.LocalDate;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import java.util.Properties;

/**
 * Handle schedules.
 * <p/>
 * We currently have a need to have things execute daily ignoring weekends.  The time that the weekend starts
 * can be before Midnight Saturday, so we need to encapsulate as much as possible.
 * <p/>
 * The current implementation uses Quartz for most of the logic and is just provides a user-friendly
 * interface to this functionality
 */
public class Schedule {
    private static final String TIME = "HH:mm";

    private DateTimeZone defaultTimeZone = DateTimeZone.forID("UTC");
    private Calendar scheduleCalendar;

    public boolean isDailySchedule() {
        return dailySchedule;
    }

    public boolean isRunOnWeekend() {
        return runOnWeekend;
    }

    private boolean dailySchedule;
    private boolean runOnWeekend;

    public Schedule(Properties props) {
        initializeFromProperties(props);
    }

    public void initializeFromProperties(Properties props) {
        // Check for timezone first
        defaultTimeZone = extractTimeZone(props, "timeZone");

        LocalDate startDate = null;
        LocalDate endDate = null;

        // Do we have a weekly schedule?
        String startDay = props.getProperty("startDay");
        String endDay = props.getProperty("endDay");
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
        String startTimeStr = props.getProperty("startTime");
        String endTimeStr = props.getProperty("endTime");

        DateTime startTime;
        DateTime endTime;

        if (startTimeStr != null && endTimeStr != null) {
            startTime = extractTime(startDate, startTimeStr);
            endTime = extractTime(endDate, endTimeStr);
        } else {
            throw new IllegalArgumentException("Must specify both startTime and endTime");
        }

        boolean singleFire = Boolean.valueOf(props.getProperty("singleFire", "false"));
        if (singleFire && startTime.equals(endTime)) {
            endTime = endTime.plusSeconds(1);
        }
        boolean runOnWeekend = Boolean.valueOf(props.getProperty("runOnWeekend", "false"));
        String weekendStartDayStr = props.getProperty("weekendStartDay");
        String weekendEndDayStr = props.getProperty("weekendEndDay");
        String weekendStartTimeStr = props.getProperty("weekendStartTime");
        String weekendEndTimeStr = props.getProperty("weekendEndTime");

        if (!runOnWeekend) {
            if (weekendStartTimeStr == null && weekendEndTimeStr == null
                    && weekendStartDayStr == null && weekendEndDayStr == null) {
                // Use the default start/end time for weekends
                // May need to chain a holiday calendar in here too
                Calendar weeklyCalendar = new WeeklyCalendar(); // defaults to skipping weekends
                DailyCalendar dailyCalendar = new DailyCalendar(weeklyCalendar, startTime.getMillis(), endTime.getMillis());
                dailyCalendar.setInvertTimeRange(true); // Make it inclusive
                scheduleCalendar = dailyCalendar;
            } else {
                // We have either a start or end for the weekend
                // Do the time first - then figure out the day
//                LocalDate day = new LocalDate();
//                LocalDate.Property dp = day.property(DateTimeFieldType.dayOfWeek());
//
//                LocalDate weekendStartDay = dp.setCopy("Saturday");
//                LocalDate weekendEndDay = dp.setCopy("Sunday");
//
//                DateTime weekendStartTime = extractTime(weekendStartDay, weekendStartTimeStr);
//                DateTime weekendEndTime = extractTime(weekendEndDay, weekendEndTimeStr);
                throw new IllegalArgumentException("Custom weekends is not currently supported");
            }
        } else {
            DailyCalendar dailyCalendar = new DailyCalendar(startTime.getMillis(), endTime.getMillis());
            dailyCalendar.setInvertTimeRange(true); // Make it inclusive
            scheduleCalendar = dailyCalendar;
        }

    }


    public boolean isSchduleActive() {
        return scheduleCalendar.isTimeIncluded(System.currentTimeMillis());
    }

    public boolean isTimeIncluded(long timeInMillis) {
        return scheduleCalendar.isTimeIncluded(timeInMillis);
    }

    public long getNextIncludedTime(long timeStamp) {
        return scheduleCalendar.getNextIncludedTime(timeStamp);
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
}
