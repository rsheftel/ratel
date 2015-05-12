package util.runcalendar;

import static util.Dates.*;

import java.util.*;

import util.*;

public class WeekDays extends RunCalendar {

    @Override public boolean isValid(Date asOf) {
        return !RunCalendar.isWeekend(asOf);
    }

    @Override public int offset() {
        return 0;
    }

    @Override public Date priorDay(Date asOf) {
        int offset = dayOfWeek(asOf) == Calendar.MONDAY ? 3 : 1;
        return daysAgo(offset, asOf);
    }
    
    @Override public Date nextDay(Date asOf) {
        int offset = dayOfWeek(asOf) == Calendar.FRIDAY ? 3 : 1;
        return daysAhead(offset, asOf);
    }

    @Override public String dbName() {
        return "weekdays";
    }

    @Override public String name() {
        return dbName();
    }

}
