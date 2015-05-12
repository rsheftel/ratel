package malbec.fer;

import static malbec.fer.FerretState.*;
import static malbec.fer.OrderStatus.*;
import static malbec.fer.processor.AbstractOrderRequestProcessor.deteremineDestination;
import static malbec.util.MessageUtil.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import malbec.fer.dao.ExecutionReportDao;
import malbec.fer.dao.FixFillDao;
import malbec.fer.dao.OrderDao;
import malbec.fer.fix.FerFixClientApplication;
import malbec.fer.fix.FixDestination;
import malbec.fer.jms.AbstractJmsApp;
import malbec.fer.jms.JmsServerSessionApp;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.fer.processor.AbstractOrderRequestProcessor;
import malbec.fer.processor.CancelReplaceRequestProcessor;
import malbec.fer.processor.CancelRequestProcessor;
import malbec.fer.processor.IOrderRequestProcessor;
import malbec.fer.processor.OrderProcessor;
import malbec.fer.processor.SpreadTradeProcessor;
import malbec.fer.processor.StagedOrderProcessor;
import malbec.fer.rediplus.RediPlusDestination;
import malbec.fix.FixClient;
import malbec.fix.message.FixFill;
import malbec.fix.message.FixFillFactory;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.InvalidConfigurationException;
import malbec.util.MessageUtil;
import malbec.util.StringUtils;
import malbec.util.TaskService;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.UtcTimeStampField;
import quickfix.Message.Header;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.Currency;
import quickfix.field.ExecID;
import quickfix.field.ExecRefID;
import quickfix.field.ExecType;
import quickfix.field.LastCapacity;
import quickfix.field.LastMkt;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.ListID;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Product;
import quickfix.field.ReportToExch;
import quickfix.field.SenderCompID;
import quickfix.field.SenderSubID;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TradeDate;
import quickfix.field.TransactTime;

/**
 * Core of the Fix Execution Router.
 * 
 */
public class FerretRouter implements FerretRouterMBean, PropertyChangeListener, IMessageProcessor {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private final List<FerretState> STATES_THAT_ACCEPT_ORDERS = new ArrayList<FerretState>();

    static {
        TaskService.getInstance().createAndAddSingleThreadScheduled("FixRouter");
        TaskService.getInstance().createAndAddSingleThreadScheduled("ScheduleTimer");
    }

    Map<String, IOrderDestination> orderDestinations = new HashMap<String, IOrderDestination>();

    List<JmsServerSessionApp> jmsClients = new ArrayList<JmsServerSessionApp>();

    private EmailSettings emailSettings;
    private IDatabaseMapper dbm;

    private FerretSchedule ferretSchedule;
    // private EmailAlertManager emailAlertManager;

    private FerretState lastCheckedFerretState;

    // Start EntityManagerFactory

    public FerretRouter(EmailSettings emailSettings, IDatabaseMapper dbm) {
        this.emailSettings = emailSettings;
        this.dbm = dbm;
        // emailAlertManager = new EmailAlertManager(emailSettings);

        STATES_THAT_ACCEPT_ORDERS.add(Stage);
        STATES_THAT_ACCEPT_ORDERS.add(Ticket);
        STATES_THAT_ACCEPT_ORDERS.add(DMA);

        FixFillFactory.initialize(dbm);
    }

    public FerretRouter(EmailSettings emailSettings) {
        this(emailSettings, new DatabaseMapper(true));
    }

    public void addOrderDestination(IOrderDestination destination) {
        String name = destination.getDestinationName().toUpperCase();
        if (orderDestinations.containsKey(name)) {
            IOrderDestination old = orderDestinations.get(name);
            throw new IllegalArgumentException("Destination already exists: " + name + " for "
                + old.getClass().getCanonicalName());
        }
        orderDestinations.put(name, destination);
        destination.addPropertyChangeListener(this);
    }

    public void setOrderDestinations(List<IOrderDestination> destinations) {
        for (IOrderDestination destination : destinations) {
            addOrderDestination(destination);
        }
    }

    public void addJmsConnection(JmsServerSessionApp jsa) {
        jmsClients.add(jsa);
        jsa.setMessageProcessor(this);
    }

    public void setJmsConnections(List<JmsServerSessionApp> jmsApps) {
        for (JmsServerSessionApp app : jmsApps) {
            addJmsConnection(app);
        }
    }

    public void start() throws InvalidConfigurationException {
        ScheduledExecutorService ses = TaskService.getInstance().getExecutor("ScheduleTimer");

        final FerretRouter fr = this;
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                fr.ferretScheduleTimer();
            }
        }, 3000, 500, TimeUnit.MILLISECONDS);

        int initialStart = DateTimeUtil.minutesToWaitFor(15);
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                fr.ferretScheduleReloadTimer();
            }
        }, initialStart, 15, TimeUnit.MINUTES);

        log.info("Ferret Schedule reload first attempt in: " + initialStart);

        for (IOrderDestination destination : orderDestinations.values()) {
            log.info("Starting destination: " + destination.getDestinationName());
            destination.start();
        }

        for (AbstractJmsApp jmsApp : jmsClients) {
            jmsApp.start();
        }

        // Register our MBeans
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName bn = new ObjectName(getClass().getName() + ":name=" + getClass().getName());
            unregisterBeanIfRequired(mbs, bn);
            mbs.registerMBean(this, bn);
        } catch (Exception e) {
            log.error("Unable to register FerretRouter", e);
        }

        // TODO Should this be moved to the beans to do self registration
        for (IOrderDestination destination : orderDestinations.values()) {
            FixClient fixClient = null;
            try {
                if (destination instanceof FixDestination) {
                    fixClient = ((FixDestination) destination).getFixClient();
                    ObjectName beanName = new ObjectName(fixClient.getClass().getName() + ":name="
                        + fixClient.getSessionName());
                    unregisterBeanIfRequired(mbs, beanName);
                    mbs.registerMBean(fixClient, beanName);
                } else if (destination instanceof RediPlusDestination) {
                    RediPlusDestination rpd = (RediPlusDestination) destination;
                    ObjectName beanName = new ObjectName(rpd.getClass().getName() + ":name=" + rpd.getName());
                    unregisterBeanIfRequired(mbs, beanName);
                    mbs.registerMBean(rpd, beanName);
                }
            } catch (Exception e) {
                log.error("Unable to register MBean for " + fixClient.getSessionName(), e);
            }
        }

        FerretState fs = currentState();
        publishFerretState(fs);
        log.info("Ferret startup state is " + fs);
    }

    protected synchronized void ferretScheduleReloadTimer() {
        try {
            XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("ferretschedule.xml"));
            FerretSchedule fs = (FerretSchedule) factory.getBean("FerretSchedule");
            log.info("Reloading ferret schedule");

            setSchedule(fs);
        } catch (Exception e) {
            log.error("Failed to reload schedule", e);
        }
    }

    protected synchronized void ferretScheduleTimer() {
        try {
            if (ferretSchedule.advanceSchedule()) {
                log.info("Advanced schedule");
            }
            FerretState fs = currentState();
            if (lastCheckedFerretState != fs) {
                lastCheckedFerretState = fs;
                publishFerretState(lastCheckedFerretState);
                log.info("Ferret state is " + lastCheckedFerretState);
            }
        } catch (Exception e) {
            log.error("Problem with schedule thread", e);
        }
    }

    private void unregisterBeanIfRequired(MBeanServer mbs, ObjectName beanName)
        throws InstanceNotFoundException, MBeanRegistrationException {
        // Ensure we have not already registered, if so, re-register
        if (mbs.isRegistered(beanName)) {
            log
                .warn("Previously registered MBean '" + beanName
                    + "' being re-registered, replacing instance");
            mbs.unregisterMBean(beanName);
        }
    }

    public void stop() {
        // Stop the incoming messages first
        for (AbstractJmsApp jmsApp : jmsClients) {
            jmsApp.stop();
        }

        for (IOrderDestination destination : orderDestinations.values()) {
            destination.stop();
        }
    }

    /**
     * Determine the type of message we received and dispatch to the appropriate logic.
     * 
     * @param mpl
     * @param message
     */
    public Map<String, String> processMessage(IMessageProcessListener mpl, Map<String, String> message) {

        if (MessageUtil.isFerretMode(message)) {
            return processFerretMode(message);
        }

        String userOrderId = MessageUtil.getUserOrderId(message);
        DateTime orderDateTime = DateTimeUtil.guessDateTime(MessageUtil.getOrderDate(message));

        LocalDate orderDate = orderDateTime.toLocalDate();

        if (MessageUtil.isOrderQuery(message)) {
            return processQueryRequest(mpl, message, userOrderId, orderDate);
        }

        return processOrderRequest(mpl, message, userOrderId, orderDate);
    }

    private Map<String, String> processFerretMode(Map<String, String> message) {
        String newState = message.get("FERRETSTATE");

        log.info("Received state change request of: " + newState);
        if ("TICKET".equalsIgnoreCase(newState)) {
            setStateToTicket();
        } else if ("DMA".equalsIgnoreCase(newState)) {
            setStateToDma();
        } else if ("STAGE".equalsIgnoreCase(newState)) {
            setStateToStage();
        }
        FerretState fs = currentState();
        message.put("FERRETSTATE", fs.toString());

        log.info("FerretState=" + fs);

        return message;
    }

    private Map<String, String> processQueryRequest(IMessageProcessListener mpl, Map<String, String> message,
        String userOrderId, LocalDate orderDate) {
        OrderDao dao = OrderDao.getInstance();

        Order queryOrder = dao.findOrderByUserOrderId(userOrderId, orderDate);
        // send back the response, don't forget the original message ID
        if (queryOrder != null) {
            Map<String, String> queryOrderMap = queryOrder.toMap();
            MessageUtil.setOriginalMessageID(MessageUtil.getOriginalMessageID(message), queryOrderMap);
            mpl.broadcastStatus(userOrderId, orderDate, queryOrderMap);
            return queryOrderMap;
        } else {
            Map<String, String> unknownOrderMap = new HashMap<String, String>(message);
            MessageUtil.setStatus(unknownOrderMap, Unknown.name());
            mpl.broadcastStatus(userOrderId, orderDate, unknownOrderMap, "Unknown order");
            return unknownOrderMap;
        }
    }

    Map<String, String> processOrderRequest(IMessageProcessListener mpl, Map<String, String> message,
        String userOrderId, LocalDate orderDate) {
        // Check that we are actually accepting orders at this time
        FerretState ferretState = currentState();

        if (!acceptingOrders(ferretState)) {
            MessageUtil.setStatus(message, FerretRejected.toString());
            mpl.broadcastStatus(userOrderId, orderDate, message);
            // TODO send out emails when we are rejecting after a certain time
            // This is put into a new application to monitor the Ferret and Tomahawk
            // if (ferretSchedule.isWithinRejectEmailSchedule()) {
            // emailAlertManager.send("AfterHoursReject", "After Hours Order Reject", message.toString());
            // }
            return message;
        }

        // We need to enforce security on these messages need platform and hostname
        if (!canTradeOnPlatform(message)) {
            mpl.sendResponse(message, "No authority to trade on platform '"
                + MessageUtil.getPlatform(message) + "'");
        }

        IOrderRequestProcessor op = processorForOrder(message);
        if (op == null) {
            mpl.broadcastStatus(userOrderId, orderDate, message, "Unable to determine message type");
            log.warn("Unable to process message type: " + message);
            return message;
        }

        Map<String, String> processResults = op.process(message, orderDestinations, ferretState);

        MessageUtil.setOriginalMessageID(MessageUtil.getOriginalMessageID(message), processResults);

        if (MessageUtil.getReplyTo(processResults) != null) {
            mpl.sendResponse(processResults, "");
        } else {
            mpl.broadcastStatus(userOrderId, orderDate, processResults);
        }
        return processResults;
    }

    private boolean acceptingOrders(FerretState ferretState) {
        return STATES_THAT_ACCEPT_ORDERS.contains(ferretState);
    }

    private AbstractOrderRequestProcessor processorForOrder(Map<String, String> message) {
        if (isNewOrder(message)) {
            // TODO we should be able to cache these
            return new OrderProcessor(dbm);
        } else if (isSpreadTrade(message)) {
            return new SpreadTradeProcessor(dbm);
        } else if (isCancelOrder(message)) {
            return new CancelRequestProcessor(dbm, this);
        } else if (isReplaceOrder(message)) {
            return new CancelReplaceRequestProcessor(dbm, this);
        } else if (isReleaseStagedOrder(message)) {
            return new StagedOrderProcessor(dbm);
        }

        return null;
    }

    private boolean canTradeOnPlatform(Map<String, String> message) {
        // String clientHostname = MessageUtil.getClientHostname(message);
        // String platform = MessageUtil.getPlatform(message);
        //        
        // return dbm.canClientTradeOnPlatform(clientHostname, platform);
        return message != null; // not implemented yet
    }

    boolean populateMappings(Order order) {
        boolean badKey = false;
        // map from strategy to Account, default to supplied account
        String account = dbm.lookupAccount(order.getPlatform(), order.getStrategy(), order.getSecurityType());
        if (account != null) {
            order.setAccount(account);
        } else {
            badKey = true;
        }

        return badKey;
    }

    private void sendEventError(FerRejectEvent event, String subject) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(subject).append("\n");
        sb.append("Event: ").append(event.getPropertyName()).append("\n");
        sb.append("Raw message: " + event.getFixMessage());
        sb.append("\n\n");
        sb.append("Referenced message: " + event.getReferencedMessage());
        sb.append("\n\n");

        EmailSender sender = new EmailSender(emailSettings.getAsProperties());
        sender.sendMessage("FER - Unable to process event", sb.toString());
    }

    private void sendEventError(FerFixEvent event, String subject) {
        sendEventError(event, subject, null);
    }

    private void sendEventError(FerFixEvent event, String subject, Exception e) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(subject).append("\n");
        sb.append("Event: ").append(event.getPropertyName()).append("\n");
        sb.append("Raw message: " + event.getFixMessage());
        sb.append("\n\n");

        if (e != null) {
            sb.append("Exception stack:\n");
            sb.append(StringUtils.exceptionToString(e));
        }

        EmailSender sender = new EmailSender(emailSettings.getAsProperties());
        sender.sendMessage("FER - Unable to process event", sb.toString());
    }

    /**
     * This updates the order status.
     * 
     * The QuickFIX/J simulator sends back a NEW order status with the orderID, REDI will send back a
     * PENDING_NEW and then NEW. We update the orderID on the NEW (active order).
     * 
     */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event instanceof FerFixEvent) {
            propertyChange((FerFixEvent) event);
        } else if (event instanceof FerStageEvent) {
            propertyChange((FerStageEvent) event);
        } else {
            // this is from REDI
            String propertyName = event.getPropertyName().toUpperCase();
            // Since we may restart the process and don't want to re-update the DB,
            // do some sanity checks on the time
            Map<String, String> record = (Map<String, String>) event.getNewValue();

            // if ("TICKET".equalsIgnoreCase(propertyName) || "EXECUTION".equalsIgnoreCase(propertyName)) {
            if (!"INFO".equalsIgnoreCase(propertyName)) {
                // LocalTime localTime = new LocalTime(record.get("Time"));
                DateTime dateTime = DateTimeUtil.getCurrentFromTime(record.get("TIME"));
                String clientOrderID = extractClientOrderID(record);

                if (clientOrderID != null) {
                    OrderStatus status = OrderStatus.fromString(MessageUtil.getStatus(record));
                    try {
                        OrderDao dao = OrderDao.getInstance();
                        Order updatedOrder = dao.updateOrder(clientOrderID, status, dateTime);
                        if (updatedOrder != null) {
                            sendReponse(updatedOrder, status);
                        }
                    } catch (NoResultException e) {
                        log.warn("Unable to find ClientOrderId=" + clientOrderID + " to update status to '"
                            + status + "'.  Are we pointing to the correct database?");
                    } catch (PersistenceException e) {
                        log.error("Problem updating order: " + clientOrderID, e);
                    }
                }
            }
        }
    }

    private String extractClientOrderID(Map<String, String> record) {
        String memo = record.get("MEMO");

        if (memo == null || memo.trim().length() == 0) {
            String clientData = record.get("CLIENTDATA");

            if (clientData == null || clientData.trim().length() == 0) {
                // return null if we have an empty string
                return null;
            } else {
                return clientData;
            }
        }

        return memo;
    }

    private void propertyChange(FerStageEvent event) {
        OrderDao dao = OrderDao.getInstance();
        String clientOrderId = event.getClientOrderId();
        Order uo = dao.findOrderByClientOrderID(clientOrderId);

        sendReponse(uo, Cancelled);
    }

    /**
     * Process FIX based execution reports.
     * 
     * @param ferEvent
     */
    private void propertyChange(FerFixEvent ferEvent) {
        try {
            if ("NONE".equals(ferEvent.getClientOrderId())) {
                // && ! ("OrderUpdate-CANCELLED".equals(property))) {
                // TODO handle these - TradeStation specific
                // We receive drop copies for orders here - the ClientOrderID == 'NONE'
                log.warn("Recieved an execution report for GUI order");
                return;
            }

            FixEventType eventType = ferEvent.getEventType();

            OrderDao dao = OrderDao.getInstance();
            if (FixEventType.PendingNew == eventType) {
                handleEventPendingNew(ferEvent, dao);
            } else if (FixEventType.New == eventType) {
                handleEventNew(ferEvent, dao);
            } else if (FixEventType.OrderRejected == eventType) {
                handleEventReject(ferEvent, dao);
            } else if (FixEventType.Fill == eventType) {
                handleEventFill(ferEvent, dao);
            } else if (FixEventType.Expired == eventType) {
                handleEventExpired(ferEvent, dao);
            } else if (FixEventType.Cancelled == eventType) {
                handleEventCancelled(ferEvent, dao);
            } else if (FixEventType.PendingCancel == eventType) {
                handleEventPendingCancel(ferEvent, dao);
            } else if (FixEventType.Replaced == eventType) {
                handleEventReplaced(ferEvent, dao);
            } else if (FixEventType.CancelReject == eventType) {
                handleEventCancelReject(ferEvent, dao);
            } else if (FixEventType.BusinessMessageReject == eventType) {
                handleEventBusinessReject(ferEvent, dao);
            } else if (FixEventType.MessageReject == eventType) {
                handleEventMessageReject(ferEvent, dao);
            } else {
                log.warn("Received unhandled event of " + eventType);
            }
        } catch (PersistenceException e) {
            sendEventError(ferEvent, "Processing Event", e);
            log.error("Failed to process event: " + ferEvent, e);
        }
    }

    private void handleEventMessageReject(FerFixEvent ferEvent, OrderDao dao) {
        if (ferEvent instanceof FerRejectEvent) {
            FerRejectEvent bEvent = (FerRejectEvent) ferEvent;

            try {
                // get the referenced message and update the order
                if (bEvent.getRejectReasonCode() == FerFixClientApplication.SEQUENCE_NUMBER_LOGON) {
                    log.warn("Received failed logon event");
                    return;
                }
                String msgType = bEvent.getReferencedMessage().getHeader().getString(MsgType.FIELD);
                String errorMessage = bEvent.getErrorMessage();
                String clientOrderId = bEvent.getReferencedMessage().getString(ClOrdID.FIELD);

                if (MsgType.ORDER_SINGLE.equals(msgType) || MsgType.ORDER_CANCEL_REQUEST.equals(msgType)
                    || MsgType.ORDER_CANCEL_REPLACE_REQUEST.equals(msgType)) {
                    Order uo = dao.updateOrder(clientOrderId, PlatformRejected, errorMessage);
                    sendReponse(uo, PlatformRejected, errorMessage);
                }
            } catch (FieldNotFound e) {
                log.error("Tag not in referenced message", e);
            }
            sendEventError(bEvent, "Message Rejected");
        } else {
            sendEventError(ferEvent, "Unknown Message Rejected");
        }
    }

    private void handleEventBusinessReject(FerFixEvent ferEvent, OrderDao dao) {
        if (ferEvent instanceof FerRejectEvent) {
            FerRejectEvent bEvent = (FerRejectEvent) ferEvent;
            if (bEvent.getRejectReasonCode() == 3) {
                try {
                    // get the referenced message and update the order
                    String clientOrderId = bEvent.getReferencedMessage().getString(ClOrdID.FIELD);
                    String errorMessage = bEvent.getErrorMessage();
                    Order uo = dao.updateOrder(clientOrderId, PlatformRejected, errorMessage);
                    sendReponse(uo, PlatformRejected, errorMessage);
                } catch (FieldNotFound e) {
                    log.error("ClientOrderId not in referenced message", e);
                }
            } else {
                sendEventError(bEvent, "Unknown BusinessMessageReject");
            }
        } else {
            sendEventError(ferEvent, "Unknown BusinessMessageReject");
        }
    }

    private void handleEventCancelReject(FerFixEvent ferEvent, OrderDao dao) {
        String clientOrderID = ferEvent.getClientOrderId();

        // check the error code: tag 58
        String text = ferEvent.getErrorMessage();
        if (text.startsWith("GC101")) {
            Order uo = dao.updateOrder(clientOrderID, PlatformRejected);
            sendReponse(uo, PlatformRejected);
            // } else if (text.startsWith("GC100")) {
            // log.warn("Add logic to handle this: " + text);
        } else {
            Order uo = dao.updateOrder(clientOrderID, PlatformRejected, text);
            sendReponse(uo, PlatformRejected);
        }
    }

    private void handleEventReplaced(FerFixEvent ferEvent, OrderDao dao) {
        // If we requested the cancel/replace, we will have an OriginalClientOrderId
        String clientOrderID = ferEvent.getClientOrderId();
        String originalClientOrderId = ferEvent.getOriginalClientOrderId();
        if (originalClientOrderId != null) {
            // We requested the cancel/replace, update the original order
            String orderID = ferEvent.getOrderID();

            Order uoo = dao.updateOrder(originalClientOrderId, Replaced);
            sendReponse(uoo, Replaced);
            Order uo = dao.updateOrderWithOrderId(clientOrderID, Accepted, orderID);
            sendReponse(uo, Accepted);
        } else {
            Order uo = dao.updateOrder(clientOrderID, PendingCancel);
            sendReponse(uo, PendingCancel);
        }
    }

    private void handleEventPendingCancel(FerFixEvent ferEvent, OrderDao dao) {
        // If we requested the cancel, we will have an OriginalClientOrderId
        String clientOrderID = ferEvent.getClientOrderId();
        String originalClientOrderId = ferEvent.getOriginalClientOrderId();
        String orderId = ferEvent.getOrderID();
        if (originalClientOrderId != null) {
            // We requested the cancel, update the original order
            Order uoo = dao.updateOrder(originalClientOrderId, PendingCancel);
            sendReponse(uoo, PendingCancel);
            Order uo = dao.updateOrderWithOrderId(clientOrderID, PendingNew, orderId);
            sendReponse(uo, PendingNew);
        } else {
            // unsolicited cancel
            Order uo = dao.updateOrder(clientOrderID, PendingCancel);
            sendReponse(uo, PendingCancel);
        }
    }

    private void handleEventCancelled(FerFixEvent ferEvent, OrderDao dao) {
        String clientOrderID = ferEvent.getClientOrderId();
        String originalClientOrderId = ferEvent.getOriginalClientOrderId();
        String orderId = ferEvent.getOrderID();
        if (originalClientOrderId != null) {
            if (originalClientOrderId.equals(clientOrderID)) {
                // unsolicited cancel
                Order uo = dao.updateOrder(clientOrderID, Cancelled);
                sendReponse(uo, Cancelled);
            } else {
                // We requested the cancel, update the original order
                Order uoo = dao.updateOrder(originalClientOrderId, Cancelled);
                sendReponse(uoo, Cancelled);
                // dao.updateOrder(clientOrderID, Accepted);
                Order uo = dao.updateOrderWithOrderId(clientOrderID, Accepted, orderId);
                sendReponse(uo, Accepted);
            }
        } else {
            // unsolicited cancel
            Order uo = dao.updateOrder(clientOrderID, Cancelled);
            sendReponse(uo, Cancelled);
        }
    }

    private void handleEventExpired(FerFixEvent ferEvent, OrderDao dao) {
        String clientOrderID = ferEvent.getClientOrderId();
        Order uo = dao.updateOrder(clientOrderID, Expired);
        sendReponse(uo, Expired);
    }

    private void handleEventFill(FerFixEvent ferEvent, OrderDao dao) {
        try {
            String clientOrderID = ferEvent.getClientOrderId();

            Message fixMessage = ferEvent.getFixMessage();

            // Record the execution as it comes in
            FixFill fill = FixFillFactory.valueOf(fixMessage);
            long exeuctionId = insertExecutionReport(fixMessage);
            long fixFillId = insertFixFill(fill);

            if (log.isDebugEnabled()) {
                log.debug("Persisted executionId of: " + exeuctionId);
                log.debug("Persisted fixFillId of: " + fixFillId);
            }

            if (fill.isBust()) {
                FixFill bustedFill = findBustedFill(fill);
                log.info("ExecutionBusted: " + bustedFill);
            }

            if (fill.getLeavesQuantity() > 0d) {
                Order uo = dao.updateExecutedQuantity(clientOrderID, Executing, fill.getCumulatedQuantity());
                sendReponse(uo, Executing);
            } else {
                Order uo = dao.updateExecutedQuantity(clientOrderID, Filled, fill.getCumulatedQuantity());
                sendReponse(uo, Filled);
            }
        } catch (Exception e) {
            log.error("Issue processing FIX Fill", e);
        }
    }

    private void handleEventReject(FerFixEvent ferEvent, OrderDao dao) {
        String clientOrderID = ferEvent.getClientOrderId();

        // The text will sometimes start with GC100, do we want to
        // do something special with this?
        Order uo = dao.updateOrder(clientOrderID, PlatformRejected, ferEvent.getErrorMessage());
        sendReponse(uo, PlatformRejected, ferEvent.getErrorMessage());
        log.info("Rejected clientOrderID=" + clientOrderID + " " + ferEvent.getErrorMessage());
    }

    private void handleEventNew(FerFixEvent ferEvent, OrderDao dao) {
        String orderID = ferEvent.getOrderID();
        String clientOrderID = ferEvent.getClientOrderId();

        Order uo = dao.updateOrderWithOrderId(clientOrderID, Accepted, orderID);
        log.info("Accepted clientOrderID=" + clientOrderID);
        sendReponse(uo, Accepted);
    }

    /**
     * Bloomberg sends back the same OrderId for the CancelRequest as the original order. This is problematic
     * as all other platforms create new OrderIds.
     * 
     * @param ferEvent
     * @param dao
     */
    private void handleEventPendingNew(FerFixEvent ferEvent, OrderDao dao) {
        String orderID = ferEvent.getOrderID();
        String clientOrderID = ferEvent.getClientOrderId();

        Order uo = null;
        if (orderID != null) {
            uo = dao.updateOrderWithOrderId(clientOrderID, PendingNew, orderID);
        } else {
            uo = dao.updateOrder(clientOrderID, PendingNew);
        }
        sendReponse(uo, PendingNew);
    }

    private long insertFixFill(FixFill fill) {
        if (fill == null || fill.isOrderAck()) {
            System.err.println("Skipping execution report: " + fill);
            return -1;
        }

        FixFillDao dao = FixFillDao.getInstance();

        return dao.persist(fill);
    }

    private FixFill findBustedFill(FixFill fill) {
        FixFillDao dao = FixFillDao.getInstance();

        return dao.findById(fill.getBeginString(), fill.getSenderCompId(), fill.getSenderSubId(), fill
            .getTradeDate(), fill.getExecutionReferenceId());
    }

    private long insertExecutionReport(Message fixMessage) {
        malbec.fer.ExecutionReport er = new malbec.fer.ExecutionReport();

        try {
            Header header = fixMessage.getHeader();
            er.setSenderCompId(header.getString(SenderCompID.FIELD));
            er.setSendingTime(header.getField(new UtcTimeStampField(SendingTime.FIELD)).getValue());

            er.setExecutionId(fixMessage.getString(ExecID.FIELD));
            er.setOrderId(fixMessage.getString(OrderID.FIELD));
            er.setExecutionType(fixMessage.getString(ExecType.FIELD));
            er.setOrderStatus(fixMessage.getString(OrdStatus.FIELD));
            er.setSymbol(fixMessage.getString(Symbol.FIELD));
            er.setSide(fixMessage.getString(Side.FIELD));
            er.setOrderQuantity(fixMessage.getDecimal(OrderQty.FIELD));
            er.setOrderType(fixMessage.getString(OrdType.FIELD));
            er.setTimeInForce(fixMessage.getString(TimeInForce.FIELD));
            er.setLastQuantity(fixMessage.getDecimal(LastQty.FIELD));
            er.setLastPrice(fixMessage.getDecimal(LastPx.FIELD));
            er.setLeavesQuantity(fixMessage.getDecimal(LeavesQty.FIELD));
            er.setCumulatedQuantity(fixMessage.getDecimal(CumQty.FIELD));
            er.setAveragePrice(fixMessage.getDecimal(AvgPx.FIELD));
            er.setTransactionTime(fixMessage.getUtcTimeStamp(TransactTime.FIELD));

            // optional fields
            if (fixMessage.isSetField(Price.FIELD)) {
                er.setPrice(fixMessage.getDecimal(Price.FIELD));
            }
            if (fixMessage.isSetField(ListID.FIELD)) {
                er.setListId(fixMessage.getString(ListID.FIELD));
            }
            if (fixMessage.isSetField(ExecRefID.FIELD)) {
                er.setExecutionReferenceId(fixMessage.getString(ExecRefID.FIELD));
            }
            if (fixMessage.isSetField(ClOrdID.FIELD)) {
                er.setClientOrderId(fixMessage.getString(ClOrdID.FIELD));
            }
            if (fixMessage.isSetField(SenderSubID.FIELD)) {
                er.setSenderSubId(fixMessage.getString(SenderSubID.FIELD));
            }
            if (fixMessage.isSetField(Account.FIELD)) {
                er.setAccount(fixMessage.getString(Account.FIELD));
            }
            if (fixMessage.isSetField(StopPx.FIELD)) {
                er.setStopPrice(fixMessage.getDecimal(StopPx.FIELD));
            }
            if (fixMessage.isSetField(Currency.FIELD)) {
                er.setCurrency(fixMessage.getString(Currency.FIELD));
            }
            if (fixMessage.isSetField(LastMkt.FIELD)) {
                er.setLastMarket(fixMessage.getString(LastMkt.FIELD));
            }
            if (fixMessage.isSetField(LastCapacity.FIELD)) {
                er.setLastCapacity(fixMessage.getString(LastCapacity.FIELD));
            }
            if (fixMessage.isSetField(TradeDate.FIELD)) {
                er.setTradeDate(fixMessage.getUtcDateOnly(TradeDate.FIELD));
            } else {
                er.setTradeDate(new LocalDate().toDateMidnight().toDate());
            }

            if (fixMessage.isSetField(ReportToExch.FIELD)) {
                er.setReportToExchange(fixMessage.getString(ReportToExch.FIELD));
            }
            if (fixMessage.isSetField(Product.FIELD)) {
                er.setProduct(fixMessage.getString(Product.FIELD));
            }
        } catch (FieldNotFound e) {
            log.error("Unable to persist execution report", e);
        }

        ExecutionReportDao dao = ExecutionReportDao.getInstance();

        return dao.persistExecutionReport(er);
    }

    private void sendReponse(Order order, OrderStatus status) {
        sendReponse(order, status, null);
    }

    private void sendReponse(Order order, OrderStatus status, String errorMessage) {
        LocalDate orderDate = new LocalDate(order.getOrderDate());

        Map<String, String> responseMessage = new HashMap<String, String>();
        MessageUtil.setUserOrderId(responseMessage, order.getUserOrderId());
        MessageUtil.setStatus(responseMessage, status.toString());
        MessageUtil.setOrderDate(responseMessage, orderDate.toString("YYYY-MM-dd"));

        if (MessageUtil.getDestination(responseMessage) == null) {
            MessageUtil.setDestination(responseMessage, deteremineDestination(order.getExchange()));
        }

        for (JmsServerSessionApp jmsApp : jmsClients) {
            jmsApp.broadcastStatus(order.getUserOrderId(), orderDate, responseMessage, errorMessage);
        }
    }

    private void publishFerretState(FerretState fs) {
        for (JmsServerSessionApp jmsApp : jmsClients) {
            jmsApp.publishStatus(fs);
        }
    }

    public FerretState currentState() {
        return ferretSchedule.currentScheduleState();
    }

    public String currentStateString() {
        return currentState().toString();
    }

    public synchronized void setSchedule(FerretSchedule fs) {
        // Ensure we do not over-ride any manual states
        if (ferretSchedule != null) {
            fs.setCurrentState(ferretSchedule.getCurrentState());
        }

        ferretSchedule = fs;
        ferretSchedule.advanceSchedule();
    }

    public boolean setStateToTicket() {
        FerretState currentState = ferretSchedule.setStateToTicket();
        if (Ticket == currentState) {
            publishFerretState(currentState);
            return true;
        }
        return false;
    }

    public boolean setStateToStage() {
        FerretState currentState = ferretSchedule.setStateToStage();

        if (Stage == currentState) {
            publishFerretState(currentState);
            return true;
        }

        return false;
    }

    public boolean setStateToDma() {
        FerretState currentState = ferretSchedule.setStateToDma();

        if (DMA == currentState) {
            publishFerretState(currentState);
            return true;
        }
        return false;
    }

}