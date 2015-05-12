package com.fftw.metadb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Utilities for manipulating strings and messages.
 */
public class TextUtil {

    // This is 1-24 hour
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    // This is 0-23 hour
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private TextUtil() {
        // prevent
    }

    /**
     * Given a string parse it into a key/value pair using the supplied seperator and map record.
     *
     * @param rawString
     * @param sep
     * @param record
     * @return
     */
    public static Map<String, String> extractRecord(String rawString, String sep, Map<String, String> record) {

        String[] parts = rawString.split(sep);

        for (String token : parts) {
            // a token is a key/value pair separated by '='
            // Take each token and split into the key/value
            String[] keyValue = token.split("=");
            if (!record.containsKey(keyValue[0])) {
                record.put(keyValue[0], keyValue[1]);
            } else {
                record.put(keyValue[0], keyValue[1]);
            }
        }

        return record;
    }

    /**
     * Given a string parse it into a key/value pair using the supplied seperator.
     *
     * @param rawString
     * @param sep
     * @return
     */
    public static Map<String, String> extractRecord(String rawString, String sep) {
        return extractRecord(rawString, sep, new HashMap<String, String>());
    }

    /**
     * Given a string parse it into key/value pairs using the default pair seperator '|'.
     *
     * @param rawString
     * @return
     */
    public static Map<String, String> extractRecord(String rawString) {
        return extractRecord(rawString, "\\|");
    }

    /**
     * Create a string with the data where the key/value pairs are seperated by the
     * specified character
     * <p/>
     * <tt>key1=value1<sep>key2|value2<tt>
     * <p/>
     * The order of the items in the string depend on the iteration order of the supplied
     * <code>Map</code>.
     *
     * @param data
     * @param sep
     * @return
     */
    public static String createRecord(Map<String, String> data, String sep) {
        StringBuilder sb = new StringBuilder(data.size() * 30);

        for (String key : data.keySet()) {
            String value = data.get(key);
            sb.append(key).append("=").append(value);
            sb.append(sep);
        }

        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Create a string with the data where the key/value pairs are seperated by the
     * default character '|'
     * <p/>
     * <tt>key1=value1|key2|value2<tt>
     * <p/>
     * The order of the items in the string depend on the iteration order of the supplied
     * <code>Map</code>.
     *
     * @param data
     * @return
     */
    public static String createRecord(Map<String, String> data) {
        return createRecord(data, "\\|");
    }

    /**
     * Format a date in using "yyyy/MM/dd kk:mm:ss"
     *
     * @param date
     * @return
     */
    public synchronized static String formatDate(Date date) {
        return sdf.format(date);
    }

    public static String formatDate() {
        return formatDate(new Date());
    }

    /**
     * Compare two maps
     *
     * @param one
     * @param two
     * @return
     */
    public static boolean compareMaps(Map<?, ?> one, Map<?, ?> two) {
        if (one.size() != two.size()) {
            return false;
        }

        for (Object key1 : one.keySet()) {
            if (two.containsKey(key1)) {
                Object value1 = one.get(key1);
                Object value2 = two.get(key1);
                if (!value1.equals(value2)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
