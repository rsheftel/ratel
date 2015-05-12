package malbec.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle receiving messages from a JMS <code>Destination</code>.
 * 
 */
public class DestinationHandler implements MessageListener {
    final private Logger log = LoggerFactory.getLogger(getClass());

    private Connection brokerConnection;

    private MessageConsumer messageConsumer;
    private String topic;
    private boolean retroConsumer;

    private boolean started;

    private ITextMessageProcessor textMessageProcessor;

    private int textReceivedCount;

    public DestinationHandler(Connection connection, String topic) {
        this(connection, topic, false);
    }

    public DestinationHandler(Connection brokerConnection, String topic, boolean retroConsumer) {
//?consumer.retroactive=true
        this.brokerConnection = brokerConnection;
        this.topic = topic;
        this.retroConsumer = retroConsumer;
    }

    public int getTextReceivedCount() {
        return textReceivedCount;
    }

    public synchronized void start() throws JMSException {
        if (!started) {
            Session localSession = brokerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            StringBuilder fullTopic = new StringBuilder(128);
            fullTopic.append(topic);
            if (retroConsumer) {
                fullTopic.append("?consumer.retroactive=true");
            }
            Destination dest = localSession.createTopic(fullTopic.toString());
            messageConsumer = localSession.createConsumer(dest);
            messageConsumer.setMessageListener(this);

            started = true;
        }
    }

    public synchronized void stop() throws JMSException {
        if (started) {
            messageConsumer.close();
            started = false;
        }
    }
    
    @Override
    public void onMessage(Message message) {
        // Make sure we are thread safe
        synchronized (this) {
            if (message instanceof TextMessage) {
                textReceivedCount++;
                if (textMessageProcessor != null) {
                    textMessageProcessor.onTextMessage((TextMessage) message);
                }
            } else {
                log.warn("received unknown message type: " + message.getClass().getName());
            }
        }
    }

    public synchronized void setTextMessageProcessor(ITextMessageProcessor textMessageProcessor) {
        this.textMessageProcessor = textMessageProcessor;
    }

    public synchronized boolean isStarted() {
        return started;
    }
}
