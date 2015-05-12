package util;

import static java.util.Calendar.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import util.runcalendar.*;

import db.*;
import db.columns.*;

public abstract class RunCalendar {
    public static final List<Integer> WEEKEND = list(SATURDAY, SUNDAY);
    
    public static final RunCalendar NYB = from("nyb");
	public static final RunCalendar LNB = from("lnb");
	public static final RunCalendar WEEKDAYS = from("weekdays");
	public static final RunCalendar SEVENDAYS = new SevenDays();
    public static final RunCalendar NYB_P1 = from("nyb+1");
	
	public static RunCalendar from(String s) {
        String dbName = s.replaceAll("[+-].*", "");
        if(dbName.equals("none")) dbName = "weekdays";
        boolean lastDay = false;
        if (dbName.matches("lastdayofweek_.*")) {
            dbName = dbName.replaceAll("^lastdayofweek_", "");
            lastDay = true;
        }
        String digits = s.replaceAll("^[^+-]*(\\+)?", "");
        if (dbName.equals("sevendays")) {
            bombIf(hasContent(digits), "offset does not make sense with sevendays!");
            return SEVENDAYS;
        }
        int offset = isEmpty(digits) ? 0 : Integer.valueOf(digits);
        RunCalendar result = dbName.equals("weekdays") ? new WeekDays() : new DbCalendar(dbName);
        if (offset != 0) result = new WithOffset(result, offset);
        if (lastDay) result = new ForceLastDayOfWeek(result);
        return result;
    }
	
	protected RunCalendar() {
	}
	
	@Override public String toString() { 
	    return dbName(); 
	}
	
	public Cell<?> cell(NvarcharColumn column) {
	    return column.with(dbName());
	}
	

	public boolean matches(String center) {
		return dbName().equals(center);
	}


	public abstract String name();
	public abstract int offset();
	public abstract boolean isValid(Date asOf);
	public abstract Date priorDay(Date asOf); 
	public abstract Date nextDay(Date asOf); 
	public abstract String dbName();

    public Date asOf(Date date) {
        return date;
    }

    public boolean isBeforeTime(Date asOf, String time) {
        Date lhs = asOf(now());
        Date rhs = timeOn(time, asOf);
        return lhs.before(rhs);
    }

    public static boolean isWeekend(Date asOf) {
        return WEEKEND.contains(dayOfWeek(asOf));
    }


}
