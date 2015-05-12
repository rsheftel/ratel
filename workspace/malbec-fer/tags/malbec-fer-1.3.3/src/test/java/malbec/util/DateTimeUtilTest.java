package malbec.util;

import static malbec.util.DateTimeUtil.*;
import static org.testng.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import malbec.AbstractBaseTest;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.Test;

public class DateTimeUtilTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testParseJdk() {

        // The formatting and parsing does not include millis - set them to zero
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        Date jdkDate = cal.getTime();
        Date d = DateTimeUtil.getJdkDate(jdkDate.toString());
        assertEquals(jdkDate, d, "Did not parse JDK date. " + jdkDate.getTime() + ":" + d.getTime());

        try {
            DateTimeUtil.parseDateTime(jdkDate.toString(), JAVA_DATETIME);
            assertFalse(false, "Joda-time can not parse short time zones");
        } catch (IllegalArgumentException e) {
            // this is what we expect
        }
    }

    @Test(groups = { "unittest" })
    public void testParseCSharp() {
        try {
            DateTime dt = DateTimeUtil.parseDateTime("9/9/2008 4:59:14 PM", CSHARP_DATETIME);
            assertNotNull(dt, "Failed to parse C# datetime");
        } catch (IllegalArgumentException e) {
            assertTrue(false, "Error parsing C# datetime");
        }
    }

    @Test(groups = { "unittest" })
    public void testGuessDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        Date jdkDate = cal.getTime();

        try {
            DateTime dt = DateTimeUtil.guessDateTime("9/9/2008 4:59:14 PM");
            assertNotNull(dt, "Failed to parse C# datetime");
        } catch (IllegalArgumentException e) {
            assertTrue(false, "Error parsing C# datetime");
        }

        try {
            DateTime dt = DateTimeUtil.guessDateTime(jdkDate.toString());
            assertNotNull(dt, "Failed to parse JDK default date");
        } catch (IllegalArgumentException e) {
            assertTrue(false, "Error parsing JDK default date");
        }

        try {
            DateTime dt = DateTimeUtil.guessDateTime(new LocalDate().toString());
            assertNotNull(dt, "Failed to parse LocalDate");
        } catch (IllegalArgumentException e) {
            assertTrue(false, "Error parsing LocalDate date");
        }

    }

    @Test(groups = { "unittest" })
    public void testFormatCSharp() {

        DateTime dt = new DateTime();
        LocalDate ld = dt.toLocalDate();

        String dtStr = formatCSharp(dt);
        DateTime parsedDT = parseDateTime(dtStr, CSHARP_DATETIME);
        assertNotNull(parsedDT, "Failed to format and parse C# DateTime");

        String ldStr = formatCSharp(ld);
        DateTime parsedLD = parseDateTime(ldStr, CSHARP_DATETIME);

        assertNotNull(parsedLD, "Failed to format and parse C# LocalDate");
    }

    @Test(groups = { "unittest" })
    public void testRediTime() {
        // Mon Jan 01 09:09:22 EST 1990
        DateTime dt = getCurrentFromTime("Mon Jan 01 09:09:22 EST 1990");
        LocalDate ld = new LocalDate();

        DateTime expected = new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth(), 9, 9, 22, 0);

        assertEquals(expected, dt, "Failed to extract time from date string");
    }

    @Test(groups = { "unittest" })
    public void testCreateDateTimeFromDay() {

        freezeTime("2009/03/16 10:00:00");

        DateTime expectedStart = new DateTime(2009, 3, 16, 5, 30, 0, 0);
        DateTime expectedEnd = new DateTime(2009, 3, 20, 20, 30, 0, 0);
        
        DateTime[] range = determineWeeklyRange("Monday", "05:30:00", "Friday", "20:30:00");
        
        assertEquals(range[0], expectedStart);
        assertEquals(range[1], expectedEnd);
        
        DateTime[] range1 = determineWeeklyRange("Monday", new LocalTime(5,30,0), "Friday",  new LocalTime(20, 30,0));
        assertEquals(range1[0], expectedStart);

        assertEquals(range1[1], expectedEnd);
    }

    @Test(groups = { "unittest" })
    public void testCalculateSchedulerConfig() {
        DateTimeUtil.freezeTime("2009/4/29 9:14:00");
        
        // requested in Hours
        ExecutorConfig ec = DateTimeUtil.scheduleEvery(24, "9:15", TimeUnit.HOURS);
        ExecutorConfig reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(ec.getInitialDelay(), 1000 * 60);
        assertEquals(ec.getPeriod(), 1000 * 60 * 60 * 24);
        assertEquals(ec.getTimeUnit(), TimeUnit.MILLISECONDS);
        
        DateTimeUtil.freezeTime("2009/4/29 9:00:00");
        ec = DateTimeUtil.scheduleEvery(24, "9:15", TimeUnit.HOURS);
        reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(ec.getInitialDelay(), 1000 * 60 * 15);
        assertEquals(ec.getPeriod(), 1000 * 60 * 60 * 24);
        assertEquals(ec.getTimeUnit(), TimeUnit.MILLISECONDS);

        DateTimeUtil.freezeTime("2009/4/29 9:00:00");
        ec = DateTimeUtil.scheduleEvery(24, "8:15", TimeUnit.HOURS, TimeUnit.MINUTES);
        reduced = ec.asLargestTimeUnit();
        ExecutorConfig minutes = ec.inTimeUnit(TimeUnit.MINUTES);
        System.out.println(reduced);
        
        assertEquals(reduced.getInitialDelay(), 1440-45);
        assertEquals(reduced.getPeriod(), 1440);
        assertEquals(reduced.getTimeUnit(), TimeUnit.MINUTES);
        
        assertEquals(minutes.getInitialDelay(), 1440-45);
        assertEquals(minutes.getPeriod(), 1440);
        assertEquals(minutes.getTimeUnit(), TimeUnit.MINUTES);
        

        
        
        DateTimeUtil.freezeTime("2009/4/29 10:00:00");
        ec = DateTimeUtil.scheduleEvery(24, "9:15", TimeUnit.HOURS);
        reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(ec.getInitialDelay(), 1000 * 60 * 60 * 24 - 1000 * 60 * 45);
        assertEquals(ec.getPeriod(), 1000 * 60 * 60 * 24);
        assertEquals(ec.getTimeUnit(), TimeUnit.MILLISECONDS);
        
        reduced = ec.asLargestTimeUnit();
        assertNotSame(reduced, ec);
        
        assertEquals(reduced.getInitialDelay(), 1395);
        assertEquals(reduced.getPeriod(), 1440);
        assertEquals(reduced.getTimeUnit(), TimeUnit.MINUTES);
        
        // requested in Minutes
        DateTimeUtil.freezeTime("2009/4/29 9:00:00");
        ec = DateTimeUtil.scheduleEvery(15, "9:15", TimeUnit.MINUTES);
        reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(reduced.getInitialDelay(), 15);
        assertEquals(reduced.getPeriod(), 15);
        assertEquals(reduced.getTimeUnit(), TimeUnit.MINUTES);

        DateTimeUtil.freezeTime("2009/4/29 9:14:00");
        ec = DateTimeUtil.scheduleEvery(15, "9:15", TimeUnit.MINUTES, TimeUnit.SECONDS);
        reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(reduced.getInitialDelay(), 1);
        assertEquals(reduced.getPeriod(), 15);
        assertEquals(reduced.getTimeUnit(), TimeUnit.MINUTES);
        
        DateTimeUtil.freezeTime("2009/4/29 9:16:00");
        ec = DateTimeUtil.scheduleEvery(15, "9:15", TimeUnit.MINUTES, TimeUnit.SECONDS);
        reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(reduced.getInitialDelay(), 14);
        assertEquals(reduced.getPeriod(), 15);
        assertEquals(reduced.getTimeUnit(), TimeUnit.MINUTES);

        DateTimeUtil.freezeTime("2009/4/29 9:16:00");
        ec = DateTimeUtil.scheduleEvery(15, "7:00", TimeUnit.MINUTES, TimeUnit.SECONDS);
        reduced = ec.asLargestTimeUnit();
        System.out.println(reduced);
        
        assertEquals(reduced.getInitialDelay(), 14);
        assertEquals(reduced.getPeriod(), 15);
        assertEquals(reduced.getTimeUnit(), TimeUnit.MINUTES);

    }
}
