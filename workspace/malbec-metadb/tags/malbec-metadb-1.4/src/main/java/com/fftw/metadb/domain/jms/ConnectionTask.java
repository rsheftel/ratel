package com.fftw.metadb.domain.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.ExceptionListener;
import java.util.Observable;


import static com.fftw.metadb.domain.jms.ConnectionEventType.*;

/**
 *
 */
public class ConnectionTask extends Observable implements Runnable, ExceptionListener {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String brokerUrl;
    private long statusCheckInterval;
    private long reconnectInterval;

    private boolean running;
    private long lastReconnectAttempt;

    private final Object lockObject;

    public long getLastConnectTime() {
        return lastConnectTime;
    }

    private long lastConnectTime;

    private Connection connection;
    private Session session;

    public ConnectionTask(String brokerUrl, Object lockObject) {
        this.lockObject = lockObject;
        this.brokerUrl = brokerUrl;
        this.statusCheckInterval = 10 * 1000l;
        this.reconnectInterval = 10 * 1000l;
        this.running = true;
    }

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public boolean isConnected() {
        synchronized (lockObject) {
            return session != null;
        }
    }

    public Session getSession() {
        synchronized (lockObject) {
            return session;
        }
    }

    private void connect() {
        synchronized (lockObject) {
            lastReconnectAttempt = System.currentTimeMillis();
            logger.info("Attempting to connect");
            try {
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
                connection = factory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                connection.setExceptionListener(this);
                connection.start();
                lastConnectTime = System.currentTimeMillis();
                logger.info("Connected to broker");
                setChanged();
                notifyObservers(new ConnectionEvent(Connected, "Connected to broker"));
                lockObject.notifyAll();
            } catch (JMSException e) {
                // ensure our locals are null
                logger.error("Unable to connection to JMS broker " + brokerUrl, e);
                connection = null;
                session = null;
            }
        }
    }

    private boolean shouldReconnect() {
        return (!isConnected() && isTimeForReconnect());
    }

    private boolean isTimeForReconnect() {
        boolean tmpCompare = lastReconnectAttempt + reconnectInterval < System.currentTimeMillis();

        return tmpCompare;
    }

    private void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            // We don't care if we are interrupted
        }
    }

    public void run() {
        while (running) {
            try {
                if (shouldReconnect()) {
                    connect();
                } else {
                    sleep(statusCheckInterval);
                }
            } catch (Throwable e) {
                logger.error("Caught exception in thread", e);
            }
        }
    }

    public void onException(JMSException e) {
        logger.error("Exception on JMS connection", e);

        Connection badConnection = null;
        synchronized (lockObject) {
            badConnection = connection;
            connection = null;
            session = null;
            setChanged();
            notifyObservers(new ConnectionEvent(Diconnected, e, "Disconnected"));
        }

        try {
            badConnection.close();
        } catch (JMSException e1) {
            logger.error("Error closing connection after exception", e1);
        }
    }
    
    public void stop() {
        running = false;
    }
}
