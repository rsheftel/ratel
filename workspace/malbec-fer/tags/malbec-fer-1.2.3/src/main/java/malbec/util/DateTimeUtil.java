package malbec.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class to handle manipulation of Date/Time.
 * 
 */
public class DateTimeUtil {

    private static final String UTC_DATE = "yyyyMMdd";

    private static final String UTC_TIMESTAMP = "yyyyMMdd-HH:mm:ss.SSS";
    private static final String UTC_DATETIME = "yyyyMMdd-HH:mm:ss";

    private static final String BB_CMF_TIMESTAMP = "yyyyMMddHHmmss";

    static final String CSHARP_DATETIME = "M/d/yyyy hh:mm:ss aa";
    static final String JAVA_DATETIME = "EEE MMM dd HH:mm:ss zzz yyyy";
    static final String EXCEL_DATE = "M/dd/yy";
    static final String EXCEL_DATETIME = "M/dd/yy/hh/mm/ss";
    static final String LOCALDATE_DATE = "yyyy-MM-dd";
    static final String AMERICAN_DATETIME = "yyyy/MM/dd HH:mm:ss";

    // Since we are calling toString on the variant, the time from Redi is just Java Date
    static final String REDI_DATETIME = JAVA_DATETIME;
    //                                       
    // Mon Jan 01 09:09:22 EST 1990

    private static final String[] GUESS_ORDER = { AMERICAN_DATETIME, CSHARP_DATETIME, EXCEL_DATETIME, EXCEL_DATE, LOCALDATE_DATE, UTC_DATETIME };

    /**
     * Return the current date as a string in 'yyyyMMdd' format.
     * 
     * @return
     */
    public static String getDateAsString() {
        return getDateAsString(System.currentTimeMillis());
    }

    /**
     * Return the specified date in 'yyyyMMdd' format.
     * 
     * @param date
     * @return
     */
    public static String getDateAsString(LocalDate localDate) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATE);
        return fmt.print(localDate);
    }

    /**
     * Return the specified date in 'yyyyMMdd' format.
     * 
     * @param date
     * @return
     */
    public static String getDateAsString(Date date) {
        return getDateAsString(date.getTime());
    }

    /**
     * Return the specified date in 'yyyyMMdd' format.
     * 
     * @param milliDate
     * @return
     */
    public static String getDateAsString(long milliDate) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATE);
        return fmt.print(milliDate);
    }

    public static String formatTimestampUTZ(Date jdkDate) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_TIMESTAMP);
        fmt = fmt.withZone(DateTimeZone.UTC);
        return fmt.print(jdkDate.getTime());
    }

    public static String formatCSharp(DateTime dateTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(CSHARP_DATETIME);
        return fmt.print(dateTime);
    }

    public static String format(DateTime dateTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(AMERICAN_DATETIME);
        return fmt.print(dateTime);
    }

    public static String formatCSharp(LocalDate localDate) {
        return formatCSharp(localDate.toDateTimeAtStartOfDay());
    }

    /**
     * Return a <code>LocalDate</code> from a String in the format of 'yyyyMMdd'.
     * 
     * @param strDate
     * @return
     */
    public static LocalDate getLocalDate(String strDate) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATE);
        DateTime dt = fmt.parseDateTime(strDate);
        return dt.toLocalDate();
    }

    public static DateTime getDateTime(String strDateTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_TIMESTAMP);
        fmt = fmt.withZone(DateTimeZone.UTC);

        return fmt.parseDateTime(strDateTime);
    }

    /**
     * From a formatted Redi date (Java Date) create a date using the passed in time and the current date.
     * 
     * @param strDateTime
     * @return
     */
    public static DateTime getCurrentFromTime(String strDateTime) {
        Date javaDate = parseDate(strDateTime, REDI_DATETIME);
        if (javaDate == null) {
            return null;
        }
        DateTime dt = new DateTime(javaDate);

        LocalDate ld = new LocalDate();

        return new DateTime(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth(), dt.getHourOfDay(), dt
                .getMinuteOfHour(), dt.getSecondOfMinute(), 0);
    }

    /**
     * From a formatted Redi date (Java Date) create a date using the passed in time and the current date.
     * 
     * @param strDateTime
     * @return
     */
    public static LocalDate getDateFromJavaString(String strDateTime) {
        Date javaDate = parseDate(strDateTime, REDI_DATETIME);
        if (javaDate == null) {
            return null;
        }
        DateTime dt = new DateTime(javaDate);

        return new LocalDate(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
    }

    /**
     * Parse the date string using UTC format (yyyyMMdd-HH:mm:ss).
     * 
     * @param strDateTime
     * @return a java.util.Date
     */
    public static Date getDate(String strDateTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATETIME);
        fmt = fmt.withZone(DateTimeZone.UTC);

        DateTime dt = fmt.parseDateTime(strDateTime.substring(0, 17));

        return new Date(dt.getMillis());
    }

    public static Date getBloombergDate(String dateStr) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(BB_CMF_TIMESTAMP);

        DateTime dt = fmt.parseDateTime(dateStr);

        return new Date(dt.getMillis());
    }

    public static Date getDate(LocalDate localDate) {
        return new Date(localDate.toDateTimeAtStartOfDay().getMillis());
    }

    /**
     * Parse the date using the default JDK format. 'EEE MMM dd HH:mm:ss zzz yyyy'
     * 
     * @param strDateTime
     * @return
     */
    public static Date getJdkDate(String strDateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(JAVA_DATETIME);

            return sdf.parse(strDateTime);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static DateTime guessDateTime(String strDateTime) {
        if (strDateTime == null) {
            return null;
        }
        // Fall back to using standard Java parser as Joda-Time cannot handle this
        // DateTime dt = parseDateTime(strDateTime,JAVA_DATETIME);
        DateTime dt = null;

        for (int i = 0; i < GUESS_ORDER.length && dt == null; i++) {
            dt = parseDateTime(strDateTime, GUESS_ORDER[i]);
        }

        if (dt == null) {
            // try the default jdk parser since Joda-Time cannot handle short timezones
            Date d = parseDate(strDateTime, JAVA_DATETIME);
            if (d != null) {
                return new DateTime(d);
            }
        }

        return dt;
    }

    static DateTime parseDateTime(String strDateTime, String pattern) {
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
            DateTime dt = fmt.parseDateTime(strDateTime);
            return dt;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Date parseDate(String strDateTime, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);

            return sdf.parse(strDateTime);
        } catch (ParseException e) {
            return null;
        }
    }
}
