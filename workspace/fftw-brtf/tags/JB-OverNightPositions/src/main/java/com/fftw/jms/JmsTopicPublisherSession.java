package com.fftw.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.bloomberg.PositionRecord;
import com.fftw.jms.client.JmsApplication;
import com.fftw.util.Emailer;

/**
 * Represent a session with a JMS Broker.
 */
public class JmsTopicPublisherSession implements ExceptionListener {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private Emailer emailer;

    private Connection connection;
    private Session session;

    private Map<String, MessageProducer> publisherMap = new HashMap<String, MessageProducer>();

    private String brokerUrl;

    private JmsApplication app;

    private BlockingQueue<QueueItem> messageQueue = new LinkedBlockingQueue<QueueItem>(1000);

    private boolean connected;

    private DateTime lastTimeoutEmail;


    public JmsTopicPublisherSession(JmsApplication app, String brokerUrl, Emailer emailer) {
        this.brokerUrl = brokerUrl;
        this.app = app;
        this.emailer = emailer;
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

    public void publishPositionMessage(String topic, PositionRecord message) {
        try {
            if (!messageQueue.isEmpty()) {
                // publish the queue first
                publishLocalQueue();
            }
            String msgStr = app.createPositionMessage(message);
            publishStringMessage(topic, msgStr);

        } catch (JMSException e) {
            // We should be re-connected by the time we get here, just resend
            publishPositionMessage(topic, message);
            logger.error("Unable to publish message.", e);
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
                logger.error("Unable to publish command.", e);
            }
        }
    }

    public void publishHeartBeat() {
        try {
            if (!messageQueue.isEmpty()) {
                // publish the queue first
                publishLocalQueue();
            }
            TextMessage textMsg = createTextMessage(app.createHeartBeatMessage());

            for (MessageProducer publisher : publisherMap.values()) {
                publisher.send(textMsg);
            }
        } catch (JMSException e) {
            synchronized (this) {
                // We should be re-connected by the time we get here, just resend
                logger.error("Unable to publish heartbeat.", e);
            }
        }
    }

    private TextMessage createTextMessage(final String messageBody) throws JMSException {

        Future<TextMessage> future = executor.submit(new Callable<TextMessage>() {

            public TextMessage call() throws Exception {
                return getSession().createTextMessage(messageBody);
            }
        });

        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Timed out creating TextMessage.", e);
            if (lastTimeoutEmail == null || lastTimeoutEmail.getMillis() + (1000 * 60 * 30) > System.currentTimeMillis()) {
                lastTimeoutEmail = new DateTime();
                emailer.emailDeveloperAndBusinessErrorMessage("Unable to create TextMessage",
                        "Attempt to create TextMessage took longer than 10 seconds."
                                + "\nCheck ActiveMQ.");
            }
            throw new JMSException(e.getMessage());
        } catch (Exception e) {
            throw new JMSException(e.getMessage());
        }
    }

    private void publishStringMessage(String topic, String message) throws JMSException {
        MessageProducer publisher = findOrCreatePublisher(topic);
        TextMessage textMsg = createTextMessage(message);
        publisher.send(textMsg);
    }

    public void publishPurgeCommand(String topic, PositionRecord message) throws JMSException {
        MessageProducer publisher = findOrCreatePublisher(topic);
        TextMessage textMsg = createTextMessage(app.createPurgeCommandMessage(message));
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
            logger.info("Creating publisher for topic: " + topic);
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
            logger.error("JMS Exception on connection", e);
            logger.error("Shutting down connection and reconnecting");
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
            logger.info("Attempting reconnect");
            try {
                getConnection().stop();
                start(); // reconnect
            } catch (JMSException e) {
                logger.error("Unable to re-connect to JMS", e);
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
}
