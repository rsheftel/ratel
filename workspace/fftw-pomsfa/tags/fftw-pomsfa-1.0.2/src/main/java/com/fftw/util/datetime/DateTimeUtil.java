package com.fftw.util.datetime;

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
public class DateTimeUtil
{

    private static final String UTC_DATE = "yyyyMMdd";

    private static final String UTC_TIMESTAMP = "yyyyMMdd-HH:mm:ss.SSS";
    private static final String UTC_DATETIME = "yyyyMMdd-HH:mm:ss";

    /**
     * Return the current date as a string in 'yyyyMMdd' format.
     * 
     * @return
     */
    public static String getDateAsString ()
    {
        return getDateAsString(System.currentTimeMillis());
    }

    /**
     * Return the specified date in 'yyyyMMdd' format.
     * 
     * @param date
     * @return
     */
    public static String getDateAsString (LocalDate localDate)
    {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATE);
        return fmt.print(localDate);
    }

    
    /**
     * Return the specified date in 'yyyyMMdd' format.
     * 
     * @param date
     * @return
     */
    public static String getDateAsString (Date date)
    {
        return getDateAsString(date.getTime());
    }

    /**
     * Return the specified date in 'yyyyMMdd' format.
     * 
     * @param milliDate
     * @return
     */
    public static String getDateAsString (long milliDate)
    {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATE);
        return fmt.print(milliDate);
    }

    public static String formatTimestampUTZ (Date jdkDate)
    {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_TIMESTAMP);
        fmt = fmt.withZone(DateTimeZone.UTC);
        return fmt.print(jdkDate.getTime());
    }
    
    /**
     * Return a <code>LocalDate</code> from a String in the format of 'yyyyMMdd'.
     * 
     * @param strDate
     * @return
     */
    public static LocalDate getLocalDate(String strDate) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_DATE);
        DateTime dt =  fmt.parseDateTime(strDate);
        return dt.toLocalDate();
    }
    
    public static DateTime getDateTime(String strDateTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(UTC_TIMESTAMP);
        fmt = fmt.withZone(DateTimeZone.UTC);
        
        return fmt.parseDateTime(strDateTime);
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
        
        DateTime dt = fmt.parseDateTime(strDateTime.substring(0,17));
        
        return new Date(dt.getMillis());
    }
}
