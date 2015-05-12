/**
 * 
 */
package malbec.util;

import org.joda.time.DateTime;

public class WeeklyRange extends AbstractDateTimeRange<WeeklyRange> {
    public WeeklyRange(DateTime start, DateTime end) {
        super(start, end);
    }

    @Override
    public WeeklyRange advanceRange(DateTime now) {
        if (isWithinRange(now)) {
            return null;
        }

        synchronized (this) {
            if (end.isBefore(now)) {
                return new WeeklyRange(start.plusWeeks(1), end.plusWeeks(1));
            }
        }
        return null;
    }

    public WeeklyRange startsAfter(DateTime dateTime) {
        if (isWithinRange(dateTime) || afterEnd(dateTime)) {
            return advanceRange(getEnd().plusSeconds(1));
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append(start).append(" - ").append(end);
        
        return sb.toString();
    }

    /**
     * Determine the best fit for the specified time with a preference for the 
     * next valid range.
     * 
     * @param dateTime
     * @return the current range if the date is within the range, the next range if the date is after 
     * the end of the current range, or null, if the date is before the range start.
     */
    public WeeklyRange currentOrNext(DateTime dateTime) {
        if (afterEnd(dateTime)) {
            return advanceRange(getEnd().plusSeconds(1));
        }
        
        if (isWithinRange(dateTime)) {
            return this;
        }
        
        return null;
    }

   
}