package db;

import static util.Dates.*;
import static util.Objects.*;
import static util.Range.*;

import java.util.*;

import util.*;
import db.columns.*;

public class TestDateColumns extends DbTestCase {

    class TempTable extends Table {
        private static final long serialVersionUID = 1L;
        public DatetimeColumn C_TIME = new DatetimeColumn("atime", "datetime", this, NOT_NULL);

        protected TempTable() {
            super("TSDB..tempfortesting");

        }
        List<Date> values() { 
            List<Date> result = empty();
            for(java.sql.Timestamp t : C_TIME.values())
                result.add(t);
            return result;
        }
        public void initialize() {
            schemaTable().create();
            Range range = range(date("2000/01/01"), date("2001/01/01"));
            for(Date d : range) {
                insert(C_TIME.with(d));
            }
        }
        public Date time(String start, String end) {
            return time(dateRange(start, end));
        }
        public Date time(Range r) {
            return C_TIME.value(C_TIME.in(r));
        }
        public List<java.sql.Timestamp> times(String start, String end) {
            return times(dateRange(start, end));
        }
        public List<java.sql.Timestamp> times(Range r) {
            return C_TIME.values(C_TIME.in(r));
        }
        
    }
    TempTable TEMP = new TempTable();
    
    @Override protected void setUp() throws Exception {
        super.setUp();
        TEMP.initialize();
    }
    public void testDateStrangeness() throws Exception {
        assertEquals(date("2000/12/31"), daysAgo(1, date("2001/01/01")));
    }
    
    public void testTempTableIsProperlySetup() throws Exception {
        List<Date> dates = TEMP.values();
        assertSize(367, dates);
        assertEquals(date("2000/01/01"), first(dates));
        assertEquals(date("2001/01/01"), last(dates));
    }
    
    public void testCanUseRange() throws Exception {
        Date ny2000 = TEMP.time("2000/01/01", "2000/01/01");
        assertEquals(date("2000/01/01"), ny2000);
        assertSize(30, TEMP.times(null, "2000/01/30"));
        assertSize(338, TEMP.times("2000/01/30", null));
    }
}
