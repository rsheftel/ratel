package com.fftw.metadb.domain.jms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.metadb.util.MessageUtil;
import com.fftw.util.PropertyLoader;

public class JmsELPublisher {

    private static boolean initialized;
    private static ConnectionTask connectionTask;

    private static final Object lockObject = new Object();

    static final Logger staticLogger = LoggerFactory.getLogger(JmsELPublisher.class);
        
    public static void publish(String topicName, String field, String value) throws Exception {

        try {
            if (!isInitialized()) {
                String brokerUrl = PropertyLoader.getProperty("brokerUrl");
                staticLogger.info("Loaded brokerUrl:" + brokerUrl);
                initialize(brokerUrl);
                staticLogger.info("Initialized connection");
            }

            Session session = connectionTask.getSession();
            Topic topic = session.createTopic(topicName);
            MessageProducer mp = session.createProducer(topic);

            Map<String, String> message = buildMessageToPublish(topicName, field, value);
            TextMessage textMessage = session.createTextMessage(MessageUtil.createRecord(message));

            mp.send(textMessage);
        } catch (Exception e) {
            staticLogger.error("Unable to publish string", e);
            throw e;
        }
    }

    private static Map<String, String> buildMessageToPublish(String topicName, String field, String value) {
        Map<String, String> message = new HashMap<String, String>();

        message.put(field, value);
        MessageUtil.setTopicName(topicName, message);
        MessageUtil.setPublishTimestamp(new Date(), message);

        return message;
    }

    private static synchronized void initialize(String brokerUrl) {

        if (brokerUrl == null) {
            throw new NullPointerException("Unable to load brokerUrl from configuration");
        }

        // connect to the broker
        connectionTask = new ConnectionTask(brokerUrl, lockObject);
        Thread connectionThread = new Thread(connectionTask, "ReconnectThread");
        connectionThread.start();
        setInitialized(true);
        synchronized (lockObject) {
            try {
                while (!connectionTask.isConnected()) {
                    lockObject.wait(1000);
                }
            } catch (InterruptedException e) {}
        }
    }

    public static void publish(String topicName, String field, double value) throws Exception {
        publish(topicName, field, String.valueOf(value));
    }

    private synchronized static boolean isInitialized() {
        return initialized;
    }

    private synchronized static void setInitialized(boolean initialized) {
        JmsELPublisher.initialized = initialized;
    }
}
