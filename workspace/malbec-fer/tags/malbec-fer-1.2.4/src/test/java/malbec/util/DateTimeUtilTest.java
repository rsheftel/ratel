package malbec.util;

import static malbec.util.DateTimeUtil.*;
import static org.testng.Assert.*;

import java.util.Calendar;
import java.util.Date;

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
        
        DateTime start = createDateTime("Monday", "05:30:00", true);
        assertEquals(start, expectedStart);
        DateTime start2 = createDateTime("Monday", new LocalTime(5,30,0), true);
        assertEquals(start2, expectedStart);

        DateTime expectedEnd = new DateTime(2009, 3, 20, 20, 30, 0, 0);
        DateTime end = createDateTime("Friday", "20:30:00", false);
        assertEquals(end, expectedEnd);
        DateTime end2 = createDateTime("Friday", new LocalTime(20, 30,0), false);
        assertEquals(end2, expectedEnd);
        
        
    }


}
