package util;

import static java.util.Calendar.*;
import static util.Dates.*;

import java.util.*;

public class QTimeZone {
    public static final QTimeZone GMT = new QTimeZone("GMT");

    private TimeZone timeZone;

    public QTimeZone(String timeZoneId) {
        timeZone = TimeZone.getTimeZone(timeZoneId);
    }
    
    private static Date dateFromYyyyMmDdHhMmSsMmm(Calendar calendar) {
        Calendar result = Calendar.getInstance();
        copyByFields(calendar, result);
        return date(result);
    }

    private Calendar calendar(Date date) {
        Calendar result = calendar();
        Calendar input = Dates.calendar(date);
        copyByFields(input, result);
        return result;
    }

    private static void copyByFields(Calendar from, Calendar to) {
        to.set(
            from.get(YEAR), 
            from.get(MONTH), 
            from.get(DAY_OF_MONTH),
            from.get(HOUR_OF_DAY), 
            from.get(MINUTE), 
            from.get(SECOND)
        );
        to.set(MILLISECOND, from.get(MILLISECOND));
    }

    private Calendar calendar() {
        return new GregorianCalendar(timeZone);
    }

    public Date toLocalTime(Date inMyTimeZone) { 
        Calendar target = Calendar.getInstance();
        Calendar calendar = calendar(inMyTimeZone); 
        target.setTimeInMillis(calendar.getTimeInMillis()); 
        return dateFromYyyyMmDdHhMmSsMmm(target);
    }
}