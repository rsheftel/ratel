/**
 * 
 */
package malbec.util;

import org.joda.time.DateTime;

public class DailyRange extends AbstractDateTimeRange<DailyRange> {
    public DailyRange(DateTime start, DateTime end) {
        super(start, end);
    }

    @Override
    public DailyRange advanceRange(DateTime now) {
        if (isWithinRange(now)) {
            return null;
        }

        synchronized (this) {
            if (end.isBefore(now)) {
                // do the daily
                return new DailyRange(start.plusDays(1), end.plusDays(1));
            }
        }
        return null;
    }
}