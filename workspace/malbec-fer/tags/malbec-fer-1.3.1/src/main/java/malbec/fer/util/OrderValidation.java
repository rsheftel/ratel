package malbec.fer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import malbec.fer.Order;

import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.Message;
import quickfix.fix44.NewOrderSingle;

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
        ORDER_TYPE_MAP.put("MOC", new OrdType(OrdType.MARKET_ON_CLOSE));
    }

    private static void initializeSide() {
        SIDE_MAP.put("BUY", new Side(Side.BUY));
        SIDE_MAP.put("SELL", new Side(Side.SELL));
        SIDE_MAP.put("SELLSHORT", new Side(Side.SELL_SHORT));
        // SIDE_MAP.put("BUYCOVER", new Side(Side.????));
    }

    /**
     * Convert an <code>Order</code> into a FIX 4.4 <code>NewOrderSingle</code>.
     * 
     * 
     * Supported Orders are: - Limit - Stop/Limit - Stop
     * 
     * @param order
     * @param conversionErrors
     * @return
     */
    public static Message createFixMessage(Order order, List<String> conversionErrors) {

        Message fixMessage = new NewOrderSingle();

        try {
            // Default handling instructions
            fixMessage.setField(new HandlInst('1'));
            fixMessage.setField(new TransactTime());
            if (order.getAccount() != null) {
                fixMessage.setField(new Account(order.getAccount()));
            }

            if (conversionErrors == null) {
                // We will not be able to pass these back, but we won't NPE everywhere
                conversionErrors = new ArrayList<String>();
            }

            boolean needLimitPrice = false;
            boolean foundLimitPrice = false;
            boolean needStopPrice = false;
            boolean foundStopPrice = false;

            if (isValidTif(order.getTimeInForce())) {
                fixMessage.setField(convertTif(order.getTimeInForce()));
            } else {
                conversionErrors.add("Unsupported TIF :" + order.getTimeInForce());
            }

            if (isValidOrderType(order.getOrderType())) {
                OrdType orderType = convertOrderType(order.getOrderType());
                fixMessage.setField(orderType);
                if (OrdType.LIMIT == orderType.getValue()) {
                    needLimitPrice = true;
                } else if (OrdType.STOP_LIMIT == orderType.getValue()) {
                    needLimitPrice = true;
                    needStopPrice = true;
                }
            } else {
                conversionErrors.add("Unsupported OrderType :" + order.getOrderType());
            }

            if (isValidSide(order.getSide())) {
                fixMessage.setField(convertSide(order.getSide()));
            } else {
                conversionErrors.add("Unsupported Side: " + order.getSide());
            }

            if (order.getLimitPrice() != null) {
                fixMessage.setField(new Price(order.getLimitPrice().doubleValue()));
                foundLimitPrice = true;
            }

            if (order.getStopPrice() != null) {
                fixMessage.setField(new StopPx(order.getStopPrice().doubleValue()));
                foundStopPrice = true;
            }

            fixMessage.setField(new ClOrdID(order.getClientOrderId()));
            fixMessage.setField(new OrderQty(order.getQuantity().doubleValue()));
            

            // special logic for futures only 
            if ("Futures".equalsIgnoreCase(order.getSecurityType())) {
                fixMessage.setField(new SecurityType(SecurityType.FUTURE));
                //fixMessage.setField(new SecurityIDSource(SecurityIDSource.BLOOMBERG_SYMBOL));
                fixMessage.setField(new SecurityIDSource("100")); // trad
                fixMessage.setField(new SecurityID(order.getSymbol()));
            } else if ("Equity".equalsIgnoreCase(order.getSecurityType())) {
//                fixMessage.setField(new SecurityType(SecurityType.COMMON_STOCK));
                // TS uses EQU
                fixMessage.setField(new SecurityType("EQU"));
                fixMessage.setField(new Symbol(order.getSymbol()));
                //fixMessage.setField(new SecurityIDSource(SecurityIDSource.EXCHANGE_SYMBOL));
//                fixMessage.setField(new SecurityID(order.getSymbol()));
            }

            if (needLimitPrice && !foundLimitPrice) {
                conversionErrors.add("No limit price found.");
            }

            if (needStopPrice && !foundStopPrice) {
                conversionErrors.add("No stop price found.");
            }
        } catch (Exception e) {
            log.error("Unable to convert Order to FIX Message", e);
            conversionErrors.add(e.getMessage());
        }
        
        return fixMessage;
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
     * @deprecated Use normalizeUserOrderId instead
     */
    static String normalizeClientOrderId(String clientOrderId) {
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
