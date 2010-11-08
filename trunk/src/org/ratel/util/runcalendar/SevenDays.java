package org.ratel.util.runcalendar;

import static org.ratel.util.Dates.*;

import java.util.*;

import org.ratel.util.*;

public class SevenDays extends RunCalendar {


    @Override public boolean isValid(Date asOf) {
        return true;
    }

    @Override public int offset() {
        return 0;
    }

    @Override public Date priorDay(Date asOf) {
        return daysAgo(1, asOf);
    }
    
    @Override public Date nextDay(Date asOf) {
        return daysAhead(1, asOf);
    }

    @Override public String dbName() {
        return "sevendays";
    }

    @Override public String name() {
        return dbName();
    }
    
}
