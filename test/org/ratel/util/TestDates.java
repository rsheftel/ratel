package org.ratel.util;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Times.*;

import java.util.*;
public class TestDates extends Asserts {

    @Override protected void setUp() throws Exception {
        super.setUp();
        Dates.freezeNow();
    }
    
    @Override protected void tearDown() throws Exception {
        Dates.thawNow();
        super.tearDown();
    }
    
    public void testMinMaxDate() throws Exception {
        assertEquals("0001/01/01", ymdHuman(date("0001/01/01")));
        assertEquals(1753, calendar(date("1753-01-01")).get(Calendar.YEAR));
        assertEquals(9999, calendar(date("9999-12-31")).get(Calendar.YEAR));
    }
    
    public void testTodayAt() throws Exception {
        Date now = date("2008/04/14 15:00:01");
        freezeNow(now);
        assertEquals(now, now());
        assertEquals(date("2008/04/14 14:00:00"), todayAt("14:00:00"));
        assertEquals(date("2008/04/14 14:00:03"), todayAt("14:00:03"));
        try {
            todayAt("5:00:00");
            fail();
        } catch (RuntimeException e) {
            assertMatches("hh:mm:ss", e);
        }
        try {
            todayAt("15:00");
            fail();
        } catch (RuntimeException e) {
            assertMatches("hh:mm:ss", e);
        }
        try {
            todayAt("24:00:00");
            fail();
        } catch (RuntimeException e) {
            assertMatches("hour 24 must be < 24", e);
        }
    }
    public void testThirdWedMinusTwo() throws Exception {
        assertEquals(date("2008/04/14"), thirdWedLessTwoLondon("200804"));
        assertEquals(date("2008/01/14"), thirdWedLessTwoLondon("200801"));
        assertEquals(date("1982/01/18"), thirdWedLessTwoLondon("198201"));
        assertEquals(date("1982/12/13"), thirdWedLessTwoLondon("198212"));
    }
    
    public void testFridayBeforeThirdWed() throws Exception {
        assertEquals(date("2008/04/11"), fridayBeforeThirdWed("200804", "nyb"));
        assertEquals(date("2008/07/11"), fridayBeforeThirdWed("200807", "nyb"));
        assertEquals(date("2007/11/16"), fridayBeforeThirdWed("200711", "nyb"));
        assertEquals(date("2008/03/14"), fridayBeforeThirdWed("200803", "nyb"));
    }
    
    private Date fridayBeforeThirdWed(String ym, String center) {
        return Dates.fridayBeforeThirdWed(yearMonth(ym), center);
    }
    private Date thirdWedLessTwoLondon(String ym) {
        return Dates.thirdWedLessTwo(yearMonth(ym), "lnb");
    }

    public void testYearMonth() throws Exception {
        assertEquals(new YearMonth(2008, 4), yearMonth(date("2008/04/06")));
        assertEquals(new YearMonth(2008, 1), yearMonth(date("2008/01/06")));
        assertEquals(new YearMonth(2012, 12), yearMonth(date("2012/12/31")));
    }
    
    public void testDateFormatsWork() throws Exception {
        assertEquals(yyyyMmDd("2007/10/24"), date("10/24/07"));
        assertEquals(yyyyMmDd("2007/01/24"), date("1/24/07"));
        assertEquals(yyyyMmDd("2007/01/05"), date("1/5/07")); 
        assertEquals(yyyyMmDd("2007/10/24"), date("10/24/2007"));
        assertEquals(yyyyMmDd("2007/01/01"), date("01/01/2007")); 
        assertEquals(yyyyMmDd("2029/01/01"), date("01/01/29")); 
        assertEquals(yyyyMmDd("1930/01/01"), date("01/01/30")); 
    }
    
    public void testSpecialDatesWork() throws Exception {
        assertEquals(midnight(), date("today"));
        assertEquals(midnight(yesterday()), date("yesterday"));
        freezeNow("2008/08/29 02:00:00");
        assertEquals(date("2008/08/28"), date("today4pm"));
        freezeNow("2008/08/29 15:59:59");
        assertEquals(date("2008/08/28"), date("today4pm"));
        freezeNow("2008/08/29 16:00:00");
        assertEquals(date("2008/08/29"), date("today4pm"));
        freezeNow("2008/08/29 23:59:59");
        assertEquals(date("2008/08/29"), date("today4pm"));
    }
    
    public void testMarkitFormat() throws Exception {
        assertEquals(date("2009/04/08"), date("08-APR-09"));
    }
    
    public void testMonthsAheadBehind() throws Exception {
        assertEquals(yyyyMmDd("2008/06/30"), monthsAhead(3, yyyyMmDd("2008/03/31")));
        assertEquals(yyyyMmDd("2008/12/31"), monthsAhead(9, yyyyMmDd("2008/03/31")));
        assertEquals(yyyyMmDd("2008/12/30"), monthsAhead(6, yyyyMmDd("2008/06/30")));
    }
    
    public void testYesterdayAndDaysAgo() throws Exception {
        assertEquals(yesterday(), daysAgo(1, now()));
        assertEquals(daysAgo(1, yesterday()), daysAgo(2, now()));
        assertEquals(daysAgo(1, now()), daysAhead(-1, now()));
        assertEquals(daysAhead(1, now()), tomorrow());

        assertEquals(midnight(tomorrow()), hoursAhead(24, midnight(now())));
        assertEquals(midnight(now()), hoursAgo(24, midnight(tomorrow())));
        Date midnight1024 = yyyyMmDd("2007/10/24");
        assertEquals(midnight(midnight1024), midnight1024);
        Calendar c = Calendar.getInstance();
        c.setTime(midnight1024);
        c.add(Calendar.MILLISECOND, 50);
        assertEquals(midnight(c.getTime()), midnight1024);
        assertEquals(midnight(yyyyMmDdHhMmSs("2007/10/24 0:30:23")), midnight1024);
        assertEquals(midnight(yyyyMmDdHhMmSs("2007/10/24 17:30:23")), midnight1024);
        assertEquals(daysAgo(1, yyyyMmDdHhMmSs("2007/10/24 0:30:23")), yyyyMmDdHhMmSs("2007/10/23 0:30:23"));
    }

    public void testBusinessDaysAgoWithoutHolidays() throws Exception {
        checkBusinessDays("2007/10/19", 1, "2007/10/22", "nyb");
        checkBusinessDays("2007/10/19", 1, "2007/10/21", "nyb");
        checkBusinessDays("2007/10/19", 1, "2007/10/20", "nyb");
    }

    public void testBusinessDaysAgoWithHolidays() throws Exception {
        checkBusinessDays("2007/12/31", 1, "2008/01/02", "nyb");
        checkBusinessDays("2005/12/30", 1, "2006/01/03", "nyb");
    }
    
    @SuppressWarnings("deprecation") public void functestBusinessDaysAgoPerf() throws Exception {
        Date d = now();
        long start = nowMillis();
        while(d.before(date("2038/01/01"))) d = businessDaysAhead(30, d, "nyb");
        debug("took " + reallyMillisSince(start) + " millis"); //24190
    }

    public void testDateByYMDWorksAsExpected() throws Exception {
        assertEquals(yyyyMmDd("2007/07/06"), date(2007, 7, 6));
        assertEquals(yyyyMmDd("2000/01/01"), date(2000, 1, 1));
    }
    
    public void testNextBusinessDay() throws Exception {
        checkNextBusinessDay("2007/12/10", "2007/12/07", "nyb");
        checkNextBusinessDay("2007/12/11", "2007/12/10", "nyb");
        checkNextBusinessDay("2007/12/11", "2007/12/10", "nyb");
        checkNextBusinessDay("2007/12/26", "2007/12/24", "nyb");
        checkNextBusinessDay("2007/12/26", "2007/12/25", "nyb");
        checkNextBusinessDay("2007/09/04", "2007/08/31", "nyb");
    }
    
    private void checkNextBusinessDay(String expectedNext, String day, String center) {
        assertEquals(yyyyMmDd(expectedNext), nextBusinessDay(yyyyMmDd(day), center));
    }

    public void testFincadDateConversionsWork() throws Exception {
        Date now = now();
        assertEquals(asJavaDate(asFincadMidnight(now)), midnight(now));
    }
    
    private void checkBusinessDays(String expected, int days, String start, String center) {
        assertEquals(yyyyMmDd(expected), businessDaysAgo(days, yyyyMmDd(start), center));
    }
    
    public void testIsHoliday() throws Exception {
        assertTrue(isHoliday(yyyyMmDd("2007/11/12"), "nyb"));
        assertFalse(isHoliday(yyyyMmDd("2007/11/13"), "nyb"));
        assertFalse(isHoliday(yyyyMmDd("2007/11/11"), "nyb"));
        // weekends don't count as holidays
        assertFalse(isHoliday(yyyyMmDd("2007/12/08"), "nyb"));
    }
    
    public void testNextBusinessDayWithoutCenter() throws Exception {
        assertEquals(yyyyMmDd("2007/12/10"), nextBusinessDay(yyyyMmDd("2007/12/07")));
        assertEquals(yyyyMmDd("2007/12/11"), nextBusinessDay(yyyyMmDd("2007/12/10")));
        assertEquals(yyyyMmDd("2007/12/25"), nextBusinessDay(yyyyMmDd("2007/12/24")));
    }
    
    public void testBusinessDaysBetween() throws Exception {
        assertEquals(21, businessDaysBetween(date("2007/12/01"), date("2008/01/02"), "nyb"));
        assertEquals(0, businessDaysBetween(date("2008/01/02"), date("2008/01/02"), "nyb"));
        assertEquals(1, businessDaysBetween(date("2008/01/02"), date("2008/01/03"), "nyb"));
        assertEquals(2, businessDaysBetween(date("2008/01/02"), date("2008/01/04"), "nyb"));
        assertEquals(3, businessDaysBetween(date("2008/01/02"), date("2008/01/05"), "nyb"));
        assertEquals(3, businessDaysBetween(date("2008/01/02"), date("2008/01/06"), "nyb"));
        assertEquals(3, businessDaysBetween(date("2008/01/02"), date("2008/01/07"), "nyb"));
        assertEquals(4, businessDaysBetween(date("2008/01/02"), date("2008/01/08"), "nyb"));
    }
    
    public void testDayGMT() throws Exception {
        freezeNow("2003/08/09 13:00:00");
        assertEquals(date("2003/08/09"), midnightGMT());
        freezeNow("2003/08/08 22:00:00");
        assertEquals(date("2003/08/09"), midnightGMT());
    }
    
    public void testTimeZone() throws Exception {
        assert10AmTime("America/Chicago", "2008/01/02", "11:00:00");
        assert10AmTime("America/Chicago", "2006/04/01", "11:00:00");
        assert10AmTime("America/Chicago", "2006/04/02", "11:00:00");
        assert10AmTime("America/Chicago", "2007/03/10", "11:00:00");
        assert10AmTime("America/Chicago", "2007/03/11", "11:00:00");
        assert10AmTime("America/St_Lucia", "2006/04/01", "09:00:00");
        assert10AmTime("America/St_Lucia", "2006/04/02", "10:00:00");
        assert10AmTime("America/St_Lucia", "2007/03/10", "09:00:00");
        assert10AmTime("America/St_Lucia", "2007/03/11", "10:00:00");
        assert10AmTime("Europe/London", "2007/03/10", "05:00:00");
        assert10AmTime("Europe/London", "2007/03/11", "06:00:00");
        assert10AmTime("Europe/London", "2007/03/24", "06:00:00");
        assert10AmTime("Europe/London", "2007/03/25", "05:00:00");
    }

    private void assert10AmTime(String timeZone, String date, String expectedTime) {
        Dates.freezeNow(date);
        Date expected = todayAt(expectedTime);
        Date actual = new QTimeZone(timeZone).toLocalTime(todayAt("10:00:00"));
        assertEquals(
            "expected\n" + ymdHuman(expected) + " did not match\n" + ymdHuman(actual),
            expected, 
            actual
        );
    }
}
