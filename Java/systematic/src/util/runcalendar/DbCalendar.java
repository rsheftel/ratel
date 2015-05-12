package util.runcalendar;

import static util.Dates.*;

import java.util.*;

import util.*;

public class DbCalendar extends RunCalendar {

    private final String name;

    public DbCalendar(String name) {
        this.name = name;
    }

    @Override public boolean isValid(Date asOf) {
        if (RunCalendar.isWeekend(asOf)) return false;
        return !isHoliday(asOf, name);
    }

    @Override public int offset() {
        return 0;
    }

    @Override public String toString() {
        return name;
    }
    
    @Override public Date priorDay(Date asOf) {
        return businessDaysAgo(1, asOf, name);
    }
    
    @Override public Date nextDay(Date asOf) {
        return businessDaysAhead(1, asOf, name);
    }

    @Override public String dbName() {
        return name;
    }

    @Override public String name() {
        return dbName();
    }
}
