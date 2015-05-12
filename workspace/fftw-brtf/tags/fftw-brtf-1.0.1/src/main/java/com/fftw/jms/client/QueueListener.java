package com.fftw.jms.client;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Listen to a JMS Queue
 */
public class QueueListener extends AbstractJmsClient implements ExceptionListener, MessageListener {

    private String commandQueueName;

    private MessageConsumer commandConsumer;

    private JmsApplication jmsApplication;


    public QueueListener(String brokerUrl, String commandQueueName, JmsApplication jmsApplication) {
        super(brokerUrl);
        this.commandQueueName = commandQueueName;
        this.jmsApplication = jmsApplication;
    }


    public synchronized void initialize() throws JMSException {
        super.initializeConnection(this);
        // Create the 'command' queue that clients will put requests on
        commandConsumer = createCommandConsumer();
        commandConsumer.setMessageListener(this);
        startConnection();
        setInitialized(true);
    }

    private MessageConsumer createCommandConsumer() throws JMSException {
        Destination destination = getSession().createQueue(commandQueueName);
        MessageConsumer commandConsumer = getSession().createConsumer(destination);
        
        logger.info("Create consumer for queue:"+ commandQueueName);
        return commandConsumer;
    }

    protected void reInitialize() throws JMSException {
        super.initializeConnection(this);
        // Create the 'command' queue that clients will put requests on
        commandConsumer = createCommandConsumer();
        commandConsumer.setMessageListener(this);
        startConnection();
        setInitialized(true);
    }

    public void onException(JMSException e) {
        logger.error("JMS Exception on connection", e);
        logger.error("Shutting down connection and reconnecting");
        setInitialized(false);

        // reconnect
        reconnect();
    }

    /**
     * Determine the Message type and pass to the appropriate method on the JmsApplication.
     *
     * @param message
     */
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                TextMessage txtMsg = (TextMessage) message;

                jmsApplication.fromQueue(txtMsg, getSession());
            } catch (JMSException e) {
                logger.error("Unable to process Text command", e);
            }
        } else {
            logger.warn("Ignoring non-text command message:" + message.getClass().getName() + "\n"
                    + message.toString());
        }
    }
}
