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
}