package com.fftw.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import malbec.util.DateTimeUtil;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.bloomberg.PositionRecord;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.jms.client.JmsApplication;

/**
 * Represent a session with a JMS Broker.
 */
public class JmsTopicPublisherSession implements ExceptionListener {

    final Logger log = LoggerFactory.getLogger(getClass().getName());

    private Connection connection;
    private Session session;

    private Map<String, MessageProducer> publisherMap = new HashMap<String, MessageProducer>();

    private String brokerUrl;

    private JmsApplication app;

    private BlockingQueue<QueueItem> messageQueue = new LinkedBlockingQueue<QueueItem>(1000);

    private boolean connected;


    public JmsTopicPublisherSession(JmsApplication app, String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.app = app;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public Session getSession() {
        return session;
    }

    public Connection getConnection() {
        return connection;
    }

    public synchronized void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);

        connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.setExceptionListener(this);
        connection.start();
        connected = true;
    }

    public void publishPositionMessage(String topic, String message) {
        try {
            if (!messageQueue.isEmpty()) {
                // publish the queue first
                publishLocalQueue();
            }
            
            StringBuilder sb = new StringBuilder(message.length() + 50);
            sb.append(message).append("|").append("TIMESTAMP=").append(DateTimeUtil.formatNow());
            
            publishStringMessage(topic, sb.toString());

        } catch (JMSException e) {
            // We should be re-connected by the time we get here, just resend
            publishPositionMessage(topic, message);
            log.error("Unable to publish message.", e);
        }
    }
    
    public void publishPositionMessage(String topic, PositionRecord message) {
        try {
            if (!messageQueue.isEmpty()) {
                // publish the queue first
                publishLocalQueue();
            }
            String msgStr = app.createPositionMessage(message);
            StringBuilder sb = new StringBuilder(msgStr.length() + 50);
            sb.append(msgStr).append("|").append("TIMESTAMP=").append(DateTimeUtil.formatNow());
            
            publishStringMessage(topic, sb.toString());

        } catch (JMSException e) {
            // We should be re-connected by the time we get here, just resend
            publishPositionMessage(topic, message);
            log.error("Unable to publish message.", e);
        }
    }

    public void publishCommandMessage(String topic, PositionRecord message) {
        try {
            if (!messageQueue.isEmpty()) {
                // publish the queue first
                publishLocalQueue();
            }
            String msgStr = app.createPurgeCommandMessage(message);
            publishStringMessage(topic, msgStr);

        } catch (JMSException e) {
            synchronized (this) {
                // We should be re-connected by the time we get here, just resend
                publishCommandMessage(topic, message);
                log.error("Unable to publish command.", e);
            }
        }
    }

    public void publishHeartBeat() {
        try {
            if (!messageQueue.isEmpty()) {
                // publish the queue first
                publishLocalQueue();
            }
            TextMessage textMsg = getSession().createTextMessage(app.createHeartBeatMessage());

            for (MessageProducer publisher : publisherMap.values()) {
                String destination = publisher.getDestination().toString().toUpperCase(); 
                if (destination.contains("AIM") || destination.contains("POSITIONS")) {
                    continue;
                }
                publisher.send(textMsg);
            }
        } catch (JMSException e) {
            synchronized (this) {
                // We should be re-connected by the time we get here, just resend
                log.error("Unable to publish heartbeat.", e);
            }
        }
    }

    private void publishStringMessage(String topic, String message) throws JMSException {
        MessageProducer publisher = findOrCreatePublisher(topic);
        TextMessage textMsg = getSession().createTextMessage(message);
        publisher.send(textMsg);
    }

    public void publishPurgeCommand(String topic, PositionRecord message) throws JMSException {
        MessageProducer publisher = findOrCreatePublisher(topic);
        TextMessage textMsg = getSession().createTextMessage(app.createPurgeCommandMessage(message));
        publisher.send(textMsg);
    }

    private MessageProducer createPuplisherForTopic(String publishTopic) throws JMSException {
        Topic topic = getSession().createTopic(publishTopic);
        MessageProducer publisher = getSession().createProducer(topic);
        publisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        publisherMap.put(publishTopic, publisher);

        return publisher;
    }

    /**
     * Find the publisher for the specified topic.
     * <p/>
     * If the publisher does not exist, create a new one and add it to the cache of
     * publishers
     *
     * @param topic
     * @return
     * @throws JMSException
     */
    private MessageProducer findOrCreatePublisher(String topic) throws JMSException {
        MessageProducer publisher = publisherMap.get(topic);
        if (publisher == null) {
            log.info("Creating publisher for topic: " + topic);
            publisher = createPuplisherForTopic(topic);
        }
        return publisher;
    }

    private synchronized void publishLocalQueue() throws JMSException {
        QueueItem item = null;
        while ((item = messageQueue.poll()) != null) {
            publishStringMessage(item.topic, item.message);
        }
    }

    public void onException(JMSException e) {
        synchronized (this) {
            log.error("JMS Exception on connection", e);
            log.error("Shutting down connection and reconnecting");
// remove all the stale publishers
            publisherMap.clear();

// reconnect
            connected = false;
            reconnect();
        }
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            // do nothing
        }
    }

    protected synchronized void reconnect() {
        while (!connected) {
            log.info("Attempting reconnect");
            try {
                getConnection().stop();
                start(); // reconnect
            } catch (JMSException e) {
                log.error("Unable to re-connect to JMS", e);
                // Sleep for 1000 milliseconds (1 second)
                sleep(1000);
            }
        }
    }

    private static class QueueItem {
        String topic;
        String message;

        QueueItem(String topic, String message) {
            this.topic = topic;
            this.message = message;
        }
    }

    public void publishRediMessage(RtfOnlinePosition message) {
        String msgStr = message.toReconString();
        StringBuilder sb = new StringBuilder(msgStr.length() + 50);
        sb.append(msgStr).append("|").append("TIMESTAMP=").append(DateTimeUtil.formatNow());
        
        String topic = createTopicForPosition(message);
        try {
            MessageProducer publisher = findOrCreatePublisher(topic);
            TextMessage textMsg = getSession().createTextMessage(sb.toString());
            
            publisher.send(textMsg);
        } catch (JMSException e) {
            log.error("Unable to publish online position to image topic", e);
        }
    }

    private String createTopicForPosition(RtfOnlinePosition position) {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append("Aim.Positions.");
        sb.append(position.getAccount()).append(".");
        sb.append(position.getReconBloombergId());
        
        return sb.toString();
    }

    public void publishAccountStrategyMessage(RtfOnlinePosition message) {
        String msgStr = message.toReconString();
        StringBuilder sb = new StringBuilder(msgStr.length() + 50);
        sb.append(msgStr).append("|").append("TIMESTAMP=").append(DateTimeUtil.formatNow());
        
        String topic = createTopicForAccountStrategyPosition(message);
        try {
            MessageProducer publisher = findOrCreatePublisher(topic);
            TextMessage textMsg = getSession().createTextMessage(sb.toString());
            
            publisher.send(textMsg);
        } catch (JMSException e) {
            log.error("Unable to publish online position to image topic", e);
        }
    }
    
    private String createTopicForAccountStrategyPosition(RtfOnlinePosition position) {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append("Aim.Positions.");
        sb.append(position.getAccount()).append(".");
        sb.append(position.getLevel1TagName()).append(".");
        sb.append(position.getReconBloombergId());
        
        return sb.toString();
    }
    
    public void publishToTopicLastImage(BatchPosition batchPosition) {
        String msgStr = app.createPositionMessage(batchPosition);
        String topic = createTopicForPosition(batchPosition);
        try {
            MessageProducer publisher = findOrCreatePublisher(topic);
            TextMessage textMsg = getSession().createTextMessage(msgStr);
            
            publisher.send(textMsg);
        } catch (JMSException e) {
            log.error("Unable to publish batch postion to image topic", e);
        }
    }

    private String createTopicForPosition(BatchPosition batchPosition) {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append("Aim.Batch.").append(batchPosition.getAccount());
        sb.append(".");
        if (batchPosition.getLevel1TagName() == null || batchPosition.getLevel1TagName().length() == 0) {
            sb.append("UNKNOWN");
        } else {
            sb.append(batchPosition.getLevel1TagName());
        }
        sb.append(".");
        sb.append(batchPosition.getSecurityId());
        
        return sb.toString();
    }
}
