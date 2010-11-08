package org.ratel.util;

import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;

import java.util.*;

public class TestRange extends Asserts {
    public void testSimpleDateRange() throws Exception {
        Range r = range("2001-01-01", "2002-01-01");
        
        assertDate("2001-01-01 00:00:00", r.start());
        assertDate("2002-01-01 23:59:59", r.end());
        
        r = range("2001-01-01 16:00:00", "2002-01-01 16:00:00");
        assertDate("2001-01-01 16:00:00", r.start());
        assertDate("2002-01-01 16:00:00", r.end());
        assertTrue(r.containsInclusive(date("2001-01-01 16:00:00")));
        assertTrue(r.containsInclusive(date("2001-01-01 17:00:00")));
        assertFalse(r.containsInclusive(date("2002-01-01 16:00:01")));
        assertTrue(r.containsInclusive(date("2002-01-01 16:00:00")));
        assertFalse(r.containsInclusive(date("2001-01-01 15:59:59")));
        
        assertTrue(r.containsInclusive(date("2002-01-01 15:59:59")));
        assertTrue(r.containsInclusive(date("2002-01-01 16:00:00")));
        assertTrue(r.containsEndExclusive(date("2002-01-01 15:59:59")));
        assertFalse(r.containsEndExclusive(date("2002-01-01 16:00:00")));
    }

    private void assertDate(String dateString, Date actual) {
        assertEquals(yyyyMmDdHhMmSs(dateString), actual);
    }
    
    public void testRangeIteration() throws Exception {
        assertEquals(list("2001/01/01", "2001/01/02", "2001/01/03", "2001/01/04", "2001/01/05"), toHumanByIteration("2001/01/01", "2001/01/05"));
        try {
            assertEquals(list(), toHumanByIteration("2000/12/31", "2000/12/30"));
            fail("out of order");
        } catch (RuntimeException e) {
            assertMatches("out of order", e);
        }
        assertEquals(list("2000/12/31"), toHumanByIteration("2000/12/31", "2000/12/31"));
    }

    private List<String> toHumanByIteration(String start, String end) {
        List<String> found = empty();
        for(Date d : range(start, end))
            found.add(ymdHuman(d));
        return found;
    }
    
    public void testAYearWithStrings() { 
        List<String> dates = toHumanByIteration("2000/01/01", "2001/01/01");
        assertEquals("2000/01/01", first(dates));
        assertEquals("2001/01/01", last(dates));
        assertSize(367, dates); // 2000 is leap year + 1 for 1st of 2001
    }
    
    public void testRangeUnion() throws Exception {
        Range until = range(null, "2000/01/01");
        Range after = range("2000/1/3", null);
        Range between = range("2000/01/02", "2000/01/02");
        Range betweenLater = range("2010/01/02", "2011/01/02");
        assertEquals(range(null, "2000/01/02"), until.union(between));
        assertEquals(range(null, (String)null), until.union(after));
        assertEquals(range("2000/01/2", null), after.union(between));
        assertEquals(between, between.union(between));
        assertEquals(range("2000/1/2", "2011/01/02"), between.union(betweenLater));
    }
    
    public void testAYearWithDates() { 
        List<String> found = empty();
        for(Date d : range(date("2000/01/01"), date("2001/01/01")))
            found.add(ymdHuman(d));
        List<String> dates = found;
        assertEquals("2000/01/01", first(dates));
        assertEquals("2001/01/01", last(dates));
        assertSize(367, dates); // 365 days [+ 1, 2000 is leap year] [+ 1, 1st of 2001]
    }
    
    public void testStringDateFactoryEquivalence() throws Exception {
        assertEquals(
            range("2000/01/01", "2001/01/01"),
            range(date("2000/01/01"), date("2001/01/01 23:59:59"))
        );
    }
    
    public void testRangeOverlaps() throws Exception {
        Range until = range(null, date("2000/01/01"));
        overlaps(until, range(null, date("1999/12/31")));
        overlaps(until, range(null, date("2000/01/01")));
        overlaps(until, range(null, date("2000/01/02")));
        
        overlaps(until, range(date("1999/12/31"), null));
        overlaps(until, range(date("2000/01/01"), null));
        noOverlap(until, range(date("2000/01/02"), null));
        
        Range after = range(date("2000/01/01"), null);
        noOverlap(after, range(null, date("1999/12/31")));
        overlaps(after, range(null, date("2000/01/01")));
        overlaps(after, range(null, date("2000/01/02")));
        
        overlaps(after, range(date("1999/12/31"), null));
        overlaps(after, range(date("2000/01/01"), null));
        overlaps(after, range(date("2000/01/02"), null));
        
        Range on = onDayOf("2000/01/01");

        noOverlap(on, range(null, date("1999/12/31")));
        overlaps(on, range(null, date("2000/01/01")));
        overlaps(on, range(null, date("2000/01/02")));
        
        overlaps(on, range(date("1999/12/31"), null));
        overlaps(on, range(date("2000/01/01"), null));
        noOverlap(on, range(date("2000/01/02"), null));
        
        Range between = range(date("2000/01/01"), date("2000/01/15"));

        noOverlap(between, range(null, date("1999/12/31")));
        overlaps(between, range(null, date("2000/01/01")));
        overlaps(between, range(null, date("2000/01/02")));
        
        overlaps(between, range(date("1999/12/31"), null));
        overlaps(between, range(date("2000/01/01"), null));
        overlaps(between, range(date("2000/01/02"), null));
        overlaps(between, range(date("2000/01/15"), null));
        noOverlap(between, range(date("2000/01/16"), null));
        
    }
    
    private void overlaps(Range until, Range range) {
        assertTrue(until.overlaps(range));
        assertTrue(range.overlaps(until));
    }

    private void noOverlap(Range until, Range range) {
        assertFalse(until.overlaps(range));
        assertFalse(range.overlaps(until));
    }
    
    public void testRangeContainsWithOpenRanges() throws Exception {
        Range until = range(null, date("2000/01/01"));
        assertTrue(until.containsInclusive(date("1999/12/31")));
        assertTrue(until.containsInclusive(date("2000/01/01")));
        assertFalse(until.containsInclusive(date("2000/01/02")));
        Range before = range("2000/01/01", null);
        assertFalse(before.containsInclusive(date("1999/12/31")));
        assertTrue(before.containsInclusive(date("2000/01/01")));
        assertTrue(before.containsInclusive(date("2000/01/02")));
        
        Range always = range( (Date) null, null);
        assertTrue(always.contains(until));
        assertTrue(always.contains(before));
        assertFalse(until.contains(always));
        assertFalse(before.contains(always));
        
        assertFalse(until.contains(before));
        assertFalse(before.contains(until));
        
        Range between = range(date("2000/01/01"), date("2000/01/01"));
        assertTrue(always.contains(between));
        assertTrue(before.contains(between));
        assertTrue(until.contains(between));
    }
    
    public void testUnorderedRange() throws Exception {
        Range ordered = range("2007/12/07", "2007/12/10");
        try {
            range("2007/12/10", "2007/12/07");
            fail("unorder range must be specified!");
        } catch (RuntimeException e) {}
        Range unordered = unorderedRange("2007/12/10", "2007/12/07");
        assertEquals(ordered, unordered);
    }
    
}
