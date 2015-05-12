package malbec.fer.fix;

import static malbec.fer.util.OrderValidation.*;
import static malbec.util.FuturesSymbolUtil.extractSymbolRoot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.fer.position.PositionCache;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;
import malbec.util.MathUtil;
import malbec.util.SystematicFacade;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.ClOrdLinkID;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

public class FixDestination extends FerFixClientApplication implements IOrderDestination {

    private FixClient fixClient;

    private String name;

    private final DatabaseMapper dbm;

    private boolean forceToTicket = true;

    protected String platform = "TEST";

    protected final BigDecimal EQUITY_TICK_SIZE = new BigDecimal("0.01");

    @SuppressWarnings("unused")
    private FixDestination() {
        super(null);
        dbm = null;
    }

    public FixDestination(String name, Properties config, EmailSettings emailSettings, DatabaseMapper dbm) {
        super(emailSettings);
        fixClient = new FixClient(name, this, config);
        this.name = name;
        this.dbm = dbm;
    }

    protected IDatabaseMapper getDatabaseMapper() {
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
            if (order.isEquity()) {
                populateEquityFields(order, fixMessage, errors);
            } else if (order.isFutures()) {
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
            if (cancelRequest.isEquity()) {
                populateEquityFields(cancelRequest, fixMessage, errors);
            } else if (cancelRequest.isFutures()) {
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
            if (cancelReplaceRequest.isEquity()) {
                populateEquityFields(cancelReplaceRequest, fixMessage, errors);
            } else if (cancelReplaceRequest.isFutures()) {
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
        //OrderValidation.createFixMessage(order, errors);

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

    protected BigDecimal getOutboundMultiplier(Order order) {
        return BigDecimal.ONE;
    }

    protected BigDecimal getSecurityTickSize(Order order) {
        if (order.isEquity()) {
            return EQUITY_TICK_SIZE;
        } else if (order.isFutures()) {
            return SystematicFacade.lookupFuturesTickSize(order.getSymbol(), order.getYellowKey());
        }

        return new BigDecimal("0.00000001");
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
            checkIfMarketOrder(fixMessage, conversionErrors);
            if (OrdType.LIMIT == orderType.getValue()) {
                needLimitPrice = true;
            } else if (OrdType.STOP_LIMIT == orderType.getValue()) {
                needLimitPrice = true;
                needStopPrice = true;
            }
        } else {
            conversionErrors.add("Unsupported OrderType: " + order.getOrderType());
        }

        populateOrderSide(order, fixMessage, conversionErrors);
        Side fixSide = extractOrderSide(fixMessage);

        BigDecimal multiplier = getOutboundMultiplier(order);
        if (multiplier == null) {
            conversionErrors.add("Unable to lookup price multiplier");
            multiplier = BigDecimal.ONE;
        }

        BigDecimal tickSize = getSecurityTickSize(order);

        if (order.getLimitPrice() != null) {
            BigDecimal price = normalizePrice(order.getLimitPrice(), multiplier, tickSize, fixSide);

            fixMessage.setDecimal(Price.FIELD, price);
            foundLimitPrice = true;
        }

        if (order.getStopPrice() != null) {
            BigDecimal stopPrice = normalizePrice(order.getStopPrice(), multiplier, tickSize, fixSide);
            fixMessage.setDecimal(StopPx.FIELD, stopPrice);
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

    private Side extractOrderSide(Message fixMessage) {
        try {
            return new Side(fixMessage.getChar(Side.FIELD));
        } catch (FieldNotFound e) {}
        // should never happen
        return null;
    }

    private BigDecimal normalizePrice(BigDecimal price, BigDecimal multiplier, BigDecimal tickSize, Side side) {

        switch (side.getValue()) {
            case Side.BUY:
            case Side.BUY_MINUS:
                return  MathUtil.normalizeDown(price, multiplier, tickSize);
            case Side.SELL:
            case Side.SELL_PLUS:
            case Side.SELL_SHORT:
            case Side.SELL_SHORT_EXEMPT:
                return  MathUtil.normalizeUp(price, multiplier, tickSize);
        }

        throw new IllegalArgumentException("side");
    }

    /**
     * Validate the requested side and populate the FIX order.
     * 
     * @param order
     * @param fixMessage
     * @param conversionErrors
     */
    protected void populateOrderSide(Order order, Message fixMessage, List<String> conversionErrors) {
        if (order.isEquity()) {
            // check current position and short sell list
            PositionCache pc = PositionCache.getInstance();
            String primeBroker = mapRouteToEquityPrimeBroker(order.getRoute());

            //pc.updatePosition(primeBroker, "BBY", BigDecimal.valueOf(-1000));
            
            String ticker = order.getSymbol();
            long currentPosition = pc.positionFor(primeBroker, ticker).longValue();
            String requestedSide = order.getSide();
            if ("SELL".equalsIgnoreCase(requestedSide) || "SELLSHORT".equalsIgnoreCase(requestedSide)) {
                if (currentPosition > 0) {
                    long orderQuantity = order.getQuantity().longValue();
                    // currentPosition - orderQuantity < 0
                    if (orderQuantity > currentPosition) {
                        // failure - crossing zero
                        conversionErrors.add("Order requiries splitting - rejecting");
                    }
                    fixMessage.setChar(Side.FIELD, Side.SELL);
                } else {
                    fixMessage.setChar(Side.FIELD, Side.SELL_SHORT);
                    getDatabaseMapper().subtractShortShares(primeBroker, ticker, order.getQuantity());
                    fixMessage.setChar(LocateReqd.FIELD, 'Y');
                }
            } else if ("BUY".equalsIgnoreCase(requestedSide)) {
                if (currentPosition < 0) {
                    long orderQuantity = order.getQuantity().longValue();
                    if (currentPosition + orderQuantity > 0) {
                        // crossing zero
                        conversionErrors.add("Order requiries splitting - rejecting");
                    }
                    // TODO this is the only part that should be platform specific
                    setBuyToCover(fixMessage);
                    order.setBuyToCover("Y");
                } else {
                    fixMessage.setChar(Side.FIELD, Side.BUY);
                }
                getDatabaseMapper().addShortShares(primeBroker, ticker, order.getQuantity());
            } else {
                conversionErrors.add("Unable to determine side for '" + requestedSide + "'");
            }
        } else {
            if (isValidSide(order.getSide())) {
                fixMessage.setField(convertSide(order.getSide()));
            } else {
                conversionErrors.add("Unsupported Side: " + order.getSide());
            }
        }
    }

    /**
     * Set buy; each platform will be different
     * @param fixMessage
     */
    protected void setBuyToCover(Message fixMessage) {
        fixMessage.setChar(Side.FIELD, Side.BUY);
    }

    private String mapRouteToEquityPrimeBroker(String route) {
        if ("MANS".equalsIgnoreCase(route)) {
            return "MFPB";
        }
        
        return "MFPB";
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
        // This is not necessary since we have the 3 above, but our test engine complains
        fixMessage.setField(new Symbol(order.getSymbol()));
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
     * 
     * 
     * @param symbol
     * @return
     */
    String bloombergToExchangesymbol(String platform, String symbol) {
        String symbolRoot = extractSymbolRoot(symbol);

        return dbm.mapBloombergRootToPlatformRoot(platform, symbolRoot, null);
    }

    public boolean isForceToTicket() {
        return forceToTicket;
    }

    public void setForceToTicket(boolean b) {
        forceToTicket = b;
    }
}
