package org.ratel.util.runcalendar;

import java.util.*;

import org.ratel.util.*;

public abstract class WrappedCalendar extends RunCalendar {

    private final RunCalendar wrapped;

    public WrappedCalendar(RunCalendar wrapped) {
        this.wrapped = wrapped;
    }

    @Override public Date asOf(Date date) {
        return wrapped.asOf(date);
    }

    @Override public boolean isValid(Date asOf) {
        return wrapped.isValid(asOf);
    }

    @Override public Date priorDay(Date asOf) {
        return wrapped.priorDay(asOf);
    }
    
    @Override public Date nextDay(Date asOf) {
        return wrapped.nextDay(asOf);
    }
    
    @Override public int offset() {
        return wrapped.offset();
    }

    @Override public String dbName() {
        return wrapped.dbName();
    }

    @Override public String name() {
        return wrapped.name();
    }

    
}