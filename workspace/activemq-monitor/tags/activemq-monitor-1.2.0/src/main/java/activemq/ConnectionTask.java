package activemq;

import static activemq.ConnectionEventType.Connected;
import static activemq.ConnectionEventType.Disconnected;
import static activemq.ConnectionEventType.Stopped;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.AbstractObservable;
import util.IObserver;

/**
 * Create connections to ActiveMQ brokers
 * 
 */
public class ConnectionTask extends AbstractObservable<IObserver<ConnectionEvent>, ConnectionEvent> implements
        Runnable, ExceptionListener {

    final Logger log = LoggerFactory.getLogger(this.getClass());

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

    public ConnectionTask(String brokerUrl) {
        this(brokerUrl, brokerUrl);
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

    public Session getNewSession() throws JMSException {
        if (isConnected()) {
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } else {
            return null;
        }
    }

    void connectAsTask() {
        Thread thread = new Thread(this);
        thread.start();
    }

    void connect() {
        synchronized (lockObject) {
            lastReconnectAttempt = System.currentTimeMillis();
            log.info("Attempting to connect");
            try {
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
                connection = factory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                connection.setExceptionListener(this);
                connection.start();
                lastConnectTime = System.currentTimeMillis();
                log.info("Connected to broker");
                setChanged();
                notifyObservers(new ConnectionEvent(Connected, "Connected to broker"));
                lockObject.notifyAll();
            } catch (JMSException e) {
                // ensure our locals are null
                log.error("Unable to connection to JMS broker " + brokerUrl, e);
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
                log.error("Caught exception in thread", e);
            }
        }
    }

    public void onException(JMSException e) {
        if (running) {
            log.error("Exception on JMS connection", e);
        }

        Connection badConnection = null;
        synchronized (lockObject) {
            badConnection = connection;
            connection = null;
            session = null;
            setChanged();
            notifyObservers(new ConnectionEvent(Disconnected, e, "OnException Disconnected"));
        }

        try {
            badConnection.close();
        } catch (JMSException e1) {
            if (running) {
                log.error("Error closing connection after exception", e1);
            }
        }
    }

    public void stop() {
        synchronized (lockObject) {
            running = false;
            session = null;
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    log.error("Error while closing connection", e);
                }
                connection = null;
            }
        }

        setChanged();
        notifyObservers(new ConnectionEvent(Stopped, "Stopped"));
    }
}
