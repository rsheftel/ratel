package org.ratel.util;
import static com.fincad.ShowAll.*;
import static java.lang.Integer.*;
import static java.lang.Long.*;
import static java.util.Calendar.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.AttributeValues.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TimeSeriesDataTable.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;
import static org.ratel.util.Strings.*;

import java.text.*;
import java.util.*;

import org.ratel.tsdb.*;
public class Dates {

    private static final ThreadLocal<SimpleDateFormat> YYYYMMDD = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDD_NO_SEPARATOR = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> EXCEL = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> EXCEL_TIME = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDD_OUT = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSS = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSSMMM = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> SQL_DATE_OUT = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> YYYYMM = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> MMYYYY_OUT = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> DDMMMYY = new ThreadLocal<SimpleDateFormat>();
    private static final List<ThreadLocal<SimpleDateFormat>> YEAR4_FORMATS = empty();
    private static final List<ThreadLocal<SimpleDateFormat>> YEAR2_FORMATS = empty();
    static {
        YEAR4_FORMATS.add(YYYYMMDDHHMMSSMMM);
        YEAR4_FORMATS.add(YYYYMMDDHHMMSS);
        YEAR4_FORMATS.add(YYYYMMDD);
        YEAR4_FORMATS.add(YYYYMMDD_NO_SEPARATOR);
        YEAR4_FORMATS.add(YYYYMM);

        YEAR2_FORMATS.add(EXCEL_TIME);
        YEAR2_FORMATS.add(EXCEL);
        YEAR2_FORMATS.add(DDMMMYY);
    }
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSS_OUT = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSSMMM_OUT = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> HHMMSS_OUT = new ThreadLocal<SimpleDateFormat>();
    private static final ThreadLocal<SimpleDateFormat> XML_FORMAT_OUT = new ThreadLocal<SimpleDateFormat>();
    
    public static final Date EPOCH = new Date(0);
    
    public static Date date(String value) {
        if(value.equals("today")) return midnight(now());
        if(value.equals("now")) return now();
        if(value.equals("yesterday")) return midnight(yesterday());
        if(value.equals("BIZ_TWO_DAYS_AGO")) return businessDaysAgo(1, businessDaysAgo(1, midnight(), "nyb"), "nyb");
        if(value.equals("BIZ_YESTERDAY")) return businessDaysAgo(1, midnight(), "nyb");
        if(value.equals("yesterdayNYB")) return midnight(businessDaysAgo(1, now(), "nyb"));
        if(value.equals("today4pm")) return midnight(hoursAgo(16, now()));
        String normalized = normalize(value);
        List<ThreadLocal<SimpleDateFormat>> formats;
        if (normalized.matches("^\\d\\d\\d\\d.*")) formats = YEAR4_FORMATS;
        else formats = YEAR2_FORMATS;
        for (ThreadLocal<SimpleDateFormat> format : formats) {
            Date result = parseSafe(format, value);
            if (result != null) return result;
        }
        throw bomb("could not find any format to parse " + sQuote(value));
    }
    
    public static Date date(Calendar calendar) {
        return calendar.getTime();
    }

    public static String yyyyMmDd(Date value) {
        initFormats();
        return YYYYMMDD_OUT.get().format(value);
    }
    
    public static String sqlDate(Date value) {
        initFormats();
        return SQL_DATE_OUT.get().format(value);
    }

    private static void initFormats() {
        if (YYYYMMDD.get() != null) return;
        YYYYMMDD.set(new SimpleDateFormat("yyyy/MM/dd"));     
        YYYYMMDD_NO_SEPARATOR.set(new SimpleDateFormat("yyyyMMdd"));     
        { EXCEL.set(new SimpleDateFormat("M/dd/yy")); 
        EXCEL.get().set2DigitYearStart(yyyyMmDd("1930/01/01")); } // do not move this block before ymd is available
        { DDMMMYY.set(new SimpleDateFormat("dd/MMM/yy")); 
        DDMMMYY.get().set2DigitYearStart(yyyyMmDd("1930/01/01")); } // do not move this block before ymd is available
        EXCEL_TIME.set(new SimpleDateFormat("M/dd/yy/hh/mm/ss"));     
        YYYYMMDDHHMMSS.set(new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss"));
        YYYYMMDDHHMMSSMMM.set(new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SSS"));
        YYYYMM.set(new SimpleDateFormat("yyyyMM"));
        YYYYMMDD_OUT.set(new SimpleDateFormat("yyyy/MM/dd"));     
        YYYYMMDDHHMMSS_OUT.set(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
        YYYYMMDDHHMMSSMMM_OUT.set(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"));
        HHMMSS_OUT.set(new SimpleDateFormat("HH:mm:ss"));
        SQL_DATE_OUT.set(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0"));
        MMYYYY_OUT.set(new SimpleDateFormat("MM/yyyy"));
        XML_FORMAT_OUT.set(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        for (ThreadLocal<SimpleDateFormat> format : YEAR4_FORMATS) 
            format.get().setLenient(false);
    }

    public static Date yyyyMmDd(String value) {
        return parseUsing(YYYYMMDD, value);
    }
    
    public static Date yyyyMmDd(Double value) {
        String digits = Strings.nDecimals(0, value);
        return parseUsing(YYYYMMDD_NO_SEPARATOR, digits);
    }

    public static long asLong(Date d) {
        initFormats();
        return parseLong(YYYYMMDD_NO_SEPARATOR.get().format(d));
    }
    
    private static Date parseUsing(ThreadLocal<SimpleDateFormat> format, String value) {
        initFormats();
        Date result = parseSafe(format, value);
        if (result != null) return result;
        throw bomb("could not parse " + value + " as a date with " + format.get().toPattern());
    }
    
    private static Date parseSafe(ThreadLocal<SimpleDateFormat> format, String value) {
        initFormats();
        try {
            return format.get().parse(normalize(value));
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date midnight(Date time) {
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.set(HOUR_OF_DAY, 0);
        c.set(MINUTE, 0);
        c.set(SECOND, 0);
        c.set(MILLISECOND, 0);
        return c.getTime();
    }
    
    static String normalize(String value) {
        return value.replaceAll("\\W+", "/");
    }
    
    public static String ymdHuman(Date start) {
        if (start == null) return "";
        if (start instanceof java.sql.Timestamp) start = new Date(start.getTime());
        return start.equals(midnight(start)) 
            ? yyyyMmDd(start)
            : yyyyMmDdHhMmSs(start);
    }
    
    public static String yyyyMmDdHhMmSs(Date value) {
        initFormats();
        return YYYYMMDDHHMMSS_OUT.get().format(value);
    }
    
    public static String yyyyMmDdHhMmSsNoSeparator(Date value) {
        return yyyyMmDdHhMmSs(value).replaceAll("\\D+", "");
    }
    
    public static String yyyyMmDdHhMmSsMmm(Date value) {
        initFormats();
        return YYYYMMDDHHMMSSMMM_OUT.get().format(value);
    }
    
    public static String mmYyyy(Date value) {
        initFormats();
        return MMYYYY_OUT.get().format(value);
    }
    
    public static Date yyyyMmDdHhMmSs(String value) {
        return parseUsing(YYYYMMDDHHMMSS, value);
    }
    
    public static Date yyyyMmDdHhMmSsMmm(String value) {
        return parseUsing(YYYYMMDDHHMMSSMMM, value);
    }
    
    public static String hhMmSs(Date d) {
        initFormats();
        return HHMMSS_OUT.get().format(d);
    }
    
    public static YearMonth yearMonth(Date d) {
        return new YearMonth(d);
    }

    public static YearMonth yearMonth(String s) {
        return new YearMonth(s);
    }
    
    public static Date yesterday() {
        return daysAgo(1, now());
    }

    public static Date tomorrow() {
        return daysAhead(1, now());
    }
    
    public static Date secondsAhead(int ahead, Date d) {
        return ago(SECOND, -ahead, d);
    }
    
    public static Date secondsAgo(int ago, Date d) {
        return ago(SECOND, ago, d);
    }
    
    public static Date minutesAhead(int ahead, Date d) {
        return ago(MINUTE, -ahead, d);
    }
    
    public static Date minutesAgo(int ago, Date d) {
        return ago(MINUTE, ago, d);
    }
    
    public static Date hoursAhead(int ahead, Date d) {
        return ago(HOUR, -ahead, d);
    }
    
    public static Date hoursAgo(int ago, Date d) {
        return ago(HOUR, ago, d);
    }
    
    public static Date monthsAhead(int ahead, Date d) {
        return ago(MONTH, -ahead, d);
    }
    
    public static Date monthsAgo(int ago, Date d) {
        return ago(MONTH, ago, d);
    }
    
    public static Date daysAhead(int ahead, Date d) {
        return daysAgo(-ahead, d);
    }

    public static Date daysAgo(int ago, Date d) {
        return ago(DATE, ago, d);
    }
    
    private static Date ago(int unit, int ago, Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(unit, -ago);
        return c.getTime();
    }

    public static Date now() {
        return nowFrozen() ? new Date(FROZEN) : reallyNow();
    }

    public static Date reallyNow() {
        return new Date();
    }

    public static boolean nowFrozen() {
        return FROZEN != null;
    }
    
    public static Date thirdWedLessTwo(YearMonth ym, String financialCenter) {
        Date first = ym.first();
        double fincadStartDate = asFincadMidnight(first);
        double fincadResultDate = theFincad(aaDateAdjust_java(
            fincadStartDate, 1, ADJUST_MONDAY, ADJUST_THIRD_WED_LESS_TWO, fincadHolidays(financialCenter)
        ));
        return asJavaDate(fincadResultDate);
    }
    
    public static Date thirdFriday(YearMonth ym) {
        Date d = ym.first();
        while(dayOfWeek(d) != FRIDAY) d = daysAhead(1, d);
        return daysAhead(14, d);
    }

    public static Date fridayBeforeThirdWed(YearMonth ym, String financialCenter) {
        Date first = ym.first();
        double fincadStartDate = asFincadMidnight(first);
        double thirdWed = theFincad(aaDateAdjust_java(
            fincadStartDate, 1, ADJUST_MONDAY, ADJUST_THIRD_WED, new double[0][0]
        ));
        double fincadResultDate = theFincad(aaDateAdjust_java(
            thirdWed, -3, ADJUST_MARKET_DAYS, ADJUST_PREV_GOOD_BUSINESS_DAY, fincadHolidays(financialCenter)
        ));
        return asJavaDate(fincadResultDate);
    }
    
    private static final int ADJUST_MARKET_DAYS = 1;
    private static final int ADJUST_MONDAY = 8;
    private static final int ADJUST_PREV_GOOD_BUSINESS_DAY = 3;
    private static final int ADJUST_NEXT_GOOD_BUSINESS_DAY = 2;
    private static final int ADJUST_THIRD_WED_LESS_TWO = 8;
    private static final int ADJUST_THIRD_WED = 10;
    private static final int ADJUST_NO_DATE_ADJUSTMENT = 1;
    private static Long FROZEN = null;
    public static Date businessDaysAgo(int days, Date startDate, String center) {
        return businessDaysAhead(-days, startDate, center);
    }

    public static Date nextBusinessDay(Date startDate, String center) {
        return businessDaysAhead(1, startDate, center);
    }

    public static Date nextBusinessDay(Date startDate) {
        double fincadStartDate = asFincadMidnight(startDate);
        int adjustment = ADJUST_NEXT_GOOD_BUSINESS_DAY;
        double fincadResultDate = theFincad(aaDateAdjust_java(
            fincadStartDate, 1, ADJUST_MARKET_DAYS, adjustment, new double[0][0]
        ));
        return asJavaDate(fincadResultDate);
    }
    
    public static Date businessDaysAhead(int days, Date startDate, String center) {
        bombIf(days == 0, "businessDaysAhead does not make sense for zero day change.");
        double fincadStartDate = asFincadMidnight(startDate);
        int adjustment = days < 0 ? ADJUST_PREV_GOOD_BUSINESS_DAY : ADJUST_NEXT_GOOD_BUSINESS_DAY;
        double fincadResultDate = theFincad(aaDateAdjust_java(
            fincadStartDate, days, ADJUST_MARKET_DAYS, adjustment, fincadHolidays(center)
        ));
        return asJavaDate(fincadResultDate);
    }
    
    public static int businessDaysBetween(Date startDate, Date endDate, String center) {
        bombIf(startDate.after(endDate), "startDate is after endDate " + paren(commaSep(ymdHuman(startDate), ymdHuman(endDate))));
        double fincadStartDate = asFincadMidnight(startDate);
        double fincadEndDate = asFincadMidnight(endDate);
        return (int) theFincad(aaDateCount_java(
            fincadStartDate, fincadEndDate, ADJUST_MARKET_DAYS, ADJUST_NO_DATE_ADJUSTMENT, fincadHolidays(center)));
    }

    static class Holidays { double[][] data; }
    static Map<String, Holidays> fincadHolidaysDoNotReference = emptyMap();
    private static synchronized double[][] fincadHolidays(String center) {
        if (!fincadHolidaysDoNotReference.containsKey(center)) {
            Observations observations = holidayObservations(allTime(), center);
            Holidays holidays = new Holidays();
            holidays.data = new double[observations.size()][1];
            List<Date> holidayDates = observations.times();
            for(int i = 0; i < observations.size(); i++) {
                holidays.data[i][0] = asFincadMidnight(holidayDates.get(i));
            }
            fincadHolidaysDoNotReference.put(center, holidays);
        }
        return bombNull(fincadHolidaysDoNotReference.get(center), "no holidays for " + center).data;
    }
    
    private static Observations holidayObservations(Range range, String center) {
        return observations(FINANCIAL_CALENDAR, range, values(
            INSTRUMENT.value("holiday"),
            FINANCIAL_CENTER.value(center)
        ));
    }

    static Date asJavaDate(double fincadResultDate) {
        double year = theFincad(aaYear_java(fincadResultDate));
        double month = theFincad(aaMonth_java(fincadResultDate));
        double day = theFincad(aaDay_java(fincadResultDate));
        return date(integer(year), integer(month), integer(day));
    }

    public static Date date(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(YEAR, year);
        c.set(MONTH, month - 1);
        c.set(DAY_OF_MONTH, day);
        return midnight(c.getTime());
    }

    static double asFincadMidnight(Date startDate) {
        Calendar start = calendar(startDate);
        double fincadStartDate = theFincad(aaDateSerial_java(
            start.get(YEAR), 
            start.get(MONTH) + 1, 
            start.get(DAY_OF_MONTH)
        ));
        return fincadStartDate;
    }

    private static double theFincad(double[] result) {
        switch(integer(result[0])) {
            case 1: return result[1]; 
            case 16: throw bomb("fincad error: " + result[1] + " msg: " + getErrorString(integer(result[1])));
            case 64: throw bomb("array returned from fincad for expected single result");
            default: throw bomb("unknown error cond " + result[0] + " from fincad");
        }
    }

    private static int integer(double value) {
        return new Double(value).intValue();
    }

    public static Calendar calendar(Date start) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        return c;
    }

    public static java.sql.Timestamp timestamp(Date start) {
        return new java.sql.Timestamp(start.getTime());
    }
    
    public static void freezeNow() {
        freezeNow(now());
    }

    public static void freezeNow(Date d) {
        FROZEN = d.getTime();
    }
    
    public static void freezeNow(String date) {
        freezeNow(date(date));
    }
    
    public static void thawNow() { 
        FROZEN = null;
    }

    public static Date setHour(Date date, int hour) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR, hour);
        date = c.getTime();
        return date;
    }

    static Map<String, Map<Date, Boolean>> holidayCache = emptyMap();
    public static boolean isHoliday(Date date, String center) {
        if(!holidayCache.containsKey(center))
            holidayCache.put(center, new HashMap<Date, Boolean>());
        Map<Date, Boolean> centerCache = holidayCache.get(center);
        if(!centerCache.containsKey(date)) {
            Range r = range(monthsAgo(12, date), monthsAhead(12, date));
            Observations observations = holidayObservations(r, center);
            for(Date d : r) 
                centerCache.put(d, observations.has(d));
        } 
        return centerCache.get(date);
    }

    public static String twoDigitYear(Date asOf) {
        return leftZeroPad(year(asOf) % 100, 2);
    }

    public static int year(Date asOf) {
        return calendar(asOf).get(YEAR);
    }

    public static Date todayAt(String at) {
        return timeOn(at, midnight());
    }

    public static Date timeOn(String at, Date date) {
        bombUnless(midnight(date).equals(date), "can only call timeOn a date (midnight)");
        bombUnless(at.matches("\\d\\d:\\d\\d:\\d\\d"), "time " + sQuote(at) + " is not in correct format: hh:mm:ss");
        String hour = at.substring(0, 2);
        String minute = at.substring(3, 5);
        String second = at.substring(6, 8);
        bombUnless(parseInt(hour) < 24, "hour " + hour + " must be < 24 in " + sQuote(at));
        bombUnless(parseInt(minute) < 60, "minute " + minute + " must be < 60 in " + sQuote(at));
        bombUnless(parseInt(second) < 60, "second " + second + " must be < 60 in " + sQuote(at));
        return date(yyyyMmDd(date) + " " + at);
    }

    public static Date laterOf(Date a, Date b) { 
        if (a == null) return b;
        if (b == null) return a;
        return a.after(b) ? a : b;
    }
    
    public static boolean isBeforeNow(Date asOf) {
        return now().before(asOf);
    }

    public static boolean isAfterNow(Date asOf) {
        return now().after(asOf);
    }

    public static boolean hasTodayValue(Date notified) {
        return notified != null && notified.after(midnight(now()));
    }

    public static int dayOfWeek(Date asOf) {
        return calendar(asOf).get(DAY_OF_WEEK);
    }

    public static Date midnight() {
        return midnight(now());
    }
    
    public static Date midnightGMT() {
        Calendar calendar = calendar(now());
        long millis = now().getTime();
        int offset = calendar.getTimeZone().getOffset(millis);
        return midnight(new Date(millis - offset));
    }
    
    public static Date dateMaybe(String s) {
        return isEmpty(s) ? null : date(s);
    }

    public static Range dateRange(String start, String end) {
        return range(dateMaybe(start), dateMaybe(end));
    }

    public static int monthNumber(Date d) {
        return monthIndex(d) + 1;
    }

    public static int monthIndex(Date d) {
        return calendar(d).get(MONTH);
    }

    public static String xmlTimestamp(Date d) {
        initFormats();
        return XML_FORMAT_OUT.get().format(d);
    }

    public static Date millisAhead(long millis, Date date) {
        return millisAgo(-millis, date);
    }

    public static Date millisAgo(long millis, Date date) {
        return new Date(date.getTime() - millis);
    }
    
    public static int secondsBetween(Date early, Date late) {
        long millisBetween = late.getTime() - early.getTime();
        return (int) (millisBetween/1000);
    }

}
