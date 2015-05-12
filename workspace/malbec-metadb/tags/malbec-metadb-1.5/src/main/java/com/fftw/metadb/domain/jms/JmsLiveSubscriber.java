package com.fftw.metadb.domain.jms;

import static com.fftw.metadb.util.MessageUtil.extractRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.metadb.domain.AbstractLiveSubscriber;
import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.service.LiveSubscriber;
import com.fftw.util.DBTools;
import com.fftw.util.PropertyLoader;

/**
 * Implements the <code>LiveSubscriber</code> interface.
 * Implemented as a <tt>quasi-singleton</tt>.
 * 
 * This listens to JMS topics to publish data on ActiveMQ/JMS.
 */
public class JmsLiveSubscriber extends AbstractLiveSubscriber implements LiveSubscriber, MessageListener, Observer {

    private static final String SELECT_RECORD = "select Topic from ActiveMQ where Name=?";

    private static final String RETRO_SUBSRIBER = "?consumer.retroactive=true";
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    static final Logger staticLogger = LoggerFactory.getLogger(JmsLiveSubscriber.class);

    //private static JmsLiveSubscriber instance;
    private static final Map<String, JmsLiveSubscriber> instances = new HashMap<String, JmsLiveSubscriber>();

    private String name = "default";

    private static String brokerUrl;

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

    private boolean initialize(String brokerUrl) {

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

    public void shutdown() {
        synchronized (lockObject) {
            connectionTask.stop();
            initialized = false;
            instances.remove(name);
        }
    }

    /**
     * Singleton method
     * 
     * Get the default instance.
     * @return
     */
    public static JmsLiveSubscriber getInstance() {
        return getInstance("default");
    }

    /**
     * Return the shared named instance.
     * 
     * @param instanceName
     * @return
     */
    public static JmsLiveSubscriber getInstance(String instanceName) {
        if (brokerUrl == null) {
            brokerUrl = PropertyLoader.getProperty("brokerUrl");
            staticLogger.info("Loaded brokerUrl:" + brokerUrl);
        }

        return getInstance(instanceName, brokerUrl);
    }

    /**
     * This was added to make it easier to test.
     * 
     * @param instanceName
     * @param brokerUrl
     * @return
     */
    static JmsLiveSubscriber getInstance(String instanceName, String brokerUrl) {
        synchronized (JmsLiveSubscriber.class) {
            JmsLiveSubscriber instance = instances.get(instanceName);
            if (instance == null) {
                instance = new JmsLiveSubscriber();
                instance.name = instanceName;
                instances.put(instanceName, instance);
            }

            if (!instance.isInitialized()) {
                instance.initialize(brokerUrl);
                staticLogger.info("Initialized subscriber - " + instanceName);
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
                subscribeToJms(rs.getString("Topic"), liveListener, true);
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

    private String getRetroTopic(String topicName) {
        return topicName + RETRO_SUBSRIBER;
    }

    /**
     * Add this <code>LiveListener</code> to the JMS listener.
     * <p/>
     * We only need on JMS listener per JMS topic.  We can have multiple <code>LiveListeners</code>
     * per topic.
     *
     * @param jmsTopic
     * @param liveListener
     * @throws Exception
     */
    void subscribeToJms(String jmsTopic, LiveListener liveListener, boolean useRetro) throws Exception {

        if (useRetro) {
            jmsTopic = getRetroTopic(jmsTopic);
        }
        // Check that we are not already subscribed
        MessageConsumer consumer = null;
        synchronized (lockObject) {
            while (!connectionTask.isConnected()) {
                try {
                    lockObject.wait(connectionTask.getReconnectInterval() * 3); // wait for 3 connect attempts
                } catch (InterruptedException e) {
                    logger.warn("Wait interrupted " + jmsTopic);
                }
            }
            Set<LiveListener> listenerSet = topicListeners.get(jmsTopic);

            // create a new subscriber for this topic (we may have multiple listeners per topic)
            if (listenerSet == null || listenerSet.isEmpty()) {
                listenerSet = new HashSet<LiveListener>();

                logger.info("Subscribing to: " + jmsTopic);
                Session session = getSession();
//                logger.info("Creating Topic for: " + jmsTopic);
                Destination destination = session.createTopic(jmsTopic);
//                logger.info("Creating consumer for: " + jmsTopic);
                consumer = session.createConsumer(destination);
//                logger.info("Storing for: " + jmsTopic);
                topicListeners.put(jmsTopic, listenerSet);
                consumers.put(jmsTopic, consumer);

                logger.info("Subscribed to " + jmsTopic);
            }
            // add this listener to our JMS listener
            logger.info("Added listener " + liveListener.getClass().getName() + ":" +
                    +liveListener.hashCode() + " to topic " + jmsTopic);
            listenerSet.add(liveListener);
        }
        // To work around us deadlocking with ActiveMQ, move the consumer listener assignment 
        // out of the lock
        if (consumer != null) {
            logger.info("Setting listener for: " + jmsTopic);
            consumer.setMessageListener(this);
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
                unsubscribeFromJms(rs.getString("Topic"), liveListener, true);
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

    private void unsubscribeFromJms(String jmsTopic, LiveListener liveListener, boolean useRetro) throws Exception {
        if (useRetro) {
            jmsTopic = getRetroTopic(jmsTopic);
        }
        
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
            } else {
                logger.warn("Did not find live listener in topic listeners, " + jmsTopic);
            }
        }
    }

    /**
     * Return a copy of all the topics that we are subscribed to
     *
     * @return
     */
    public Set<String> getSubscriptions() {
        return new HashSet<String>(topicListeners.keySet());
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

                String topicName = getTopicName(message);
                synchronized (lockObject) {
                    Set<LiveListener> listenerSet = topicListeners.get(topicName);
                    Set<LiveListener> retroListenerSet = topicListeners.get(getRetroTopic(topicName));

                    if ((listenerSet == null || listenerSet.size() == 0)
                            && (retroListenerSet == null || retroListenerSet.size() == 0)) {
                        // ignore, we are subscribed, but no listeners
                        logger.error("We are subscribed to " + topicName + ", but no listeners");
                        return;
                    }

                    Set<LiveListener> bothSets = new HashSet<LiveListener>();
                    if (listenerSet != null) {
                        bothSets.addAll(listenerSet);
                    }
                    if (retroListenerSet != null) {
                        bothSets.addAll(retroListenerSet);
                    }

                    for (LiveListener listener : bothSets) {
                        listener.onData(record);
                    }
                }
            } else {
                logger.warn("Received non-TextMessage", message);
            }
        } catch (JMSException e) {
            logger.error("Unable to extract text from TextMessage", e);
        } catch (Exception e) {
            logger.error("Add me to the TODO list", e);
        }
    }

    private String getTopicName(Message message) throws JMSException {
        Topic topic = (Topic)message.getJMSDestination();

        return topic.getTopicName();
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

    /**
     * Remove the first subscription we can find.
     * 
     * This should be called by processes that know what they are doing.  This is only intended to be  
     * used by the EasyLanguage to be the opposite of the <code>get</code>.  This ensures that only 
     * one <code>LiveListener</code> is connected at one time.
     *  
     * @param topicName
     * @param useRetro
     * @throws JMSException
     */
    void unsubscribeFromJms(String topicName, boolean useRetro) throws JMSException {
        String jmsTopic = useRetro ? getRetroTopic(topicName) : topicName;

        synchronized (lockObject) {
            Set<LiveListener> listenerSet = topicListeners.get(jmsTopic);

            // if we have subscribers remove the listener
            if (listenerSet != null) {
                logger.info("Removed listener from " + jmsTopic);
                removeOne(listenerSet);
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

    private void removeOne(Set<LiveListener> listenerSet) {
        Iterator<LiveListener> it = listenerSet.iterator();

        while (it.hasNext()) {
            it.remove();
        }

    }
}
