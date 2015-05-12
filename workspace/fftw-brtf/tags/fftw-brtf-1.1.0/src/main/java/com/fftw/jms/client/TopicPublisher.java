package com.fftw.jms.client;

import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TopicPublisher extends AbstractJmsClient implements ExceptionListener {

    private String[] publishTopics = new String[0];


    private Map<String, MessageProducer> publisherMap = new HashMap<String, MessageProducer>();

    public TopicPublisher(String brokerUrl) {
        super(brokerUrl);
    }

    public TopicPublisher(String brokerUrl, String publishTopic) {
        this(brokerUrl);
        this.publishTopics = new String[]{publishTopic};
    }

    public TopicPublisher(String brokerUrl, String[] publishTopics) {
        this(brokerUrl);
        this.publishTopics = publishTopics;
    }

    public synchronized void initialize() throws JMSException {
        super.initializeConnection(this);
        // Create all the publisher for each topic
        createTopicPublishers();
        startConnection();
        setInitialized(true);
    }

    private void createTopicPublishers() throws JMSException {
        for (String publishTopic : publishTopics) {
            logger.info("Creating predefined publisher for topic: " + publishTopic);
            createPuplisherForTopic(publishTopic);
        }
    }

    protected synchronized void reInitialize() throws JMSException {
        // Create all the publisher for each topic
        super.initializeConnection(this);
        createTopicPublishers();
        startConnection();
        setInitialized(true);
    }

    private MessageProducer createPuplisherForTopic(String publishTopic) throws JMSException {
        Topic topic = getSession().createTopic(publishTopic);
        MessageProducer publisher = getSession().createProducer(topic);
        publisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        publisherMap.put(publishTopic, publisher);

        return publisher;
    }

    /**
     * Publish the message on the specified topic.
     * <p/>
     * If the topic has not been previously created, the topic is created prior
     * to the message being published.
     *
     * @param topic
     * @param message
     * @throws JMSException
     */
    public boolean publishMessage(String topic, String message) throws JMSException {
        MessageProducer publisher = findOrCreatePublisher(topic);

        TextMessage textMsg = getSession().createTextMessage(message);
        publisher.send(textMsg);
        return true;
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

    public void onException(JMSException e) {
        logger.error("JMS Exception on connection", e);
        logger.error("Shutting down connection and reconnecting");
        setInitialized(false);
        // remove all the stale publishers
        publisherMap.clear();

        // reconnect
        reconnect();
    }


}
