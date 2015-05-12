package util;

import static db.clause.Clause.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;
import java.io.*;
import db.clause.*;
import db.columns.*;

public class Range implements Iterable<Date>, Serializable {

	private static final long serialVersionUID = 1L;
    private final Date end;
	private final Date start;

	public Range(Date start, Date end) {
		this.start = start;
		this.end = end;
	}

	public static Range range(String start, String end) {
		return range(start, end, false);
	}
	
	public static Range range(String start, String end, boolean reorder) {
		Date startDate = start == null ? null : parseStartDate(start);
		Date endDate = end == null ? null : parseEndDate(end);
		if (closed(startDate, endDate) && endDate.before(startDate)) {
			if (reorder) return range(end, start);
			bomb("dates out of order (" + start + " to " + end);
		}
		return new Range(startDate, endDate);
	}

	public static Range allTime() {
	    return new Range((Date)null, null);
	}
	
    private static boolean closed(Date startDate, Date endDate) {
        return startDate != null && endDate != null;
    }

    private static Date parseEndDate(String end) {
        Date endDate;
		try {
			endDate = yyyyMmDdHhMmSs(end);
		} catch (RuntimeException e) {
			endDate = yyyyMmDdHhMmSs(end + " 23:59:59");
		}
        return endDate;
    }

    private static Date parseStartDate(String start) {
        Date startDate;
		try {
			startDate = yyyyMmDdHhMmSs(start);
		} catch (RuntimeException e) {
			startDate = yyyyMmDd(start);
		}
        return startDate;
    }
	
	public static Range unorderedRange(String start, String end) {
		return range(start, end, true);
	}
	
	public static Range unorderedRange(Date start, Date end) {
		return end.before(start) ? range(end, start) : range(start, end);
	}
	
	public Clause matches(DatetimeColumn c) {
	    if (start == null && end == null) return TRUE;
	    if (start == null) return c.lessThanOr(stamp(end));
	    if (end == null) return c.greaterThanOr(stamp(start));
		return new BetweenClause(c, stamp(start), stamp(end));
	}

	public static java.sql.Timestamp stamp(Date d) { 
	    return new java.sql.Timestamp(d.getTime());
	}
	
	public Date start() {
		return start;
	}
	public Date end() {
		return end;
	}

	public static Range range(String date) {
		return range(date, date);
	}

	public static Range range(Date date) {
		if(!date.equals(midnight(date))) return range(date, date);
		return range(yyyyMmDd(date));
	}

	public static Range range(Date startDate, Date endDate) {
		return new Range(startDate, endDate);
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Range other = (Range) obj;
		if (end == null) {
			if (other.end != null) return false;
		} else if (!end.equals(other.end)) return false;
		if (start == null) {
			if (other.start != null) return false;
		} else if (!start.equals(other.start)) return false;
		return true;
	}
	
	@Override public String toString() {
		return "range " + ymdHuman(start()) + " to " + ymdHuman(end());
	}

	public static Range onDayOf(Date asOf) {
		return range(yyyyMmDd(asOf));
	}
	
	public static Range onDayOf(String asOf) {
	    return onDayOf(date(asOf));
	}

	public static Range range(Date start, int daysAhead) {
		return unorderedRange(start, daysAhead(daysAhead, start));
	}

    public boolean containsEndExclusive(Date date) {
        if (!containsInclusive(date)) return false;
        if (hasEnd() && date.equals(end)) return false;
        return true;
    }
	
    public boolean containsInclusive(Date date) {
        if(start == null && end == null) return true;
        if (start == null) return !end().before(date);
        if (end == null) return !start().after(date);
        return !start().after(date) && !end().before(date);
    }

    public boolean contains(Range inner) {
        if(inner.start() == null && start != null) return false;
        if(inner.end() == null && end != null) return false;
        return containsInclusive(inner.start()) && containsInclusive(inner.end());
    }

    @Override public Iterator<Date> iterator() {
        return new Iterator<Date>() {
            Date current = start;
            boolean advanced = false;
            boolean isOneDayRange = midnight(start).equals(midnight(end));
            @Override public boolean hasNext() {
                if (isOneDayRange) return !advanced;
                return !daysAhead(1, current).after(end);
            }

            @Override public Date next() {
                if (advanced) current = daysAhead(1, current);
                advanced = true;
                return current;
            }

            @Override public void remove() {
                throw bomb("unimplemented");
            } 
            
        };
    }

    public static void requireOrdered(Date start, Date end) {
        if (start == null || end == null) return;
        bombIf(end.before(start), "misordered dates! " + ymdHuman(end) + " before " + ymdHuman(start));
    }

    public boolean overlaps(Range range) {
        if (start != null && range.containsInclusive(start)) return true;
        if (end != null && range.containsInclusive(end)) return true;
        if (range.start != null && containsInclusive(range.start)) return true;
        if (range.end != null && containsInclusive(range.end)) return true;
        return false;
    }

    public static void requireNoOverlaps(List<Range> ranges) {
        if (ranges.isEmpty()) return;
        Range first = first(ranges);
        for(Range r : rest(ranges)) 
            bombIf(first.overlaps(r), "overlap detected \n" + first + " \noverlaps\n" + r);
        requireNoOverlaps(rest(ranges));
    }

    public boolean adjacentTo(Range range) {
        if (endMatchesStart(range)) return true;
        if (range.endMatchesStart(this)) return true;
        return false;
    }

    private boolean endMatchesStart(Range range) {
        return end != null && range.start != null && daysAhead(1, end).equals(range.start);
    }

    public Range union(Range newRange) {
        return new Range(min(start(), newRange.start()), max(end(), newRange.end()));
    }

    private Date min(Date a, Date b) {
        if (a == null || b == null) return null;
        return a.after(b) ? b : a;
    }

    private Date max(Date a, Date b) {
        if (a == null || b == null) return null;
        return a.after(b) ? a : b;
    }

    public boolean hasStart() {
        return start != null;
    }

    public boolean hasEnd() {
        return end != null;
    }
    
	
	
}
