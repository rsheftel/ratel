package com.fftw.util.strings;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Currency;

import com.fftw.bloomberg.types.BBSide;

/**
 *
 */
public class FixedWidthExtractor {

    private static final String BB_DATE = "yyyyMMdd";

    /**
     * This helps in translating a BB file spec into Java code
     *
     * @param sourceStr
     * @param start
     * @param end
     * @return
     */
    public static String extractString(String sourceStr, int start, int end) {
        return extractStringNoTrim(sourceStr, start, end).trim();
    }

    /**
     * @param sourceStr
     * @param start
     * @param end
     * @return
     */
    public static String extractStringNoTrim(String sourceStr, int start, int end) {
        return sourceStr.substring(start - 1, end);
    }

    /**
     * This helps in translating a BB file spec into Java code
     *
     * @param sourceStr
     * @param position
     * @return
     */
    public static char extractChar(String sourceStr, int position) {
        return extractString(sourceStr, position, position).charAt(0);
    }

    /**
     * For the times when BB has a character value in a string field
     *
     * @param sourceStr
     * @param start
     * @param end
     * @return
     */
    public static char extractChar(String sourceStr, int start, int end) {
        return extractString(sourceStr, start, end).charAt(0);
    }

    public static int extractInt(String sourceStr, int start, int end) {
        return Integer.parseInt(extractString(sourceStr, start, end));
    }

    public static Integer extractInteger(String sourceStr, int start, int end, Integer defaultValue) {
        String intStr = extractString(sourceStr, start, end);

        if (intStr.length() < 1) {
            return defaultValue;
        }

        return Integer.parseInt(intStr);
    }

    /**
     * @param sourceStr
     * @param start
     * @param end
     * @return
     */
    public static long extractLong(String sourceStr, int start, int end) {
        return Long.parseLong(extractString(sourceStr, start, end));
    }

    public static Long extractLong(String sourceStr, int start, int end, Long defaultValue) {
        String intStr = extractString(sourceStr, start, end);

        if (intStr.length() < 1) {
            return defaultValue;
        }

        return Long.parseLong(intStr);
    }

    public static BigDecimal extractBigDecimal(String sourceStr, int start, int end, BigDecimal defaultValue) {
        String intStr = extractString(sourceStr, start, end);

        if (intStr.length() < 1) {
            return defaultValue;
        }

        return new BigDecimal(intStr);
    }

    public static float extractFloat(String sourceStr, int start, int end) {
        return Float.parseFloat(extractString(sourceStr, start, end));
    }

    public static LocalDate extractDate(String sourceStr, int start, int end) {
        String dateStr = extractString(sourceStr, start, end);

        DateTimeFormatter fmt = DateTimeFormat.forPattern(BB_DATE);
        return fmt.parseDateTime(dateStr).toLocalDate();
    }

    public static LocalDate extractDate(String sourceStr, int start, int end, LocalDate defaultValue) {
        String dateStr = extractString(sourceStr, start, end);

        // We could check for < 1, but we cannot do anything with something less
        // than 8 - really
        if (dateStr.length() < 8) {
            return defaultValue;
        }

        DateTimeFormatter fmt = DateTimeFormat.forPattern(BB_DATE);
        return fmt.parseDateTime(dateStr).toLocalDate();
    }

    public static Currency extractCurrency(String sourceStr, int start, int end, Currency defaultValue) {
        String currencyStr = extractString(sourceStr, start, end);

        if (currencyStr.length() < 1) {
            return defaultValue;
        }

        return Currency.getInstance(currencyStr.toUpperCase());
    }

    public static BBSide extractSide(String sourceStr, int start, int end, BBSide defaultValue) {
        String sideStr = extractString(sourceStr, start, end);

        if (sideStr.length() < 1) {
            return defaultValue;
        }

        return BBSide.valueOf(sideStr.charAt(0));
    }

}
