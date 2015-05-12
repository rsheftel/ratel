package malbec.fer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import malbec.fer.fix.FixDestination;
import malbec.fer.jms.AbstractJmsApp;
import malbec.fer.jms.JmsServerApp;
import malbec.fix.FixClient;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.MessageUtil;
import malbec.util.StrategyAccountMapper;
import malbec.util.StringUtils;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ClOrdID;
import quickfix.field.OrderID;
import quickfix.field.TradeDate;

/**
 * Core of the Fix Execution Router.
 * 
 */
public class FerRouter implements PropertyChangeListener {

    final private Logger log = LoggerFactory.getLogger(getClass());

    static {
        TaskService.getInstance().addExecutor("FixRouter",
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RouterProcess")));
    }

    private ScheduledFuture<?> monitorFuture;

    Map<String, IOrderDestination> orderDestinations = new HashMap<String, IOrderDestination>();

    List<JmsServerApp> jmsClients = new ArrayList<JmsServerApp>();

    private EmailSettings emailSettings;
    private StrategyAccountMapper sam;

    // Start EntityManagerFactory
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    public FerRouter(EmailSettings emailSettings, StrategyAccountMapper sam) {
        this.emailSettings = emailSettings;
        this.sam = sam;

    }

    public FerRouter(EmailSettings emailSettings) {
        this(emailSettings, new StrategyAccountMapper(true));

    }

    public void addOrderDestination(IOrderDestination destination) {
        orderDestinations.put(destination.getDestinationName().toUpperCase(), destination);
        destination.addPropertyChangeListener(this);
    }

    public void setOrderDestinations(List<IOrderDestination> destinations) {
        for (IOrderDestination destination : destinations) {
            addOrderDestination(destination);
        }
    }

    public void addJmsConnection(JmsServerApp jsa) {
        jmsClients.add(jsa);
    }

    public void setJmsConnections(List<JmsServerApp> jmsApps) {
        for (JmsServerApp app : jmsApps) {
            addJmsConnection(app);
        }
    }

    public void start() throws InvalidConfigurationException {
        for (IOrderDestination destination : orderDestinations.values()) {
            destination.start();
        }
        
        for (AbstractJmsApp jmsApp : jmsClients) {
            jmsApp.start();
        }

        if (monitorFuture == null || monitorFuture.isCancelled()) {
            // start a monitor that ensures messages are processed
            ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance()
                    .getExecutor("FixRouter");
            monitorFuture = executor.scheduleAtFixedRate(new Runnable() {
                boolean once;

                @Override
                public void run() {
                    if (!once) {
                        log.info("Started FixRouter Thread");
                        once = true;
                    }
                    process();
                }
            }, 10, 10, TimeUnit.MILLISECONDS);
        }

        // Register our MBeans
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // TODO Should this be moved to the beans to do self registration
        for (IOrderDestination destination : orderDestinations.values()) {
            FixClient fixClient = null;
            try {
                if (destination instanceof FixDestination) {
                    fixClient = ((FixDestination)destination).getFixClient();
                    ObjectName beanName = new ObjectName(fixClient.getClass().getName() + ":name="
                            + fixClient.getSessionName());
                    mbs.registerMBean(fixClient, beanName);
                }
            } catch (Exception e) {
                log.error("Unable to register MBean for " + fixClient.getSessionName(), e);
            }
        }
    }

    public void stop() {
        // Stop the incoming messages first
        for (AbstractJmsApp jmsApp : jmsClients) {
            jmsApp.stop();
        }

        // Stop the processing of messages
        if (monitorFuture != null) {
            monitorFuture.cancel(false);
            monitorFuture = null;
        }

        // TODO this replaces the FIX clients
        for (IOrderDestination destination : orderDestinations.values()) {
            destination.stop();
        }
    }

    /**
     * Take any orders from the JMS clients and send using FIX clients
     */
    public synchronized void process() {
        // We only have one client, so this is a bit much - we have made assumptions
        // in other parts of the code that we only have ONE JMS client - if you change
        // that assumption, you must FIND and FIX the other locations
        for (JmsServerApp jmsApp : jmsClients) {
            synchronized (jmsApp) {
                while (jmsApp.getUnprocessedMessageCount() > 0) {
                    Map<String, String> message = jmsApp.getNextMessage();
                    if (message != null) {
                        try {
                            log.debug("Processing messages for jmsServer");
                            processMessage(jmsApp, message);
                        } catch (Throwable e) {
                            log.error("Unable to process jms message.  " + message, e);
                        }
                    }
                }
            }
        }
    }

    private void processMessage(JmsServerApp jmsApp, Map<String, String> message) {
        //List<String> errors = new ArrayList<String>();

        // We only understand two messages at this time
        if (MessageUtil.isNewOrder(message)) {
            //processNewOrder(jmsApp, message, errors);
            processNewOrder(jmsApp, message);
        } else if (MessageUtil.isOrderQuery(message)) {
            Order messageOrder = new Order(message);
            Order queryOrder = findOrderByClientOrderID(messageOrder.getClientOrderID());
            // send back the response, don't forget the original message ID
            if (queryOrder != null) {
                queryOrder.setReplyTo(messageOrder.getReplyTo());
                Map<String, String> queryOrderMap = queryOrder.toMap();
                MessageUtil.setOriginalMessageID(MessageUtil.getOriginalMessageID(message), queryOrderMap);
                jmsApp.publishResponse(queryOrderMap);
            } else {
                message.put("STATUS", "UNKNOWN");
                jmsApp.publishResponse(message, "Unknown order");
            }
        } else {
            jmsApp.publishResponse(message, "Unable to determine message type");
        }

    }

    private void processNewOrder(JmsServerApp jmsApp, Map<String, String> message) {
        Order order = new Order(message);

        // Apply any mappings we might have
        boolean badKey = populateMappings(order);
        // Determine our destination
        String platform = order.getPlatform();
        IOrderDestination destination = determineDestination(platform);
        List<String> errors = new ArrayList<String>();
        ITransportableOrder orderToSend = null;

        if (destination != null) {
            orderToSend = destination.createOrder(order);
            if (orderToSend != null) {
                List<String> orderErrors = orderToSend.errors();
                if (orderErrors.size() > 0) {
                    errors.addAll(orderErrors);
                }
            } else {
                errors.add("Unable to create order");
                log.error("Unable to create order for " + destination.getDestinationName());
            }
        }

        if (destination == null || platform == null || badKey || errors.size() > 0) {
            if (platform == null) {
                errors.add("No platform specified");
            } else if (destination == null) {
                errors.add("No destination mapping for platform");
            }

            if (badKey) {
                errors.add("Bad platform/strategy/security type");
            }
            message.put("STATUS", "INVALID");
            // send back a message about the invalid order
            jmsApp.publishResponse(message, errors);
        } else {
            // We need to prevent other threads from updating !!
            synchronized (getLock(order.getClientOrderID())) {
                // We are pretty sure we have a valid order, save it and send it
                long orderID = persistOrder(order);
                if (orderID != -1) {
                    if (orderToSend.transport()) {
                        updateOrder(order, "SENT");
                        message.put("STATUS", "SENT");
                    } else {
                        message.put("STATUS", "FAILED");
                        List<String> sendErrors = orderToSend.errors();
                        
                        if (sendErrors.size() >0) {
                            String combinedErrors = addErrorsToMessage(message, sendErrors);
                            updateOrder(order, "FAILED", combinedErrors);
                        } else {
                            message.put("ERROR_1", "Failed to send order");
                            updateOrder(order, "FAILED");
                        }
                    }
                    jmsApp.publishResponse(message);
                } else {
                    // unable to persist, send back response
                    jmsApp.publishResponse(message, "Unable to persist order, " + order.getStatus());
                }
            }
        }
    }



    private String addErrorsToMessage(Map<String, String> message, List<String> errors) {
        StringBuilder sb = new StringBuilder(1024);
        int i = 1;
        
        for (String error : errors) {
            message.put("ERROR_"+ i, "Failed to send order: " + error);
            sb.append(error).append(";");
            i++;
        }
        
        return sb.toString();
    }

    private IOrderDestination determineDestination(String platform) {
        if (platform == null) {
            return null;
        }
        return orderDestinations.get(platform.toUpperCase());
    }

    private boolean populateMappings(Order order) {
        boolean badKey = false;
        // map from strategy to Account, default to supplied account
        String account = sam.lookupAccount(order.getPlatform(), order.getStrategy(), order.getSecurityType());
        if (account != null) {
            order.setAccount(account);
        } else {
            badKey = true;
        }

        // Handle the symbol mapping here. This might be platform specific
        if ("Futures".equalsIgnoreCase(order.getSecurityType())) {
            order.setSecurityIDSource("A"); // Bloomberg
        }
        return badKey;
    }

    private boolean updateOrder(Order order, String status) {
        EntityManager em = null;
        try {
            synchronized (getLock(order.getClientOrderID())) {
                em = emf.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                Order dbOrder = em.getReference(Order.class, order.getId());
                dbOrder.setStatus(status);
                em.persist(dbOrder);
                tx.commit();
            }
            return true;
        } catch (RuntimeException e) {
            log.error("Unable to update order status:" + order.toString(), e);
            sendPersistenceError(order, "Unable to update status", e);
        } finally {
            em.close();
        }

        return false;
    }

    private boolean updateOrder(Order order, String status, String message) {
        EntityManager em = null;
        try {
            synchronized (getLock(order.getClientOrderID())) {
                em = emf.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                Order dbOrder = em.getReference(Order.class, order.getId());
                dbOrder.setStatus(status);
                // Limit the error message to the column size
                dbOrder.setMessage(message.substring(0, Math.min(message.length(),255)));
                em.persist(dbOrder);
                tx.commit();
            }
            return true;
        } catch (RuntimeException e) {
            log.error("Unable to update order status:" + order.toString(), e);
            sendPersistenceError(order, "Unable to update status", e);
        } finally {
            em.close();
        }

        return false;
    }

    private boolean updateOrder(String clientOrderID, LocalDate orderDate, String status) {
        synchronized (getLock(clientOrderID)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em
                    .createQuery("from Order as orders where orders.clientOrderID = :clientOrderID and orders.orderDate = :orderDate");
            query.setParameter("clientOrderID", clientOrderID);
            query.setParameter("orderDate", DateTimeUtil.getDate(orderDate));

            Order dbOrder = (Order) query.getSingleResult();

            dbOrder.setStatus(status);
            try {
                em.persist(dbOrder);
                tx.commit();
            } finally {
                em.close();
            }
        }
        return true;
    }

    long persistOrder(Order order) {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            em.persist(order);
            tx.commit();

            return order.getId();
        } catch (RuntimeException e) {
            log.error("Unable to save new order:" + order.toString(), e);
            Throwable sql = findSqlException(e);
            if (sql != null) {
                order.setStatus(sql.getMessage());
            } else {
                order.setStatus("FAILEDINSERT");
            }
            sendPersistenceError(order, "Unable to persist order - will not be sending.", e);
            try {
                tx.rollback(); // might be redundant as the Exception is suppose to be marked automatically
            } catch (RuntimeException e1) {
                log.error("Error during rollback", e1);
            }
        } finally {
            em.close();
        }

        return -1;
    }

    private Throwable findSqlException(Throwable e) {
        Throwable cause = e.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }

        return cause;
    }

    private void sendPersistenceError(Order order, String subject, RuntimeException e) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(subject).append("\n");
        sb.append("Order details:").append(order.toString()).append("\n\n");
        sb.append("Exception stack:\n");
        sb.append(StringUtils.exceptionToString(e));

        EmailSender sender = new EmailSender(emailSettings.getAsProperties());
        sender.sendMessage("FER - Unable to persist order", sb.toString());
    }

    /**
     * This updates the order status.
     * 
     * The QuickFIX/J simulator sends back a NEW order status with the orderID, REDI will send back a
     * PENDING_NEW and then NEW. We update the orderID on the NEW (active order).
     * 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            // TODO we may want to change this logic, the pending should have the orderID too
            String property = evt.getPropertyName();
            if ("OrderUpdate-PENDING_NEW".equals(property)) {
                Message fixMessage = (Message) evt.getNewValue();
                ClOrdID clientOrderID = new ClOrdID(fixMessage.getString(ClOrdID.FIELD));

                LocalDate tradeDate = new LocalDate();
                if (fixMessage.isSetField(TradeDate.FIELD)) {
                    String strDate = fixMessage.getString(TradeDate.FIELD);
                    tradeDate = DateTimeUtil.getLocalDate(strDate);
                }
                updateOrder(clientOrderID.getValue(), tradeDate, "PENDING");
                sendReponse(clientOrderID.getValue(), "PENDING");
            } else if ("OrderUpdate-NEW".equals(property)) {
                Message fixMessage = (Message) evt.getNewValue();
                OrderID orderID = new OrderID(fixMessage.getString(OrderID.FIELD));
                ClOrdID clientOrderID = new ClOrdID(fixMessage.getString(ClOrdID.FIELD));

                updateAcceptedOrder(clientOrderID.getValue(), orderID.getValue());
                log.info("Accepted clientOrderID=" + clientOrderID.getValue());
                sendReponse(clientOrderID.getValue(), "ACCEPTED");

            } else {
                log.warn("Received changeEvent of " + property);
            }
        } catch (FieldNotFound e) {

        }
    }

    private void sendReponse(String clientOrderID, String status) {
        Map<String, String> responseMessage = new HashMap<String, String>();
        responseMessage.put("ClientOrderID", clientOrderID);
        responseMessage.put("Status", status);

        Order order = findOrderByClientOrderID(clientOrderID);
        MessageUtil.setReplyTo(order.getReplyTo(), responseMessage);

        for (JmsServerApp jmsApp : jmsClients) {
            jmsApp.publishResponse(responseMessage);
        }
    }

    boolean updateAcceptedOrder(String clientOrderID, String orderID) {
        synchronized (getLock(clientOrderID)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em
                    .createQuery("from Order as orders where orders.clientOrderID = :clientOrderID");
            query.setParameter("clientOrderID", clientOrderID);

            Order dbOrder = (Order) query.getSingleResult();

            dbOrder.setStatus("ACCEPTED");
            dbOrder.setOrderID(orderID);
            try {
                em.persist(dbOrder);
                tx.commit();
            } finally {
                em.close();
            }
        }
        return true;
    }

    public Order findOrderByClientOrderID(String clientOrderID) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Query query = em
                .createQuery("from Order as orders where orders.clientOrderID = :clientOrderID");
        query.setParameter("clientOrderID", clientOrderID);

        Order dbOrder = null;

        // Put in our own logic as we may get no results
        List<?> results = query.getResultList();
        if (results.size() > 0) {
            dbOrder = (Order) results.get(0);
        }

        tx.commit();
        em.close();

        return dbOrder;
    }

    /**
     * Return the object to lock on.
     * 
     * We return the passed in string, depending on the JVM to ensure that it is not a duplicate.
     * 
     * If this does not solve our problem we may have to <code>intern</code> the string or use a different
     * mechanism.
     * 
     * @param clientOrderID
     * @return
     */
    @SuppressWarnings("all")
    private synchronized Object getLock(String clientOrderID) {
        // return clientOrderID;
        // Switching to using 'this' as we need security not speed at this time
        clientOrderID = clientOrderID;
        return this;
    }

}
