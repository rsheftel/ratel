package org.ratel.util.runcalendar;

import static org.ratel.util.Dates.*;

import java.util.*;

import org.ratel.util.*;

public class ForceLastDayOfWeek extends WrappedCalendar {

    public ForceLastDayOfWeek(RunCalendar wrapped) {
        super(wrapped);
    }

    @Override public boolean isValid(Date asOf) {
        return super.isValid(asOf) && isLastDayOfWeek(asOf);
    }

    private boolean isLastDayOfWeek(Date asOf) {
        return dayOfWeek(super.nextDay(asOf)) < dayOfWeek(asOf);
    }

    @Override public Date priorDay(Date asOf) {
        Date nextAttempt = daysAgo(1, asOf);
        while(!isValid(nextAttempt)) nextAttempt = daysAgo(1, nextAttempt);
        return nextAttempt;
    }
    
    @Override public Date nextDay(Date asOf) {
        Date nextAttempt = daysAhead(1, asOf);
        while(!isValid(nextAttempt)) nextAttempt = daysAhead(1, nextAttempt);
        return nextAttempt;
    }

    @Override public String dbName() {
        return "lastdayofweek_" + super.dbName();
    }

    @Override public String name() {
        return super.name();
    }
    
}
