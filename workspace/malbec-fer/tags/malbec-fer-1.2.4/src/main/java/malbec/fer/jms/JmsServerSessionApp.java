package malbec.fer.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;

import malbec.fer.FerretState;
import malbec.fer.IMessageProcessListener;
import malbec.fer.IMessageProcessor;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.InvalidConfigurationException;
import malbec.util.MessageUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMS client represents one connection/session with a broker. The client may have multiple queues that it
 * subscribes/publishes to.
 * 
 */
public class JmsServerSessionApp extends AbstractJmsApp implements MessageListener, IMessageProcessListener {

    final private Logger log = LoggerFactory.getLogger(getClass());

    final EmailSettings emailSettings;

    private String commandQueueName;

    private String topicPrefix;

    private int receivedMessageCount;

    private Queue<Map<String, String>> unprocessedMessages = new LinkedBlockingQueue<Map<String, String>>();

    /**
     * Clients that are interested in a specific clientOrderId
     */
    private Map<String, List<Destination>> interestedClients = new HashMap<String, List<Destination>>();

    /**
     * Destinations that can be sent a direct response for the received message
     */
    private Map<String, Destination> responseDest = new HashMap<String, Destination>();

    private IMessageProcessor messageProcessor;

    public JmsServerSessionApp(String sessionName, EmailSettings emailSettings) {
        super(sessionName, new JmsServerSession(emailSettings));
        this.emailSettings = emailSettings;

    }

    public JmsServerSessionApp(String sessionName, Properties config) throws InvalidConfigurationException {
        this(sessionName, new EmailSettings(config));
        setConfiguration(config);
    }

    public JmsServerSessionApp(String sessionName, EmailSettings emailSettings, Properties config)
        throws InvalidConfigurationException {
        this(sessionName, emailSettings);
        setConfiguration(config);
    }

    public void setConfiguration(Properties props) throws InvalidConfigurationException {
        jmsSession.setBrokerUrl(props.getProperty("jms.brokerurl"));

        commandQueueName = props.getProperty("jms.command.queue");
        topicPrefix = props.getProperty("jms.response.topic.prefix");

        if (commandQueueName == null) {
            throw new InvalidConfigurationException("Must specify a jms.command.queue");
        }

        if (topicPrefix == null) {
            throw new InvalidConfigurationException("Must specify a jms.response.topic.prefix");
        }
    }

    public synchronized int getReceivedMessageCount() {
        return receivedMessageCount;
    }

    private synchronized int incrementReceivedMessageCount() {
        return ++receivedMessageCount;
    }

    @Override
    public int getUnprocessedMessageCount() {
        return unprocessedMessages.size();
    }

    private JmsServerSession getJmsSession() {
        return (JmsServerSession) jmsSession;
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fer.jms.AbstractJmsApp#start()
     */
    @Override
    public void start() {
        try {
            if (messageProcessor == null) {
                throw new IllegalStateException("No message processor set.");
            }
            // connect first and then start the monitor
            jmsSession.connect();
            super.start();

            // create the command listener
            JmsServerSession jmsSession = getJmsSession();
            Destination commandQueue = jmsSession.getQueueFor(commandQueueName);

            MessageConsumer commandConsumer = jmsSession.createConsumerFor(commandQueue);
            commandConsumer.setMessageListener(this);

            // register it so that it will be closed when we shutdown
            getJmsSession().manageConsumer("CommandConsumer", commandConsumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean sendEmail(String subject, String messageBody) {
        EmailSender emailSender = new EmailSender(emailSettings.getAsProperties());
        return emailSender.sendMessage(subject, messageBody);
    }

    @Override
    public void onMessage(Message message) {
        log.info("Received message");
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            try {
                String messageBody = textMessage.getText();
                log.info("Received message: " + messageBody);
                Map<String, String> orderRecord = MessageUtil.extractRecord(messageBody);

                if (!MessageUtil.isFerretMode(orderRecord)) {
                    // Extract the two pieces of information we need later
                    Destination replyTo = textMessage.getJMSReplyTo();
                    String userOrderID = MessageUtil.getUserOrderId(orderRecord);
                    DateTime orderDateTime = DateTimeUtil
                        .guessDateTime(MessageUtil.getOrderDate(orderRecord));
                    String jmsMessageId = textMessage.getJMSMessageID();

                    MessageUtil.setOriginalMessageID(jmsMessageId, orderRecord);

                    MessageUtil.setPublishTimestamp(textMessage.getJMSTimestamp(), orderRecord);
                    log.debug("Message with additional fields : " + orderRecord);

                    // TODO Handle the logic of message state change
                    if (orderDateTime == null) {
                        log.error("No OrderDate, ignoring message.");
                        sendEmail("Invalid Feret Message - OrderDate", messageBody);
                        return;
                    }

                    if (userOrderID == null) {
                        // check if we have a spread order
                        String pairId = MessageUtil.getPairId(orderRecord);
                        if (pairId == null) {
                            log.error("No UserOrderID or pairId, ignoring message.");
                            sendEmail("Invalid Feret Message - UserOrderId/PariId", messageBody);
                            return;
                        }
                        log.info("Assigning clientOrderId to pairId: '" + pairId + "'");
                        userOrderID = pairId;
                    }
                    List<Destination> destList = interestedClients.get(userOrderID.toUpperCase());
                    if (destList == null) {
                        destList = new ArrayList<Destination>();
                        interestedClients.put(userOrderID.toUpperCase(), destList);
                    }

                    if (replyTo != null) {
                        if (!destList.contains(replyTo)) {
                            destList.add(replyTo);
                            log.info("Added new destination for broadcast: " + replyTo);
                        }
                        // when we only need to send a response, instead of a broadcast
                        if (jmsMessageId != null && jmsMessageId.trim().length() > 0) {
                            responseDest.put(jmsMessageId, replyTo);
                        }
                    } else if (replyTo == null) {
                        log.warn("Received message with no 'ReplyTo', no response will be sent."
                            + orderRecord);
                    }
                } else {
                    log.info("Received state change request");
                }

                incrementReceivedMessageCount();
                if (messageProcessor == null) {
                    log.error("MessageProcessor is null!");
                    sendEmail("Design Error In Feret", "MessageProcessor is null, this should not happend!");
                } else {
                    messageProcessor.processMessage(this, orderRecord);
                }

            } catch (JMSException e) {
                log.error("Unable to extract text from JMS message", e);
            } catch (Exception e) {
                log.error("Error processing JMS message", e);
            }
        }
    }

    @Override
    public int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> message) {
        return broadcastStatus(userOrderId, orderDate, message, Collections.<String> emptyList());
    }

    public int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> message,
        List<String> errors) {
        List<Destination> destinations = interestedClients.get(userOrderId.toUpperCase());

        if (destinations == null || destinations.size() == 0) {
            log.warn("No destinations found for clientOrderId: " + userOrderId);
            // return 0;
            destinations = Collections.<Destination> emptyList();
        }

        int errorCount = 0;
        Map<String, String> messageWithErrors = new HashMap<String, String>(
            (message.size() + errors.size()) * 4);
        messageWithErrors.putAll(message);

        for (String error : errors) {
            messageWithErrors.put("ERROR_" + (++errorCount), error);
        }

        int sentCount = 0;
        JmsServerSession jmsSession = getJmsSession();

        List<Destination> destToDelete = new ArrayList<Destination>();
        try {
            TextMessage textMessage = jmsSession.createTextMessage(MessageUtil
                .createRecord(messageWithErrors));
            // send to a topic so using the prefix and the ClientOrderId
            // topicStatusBroadcast(topicPrefix, userOrderId, textMessage);
            topicStatusBroadcast(topicPrefix, orderDate.toString("YYYYMMdd"), userOrderId, textMessage);

            // TODO This is not totally necessary, but used in the unit tests
            textMessage.setStringProperty("JmsOriginalMessageID", MessageUtil
                .getOriginalMessageID(messageWithErrors));

            for (Destination destination : destinations) {
                try {
                    MessageProducer producer = getJmsSession().createProducerFor(destination);
                    producer.send(textMessage);
                    sentCount++;
                    producer.close();
                } catch (InvalidDestinationException e) {
                    destToDelete.add(destination);
                } catch (JMSException e) {
                    log.error(
                        "Unable to publish status for " + userOrderId + " to " + destination.toString(), e);
                }
            }
            destinations.removeAll(destToDelete);
        } catch (JMSException e) {
            log.error("Unable to create JMS message for " + message);
        }

        return sentCount;
    }

    private void topicStatusBroadcast(String topicBase, String topicPrefix, String clientOrderId,
        TextMessage textMessage) throws JMSException {
        StringBuilder sb = new StringBuilder(128);
        sb.append(topicBase).append(".");
        sb.append(topicPrefix).append(".");
        sb.append(clientOrderId);

        Topic topic = getJmsSession().getTopicFor(sb.toString());

        MessageProducer producer = getJmsSession().createProducerFor(topic);
        producer.send(textMessage);
        producer.close();
        log.debug("Sent message to topic: " + topic.getTopicName());
    }

    @Override
    public int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> message,
        String errorMessage) {
        List<String> errorMessages = new ArrayList<String>();
        if (errorMessage != null) {
            errorMessages.add(errorMessage);
        }

        return broadcastStatus(userOrderId, orderDate, message, errorMessages);
    }

    public void sendResponse(Map<String, String> message, String errorMessage) {
        Map<String, String> messageWithErrors = new HashMap<String, String>(message);
        messageWithErrors.put("ERROR_1", errorMessage);

        String orginalJmsMessageId = MessageUtil.getOriginalMessageID(message);
        Destination dest = responseDest.get(orginalJmsMessageId);

        if (dest == null) {
            log.error("Could not find destination for message: " + orginalJmsMessageId);
            sendEmail("Logic Error with Feret", "Tried to send repsonse of:\n" + messageWithErrors);
            return;
        }

        JmsServerSession jmsSession = getJmsSession();

        try {
            TextMessage textMessage = jmsSession.createTextMessage(MessageUtil
                .createRecord(messageWithErrors));
            // TODO This is not totally necessary, but used in the unit tests
            textMessage.setStringProperty("JmsOriginalMessageID", MessageUtil
                .getOriginalMessageID(messageWithErrors));
            try {
                MessageProducer producer = getJmsSession().createProducerFor(dest);
                producer.send(textMessage);
                producer.close();
            } catch (JMSException e) {
                log.error("Unable to send response for " + orginalJmsMessageId + " to " + dest.toString(), e);
            }

        } catch (JMSException e) {
            log.error("Unable to create JMS message for " + message);
        }

        // we made our attempt to send, remove the destination
        responseDest.remove(orginalJmsMessageId);
    }

    /**
     * @return the messageProcessor
     */
    public IMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    /**
     * @param messageProcessor
     *            the messageProcessor to set
     */
    public void setMessageProcessor(IMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public boolean publishStatus(FerretState fs) {
        try {
            Topic topic = getJmsSession().getTopicFor("FER.State");

            JmsServerSession jmsSession = getJmsSession();
            MessageProducer producer = jmsSession.createProducerFor(topic);
            StringBuilder sb = new StringBuilder(128);
            sb.append("TIMESTAMP=").append(DateTimeUtil.format(new DateTime()));
            sb.append("|FERRETSTATE=").append(fs);
            TextMessage tm = jmsSession.createTextMessage(sb.toString());
            producer.send(tm);
            producer.close();
            return true;

        } catch (JMSException e) {
            log.error("Unable to publish FerretState", e);
        }
        return false;
    }
}
