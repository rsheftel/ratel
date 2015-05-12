package com.fftw.util.datetime;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

/**
 *@deprecated  Use the version form malbec-fer (1.2.4)as it is more complete.
 */
@Deprecated
public class DateTimeUtils {

    private static final String BB_DATE = "MM/dd/yy";

    /**
     * Format the date in the Bloomberg MM/DD/YY format.
     *
     * @param date
     * @return
     */
    public static String formatBBDate(LocalDate date) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(BB_DATE);
        return fmt.print(date);
    }

    /**
     * Parse the string using the Bloomberg MM/DD/YY format.
     * 
     * @param dateStr
     * @return
     */
    public static LocalDate parseBBDate(String dateStr) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(BB_DATE);

        DateTime dt = fmt.parseDateTime(dateStr);
        return dt.toLocalDate();
    }
    
    public static boolean isWeekend(LocalDate ld) {
        int dayOfWeek = ld.dayOfWeek().get();

        return (DateTimeConstants.SATURDAY == dayOfWeek || DateTimeConstants.SUNDAY == dayOfWeek);
    }
    
    public static String formatNow() {
        return malbec.util.DateTimeUtil.format(new DateTime());
    }
}
