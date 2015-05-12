package malbec.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.ExceptionListener;
import java.util.Observable;


import static malbec.jms.ConnectionEventType.*;

/**
 * A task that keeps the Jms connection alive.
 * <p/>
 * It 'fires' <code>ConnectionEvent</code>s when the connection status changes.
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

    /**
     * Create a <code>ConnectionTask</code> defaulting the statusCheckInterval and recoonectInterval to 10
     * seconds.
     *
     * @param brokerUrl
     * @param lockObject
     */
    public ConnectionTask(String brokerUrl, Object lockObject) {
        this(brokerUrl, 10 * 1000l, 10 * 1000l, lockObject);
    }

    /**
     * @param brokerUrl
     * @param statusCheckInterval
     * @param reconnectInterval
     * @param lockObject
     */
    public ConnectionTask(String brokerUrl, long statusCheckInterval, long reconnectInterval, Object lockObject) {
        this.brokerUrl = brokerUrl;
        this.statusCheckInterval = statusCheckInterval;
        this.reconnectInterval = reconnectInterval;
        this.lockObject = lockObject;

        // used internally
        this.running = true;
    }

    public ConnectionTask(String brokerUrl) {
        this(brokerUrl, new Object());
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

    /**
     * Provide the object used for locking to allow threads to co-operate.
     *
     * @return
     */
    public Object getLockObject() {
        return lockObject;
    }

    private void connect() {
        synchronized (lockObject) {
            lastReconnectAttempt = System.currentTimeMillis();
            logger.info("Attempting to connect to " + brokerUrl);
            try {
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
                connection = factory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                connection.setExceptionListener(this);
                connection.start();
                lastConnectTime = System.currentTimeMillis();
                logger.info("Connected to broker");
                // fire off the events
                setChanged();
                notifyObservers(new ConnectionEvent(Connected, "Connected to broker " + brokerUrl));
            } catch (JMSException e) {
                // ensure our locals are null
                logger.error("Unable to connection to JMS broker " + brokerUrl, e);
                connection = null;
                session = null;
            }
        }
    }

    /**
     * Run until told to stop, keeping the connection alive.
     *
     */
    public void run() {
        while (running) {
            try {
                if (shouldReconnect()) {
                    connect();
                } else {
                    sleep(statusCheckInterval);
                }
            } catch (Exception e) {
                logger.error("Caught exception in thread", e);
                setChanged();
                notifyObservers(new ConnectionEvent(Exception, e, "Loop"));
            }
        }
    }

    /**
     * Handle the connection exception.
     *
     * Notifies observers of the exception.
     *
     * @param e
     */
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
}
