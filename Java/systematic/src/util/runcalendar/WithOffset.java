package util.runcalendar;

import static util.Dates.*;

import java.util.*;

import util.*;

public class WithOffset extends WrappedCalendar {

    private final int offset;

    public WithOffset(RunCalendar wrapped, int offset) {
        super(wrapped);
        this.offset = offset;
    }

    @Override public Date asOf(Date date) {
        return daysAgo(offset, super.asOf(date));
    }

    @Override public int offset() {
        return offset;
    }

    @Override public String dbName() {
        return super.dbName() + (offset > 0 ? "+" : "") + offset;
    }

    @Override public String name() {
        return super.name();
    }
      
}