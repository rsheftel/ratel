package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utilities for manipulating strings and messages.
 */
public class MessageUtil {

    private static final String JMS_TOPIC_KEY = "MSTopicName";
    private static final String JMS_TIMESTAMP_KEY = "MSTimestamp";

    // This is 1-24 hour
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    // This is 0-23 hour
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    //yyyy-MM-dd'T'HH:mm:ss.SSSZ
    private static DateTimeFormatter recordDateFormat;
    //private static final SimpleDateFormat recordDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS ZZ");

    private MessageUtil() {
        // prevent
    }

    static {
        recordDateFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss.SSS ZZ");
    }

    /**
     * Given a string parse it into a key/value pair using the supplied separator and map record.
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
            record.put(keyValue[0], keyValue[1]);
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

    
    public static Map<String, String> extractRecord(String rawString, Map<String,String> map) {
        return extractRecord(rawString, "\\|", map);
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

    public static String getTopicName(Map<String, String> record) {
        return record.get(JMS_TOPIC_KEY);
    }


    public static String getPublishTimestampString(Map<String, String> record) {
        return record.get(JMS_TIMESTAMP_KEY);
    }

    public static Date getPublishTimestamp(Map<String, String> record) {
        String timestamp = getPublishTimestampString(record);

        if (timestamp == null) {
            throw new NullPointerException("No publish timestamp found on record");
        }

        try {
            return recordDateFormat.parseDateTime(timestamp).toDate();
        } catch (IllegalArgumentException e) {
            return new Date();
        }
    }


    public static void setTopicName(String topic, Map<String, String> record) {
        record.put(JMS_TOPIC_KEY, topic);
    }

    static void setPublishTimestamp(String timestamp, Map<String, String> record) {
        record.put(JMS_TIMESTAMP_KEY, timestamp);
    }

    public static void setPublishTimestamp(Date timestamp, Map<String, String> record) {

        String timestampStr = null;

        synchronized (recordDateFormat) {
            timestampStr = recordDateFormat.print(new DateTime(timestamp));
        }

        setPublishTimestamp(timestampStr, record);
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

        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            sb.append(sep);
        }

        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Create a string with the data where the key/value pairs are separated by the
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
        return createRecord(data, "|");
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

        for (Map.Entry<?, ?> entry : one.entrySet()) {
            if (two.containsKey(entry.getKey())) {
                Object value1 = entry.getValue();
                Object value2 = two.get(entry.getKey());
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
