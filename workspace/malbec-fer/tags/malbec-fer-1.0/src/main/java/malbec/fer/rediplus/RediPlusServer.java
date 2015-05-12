package malbec.fer.rediplus;

import static malbec.fer.rediplus.RediExchange.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import malbec.fer.Order;
import malbec.fer.com.ComClient;
import malbec.jacob.rediplus.RediPlusOrder;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RediPlusServer implements ComClient {

    final private Logger log = LoggerFactory.getLogger(getClass());
    
    final private static Executor executor = Executors.newFixedThreadPool(1);

    private String name;

    private String userID;

    private String password;

    private EmailSettings emailSettings;
    
    private boolean forceToTicket = false;

    final private static Map<ExchangeKey, String> TIF_MAP = new HashMap<ExchangeKey, String>();
    final private static Map<ExchangeKey, String> ORDER_TYPE_MAP = new HashMap<ExchangeKey, String>();
    final private static Map<ExchangeKey, String> SIDE_MAP = new HashMap<ExchangeKey, String>();

    static {
        initializeTif();
        initializeOrderType();
        initializeSide();
    }

    private static void initializeTif() {
        // These are for production
        TIF_MAP.put(new ExchangeKey(SIGMA, "COR"), "COR");
        TIF_MAP.put(new ExchangeKey(SIGMA, "EXT"), "EXT");
        TIF_MAP.put(new ExchangeKey(SIGMA, "IOC"), "IOC");
        TIF_MAP.put(new ExchangeKey(SIGMA, "DAY"), "Day");
        TIF_MAP.put(new ExchangeKey(SIGMA, "OPG"), "OPG");
        // This is for testing
        TIF_MAP.put(new ExchangeKey("DEMO", "DAY"), "Day");
    }

    private static void initializeOrderType() {
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "LIMITCLOSE"), "Limit Close");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "MARKETCLOSE"), "Market close");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "MARKETONOPEN"), "Market on Open");
//        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "PEGASK"), "Peg Ask");
//        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "PEGBIG"), "Peg Bid");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "SMARTLIMIT"), "Smart Lmt");
        //ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "SMARTMKT"), "Smart Mkt");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "SMARTSWEEP"), "Smart Sweep");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "STEALTH"), "Stealth");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "STOPLIMIT"), "Stop Limit");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "STOP"), "Stop");
        //ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "LIMIT"), "Smart Lmt"); // no limit order
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "TRAILINGSTOP"), "Trailing Stop");
        ORDER_TYPE_MAP.put(new ExchangeKey(SIGMA, "TRAILINGSTOPLIMIT"), "Trailing StopLmt");
        // Send as ticket/staged
        ORDER_TYPE_MAP.put(new ExchangeKey(TICKET, "LIMIT"), "Limit");
        // This is for testing
        ORDER_TYPE_MAP.put(new ExchangeKey(DEMO, "LIMIT"), "Limit");
    }

    private static void initializeSide() {
        SIDE_MAP.put(new ExchangeKey(SIGMA, "BUY"), "BUY");
        SIDE_MAP.put(new ExchangeKey(SIGMA, "SELL"), "SELL");
        SIDE_MAP.put(new ExchangeKey(SIGMA, "SELLSHORT"), "SELL SHORT");
        // Send as tickets/staged orders
        SIDE_MAP.put(new ExchangeKey(TICKET, "BUY"), "BUY");
        SIDE_MAP.put(new ExchangeKey(TICKET, "SELL"), "SELL");
        SIDE_MAP.put(new ExchangeKey(TICKET, "SELLSHORT"), "SELL SHORT");

        
        // This is for testing
        SIDE_MAP.put(new ExchangeKey(DEMO, "BUY"), "BUY");
        SIDE_MAP.put(new ExchangeKey(DEMO, "SELL"), "SELL");
        SIDE_MAP.put(new ExchangeKey(DEMO, "SELLSHORT"), "SELL SHORT");
    }

    public RediPlusServer(String name, Properties config, EmailSettings emailSettings) {
        this.name = name;
        this.userID = config.getProperty("userID");
        this.password = config.getProperty("password");
        this.emailSettings = emailSettings;
        this.forceToTicket = Boolean.valueOf(config.getProperty("forceToTicket", "false"));
    }

    @Override
    public boolean sendOrder(Order order) {
        // double check that this is not a market order
        if (order.getOrderType().equalsIgnoreCase("MARKET")) {
            sendFailedMarketOrder(order);
            return false;
        }
        
        if (order.getClientOrderID() == null) {
            sendNoClientOrerID(order);
            return false;
        }

        if (!name.equalsIgnoreCase(order.getPlatform())) {
            log.warn("Order platform does not match ComClient name:" + order.getPlatform() + ":"+ name);
        }
        
        List<String> errors = new ArrayList<String>();
        
        final RediPlusOrder rediOrder = createOrder(order, errors);
        if (errors.size() > 0) {
            order.setMessage(errors.get(0));
            return false;
        }
        
        return sendOrder(order, rediOrder);
    }

    protected boolean sendOrder(Order order, final RediPlusOrder rediOrder) {
        final StringBuilder sb = new StringBuilder();

        boolean submittedOrder = false;
        FutureTask<Boolean> soft = null;
        try {
            // Put this into a thread so that we can detect that a dialog has been popped up
            soft = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return rediOrder.submit(sb);
                }
            });
                
            executor.execute(soft);
            submittedOrder = soft.get(10, TimeUnit.SECONDS);

            if (!submittedOrder) {
                log.error("Unable to send RediPlus order '" + sb.toString()+"', order was: "+ order);
                order.setMessage(sb.toString());
            }
        } catch (TimeoutException e) {
            sendPossiblePopup();
            log.error("Timedout waiting for send order.  Possible dialog", e);
        } catch (Exception e) {
            sendErrorSendingOrder(e);
            log.error("Unable to send RediPlus Order", e);
        }

        return submittedOrder;
    }

    protected boolean sendOrder(final RediPlusOrder rediOrder, List<String> errors) {
        final StringBuilder sb = new StringBuilder();

        boolean submittedOrder = false;
        FutureTask<Boolean> soft = null;
        try {
            // Put this into a thread so that we can detect that a dialog has been popped up
            soft = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return rediOrder.submit(sb);
                }
            });
                
            executor.execute(soft);
            submittedOrder = soft.get(10, TimeUnit.SECONDS);

            if (!submittedOrder) {
                log.error("Unable to send RediPlus order '" + sb.toString()+"', order was: "+ rediOrder.getClientData());
                errors.add(sb.toString());
            }
        } catch (TimeoutException e) {
            sendPossiblePopup();
            errors.add("Time out sending order, possible dialog on server");
            log.error("Timedout waiting for send order.  Possible dialog", e);
        } catch (Exception e) {
            sendErrorSendingOrder(e);
            errors.add("Error sending order: "+ e.getMessage());
            log.error("Unable to send RediPlus Order", e);
        }

        return submittedOrder;
    }
    
    RediPlusOrder createOrder(Order order, List<String> errors) {
        
        RediExchange exchange = RediExchange.valueFor(order.getExchange());
        
        if (UNKNOWN == exchange) {
            if (order.getExchange() == null || order.getExchange().trim().length() ==0) {
                errors.add("Missing required RediPlus exchange");
            } else {
                errors.add("Unknown RediPlus exchange: "+ order.getExchange());
            }
            return null;
        }

        FutureTask<RediPlusOrder> roft = null;
        RediPlusOrder createdOrder = null;
        
        try {
            // Put this into a thread so that we can detect that a dialog has been popped up
            roft = new FutureTask<RediPlusOrder>(new Callable<RediPlusOrder>() {
                @Override
                public RediPlusOrder call() throws Exception {
                    return new RediPlusOrder();
                }
            });
                
            executor.execute(roft);
            createdOrder = roft.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            errors.add("Time out waiting to create order, possible dialog on server");
            log.error("Time out waiting for to create order.  Possible dialog", e);
            sendPossibleCreatePopup(order);
            return null;
        }
        
        final RediPlusOrder rediOrder = createdOrder;
        
        rediOrder.setSymbol(order.getSymbol());
        rediOrder.setQuantity(order.getQuantity());
       
        if (forceToTicket && TICKET != exchange) {
            errors.add("Only accepting *ticket destination - rejecting " + exchange);
            return null;
        }
        // If they did not specify a correct exchange, use ticket
        if (SIGMA == exchange) {
            populateSigmaOrder(rediOrder, order, errors);
        } else {
            populateTicketOrder(rediOrder, order, errors);
        }
        
        // rediOrder.setMemo();
        rediOrder.setAccount(order.getAccount());
        rediOrder.setPassword(password);
        rediOrder.setUserID(userID);
        rediOrder.setClientData(order.getClientOrderID());

        //rediOrder.setWarning(false);
        return rediOrder;
    }

    private boolean populateTicketOrder(RediPlusOrder rediOrder, Order order, List<String> errors) {
        if (errors == null) {
            errors = new ArrayList<String>();
        }
        RediExchange exchange = TICKET;
        
        rediOrder.setExchange(exchange);
        // Mapped fields TIF/side/order type
        // There is no TIF for ticket orders (system defaults to day)
        String side = mapSide(exchange, order.getSide());
        if (side == null) {
            errors.add("No matching side for "+ order.getSide() +" on "+exchange);
        } else {
            rediOrder.setSide(side);
        }
        
        rediOrder.setPriceType(mapOrderType(exchange, order.getOrderType(), null));        
        rediOrder.setTIF(mapTIF(exchange, order.getTimeInForce(), "Day"));
        
        rediOrder.setPrice(order.getLimitPrice());
        
        return errors.size() == 0;
    }
    
    private boolean populateSigmaOrder(RediPlusOrder rediOrder, Order order, List<String> errors) {
        if (errors == null) {
            errors = new ArrayList<String>();
        }
        RediExchange exchange = SIGMA;
        
        rediOrder.setExchange(exchange);
        // Mapped fields TIF/side/order type
        rediOrder.setTIF(mapTIF(exchange, order.getTimeInForce(), "Day"));
        
        String side = mapSide(exchange, order.getSide());
        if (side == null) {
            errors.add("No matching side for "+ order.getSide() +" on "+exchange);
        } else {
            rediOrder.setSide(side);
        }

        rediOrder.setTIF(mapTIF(exchange, order.getOrderType(), "Day"));
        rediOrder.setPriceType(mapOrderType(exchange, order.getOrderType(), null));
        if ("invalid".equalsIgnoreCase(rediOrder.getPriceType())) {
            errors.add("Unsupported OrderType for SIGMA - " + order.getOrderType());
        }
        
        if ("STOPLIMIT".equals(order.getOrderType()) || "STOP".equals(order.getOrderType())
                || "TRAILINGSTOP".equals(order.getOrderType()) || "TRAILINGSTOPLIMIT".equals(order.getOrderType())) {
            rediOrder.setStopPrice(order.getStopPrice());
        }
        
        if (!order.getOrderType().toUpperCase().contains("MARKET") ) {
            rediOrder.setPrice(order.getLimitPrice());
        }
        
        return errors.size() == 0; 
    }
    
    private void sendNoClientOrerID(Order order) {
        EmailSender sender = new EmailSender(emailSettings.getAsProperties());

        StringBuilder sb = new StringBuilder(1024);
        sb.append("No clientOrderID specified, not sending order.\n\n");
        sb.append(order.toString()).append("\n");
        
        sender.sendMessage("Order missing required data", sb.toString());
    }

    private void sendErrorSendingOrder(Exception e) {
        EmailSender sender = new EmailSender(emailSettings.getAsProperties());

        StringBuilder sb = new StringBuilder(1024);
        sb.append("Error sending RediPlus order.\n\n");
        
        sb.append(StringUtils.exceptionToString(e));

        sender.sendMessage("Error Sending RediPlus Order", sb.toString());
    }

    private void sendPossibleCreatePopup(Order order) {
        EmailSender sender = new EmailSender(emailSettings.getAsProperties());

        StringBuilder sb = new StringBuilder(1024);
        sb.append("Create order took longer than 10 seconds, assuming user intervention is required.\n");
        sb.append("Ensure RediPlus is running on the server.\n\n");
        sb.append(order.toString());

        sender.sendMessage("Possible RediPlus Dialog", sb.toString());
    }

    private void sendPossiblePopup() {
        EmailSender sender = new EmailSender(emailSettings.getAsProperties());

        StringBuilder sb = new StringBuilder(1024);
        sb.append("Send order took longer than 10 seconds, assuming user intervention is required.\n");
        sb.append("Logon to server and send order manually.\n\n");

        sender.sendMessage("Possible RediPlus Dialog", sb.toString());
    }

    private void sendFailedMarketOrder(Order order) {
        EmailSender sender = new EmailSender(emailSettings.getAsProperties());

        StringBuilder sb = new StringBuilder(1024);
        sb.append("Attempt to send 'Market Order'.\n\n");
        sb.append(order.toString());
        sb.append(" minutes.\n");

        sender.sendMessage("Market Order Attempt", sb.toString());
    }

    private String mapSide(RediExchange exchange, String side) {
        return SIDE_MAP.get(new ExchangeKey(exchange, side.toUpperCase()));
    }

    
    private String mapOrderType(RediExchange exchange, String orderType, String defaultValue) {
        String ot =  ORDER_TYPE_MAP.get(new ExchangeKey(exchange, orderType));
        
        if (ot == null) {
            return defaultValue;
        }
        
        return ot;
    }

    private String mapTIF(RediExchange exchange, String timeInForce, String defaultValue) {
        String tif = TIF_MAP.get(new ExchangeKey(exchange, timeInForce));
        
        if (tif == null) {
            return defaultValue;
        }
        
        return tif;
    }

    @Override
    public String getName() {
        return name;
    }

    private static class ExchangeKey {
        final String exchange;
        final String value;

        ExchangeKey(String e, String v) {
            this.exchange = e;
            this.value = v;
        }

        ExchangeKey(RediExchange e, String v) {
            this.exchange = e.name();
            this.value = v;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ExchangeKey) {
                ExchangeKey key = (ExchangeKey) obj;

                return exchange.equals(key.exchange) && value.equals(key.value);
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return exchange.hashCode() + value.hashCode();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("exchange=").append(exchange);
            sb.append(", value=").append(value);

            return sb.toString();
        }

    }
}
