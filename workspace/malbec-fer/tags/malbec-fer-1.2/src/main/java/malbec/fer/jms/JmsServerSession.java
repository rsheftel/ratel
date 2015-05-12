package malbec.fer.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import malbec.fer.IConnectable;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.NamedThreadFactory;
import malbec.util.TaskService;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a connection and a session.
 * 
 * If requested to, it can manage consumers and producers.
 * 
 */
public class JmsServerSession implements IConnectable, IJmsSession {

    private String brokerUrl;
    final private Logger log = LoggerFactory.getLogger(getClass());

    private Connection connection;
    private Session session;

    private EmailSettings emailSettings;

    private Map<String, MessageConsumer> namedConsumers = new HashMap<String, MessageConsumer>();
    private Map<String, MessageProducer> namedProducers = new HashMap<String, MessageProducer>();

    private ScheduledFuture<?> monitorFuture;
    private boolean connected;

    static {
        TaskService.getInstance().addExecutor("JmsConnection",
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("JmsConnectionSchedule")));
    }

    public JmsServerSession(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    public JmsServerSession(String brokerUrl, EmailSettings emailSettings) {
        this(emailSettings);
        this.brokerUrl = brokerUrl;
    }

    Logger getLogger() {
        return log;
    }

    public void start() {
        if (monitorFuture == null || monitorFuture.isCancelled()) {
            ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance()
                    .getExecutor("JmsConnection");
            monitorFuture = executor.scheduleAtFixedRate(new JmsServerSessionMonitor(this), 0, 1,
                    TimeUnit.SECONDS);
        }
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
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
            for (MessageProducer producer : namedProducers.values()) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    log.error("Disconnected error", e);
                }
            }

            for (MessageConsumer consumer : namedConsumers.values()) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    log.error("Disconnect error", e);
                }
            }

            try {
                if (session != null) {
                    session.close();
                }
            } catch (JMSException e) {
                log.error("Disconnect error", e);
            }
            try {
                if (connection != null) {
                    // during the unit tests we get 'already closed exceptions'
                    connection.close();
                }
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
        log.info("Connecting to " + brokerUrl);
        synchronized (this) {
            if (connected) {
                return;
            }
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            connection = factory.createConnection();
            connection.setExceptionListener(new ExceptionListener() {

                @Override
                public void onException(JMSException exception) {
                    // TODO deal with this
                    if (brokerUrl.startsWith("failover")) {
                        StringBuilder sb = new StringBuilder(1024);
                        sb.append("We are using failover connection and received exception on ActiveMQ!\n\n");
                        sb.append("Investigate this issue and restart the FER\n");

                        sendEmail("ActiveMQ Connection Error", sb.toString());
                    } else {
                        sendEmail("ActiveMQ Connection Error", "Why are we not using failover?");
                    }
                    log.error("Connection exception:", exception);
                }

            });
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            connection.start();
            connected = true;

            log.info("Connected to " + brokerUrl);
        }
    }

    private boolean sendEmail(String subject, String messageBody) {
        EmailSender emailSender = new EmailSender(emailSettings.getAsProperties());
        return emailSender.sendMessage(subject, messageBody);
    }

    public Queue createTempQueue() throws JMSException {
        return session.createTemporaryQueue();
    }

    public void manageProducer(String producerName, MessageProducer messageProducer) {
        namedProducers.put(producerName, messageProducer);
    }

    public MessageProducer createProducerFor(Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    public MessageProducer getProducer(String producerName) {
        return namedProducers.get(producerName);
    }

    public TextMessage createTextMessage(String messageBody) throws JMSException {
        return session.createTextMessage(messageBody);
    }

    public MessageProducer removeProducer(String producerName) {
        return namedProducers.remove(producerName);
    }

    public MessageConsumer createConsumerFor(Destination destination) throws JMSException {
        MessageConsumer messageConsumer = session.createConsumer(destination);

        return messageConsumer;
    }

    public Queue getQueueFor(String queueName) throws JMSException {
        return session.createQueue(queueName);
    }

    public Topic getTopicFor(String topicName) throws JMSException {
        return session.createTopic(topicName);
    }

    public void manageConsumer(String consumerName, MessageConsumer consumer) {
        namedConsumers.put(consumerName, consumer);
    }

    public MessageConsumer getConsumer(String consumerName) {
        return namedConsumers.get(consumerName);
    }

    public MessageConsumer removeConsumer(String consumerName) {
        return namedConsumers.get(consumerName);
    }

}
