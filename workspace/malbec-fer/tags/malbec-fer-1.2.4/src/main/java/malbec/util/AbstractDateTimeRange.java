package malbec.util;

import org.joda.time.DateTime;

public abstract class AbstractDateTimeRange<T> {

    protected final DateTime start;
    protected final DateTime end;

    protected AbstractDateTimeRange(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
    }
    
    
    public abstract T advanceRange(DateTime now);
    
    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        return end;
    }

    public boolean beforeEnd(long timeToCheck) {
        return end.isAfter(timeToCheck) || end.getMillis() == timeToCheck;
    }

    public boolean afterStart(long timeToCheck) {
        return start.isBefore(timeToCheck) || start.getMillis() == timeToCheck;
    }

    public boolean isWithinRange(long timeToCheck) {
        boolean afterStart = afterStart(timeToCheck);
        boolean beforeEnd = beforeEnd(timeToCheck);
    
        return afterStart && beforeEnd;
    }

    public boolean isWithinRange(DateTime timeToCompare) {
        long timeToCheck = timeToCompare.getMillis();
    
        return isWithinRange(timeToCheck);
    }

    public boolean afterStart(DateTime timeToCheck) {
        return afterStart(timeToCheck.getMillis());
    }

    public boolean beforeEnd(DateTime timeToCheck) {
        return beforeEnd(timeToCheck.getMillis());
    }

    public boolean isWithinRange(DateTime start, DateTime end) {
        return isWithinRange(start.getMillis()) && isWithinRange(end.getMillis());
    }

    public boolean isWithinRange(DailyRange range) {
        return isWithinRange(range.start, range.end);
    }

    public boolean isWithinRange(WeeklyRange range) {
        return isWithinRange(range.start, range.end);
    }

}
