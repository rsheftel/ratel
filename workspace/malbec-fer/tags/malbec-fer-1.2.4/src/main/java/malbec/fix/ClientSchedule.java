package malbec.fix;

import static malbec.util.DateTimeUtil.createDateTime;
import static malbec.util.ScheduleUnit.WEEKLY;
import malbec.util.ScheduleUnit;

import org.joda.time.DateTime;

public class ClientSchedule {

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
        return isWithinSchedule(new DateTime().getMillis());
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

    /**
     * Used to read configuration from property files.
     */
    public static enum Config {
        SCHEDULE_TYPE;
    }

    public synchronized DateTime getStartTime() {
        return new DateTime(scheduleStart);
    }

    public synchronized DateTime getEndTime() {
        return new DateTime(scheduleEnd);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        
        if (schedule == WEEKLY) {
            sb.append("Weekly schedule: ");
        } else {
            sb.append("Daily schedule: ");
        }
        
        sb.append(getStartTime()).append(" to ").append(getEndTime());
    
        return sb.toString();
    }
    
   
}
