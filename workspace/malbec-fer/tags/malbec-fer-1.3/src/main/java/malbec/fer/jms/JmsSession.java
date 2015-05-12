package malbec.fer.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import malbec.fer.IConnectable;
import malbec.util.MessageUtil;
import malbec.util.TaskService;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.TransportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the connection, session and publisher/subscriber.
 * 
 */
public class JmsSession implements IConnectable, IJmsSession {

    private String brokerUrl;
    final private Logger log = LoggerFactory.getLogger(getClass());

    private IJmsApplication jmsApp;

    private Connection connection;
    private Session session;
    private String consumerQueue;
    private String producerQueue;

    // this is used by the client
    private boolean useTempQueue;
    private Queue tempQueue;

    private List<MessageConsumer> consumers = new ArrayList<MessageConsumer>();
    private List<MessageProducer> producers = new ArrayList<MessageProducer>();

    private ScheduledFuture<?> monitorFuture;
    private boolean connected;

    private String clientID;

    static {
        TaskService.getInstance().createAndAddSingleThreadScheduled("JmsConnection");
    }

    public JmsSession(IJmsApplication jmsApplication) {
        this.jmsApp = jmsApplication;
    }

    public JmsSession(String brokerUrl, IJmsApplication app) {
        this.brokerUrl = brokerUrl;
        this.jmsApp = app;
    }

    Logger getLogger() {
        return log;
    }

    public void start() {
        if (monitorFuture == null || monitorFuture.isCancelled()) {
            ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance()
                .getExecutor("JmsConnection");
            monitorFuture = executor.scheduleAtFixedRate(new SessionMonitor(this), 0, 1, TimeUnit.SECONDS);
        }
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public synchronized void setConsumerQueue(String consumerQueue) {
        this.consumerQueue = consumerQueue;
    }

    public synchronized void setProducerQueue(String producerQueue) {
        this.producerQueue = producerQueue;
    }

    public boolean isRunning() {
        return (monitorFuture != null && !monitorFuture.isCancelled());
    }

    public void stop() {
        // stop the session monitor from being called again
        if (monitorFuture != null) {
            if (monitorFuture.cancel(false)) {
                monitorFuture = null;
            } else {
                log.info("Unable to cancel monitor");
            }
        }
        disconnect();
    }

    private void disconnect() {
        log.info("Disconnecting from broker " + brokerUrl);
        synchronized (this) {
            for (MessageProducer producer : producers) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    log.error("Disconnected error", e);
                }
            }

            for (MessageConsumer consumer : consumers) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    log.error("Disconnect error", e);
                }
            }

            try {
                session.close();
            } catch (JMSException e) {
                log.error("Disconnect error", e);
            }
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("Disconnect error", e);
            }
            connected = false;
        }
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public void connect() throws JMSException {
        connect(null);
    }

    public void connect(TransportListener transportListener) throws JMSException {
        log.info("Connecting to " + brokerUrl);
        synchronized (this) {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);

            if (transportListener != null) {
                factory.setTransportListener(transportListener);
            }
            connection = factory.createConnection();
            connection.setExceptionListener(new ExceptionListener() {

                @Override
                public void onException(JMSException exception) {
                    // TODO deal with this
                    if (brokerUrl.startsWith("failover")) {
                        StringBuilder sb = new StringBuilder(1024);
                        sb.append("We are using failover connection and received exception on ActiveMQ!\n\n");
                        sb.append("Investigate this issue and restart the FER\n");

                        jmsApp.sendEmail("ActiveMQ Connection Error", sb.toString());
                    } else {
                        jmsApp.sendEmail("ActiveMQ Connection Error", "Why are we not using failover?");
                    }
                    log.error("Connection exception:", exception);
                }

            });
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a temp queue for all of our responses
            MessageConsumer consumer = null;

            if (isUseTempQueue()) {
                tempQueue = session.createTemporaryQueue();
                consumer = session.createConsumer(tempQueue);
            } else {
                clientID = connection.getClientID().replace("ID:", "").replaceAll(":", "");
                // If we are a client application, we only create filtering consumers
                String selector = null;
                if (jmsApp.filterConsumers()) {
                    selector = "JMSCorrelationID = '" + clientID + "'";
                    log.info("Creating consumer with selector:" + selector);
                }
                consumer = session.createConsumer(session.createQueue(consumerQueue), selector);
            }

            consumers.add(consumer);
            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    jmsApp.inboundApp(message);
                }

            });
            log.info("Created consumer for: " + consumerQueue);
            MessageProducer producer = session.createProducer(session.createQueue(producerQueue));
            producers.add(producer);

            log.info("Created producer for: " + producerQueue);
            connection.start();
            connected = true;

            log.info("Connected to " + brokerUrl);
        }
    }

    private boolean send(Message message) {
        try {
            MessageProducer publisher = producers.get(0);
            publisher.send(message);
            return true;
        } catch (JMSException e) {
            log.error("Failed to publish message", e);
        }
        return false;
    }

    /**
     * Create an error message to be returned to the client.
     * 
     * @param message
     * @param errors
     */
    public void publishResponse(Map<String, String> message, List<String> errors) throws JMSException {
        int errorCount = 0;
        Map<String, String> errorMessage = new HashMap<String, String>((message.size() + errors.size()) * 4);
        errorMessage.putAll(message);

        for (String error : errors) {
            errorMessage.put("ERROR_" + (++errorCount), error);
        }

        TextMessage textMessage = createTextMessage(MessageUtil.createRecord(errorMessage));

        textMessage.setJMSCorrelationID(MessageUtil.getReplyTo(message));
        textMessage.setStringProperty("JmsOriginalMessageID", MessageUtil.getOriginalMessageID(message));

        send(textMessage);

        log.info("Sent message: " + errorMessage);
        log.info("Sent message response to client " + MessageUtil.getReplyTo(message));
    }

    private synchronized TextMessage createTextMessage(String messageBody) throws JMSException {
        return session.createTextMessage(messageBody);
    }

    public synchronized TextMessage sendSpecifyResponse(Map<String, String> order) throws JMSException {
        TextMessage textMessage = session.createTextMessage(MessageUtil.createRecord(order));
        if (isUseTempQueue()) {
            textMessage.setJMSReplyTo(tempQueue);
            log.info("Response should arrive on: " + tempQueue);
        } else {
            // This is the JMSCorrelationID that we are listening to
            textMessage.setStringProperty("ReplyTo", clientID);
        }
        send(textMessage);
        return textMessage;
    }

    public TextMessage send(Map<String, String> order, boolean createListener) throws JMSException {
        if (!createListener) {
            return sendSpecifyResponse(order);
        } else {
            // send the message and create a consumer for the response using JMSMessageID
            TextMessage textMessage = sendSpecifyResponse(order);
            String jmsMessageID = textMessage.getJMSMessageID();
            final String selector = "JMSCorrelationID = '" + jmsMessageID + "'";

            final MessageConsumer consumer = createConsumerWithSelector(selector);
            // final MessageConsumer consumer = session.createConsumer(session.createQueue(consumerQueue));
            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    try {
                        log.info("Received Message for selector: " + selector);
                        jmsApp.inboundApp(message);
                        log.info("CorrelationID=" + message.getJMSCorrelationID());
                        consumer.close();
                    } catch (JMSException e) {
                        log.error("Unable to close message response consumer", e);
                    }
                }

            });
            log.debug("Created listener using selector: " + selector);
            return textMessage;
        }
    }

    private synchronized MessageConsumer createConsumerWithSelector(final String selector)
        throws JMSException {
        MessageConsumer consumer = session.createConsumer(session.createQueue(consumerQueue), selector);
        return consumer;
    }

    public IJmsApplication getJmsApp() {
        return jmsApp;
    }

    public void setUseTempQueue(boolean useTempQueue) {
        this.useTempQueue = useTempQueue;
    }

    public boolean isUseTempQueue() {
        return useTempQueue;
    }

    public synchronized String getConsumerQueue() {
        return consumerQueue;
    }

    public synchronized String getProducerQueue() {
        return producerQueue;
    }

}
