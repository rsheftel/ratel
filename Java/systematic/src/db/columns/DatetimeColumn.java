package db.columns;

import static db.clause.Clause.*;
import static util.Dates.*;

import java.sql.*;
import java.util.Date;

import util.*;
import db.*;
import db.clause.*;

public class DatetimeColumn extends ConcreteColumn<Timestamp> {
    private static final long serialVersionUID = 1L;

	public DatetimeColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}

	public Clause in(Range dateRange) {
		return dateRange.matches(this);
	}
	
	public Cell<Timestamp> with(java.util.Date t) {
		return super.with(timestamp(t));
	}

	private Timestamp timestamp(java.util.Date t) {
		return new Timestamp(t.getTime());
	}

	public Clause is(Date time) {
		return super.is(timestamp(time));
	}

	public Clause lessThanOr(Date date) {
		return new ComparisonClause<Timestamp>(this, timestamp(date), Comparison.LE);
	}
	
	public Clause greaterThanOr(Date date) {
		return new ComparisonClause<Timestamp>(this, timestamp(date), Comparison.GE);
	}
	
	public Clause greaterThan(Date date) {
		return new ComparisonClause<Timestamp>(this, timestamp(date), Comparison.GT);
	}
	
	@Override public Timestamp valueFromString(String s) {
		return timestamp(date(s));
	}

	public Clause notInFuture() {
		return lessThanOr(Dates.now());
	}
	
	public Cell<Timestamp> now() { 
		return with(Dates.now());
	}

    public void updateOne(Clause matches) {
        updateOne(matches, Dates.now());
    }

    public void updateOne(Clause matches, Date value) {
        updateOne(matches, timestamp(value));
    }

	public boolean hasTodayValue(Clause idMatches) {
		return Dates.hasTodayValue(value(idMatches));
	}

    public Cell<Timestamp> withMaybe(Date end) {
        return  end == null ? with(null) : with(end);
    }
    
    @Override public String string(Timestamp value) {
        return ymdHuman(value);
    }

    public static Clause dateInRange(Date now, DatetimeColumn start, DatetimeColumn end) {
        Clause isAlive = start.lessThanOr(now );
        Clause isStillAlive = end.isNull().or(end.greaterThanOr(now));
        return isAlive.and(parenGroup(isStillAlive));
    }

    public Clause lessThan(Date d) {
        return lessThan(timestamp(d));
    }
	
}
