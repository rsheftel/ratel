package com.fftw.metadb.domain.jms;

import com.fftw.metadb.domain.AbstractLiveSubscriber;
import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.service.LiveSubscriber;
import static com.fftw.metadb.util.TextUtil.extractRecord;
import com.fftw.util.DBTools;
import com.fftw.util.PropertyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Implements the <code>LiveSubscriber</code> interface.
 * <p/>
 * Implemented as a <tt>singleton</tt>.
 * <p/>
 * This listens to JMS topics to publish data into OwnData.
 */
public class JmsLiveSubscriber extends AbstractLiveSubscriber implements LiveSubscriber, MessageListener, Observer {

    private static final String SELECT_RECORD = "select Topic from ActiveMQ where Name=?";

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    static final Logger staticLogger = LoggerFactory.getLogger(JmsLiveSubscriber.class);

    private static JmsLiveSubscriber instance;

    private final Object lockObject = new Object();

    private boolean disconnected;

    private Map<String, Set<LiveListener>> topicListeners = new HashMap<String, Set<LiveListener>>();
    private Map<String, MessageConsumer> consumers = new HashMap<String, MessageConsumer>();

    private boolean initialized;

    private ConnectionTask connectionTask;

    private JmsLiveSubscriber() {

    }

    public boolean isInitialized() {
        synchronized (lockObject) {
            return initialized;
        }
    }

    /**
     * Package scope for testing, should not be called otherwise
     */
    void clearSubscribers() {
        topicListeners.clear();
    }

    private boolean initialize() {
        String brokerUrl = PropertyLoader.getProperty("brokerUrl");
        logger.info("Loaded brokerUrl:" + brokerUrl);
        if (brokerUrl == null) {
            throw new NullPointerException("Unable to load brokerUrl from configuration");
        }

        synchronized (lockObject) {
            // connect to the broker
            connectionTask = new ConnectionTask(brokerUrl, lockObject);
            connectionTask.addObserver(this);
            Thread connectionThread = new Thread(connectionTask, "ReconnectThread");
            connectionThread.start();
            initialized = true;

            return initialized;
        }
    }

    /**
     * Package scoped for testing and local use
     *
     * @return
     */
    final Session getSession() {
        synchronized (lockObject) {
            return connectionTask.getSession();
        }
    }

    /**
     * Singleton method
     *
     * @return
     */
    public static JmsLiveSubscriber getInstance() {
        synchronized (JmsLiveSubscriber.class) {
            if (instance == null) {
                instance = new JmsLiveSubscriber();
            }

            if (!instance.isInitialized()) {
                instance.initialize();
                staticLogger.info("Initialized subscriber");
            }
            return instance;
        }
    }

    /**
     * Subscribe to a topic, using the specified <code>LiveListener</code>.
     *
     * @param name
     * @param liveListener
     * @return
     * @throws Exception
     */
    public boolean subscribe(String name, LiveListener liveListener) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Lookup the ActiveMQ name information - this builds the name in
            // a standard way so that other applications can read it
            con = DBTools.getConnection("DB.SystemDB");
            ps = con.prepareStatement(SELECT_RECORD);
            ps.setString(1, name);
            rs = ps.executeQuery();

            if (rs.next()) {
                subscribeToJms(rs.getString("Topic"), liveListener);
                return true;
            }

            logger.error("No database entry for " + name);
            // We did not find a mapping for how to subscribe, send the listener
            // and invalid record
            Map<String, String> fieldMap = createInvalidDataRecord();
            liveListener.onData(fieldMap);
        } finally {
            DBTools.close(rs, ps);
            DBTools.close(con);
        }

        return true;
    }

    /**
     * Add this <code>LiveListener</code> to the JMS listener.
     * <p/>
     * We only need on JMS listner per JMS topic.  We can have multiple <code>LiveListeners</code>
     * per topic.
     *
     * @param jmsTopic
     * @param liveListener
     * @throws Exception
     */
    private void subscribeToJms(String jmsTopic, LiveListener liveListener) throws Exception {

        // Check that we are not already subscribed
        synchronized (lockObject) {
            while (!connectionTask.isConnected()) {
                logger.info("Waiting " + jmsTopic);
                try {
                    lockObject.wait(connectionTask.getReconnectInterval() * 3); // wait for 3 connect attempts
                } catch (InterruptedException e) {
                    logger.info("Wait interrupted " + jmsTopic);
                }
            }
            Set<LiveListener> listenerSet = topicListeners.get(jmsTopic);

            // create a new subscriber for this topic (we may have multiple listeners per topic)
            if (listenerSet == null || listenerSet.isEmpty()) {
                listenerSet = new HashSet<LiveListener>();

                logger.info("Subscribing to: " + jmsTopic);
                Session session = getSession();
                Destination destination = session.createTopic(jmsTopic);
                MessageConsumer consumer = session.createConsumer(destination);
                topicListeners.put(jmsTopic, listenerSet);
                consumers.put(jmsTopic, consumer);
                consumer.setMessageListener(this);

                logger.info("Subscribed to " + jmsTopic);
            }
            // add this listener to our JMS listener
            logger.info("Added listener " + liveListener.getClass().getName() + ":" +
                    +liveListener.hashCode() + " to topic " + jmsTopic);
            listenerSet.add(liveListener);
        }

    }

    public void unsubscribe(String topic, LiveListener liveListener) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Lookup the ActiveMQ topic information - this builds the topic in
            // a standard way so that other applications can read it
            con = DBTools.getConnection("DB.SystemDB");
            ps = con.prepareStatement(SELECT_RECORD);
            ps.setString(1, topic);
            rs = ps.executeQuery();

            if (rs.next()) {
                unsubscribeFromJms(rs.getString("Topic"), liveListener);
                return;
            }
        } catch (Exception e) {
            logger.error("Unsubscription error for " + topic, e);
            throw e;
        } finally {
            DBTools.close(rs, ps);
            DBTools.close(con);
        }

    }

    private void unsubscribeFromJms(String jmsTopic, LiveListener liveListener) throws Exception {
        synchronized (lockObject) {
            Set<LiveListener> listenerSet = topicListeners.get(jmsTopic);

            // if we have subscribers remove the listener
            if (listenerSet != null) {
                logger.info("Removed listener " + liveListener.getClass().getName() + ":" + liveListener.hashCode() +
                        " from " + jmsTopic);
                listenerSet.remove(liveListener);
                // if we have no listeners left, unsubscribe
                if (listenerSet.isEmpty()) {
                    MessageConsumer consumer = consumers.get(jmsTopic);
                    // If we never subscribed, we will not have a consumer
                    if (consumer != null) {
                        consumer.setMessageListener(null);
                        topicListeners.remove(jmsTopic);
                        logger.info("Unsubscribed from topic " + jmsTopic);
                    }
                }
            }
        }
    }

    /**
     * Return a copy of all the topics that we are subscribed to
     *
     * @return
     */
    public Set<String> getSubscriptions() {
        return new HashSet(topicListeners.keySet());
    }

    /**
     * Received a message from ActiveMQ
     *
     * @param message
     */
    public void onMessage(Message message) {
        try {
            // parse the record and put it into a Map of name/values
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;

                if (logger.isDebugEnabled()) {
                    logger.debug(textMessage.getText());
                }

                Map<String, String> record = extractRecord(textMessage.getText());

                String topicName = record.get("MSTopicName");
                synchronized (lockObject) {
                    Set<LiveListener> listenerSet = topicListeners.get(topicName);
                    if (listenerSet == null || listenerSet.size() == 0) {
                        // ignore, we are subscribed, but no listeners
                        logger.error("We are subscribed to " + topicName + ", but no listeners");
                        return;
                    }

                    for (LiveListener listener : listenerSet) {
                        listener.onData(record);
                    }
                }
            } else {
                logger.warn("Received non-TextMessage", message);
            }
        } catch (JMSException e) {
            logger.error("Unable to extract text from TextMessage", e);
        }
    }

    public void update(Observable observed, Object eventArgs) {
        logger.info("Received event");
        if (observed instanceof ConnectionTask && eventArgs instanceof ConnectionEvent) {
            ConnectionEvent event = (ConnectionEvent) eventArgs;
            switch (event.getEventType()) {
                default:
                    break;
                case Connected:
                    processConnectedEvent();
                    break;
                case Diconnected:
                    logger.error("Connection disconnected");
                    processDisconnect();
                    break;
            }
        }
    }

    private void processConnectedEvent() {
        synchronized (lockObject) {
            // If this is not the first time, we need to reconnect our listeners
            if (disconnected) {
                // re-create consumers for each JmsTopic we are subscribed to
                logger.warn("Resubscribing after disconnect");
                resubscribeToJmsTopics();
            }
            logger.info("NotifyAll for connected");
            lockObject.notifyAll();
        }
    }

    private void processDisconnect() {
        synchronized (lockObject) {
            disconnected = true;
            consumers.clear();
        }
    }

    private void resubscribeToJmsTopics() {
        synchronized (lockObject) {
            Session session = getSession();
            for (String jmsTopic : getSubscriptions()) {
                try {
                    Destination destination = session.createTopic(jmsTopic);
                    MessageConsumer consumer = session.createConsumer(destination);
                    consumers.put(jmsTopic, consumer);
                    consumer.setMessageListener(this);
                    logger.info("Re-subscribed to " + jmsTopic);
                } catch (JMSException e) {
                    logger.error("Unable to resubscribe to topic " + jmsTopic);
                }
            }
        }
    }
}
