/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved. 
 * 
 * This file is part of the QuickFIX FIX Engine 
 * 
 * This file may be distributed under the terms of the quickfixengine.org 
 * license as defined by quickfixengine.org and appearing in the file 
 * LICENSE included in the packaging of this file. 
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 * 
 * See http://www.quickfixengine.org/LICENSE for licensing information. 
 * 
 * Contact ask@quickfixengine.org if any conditions of this licensing 
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.executor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import malbec.fix.FixVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.CxlRejResponseTo;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SenderCompID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

public class ExecutorApplication extends quickfix.MessageCracker implements quickfix.Application {
    private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
    private static final String ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final boolean alwaysFillLimitOrders;
    private final HashSet<String> validOrderTypes = new HashSet<String>();
    private IMarketDataProvider marketDataProvider;

    private final ScheduledExecutorService orderMatcher = Executors.newSingleThreadScheduledExecutor();


    // TODO add executions to this

    public ExecutorApplication(SessionSettings settings) throws ConfigError, FieldConvertError {
        initializeValidOrderTypes(settings);
        initializeMarketDataProvider(settings);

        if (settings.isSetting(ALWAYS_FILL_LIMIT_KEY)) {
            alwaysFillLimitOrders = settings.getBool(ALWAYS_FILL_LIMIT_KEY);
        } else {
            alwaysFillLimitOrders = false;
        }

        orderMatcher.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    matchOrders();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void matchOrders() {
                synchronized (this) {
                    Collection<Message> orders = OrderDB.getInstance().getAllOpenOrders();
                    for (Message order : orders) {
                        try {
                            SessionID sessionId = createSessionId(order);
                            Price price = getPrice(order);
                            if (isOrderExecutable(order, price)) {
                                String beginString = sessionId.getBeginString();
                                FixVersion version = FixVersion.fromString(beginString);
                                Message fill = createFill(order, price, sessionId, version);

                                OrderDB.getInstance().setOrderFilled(order.getString(ClOrdID.FIELD));
                                sendMessage(sessionId, fill);
                            }
                        } catch (FieldNotFound e) {
                            throw new FieldNotFoundException(e);
                        }
                    }
                }
            }

            

        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private SessionID createSessionId(Message order) throws FieldNotFound {
        SessionID sessionId = new SessionID(order.getHeader().getString(BeginString.FIELD),
                order.getHeader().getString(TargetCompID.FIELD), order.getHeader().getString(SenderCompID.FIELD));
        return sessionId;
    }
    
    private void initializeMarketDataProvider(SessionSettings settings) throws ConfigError, FieldConvertError {
        if (settings.isSetting(DEFAULT_MARKET_PRICE_KEY)) {
            if (marketDataProvider == null) {
                final double defaultMarketPrice = settings.getDouble(DEFAULT_MARKET_PRICE_KEY);
                marketDataProvider = new RandomMarketDataProvider(defaultMarketPrice);
                //                marketDataProvider = new IMarketDataProvider() {
                //                    public double getAsk(String symbol) {
                //                        return defaultMarketPrice;
                //                    }
                //
                //                    public double getBid(String symbol) {
                //                        return defaultMarketPrice;
                //                    }
                //                };
            } else {
                log.warn("Ignoring " + DEFAULT_MARKET_PRICE_KEY + " since provider is already defined.");
            }
        }
    }

    private void initializeValidOrderTypes(SessionSettings settings) throws ConfigError, FieldConvertError {
        if (settings.isSetting(VALID_ORDER_TYPES_KEY)) {
            List<String> orderTypes = Arrays.asList(settings.getString(VALID_ORDER_TYPES_KEY).trim().split(
            "\\s*,\\s*"));
            validOrderTypes.addAll(orderTypes);
        } else {
            validOrderTypes.add(OrdType.LIMIT + "");
        }
    }

    public void onCreate(SessionID sessionID) {
        Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    public void onLogon(SessionID sessionID) {}

    public void onLogout(SessionID sessionID) {}

    public void toAdmin(quickfix.Message message, SessionID sessionID) {}

    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {}

    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound,
    IncorrectDataFormat, IncorrectTagValue, RejectLogon {}

    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound,
    IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }



    public void onMessage(quickfix.fix40.NewOrderSingle order, SessionID sessionID) throws FieldNotFound,
    UnsupportedMessageType, IncorrectTagValue {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();

            Price price = getPrice(order);

            quickfix.fix40.ExecutionReport accept = new quickfix.fix40.ExecutionReport(genOrderID(),
                    genExecID(), new ExecTransType(ExecTransType.NEW), new OrdStatus(OrdStatus.NEW), order
                    .getSymbol(), order.getSide(), orderQty, new LastShares(0), new LastPx(0),
                    new CumQty(0), new AvgPx(0));

            accept.set(order.getClOrdID());
            sendMessage(sessionID, accept);

            if (isOrderExecutable(order, price)) {
                quickfix.fix40.ExecutionReport fill = new quickfix.fix40.ExecutionReport(genOrderID(),
                        genExecID(), new ExecTransType(ExecTransType.NEW), new OrdStatus(OrdStatus.FILLED),
                        order.getSymbol(), order.getSide(), orderQty, new LastShares(orderQty.getValue()),
                        new LastPx(price.getValue()), new CumQty(orderQty.getValue()), new AvgPx(price
                                .getValue()));

                fill.set(order.getClOrdID());

                sendMessage(sessionID, fill);
            }
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    }

    private boolean isOrderExecutable(Message order, Price price) throws FieldNotFound {
        if (order.getChar(OrdType.FIELD) == OrdType.LIMIT) {
            BigDecimal limitPrice = new BigDecimal(order.getString(Price.FIELD));
            char side = order.getChar(Side.FIELD);
            BigDecimal thePrice = new BigDecimal(price.getValue());

            return (side == Side.BUY && thePrice.compareTo(limitPrice) <= 0)
            || ((side == Side.SELL || side == Side.SELL_SHORT) && thePrice.compareTo(limitPrice) >= 0);
        }
        return true;
    }

    private Price getPrice(Message message) throws FieldNotFound {
        Price price;
        if (message.getChar(OrdType.FIELD) == OrdType.LIMIT && alwaysFillLimitOrders) {
            price = new Price(message.getDouble(Price.FIELD));
        } else {
            if (marketDataProvider == null) {
                throw new RuntimeException("No market data provider specified for market order");
            }
            char side = message.getChar(Side.FIELD);
            if (side == Side.BUY) {
                price = new Price(marketDataProvider.getAsk(message.getString(Symbol.FIELD)));
            } else if (side == Side.SELL || side == Side.SELL_SHORT) {
                price = new Price(marketDataProvider.getBid(message.getString(Symbol.FIELD)));
            } else {
                throw new RuntimeException("Invalid order side: " + side);
            }
        }
        return price;
    }

    private void sendMessage(SessionID sessionID, Message message) {
        try {
            Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }

            DataDictionary dataDictionary = session.getDataDictionary();
            if (dataDictionary != null) {
                try {
                    session.getDataDictionary().validate(message, true);
                } catch (Exception e) {
                    LogUtil.logThrowable(sessionID, "Outgoing message failed validation: " + e.getMessage(),
                            e);
                    return;
                }
            }

            session.send(message);
        } catch (SessionNotFound e) {
            log.error(e.getMessage(), e);
        }
    }

    public void onMessage(quickfix.fix41.NewOrderSingle order, SessionID sessionID) throws FieldNotFound,
    UnsupportedMessageType, IncorrectTagValue {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();
            Price price = getPrice(order);

            quickfix.fix41.ExecutionReport accept = new quickfix.fix41.ExecutionReport(genOrderID(),
                    genExecID(), new ExecTransType(ExecTransType.NEW), new ExecType(ExecType.FILL),
                    new OrdStatus(OrdStatus.NEW), order.getSymbol(), order.getSide(), orderQty,
                    new LastShares(0), new LastPx(0), new LeavesQty(0), new CumQty(0), new AvgPx(0));

            accept.set(order.getClOrdID());
            sendMessage(sessionID, accept);

            if (isOrderExecutable(order, price)) {
                quickfix.fix41.ExecutionReport executionReport = new quickfix.fix41.ExecutionReport(
                        genOrderID(), genExecID(), new ExecTransType(ExecTransType.NEW), new ExecType(
                                ExecType.FILL), new OrdStatus(OrdStatus.FILLED), order.getSymbol(), order
                                .getSide(), orderQty, new LastShares(orderQty.getValue()), new LastPx(price
                                        .getValue()), new LeavesQty(0), new CumQty(orderQty.getValue()), new AvgPx(
                                                price.getValue()));

                executionReport.set(order.getClOrdID());

                sendMessage(sessionID, executionReport);
            }
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(quickfix.fix42.NewOrderSingle order, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        
        onNewOrderSingleMessage(order, sessionId, FixVersion.F42);
    }

    private void validateOrder(Message order) throws IncorrectTagValue, FieldNotFound {
        OrdType ordType = new OrdType(order.getChar(OrdType.FIELD));
        if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
            log.error("Order type not in ValidOrderTypes setting");
            throw new IncorrectTagValue(ordType.getField());
        }
        if (ordType.getValue() == OrdType.MARKET && marketDataProvider == null) {
            log.error("DefaultMarketPrice setting not specified for market order");
            throw new IncorrectTagValue(ordType.getField());
        }
    }

    public void onMessage(quickfix.fix43.NewOrderSingle order, SessionID sessionID) throws FieldNotFound,
    UnsupportedMessageType, IncorrectTagValue {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();


            quickfix.fix43.ExecutionReport accept = new quickfix.fix43.ExecutionReport(genOrderID(),
                    genExecID(), new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.NEW), order.getSide(),
                    new LeavesQty(order.getOrderQty().getValue()), new CumQty(0), new AvgPx(0));

            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            sendMessage(sessionID, accept);

            Price price = getPrice(order);
            if (isOrderExecutable(order, price)) {

                quickfix.fix43.ExecutionReport executionReport = new quickfix.fix43.ExecutionReport(
                        genOrderID(), genExecID(), new ExecType(ExecType.FILL), new OrdStatus(
                                OrdStatus.FILLED), order.getSide(), new LeavesQty(0), new CumQty(orderQty
                                        .getValue()), new AvgPx(price.getValue()));

                executionReport.set(order.getClOrdID());
                executionReport.set(order.getSymbol());
                executionReport.set(orderQty);
                executionReport.set(new LastQty(orderQty.getValue()));
                executionReport.set(new LastPx(price.getValue()));

                sendMessage(sessionID, executionReport);
            }
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(quickfix.fix44.NewOrderSingle order, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {

        onNewOrderSingleMessage(order, sessionId, FixVersion.F44);
    }
    
    protected void onNewOrderSingleMessage(Message order, SessionID sessionId, FixVersion version) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        try {
            validateOrder(order);

            Message acceptOrder = createNewOrderAccept(order, sessionId, version);

            sendMessage(sessionId, acceptOrder);

            // Order should be put into the pool to be executed when possible
            // For now, send back a complete fill
            Price price = getPrice(order);
            if (isOrderExecutable(order, price)) {
                System.err.println("Received executable order");
                Message fill = createFill(order, price, sessionId, version);

                OrderDB.getInstance().setOrderFilled(order.getString(ClOrdID.FIELD));
                sendMessage(sessionId, fill);
            }
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(quickfix.fix44.OrderCancelReplaceRequest message, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {

        onOrderCancelReplaceRequestMessage(message, sessionId, FixVersion.F44);
    }
    
    @Override
    public void onMessage(quickfix.fix42.OrderCancelReplaceRequest message, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {

        onOrderCancelReplaceRequestMessage(message, sessionId, FixVersion.F42);
    }

    protected void onOrderCancelReplaceRequestMessage(Message message, SessionID sessionId, FixVersion version) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {

        String originalClientOrderId = message.getString(OrigClOrdID.FIELD);
        String newOrderId = OrderDB.getInstance().addNewOrder(message);

        OrdStatus orderStatus = OrderDB.getInstance().getOrderStatus(originalClientOrderId);
        if (orderStatus == null) {
           System.err.println("OrderStatus is null, something is wrong!");
           return;
        }
        if (OrdStatus.CANCELED == orderStatus.getValue()) {
            //reject the request
            String orderId = OrderDB.getInstance().getOrderId(originalClientOrderId);
            Message cancelReject = createCancelReject(message, sessionId, "Order already cancelled", 
                    orderId, new OrdStatus(OrdStatus.CANCELED));

            sendMessage(sessionId, cancelReject);
        } else {
            // for now, everything else is cancellable
            Message originalOrder = OrderDB.getInstance().getOrder(originalClientOrderId);
            Message cancelAccepted = createCancelAccept(message, sessionId, originalOrder, newOrderId, version);

            OrderDB.getInstance().setOrderCancelled(originalClientOrderId);

            sendMessage(sessionId, cancelAccepted);
        }

    }

    @Override
    public void onMessage(quickfix.fix44.OrderCancelRequest message, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        
        onOrderCancelRequestMessage(message, sessionId, FixVersion.F44);
    }

    @Override
    public void onMessage(quickfix.fix42.OrderCancelRequest message, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        
        onOrderCancelRequestMessage(message, sessionId, FixVersion.F42);
    }

    protected void onOrderCancelRequestMessage(Message message, SessionID sessionId, FixVersion version) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {

        String originalClientOrderId = message.getString(OrigClOrdID.FIELD);
        String newOrderId = OrderDB.getInstance().addNewOrder(message);
        OrdStatus orderStatus = OrderDB.getInstance().getOrderStatus(originalClientOrderId);

        if (OrdStatus.CANCELED == orderStatus.getValue()) {
            //reject the request
            String orderId = OrderDB.getInstance().getOrderId(originalClientOrderId);
            Message cancelReject = createCancelReject(message, sessionId, "Order already cancelled", 
                    orderId, new OrdStatus(OrdStatus.CANCELED));

            sendMessage(sessionId, cancelReject);
        } else {
            // for now, everything else is cancellable
            Message originalOrder = OrderDB.getInstance().getOrder(originalClientOrderId);
            Message cancelAccepted = createCancelAccept(message, sessionId, originalOrder, newOrderId, version);

            OrderDB.getInstance().setOrderCancelled(originalClientOrderId);
            if (FixVersion.F42 == version) {
                cancelAccepted.setChar(ExecTransType.FIELD, ExecTransType.NEW);
            }
            sendMessage(sessionId, cancelAccepted);
        }
    }

    private Message createCancelAccept(Message message, SessionID sessionId, 
            Message originalOrder, String newOrderId, FixVersion version) {
        Message cancelAccept = createEmptyExecutionReport(sessionId);

        try {
            cancelAccept.setString(OrderID.FIELD, newOrderId);
            cancelAccept.setString(ClOrdID.FIELD, message.getString(ClOrdID.FIELD));
            cancelAccept.setString(OrigClOrdID.FIELD, message.getString(OrigClOrdID.FIELD));
            cancelAccept.setChar(OrdStatus.FIELD, OrdStatus.CANCELED);
            cancelAccept.setChar(ExecType.FIELD, ExecType.CANCELED);
            //            cancelAccept.setString(OrderID.FIELD, originalOrder.getString(OrderID.FIELD));

            // may need to add logic to handle non-equity
            cancelAccept.setChar(Side.FIELD, message.getChar(Side.FIELD));
            cancelAccept.setString(Symbol.FIELD, message.getString(Symbol.FIELD));

            if (originalOrder.isSetField(AvgPx.FIELD)) {
                cancelAccept.setDouble(AvgPx.FIELD, originalOrder.getDouble(AvgPx.FIELD));
            } else {
                cancelAccept.setDouble(AvgPx.FIELD, 0d);
            }

            if (originalOrder.isSetField(AvgPx.FIELD)) {
                cancelAccept.setDouble(CumQty.FIELD, originalOrder.getDouble(CumQty.FIELD));
            } else {
                cancelAccept.setDouble(CumQty.FIELD, 0d);
            }

            if (originalOrder.isSetField(AvgPx.FIELD)) {
                cancelAccept.setDouble(LeavesQty.FIELD, originalOrder.getDouble(LeavesQty.FIELD));
            } else {
                cancelAccept.setDouble(LeavesQty.FIELD, 0d);
            }

            // Logic for older FIX versions
            if (FixVersion.F42 == version) {
                // Not sure what we should really be sending here
                cancelAccept.setChar(ExecTransType.FIELD, ExecTransType.NEW);
            }

        } catch (FieldNotFound e) {
            throw new FieldNotFoundException(e);
        }

        return cancelAccept;
    }

    private Message createCancelReject(Message message, SessionID sessionId, String reason, 
            String orderId, OrdStatus orderStatus) {
        Session session = Session.lookupSession(sessionId);
        MessageFactory mf = session.getMessageFactory();

        Message cancelReject = mf.create(sessionId.getBeginString(), MsgType.ORDER_CANCEL_REJECT);

        try {
            cancelReject.setString(ClOrdID.FIELD, message.getString(ClOrdID.FIELD));
            cancelReject.setString(Text.FIELD, reason);
            cancelReject.setString(OrderID.FIELD, orderId);
            cancelReject.setString(OrigClOrdID.FIELD, message.getString(OrigClOrdID.FIELD));

            if (orderStatus == null) {
                cancelReject.setChar(OrdStatus.FIELD, OrdStatus.REJECTED);
            } else {
                cancelReject.setChar(OrdStatus.FIELD, orderStatus.getValue());
            }
            String msgType = message.getString(MsgType.FIELD);

            if (MsgType.ORDER_CANCEL_REQUEST.equals(msgType)) {
                cancelReject.setChar(CxlRejResponseTo.FIELD, CxlRejResponseTo.ORDER_CANCEL_REQUEST);
            } else if (MsgType.ORDER_CANCEL_REPLACE_REQUEST.equals(msgType)) {
                cancelReject.setChar(CxlRejResponseTo.FIELD, CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST);
            }
        } catch (FieldNotFound e) {
            throw new FieldNotFoundException(e);
        }

        return cancelReject;
    }

    private Message createFill(Message order, Price price, SessionID sessionId, FixVersion version) throws FieldNotFound {
        Message fill = createEmptyExecutionReport(sessionId);

        // Lookup the orderId
        String clientOrderId =  order.getString(ClOrdID.FIELD);
        String orderId = OrderDB.getInstance().getOrderId(clientOrderId);
        fill.setString(OrderID.FIELD, orderId);
        fill.setString(ClOrdID.FIELD, clientOrderId);
        fill.setUtcTimeStamp(TransactTime.FIELD, new Date());

        fill.setChar(ExecType.FIELD, ExecType.FILL);
        fill.setChar(OrdType.FIELD, order.getChar(OrdType.FIELD));
        fill.setChar(OrdStatus.FIELD, OrdStatus.FILLED);
        fill.setChar(Side.FIELD, order.getChar(Side.FIELD));
        fill.setChar(TimeInForce.FIELD, order.getChar(TimeInForce.FIELD));
        fill.setString(Account.FIELD, order.getString(Account.FIELD));

        double orderQty = order.getDouble(OrderQty.FIELD);
        fill.setDouble(Price.FIELD, price.getValue());
        fill.setDouble(LeavesQty.FIELD, 0d);
        fill.setDouble(CumQty.FIELD, orderQty);
        fill.setDouble(AvgPx.FIELD, price.getValue());

        fill.setString(Symbol.FIELD, order.getString(Symbol.FIELD));

        fill.setDouble(OrderQty.FIELD, orderQty);
        fill.setDouble(LastQty.FIELD, orderQty);
        fill.setDouble(LastPx.FIELD, price.getValue());

        if (FixVersion.F42 == version) {
            fill.setChar(ExecTransType.FIELD, ExecTransType.NEW);
        }
        
        return fill;
    }

    private Message createEmptyExecutionReport(SessionID sessionId) {
        Session session = Session.lookupSession(sessionId);
        MessageFactory mf = session.getMessageFactory();

        Message er = mf.create(sessionId.getBeginString(), MsgType.EXECUTION_REPORT);
        er.setString(ExecID.FIELD, genExecID().getValue());

        return er;
    }

    private Message createNewOrderAccept(Message order, SessionID sessionId, FixVersion version) throws FieldNotFound {
        Message accept = createEmptyExecutionReport(sessionId);

        // Some of this may be in-efficient, but it makes life easier
        String clientOrderId = order.getString(ClOrdID.FIELD);

        accept.setString(ClOrdID.FIELD, clientOrderId);
        accept.setChar(ExecType.FIELD, ExecType.FILL);
        accept.setChar(OrdStatus.FIELD, OrdStatus.NEW);
        accept.setChar(Side.FIELD, order.getChar(Side.FIELD));
        accept.setDouble(LeavesQty.FIELD, order.getDouble(OrderQty.FIELD));
        accept.setDouble(CumQty.FIELD, 0d);
        accept.setDouble(AvgPx.FIELD, 0);
        accept.setString(Symbol.FIELD, order.getString(Symbol.FIELD));

        // Store this so we use it later with the executions
        OrderDB db = OrderDB.getInstance();
        String orderId = db.addNewOrder(order);
        accept.setString(OrderID.FIELD, orderId);
        
        if (FixVersion.F42 == version) {
            accept.setChar(ExecTransType.FIELD, ExecTransType.NEW);
        }
        
        return accept;
    }

    public OrderID genOrderID() {
        String orderId = OrderDB.getInstance().nextOrderId();
        return new OrderID(orderId);
    }

    public ExecID genExecID() {
        return new ExecID(String.valueOf(++m_execID));
    }

    /**
     * Allows a custom market data provider to be specified.
     * 
     * @param marketDataProvider
     */
    public void setMarketDataProvider(IMarketDataProvider marketDataProvider) {
        this.marketDataProvider = marketDataProvider;
    }

    private long m_execID = System.currentTimeMillis();




}