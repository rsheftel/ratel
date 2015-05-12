package malbec.fer.fix;

import static malbec.fer.util.OrderValidation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.joda.time.LocalDate;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.util.OrderValidation;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.ClOrdLinkID;
import quickfix.field.HandlInst;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;
import quickfix.field.TransactTime;

public class FixDestination extends FerFixClientApplication implements IOrderDestination {

    private FixClient fixClient;

    private String name;

    private DatabaseMapper dbm;
    
    protected String platform = "TEST";

    @SuppressWarnings("unused")
    private FixDestination() {
        super(null);
    }

    public FixDestination(String name, Properties config, EmailSettings emailSettings, DatabaseMapper dbm) {
        super(emailSettings);
        fixClient = new FixClient(name, this, config);
        this.name = name;
        this.dbm = dbm;
    }
    
    protected DatabaseMapper getDatabaseMapper() {
        return dbm;
    }
    
    @Override
    public ITransportableOrder createOrder(Order order) {
        List<String> errors = new ArrayList<String>();

        Message fixMessage = null;
        try {
            // create a message
            fixMessage = getFixClient().createMessage(MsgType.ORDER_SINGLE);
            // populate common fields
            populateCommonFields(order, fixMessage, errors);
            // determine order type Futures/Equity
            // populate order type specific fields
            if ("equity".equalsIgnoreCase(order.getSecurityType())) {
                populateEquityFields(order, fixMessage, errors);
            } else if ("futures".equalsIgnoreCase(order.getSecurityType())) {
                populateFuturesFields(order, fixMessage, errors);
            } else {
                fixLog.error("Unknown security type: " + order.getSecurityType());
            }

        } catch (Exception e) {
            // This should be QuickFIX/J field exceptions
            errors.add("Unable to convert order to FIX message: " + e.getMessage());
        }
        return new FixTransportableOrder(getFixClient(), errors, fixMessage);
    }

    @Override
    public ITransportableOrder createCancelOrder(CancelRequest cancelRequest) {
        List<String> errors = new ArrayList<String>();

        Message fixMessage = null;
        try {
            // create a message
            fixMessage = getFixClient().createMessage(MsgType.ORDER_CANCEL_REQUEST);
            // populate common fields
            populateCommonFieldsForCancel(cancelRequest, fixMessage, errors);
            // determine order type Futures/Equity
            // populate order type specific fields
            if ("equity".equalsIgnoreCase(cancelRequest.getSecurityType())) {
                populateEquityFields(cancelRequest, fixMessage, errors);
            } else if ("futures".equalsIgnoreCase(cancelRequest.getSecurityType())) {
                populateFuturesFields(cancelRequest, fixMessage, errors);
            } else {
                fixLog.error("Unknown security type: " + cancelRequest.getSecurityType());
            }

        } catch (Exception e) {
            // This should be QuickFIX/J field exceptions
            errors.add("Unable to convert order to FIX message: " + e.getMessage());
        }
        return new FixTransportableOrder(getFixClient(), errors, fixMessage);
    }

    protected void populateCommonFieldsForCancel(CancelRequest cancelRequest, Message fixMessage,
            List<String> errors) {
        fixMessage.setField(new OrigClOrdID(cancelRequest.getOriginalClientOrderId()));
        fixMessage.setField(new ClOrdID(cancelRequest.getClientOrderId()));
        fixMessage.setField(convertSide(cancelRequest.getSide()));
        fixMessage.setField(new OrderQty(cancelRequest.getQuantity().doubleValue()));
        fixMessage.setField(new TransactTime(new Date()));

        if (cancelRequest.getExchange() != null) {
            fixMessage.getHeader().setField(new TargetSubID(cancelRequest.getExchange()));
        }
    }

    @Override
    public ITransportableOrder createReplaceOrder(CancelReplaceRequest cancelReplaceRequest) {
        List<String> errors = new ArrayList<String>();

        Message fixMessage = null;
        try {
            // create a message
            fixMessage = getFixClient().createMessage(MsgType.ORDER_CANCEL_REPLACE_REQUEST);
            // populate common fields
            populateCommonFields(cancelReplaceRequest, fixMessage, errors);
            populateCommonFieldsForCancel(cancelReplaceRequest, fixMessage, errors);
            // determine order type Futures/Equity
            // populate order type specific fields
            if ("equity".equalsIgnoreCase(cancelReplaceRequest.getSecurityType())) {
                populateEquityFields(cancelReplaceRequest, fixMessage, errors);
            } else if ("futures".equalsIgnoreCase(cancelReplaceRequest.getSecurityType())) {
                populateFuturesFields(cancelReplaceRequest, fixMessage, errors);
            } else {
                fixLog.error("Unknown security type: " + cancelReplaceRequest.getSecurityType());
            }

        } catch (Exception e) {
            // This should be QuickFIX/J field exceptions
            errors.add("Unable to convert order to FIX message: " + e.getMessage());
        }
        return new FixTransportableOrder(getFixClient(), errors, fixMessage);
    }

    @Override
    public String getDestinationName() {
        return name;
    }

    @Override
    public List<String> validateOrder(Order order) {
        List<String> errors = new ArrayList<String>();
        OrderValidation.createFixMessage(order, errors);

        return errors;
    }

    public FixClient getFixClient() {
        return fixClient;
    }

    @Override
    public void start() {
        fixClient.start();
    }

    @Override
    public void stop() {
        fixClient.stop();
    }

    protected void populateCommonFields(Order order, Message fixMessage, List<String> conversionErrors) {
        // Default handling instructions
        fixMessage.setField(new HandlInst('1'));
        fixMessage.setField(new TransactTime());
        try {
            // This is FIX 4.4 specific
            if ("FIX.4.4".equals(fixMessage.getHeader().getString(BeginString.FIELD))) {
                fixMessage.setField(new ClOrdLinkID(order.getStrategy()));
            }
        } catch (FieldNotFound e) {
            // This should not happen, how can a fix message not have a begin string?
        }

        String account = determineAccount(order);
        
        if (account != null) {
            fixMessage.setField(new Account(account));
            // We are changing the account, update the original order as it 
            // we be saved to the database
            order.setAccount(account);
        } else {
            conversionErrors.add("Unable to determine account.");
        }

        boolean needLimitPrice = false;
        boolean foundLimitPrice = false;
        boolean needStopPrice = false;
        boolean foundStopPrice = false;

        if (isValidTif(order.getTimeInForce())) {
            fixMessage.setField(convertTif(order.getTimeInForce()));
        } else {
            conversionErrors.add("Unsupported TIF: " + order.getTimeInForce());
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
            conversionErrors.add("Unsupported OrderType: " + order.getOrderType());
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

        if (needLimitPrice && !foundLimitPrice) {
            conversionErrors.add("No limit price found.");
        }

        if (needStopPrice && !foundStopPrice) {
            conversionErrors.add("No stop price found.");
        }

    }

    protected String determineAccount(Order order) {
        return lookupAccount(getPlatform(), order.getStrategy(), order.getSecurityType());
    }

    protected String getPlatform() {
        return platform;
    }

    protected void populateFuturesFields(Order order, Message fixMessage, List<String> errors) {
        fixMessage.setField(new SecurityType(SecurityType.FUTURE));
        fixMessage.setField(new SecurityIDSource(SecurityIDSource.BLOOMBERG_SYMBOL));
        fixMessage.setField(new SecurityID(order.getSymbol()));
    }

    protected void populateEquityFields(Order order, Message fixMessage, List<String> errors) {
        fixMessage.setField(new SecurityIDSource(SecurityIDSource.EXCHANGE_SYMBOL));
        fixMessage.setField(new SecurityID(order.getSymbol()));
        fixMessage.setField(new Symbol(order.getSymbol()));
    }

    @Override
    public boolean isActiveSession() {
        return getFixClient().isActiveSession();
    }

    protected String lookupAccount(String platform, String strategy, String securityType) {
        return dbm.lookupAccount(platform, strategy, securityType);
    }

    /**
     * There are here for reference, not needed since we can send TS symbols
     * 
     * @param symbol
     * @return
     */
    String bloombergToExchangesymbol(String platform, String symbol) {
        String[] parts = symbol.split("\\d");  // get everything but the year
        int length = parts[0].length();
        String symbolRoot = parts[0].substring(0, length-1);
        
        return dbm.mapFuturesSymbol(platform, symbolRoot, symbolRoot);
    }

    String bloombergToMaturityMonthYear(String symbol) {
        String[] parts = symbol.split("\\d");  // get everything but the year
        int length = parts[0].length();
        
        char monthCode = parts[0].charAt(length -1);
        String year = symbol.substring(parts[0].length());
        int month = dbm.mapFuturesMonth(monthCode);
        
        LocalDate ld = new LocalDate(Integer.valueOf(year), month, 1);
        
        return ld.toString("MMyyyy");
    }

}
