package malbec.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for manipulating strings and messages.
 */
public class MessageUtil {

    // Message types
    private static final String QUERY_ORDER = "QueryOrder";
    private static final String NEW_ORDER = "NewOrder";
    private static final String NEW_SPREAD_TRADE = "NewSpreadTrade";
    private static final String CANCEL_ORDER = "CancelOrder";
    private static final String REPLACE_ORDER = "ReplaceOrder";
    private static final String RELEASE_STAGED_ORDER = "ReleaseStagedOrder";
    private static final String FERRET_MODE = "FerretMode";

    // Field keys
    private static final String FERRET_STATE = "FERRETSTATE";
    private static final String QUERY_ORDER_RESPONSE = "QueryOrderResponse";
    private static final String JMS_TOPIC_KEY = "MSTopicName";
    private static final String JMS_TIMESTAMP_KEY = "MSTimestamp";
    private static final String PLATFORM = "PLATFORM";
    private static final String JMS_MESSAGE_ID = "JmsMessageID";
    private static final String JMS_ORIGINAL_MESSAGE_ID = "JmsOriginalMessageID";
    private static final String REPLY_TO = "ReplyTo";
    private static final String MESSAGE_TYPE = "MESSAGETYPE";
    private static final String CLIENT_USER_ID = "CLIENTUSERID";
    private static final String USER_ORDER_ID = "USERORDERID";
    private static final String ORIGINAL_USER_ORDER_ID = "ORIGINALUSERORDERID";
    private static final String CLIENT_HOSTNAME = "CLIENTHOSTNAME";
    private static final String CLIENT_APPNAME = "CLIENTAPPNAME";
    private static final String CLIENT_ORDER_ID = "CLIENTORDERID";
    private static final String ORDER_DATE = "ORDERDATE";
    private static final String ORIGINAL_CLIENT_ORDER_ID = "ORIGINALCLIENTORDERID";
    private static final String PAIR_ID = "PAIRID";
    private static final String STATUS = "STATUS";
    private static final String DESTINATION = "DESTINATION";

    // private static final String CORRELATION_CLIENT_ID = "CorrelationClientID";

    static final Logger staticLog = LoggerFactory.getLogger(MessageUtil.class);

    // This is 1-24 hour
    // private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    // This is 0-23 hour
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // yyyy-MM-dd'T'HH:mm:ss.SSSZ
    private static DateTimeFormatter recordDateFormat = DateTimeFormat
        .forPattern("yyyy/MM/dd HH:mm:ss.SSS ZZ");

    // private static final SimpleDateFormat recordDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS
    // ZZ");

    private MessageUtil() {
    // prevent
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
            if (keyValue != null && keyValue.length > 1) {
                record.put(keyValue[0].toUpperCase(), keyValue[1]);
            } else if (keyValue != null && keyValue.length == 1) {
                record.put(keyValue[0].toUpperCase(), "");
            } else {
                staticLog.error("Unable to parse record: " + rawString + "\n\t" + token);
            }
        }

        return record;
    }

    /**
     * Given a string parse it into a key/value pair using the supplied separator.
     * 
     * @param rawString
     * @param sep
     * @return
     */
    public static Map<String, String> extractRecord(String rawString, String sep) {
        return extractRecord(rawString, sep, new HashMap<String, String>());
    }

    public static Map<String, String> extractRecord(String rawString, Map<String, String> map) {
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
            // throw new NullPointerException("No publish timestamp found on record");
            return null;
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
        setPublishTimestamp(timestamp.getTime(), record);
    }

    public static void setPublishTimestamp(long timestamp, Map<String, String> record) {
        String timestampStr = null;

        synchronized (recordDateFormat) {
            timestampStr = recordDateFormat.print(new DateTime(timestamp));
        }
        setPublishTimestamp(timestampStr, record);
    }

    public static void setPlatform(String platform, Map<String, String> record) {
        record.put(PLATFORM, platform);
    }

    public static String getPlatform(Map<String, String> record) {
        return record.get(PLATFORM);
    }

    public static void setJmsMessageID(String jmsMessageID, Map<String, String> record) {
        record.put(JMS_MESSAGE_ID, jmsMessageID);
    }

    public static String getJmsMessageID(Map<String, String> record) {
        return record.get(JMS_MESSAGE_ID);
    }

    public static void setClientUserId(String clientUserId, Map<String, String> record) {
        record.put(CLIENT_USER_ID, clientUserId);
    }

    public static String getClientUserId(Map<String, String> record) {
        return record.get(CLIENT_USER_ID);
    }

    public static String getClientOrderId(Map<String, String> record) {
        return record.get(CLIENT_ORDER_ID);
    }

    public static String getPairId(Map<String, String> record) {
        return record.get(PAIR_ID);
    }

    public static void setOriginalMessageID(String messageID, Map<String, String> record) {
        record.put(JMS_ORIGINAL_MESSAGE_ID, messageID);
    }

    public static String getOriginalMessageID(Map<String, String> record) {
        return record.get(JMS_ORIGINAL_MESSAGE_ID);
    }

    public static void setReplyTo(String replyTo, Map<String, String> record) {
        record.put(REPLY_TO, replyTo);
    }

    public static String getReplyTo(Map<String, String> record) {
        return record.get(REPLY_TO);
    }

    public static void setNewOrder(Map<String, String> message) {
        message.put(MESSAGE_TYPE, NEW_ORDER);
    }

    public static boolean isSpreadTrade(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (NEW_SPREAD_TRADE.equalsIgnoreCase(messageType));
    }

    public static boolean isNewOrder(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (NEW_ORDER.equalsIgnoreCase(messageType));
    }

    public static boolean isOrderQuery(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (QUERY_ORDER.equalsIgnoreCase(messageType));
    }

    public static boolean isCancelOrder(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (CANCEL_ORDER.equalsIgnoreCase(messageType));
    }

    public static boolean isReplaceOrder(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (REPLACE_ORDER.equalsIgnoreCase(messageType));
    }

    public static void setReleaseStagedOrder(Map<String, String> message) {
        message.put(MESSAGE_TYPE, RELEASE_STAGED_ORDER);
    }

    public static boolean isReleaseStagedOrder(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (RELEASE_STAGED_ORDER.equalsIgnoreCase(messageType));
    }

    
    public static void setFerretMode(Map<String, String> message, String newState) {
        message.put(MESSAGE_TYPE, FERRET_MODE);
        message.put(FERRET_STATE, newState);
    }
    
    public static boolean isFerretMode(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (FERRET_MODE.equalsIgnoreCase(messageType));
    }
    
    public static void setOrderQuery(Map<String, String> message) {
        message.put(MESSAGE_TYPE, QUERY_ORDER);
    }

    public static void setOrderQueryResponse(Map<String, String> message) {
        message.put(MESSAGE_TYPE, QUERY_ORDER_RESPONSE);
    }

    public static boolean isOrderQueryResponse(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);
        return (QUERY_ORDER_RESPONSE.equalsIgnoreCase(messageType));
    }

    /**
     * Create a string with the data where the key/value pairs are separated by the specified character
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
     * Create a string with the data where the key/value pairs are separated by the default character '|'
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

    public static String getMessageType(Map<String, String> message) {
        String messageType = message.get(MESSAGE_TYPE);

        if (messageType != null) {
            return messageType.toUpperCase();
        }

        return null;
    }

    public static void setNewSpreadTrade(Map<String, String> message) {
        message.put(MESSAGE_TYPE, NEW_SPREAD_TRADE);
    }

    public static void setCancelOrder(Map<String, String> message) {
        message.put(MESSAGE_TYPE, CANCEL_ORDER);
    }

    public static void setCancelReplaceOrder(Map<String, String> message) {
        message.put(MESSAGE_TYPE, REPLACE_ORDER);
    }

    public static void setClientOrderId(Map<String, String> message, String clientOrderId) {
        message.put(CLIENT_ORDER_ID, clientOrderId);
    }

    public static String getStatus(Map<String, String> message) {
        return message.get(STATUS);
    }

    public static String getClientHostname(Map<String, String> message) {
        return message.get(CLIENT_HOSTNAME);
    }

    public static void setClientHostname(String clientHostname, Map<String, String> crMap) {
        crMap.put(CLIENT_HOSTNAME, clientHostname);
    }

    public static String getOriginalClientOrderId(Map<String, String> message) {
        return message.get(ORIGINAL_CLIENT_ORDER_ID);
    }

    public static void setOriginalClientOrderId(Map<String, String> message, String originalClientOrderId) {
        message.put(ORIGINAL_CLIENT_ORDER_ID, originalClientOrderId);
    }

    public static void setClientAppName(String clientAppName, Map<String, String> crMap) {
        crMap.put(CLIENT_APPNAME, clientAppName);
    }

    public static String getClientAppName(Map<String, String> crMap) {
        return crMap.get(CLIENT_APPNAME);
    }

    public static void setStatus(Map<String, String> message, String status) {
        message.put(STATUS, status);
    }

    public static void setUserOrderId(Map<String, String> map, String userOrderId) {
        map.put(USER_ORDER_ID, userOrderId);
    }

    public static String getUserOrderId(Map<String, String> crMap) {
        return crMap.get(USER_ORDER_ID);
    }

    public static void setOriginalUserOrderId(Map<String, String> map, String originalUserOrderId) {
        map.put(ORIGINAL_USER_ORDER_ID, originalUserOrderId);
    }

    public static String getOriginalUserOrderId(Map<String, String> map) {
        return map.get(ORIGINAL_USER_ORDER_ID);
    }

    public static String getOrderDate(Map<String, String> message) {
        return message.get(ORDER_DATE);
    }

    public static void setOrderDate(Map<String, String> message, String orderDate) {
        message.put(ORDER_DATE, orderDate);
    }

    public static void setOrderDate(Map<String, String> message, LocalDate orderDate) {
        setOrderDate(message, orderDate.toString("YYYY-MM-dd"));
    }

    public static String getDestination(Map<String, String> message) {
        return message.get(DESTINATION);
    }

    public static void setDestination(Map<String, String> message, String destination) {
        message.put(DESTINATION, destination);
    }



}
