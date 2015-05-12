package com.fftw.jms.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.ExceptionListener;

/**
 *
 */
public abstract class AbstractJmsClient {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private Connection connection;
    private Session session;

    private String brokerUrl;
    
    private boolean initialized = false;

    private boolean sentDisconnectEmail;


    protected AbstractJmsClient(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public Session getSession() {
        return session;
    }

    public Connection getConnection() {
        return connection;
    }

    protected abstract void reInitialize() throws JMSException;

    public synchronized void initializeConnection(ExceptionListener exceptionListener) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);

        connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.setExceptionListener(exceptionListener);
    }

    protected void startConnection() throws JMSException {
        connection.start();
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            // do nothing
        }
    }

    protected synchronized void reconnect() {
        initialized = false;
        int attempts = 0;

        while (!initialized) {
            logger.info("Attempting reconnect");
            try {
                getConnection().stop();
                reInitialize();
                if (sentDisconnectEmail) {
                    // send email

                    sentDisconnectEmail = false;
                }
            } catch (JMSException e3) {
                logger.error("Unable to re-connect to JMS", e3);
                if (attempts > 10 && !sentDisconnectEmail) {

                    // send email

                    sentDisconnectEmail = true;
                }
                attempts++;
                // Sleep for 1 second
                sleep(1000);
            }
        }
    }
}
