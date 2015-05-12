package util;

import static java.lang.Integer.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Strings.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.columns.*;

public class YearMonth {

    private final int year;
    private final int month;

    public YearMonth(String yyyyMm) {
        String normalized = yyyyMm.replaceAll("\\D+", "");
        String malformed = "could not parse " + yyyyMm + " as a YearMonth";
        bombUnless(normalized.length() == 6, malformed);
        year = parseInt(normalized.substring(0, 4)); 
        month = parseInt(normalized.substring(4)); 
        bombUnless(year >= 1900 && year <= 2200, malformed);
        bombUnless(month >= 1 && month <= 12, malformed);
    }

    public YearMonth(Date d) {
        this(Dates.year(d), monthNumber(d), "could not get year month from " + ymdHuman(d));
    }
    
    private YearMonth(int year, int month, String malformed) {
        bombUnless(year >= 1900 && year <= 2200, malformed);
        bombUnless(month >= 1 && month <= 12, malformed);
        this.year = year;
        this.month = month;
    }

    public YearMonth(int year, int month) {
        this(year, month, "could not create year month from " + year + " " + month);
    }

    public int year() {
        return year;
    }

    public int month() {
        return month;
    }

    public Date first() {
        return date(year, month, 1);
    }

    public Date end() {
        Date nextMonth = monthsAhead(1, date(year, month, 1));
        return daysAgo(1, nextMonth);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + month;
        result = prime * result + year;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final YearMonth other = (YearMonth) obj;
        if (month != other.month) return false;
        if (year != other.year) return false;
        return true;
    }

    public String string() {
        return year + leftZeroPad(month(), 2);
    }
    
    @Override public String toString() {
        return string();
    }

    public Clause matches(NcharColumn c) {
        return c.is(string());
    }

    public Cell<?> in(NcharColumn c) {
        return c.with(string());
    }
    
}