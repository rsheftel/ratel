package malbec.fer.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.fer.Order;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.TimeInForce;

/**
 * Contain the data and logic to validate an order.
 * 
 */
public class OrderValidation {
    private static final int CLIENT_ORDER_ID_MAX_LENGTH = 16;
    private static final int USER_ORDER_ID_MAX_LENGTH = 6;
    final private static Map<String, TimeInForce> TIF_MAP = new HashMap<String, TimeInForce>();
    final private static Map<String, OrdType> ORDER_TYPE_MAP = new HashMap<String, OrdType>();
    final private static Map<String, Side> SIDE_MAP = new HashMap<String, Side>();

    final private static Logger log = LoggerFactory.getLogger(OrderValidation.class);

    private OrderValidation() {
    // this is a static member only class
    }

    static {
        initializeTif();
        initializeOrderType();
        initializeSide();
    }

    private static void initializeTif() {
        TIF_MAP.put("DAY", new TimeInForce(TimeInForce.DAY));
        TIF_MAP.put("OPEN", new TimeInForce(TimeInForce.AT_THE_OPENING));
        TIF_MAP.put("IOC", new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL));
        TIF_MAP.put("FOK", new TimeInForce(TimeInForce.FILL_OR_KILL));
        TIF_MAP.put("CLOSE", new TimeInForce(TimeInForce.AT_THE_CLOSE));
    }

    private static void initializeOrderType() {
        ORDER_TYPE_MAP.put("LIMIT", new OrdType(OrdType.LIMIT));
        ORDER_TYPE_MAP.put("STOP", new OrdType(OrdType.STOP));
        ORDER_TYPE_MAP.put("STOPLIMIT", new OrdType(OrdType.STOP_LIMIT));
        ORDER_TYPE_MAP.put("MARKET", new OrdType(OrdType.MARKET)); // only valid with TIF of CLOSE
//        ORDER_TYPE_MAP.put("MOC", new OrdType(OrdType.MARKET_ON_CLOSE));
    }

    private static void initializeSide() {
        SIDE_MAP.put("BUY", new Side(Side.BUY));
        SIDE_MAP.put("SELL", new Side(Side.SELL));
        SIDE_MAP.put("SELLSHORT", new Side(Side.SELL_SHORT));
        // SIDE_MAP.put("BUYCOVER", new Side(Side.????));
    }

    public static void checkIfMarketOrder(Message fixMessage, List<String> conversionErrors) {
        try {
            char orderType = fixMessage.getChar(OrdType.FIELD);
            char tif = fixMessage.getChar(TimeInForce.FIELD);
            
            if (OrdType.MARKET == orderType && tif != TimeInForce.AT_THE_CLOSE) {
                conversionErrors.add("Market order only valid at the close");
            }
        } catch (FieldNotFound e) {
            // TODO Auto-generated catch block
            log.error("Generated log", e);
        }
    }

    public static OrdType convertOrderType(String orderType) {
        return ORDER_TYPE_MAP.get(orderType.trim().toUpperCase());
    }

    public static TimeInForce convertTif(String tif) {
        return TIF_MAP.get(tif.trim().toUpperCase());
    }

    public static Side convertSide(String side) {
        return SIDE_MAP.get(side.trim().toUpperCase());
    }

    public static boolean isValidTif(String tif) {
        return TIF_MAP.containsKey(tif.trim().toUpperCase());
    }

    public static boolean isValidOrderType(String orderType) {
        return ORDER_TYPE_MAP.containsKey(orderType.trim().toUpperCase());
    }

    public static boolean isValidSide(String side) {
        return SIDE_MAP.containsKey(side.trim().toUpperCase());
    }

    /**
     * Implement the logic that Redi requires for their client order id
     * 
     * @param clientOrderId
     * @return
     * 
     */
    private static String normalizeClientOrderId(String clientOrderId) {
        clientOrderId = clientOrderId.replaceAll(" ", "");
        clientOrderId = clientOrderId.replaceAll("%", "");
        clientOrderId = clientOrderId.replaceAll("\\\\", "");
        int maxLength = Math.min(CLIENT_ORDER_ID_MAX_LENGTH, clientOrderId.length());
        return clientOrderId.substring(0, maxLength);
    }
    
    /**
     * Implement the Redi logic with Bloomberg's smaller limitations.
     * 
     * @param userOrderId
     * @return
     */
    private static String normalizeUserOrderId(String userOrderId) {
        if (userOrderId == null)  {
            return null;
        }
        
        userOrderId = userOrderId.replaceAll(" ", "");
        userOrderId = userOrderId.replaceAll("%", "");
        userOrderId = userOrderId.replaceAll("\\\\", "");
        int maxLength = Math.min(USER_ORDER_ID_MAX_LENGTH, userOrderId.length());

        return userOrderId.substring(0, maxLength);
    }
    
    public static boolean isValidClientOrderId(String clientOrderId) {
        String normalized = normalizeClientOrderId(clientOrderId);
        
        return normalized.equals(clientOrderId);
    }
    
    public static boolean isValidUserOrderId(String userOrderId) {
        String normalized = normalizeUserOrderId(userOrderId);
        
        return normalized.equals(userOrderId);
    }
    
    /**
     * Generate a valid ClientOrderId from the supplied parameters.
     * 
     * @param orderDate
     * @param messageType
     * @param userOrderId
     * 
     * @throws IllegalArgumentException is userOrderId is not valid
     * @return
     */
    public static String generateClientOrderId(LocalDate orderDate, Class<? extends Order> messageType,
            String userOrderId) {
        
        if (isValidUserOrderId(userOrderId)) {
            StringBuilder sb = new StringBuilder(20);
            sb.append(orderDate.toString("YYYYMMdd"));

            if (messageType == Order.class) {
                sb.append("-0");
            } else {
                sb.append("-1");
            }

            sb.append(normalizeUserOrderId(userOrderId));
            return sb.toString();
        } else {
            throw new IllegalArgumentException("UserOrderId is not valid");
        }
    }
    
}
