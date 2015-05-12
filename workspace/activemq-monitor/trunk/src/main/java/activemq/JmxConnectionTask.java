package activemq;

import java.io.IOException;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxConnectionTask implements NotificationListener {
    final Logger log = LoggerFactory.getLogger(getClass().getName());

    private String brokerJmxUrl;

    private JMXConnector connector;

    private MBeanServerConnection connection;

    public JmxConnectionTask(String brokerJmxUrl) {
        this.brokerJmxUrl = brokerJmxUrl;
    }

    public boolean isConnected() {
        return (connector != null);
    }

    public void connect() throws JmsException {
        String[] urls = brokerJmxUrl.split(",");

        if (urls == null || urls.length == 0) {
            urls = new String[] { brokerJmxUrl };
        }

        Exception exception = null;
        for (int i = 0; i < urls.length; i++) {
            try {
                JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(urls[i]));
                connector.addConnectionNotificationListener(this, null, null);
                connector.connect();
                connection = connector.getMBeanServerConnection();

                Set<ObjectName> brokers = findBrokers(connection);
                if (brokers.size() > 0) {
                    log.info("Connected via JMX to the broker at " + urls[i]);
                    this.connector = connector;

                    return;
                }
            } catch (Exception e) {
                // Keep the exception for later
                exception = e;
            }
        }
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else {
                throw new RuntimeException(exception);
            }
        }
        throw new IllegalStateException("No broker is found at any of the urls " + brokerJmxUrl);
    }

    protected Set<ObjectName> findBrokers(MBeanServerConnection connection) {
        try {
            ObjectName name = new ObjectName("org.apache.activemq:Type=Broker,*");

            return connection.queryNames(name, null);
        } catch (MalformedObjectNameException e) {
            log.error("JMX server URL is not correct", e);
            throw new JmxException("JMX server URL is not correct", e);
        } catch (IOException e) {
            log.error("JMX Error", e);
            throw new JmxException("JMX Error", e);
        }
    }

    public ObjectName getFirstBroker() {
        Set<ObjectName> brokers = findBrokers(getMBeanConnection());

        if (brokers != null && brokers.size() > 0) {
            return brokers.iterator().next();
        } else {
            throw new JmxException("No brokers found");
        }
    }

    public MBeanServerConnection getMBeanConnection() {
        return connection;
    }

    public void stop() {

        if (connector != null) {
            try {
                connector.close();
                connector = null;
            } catch (IOException e) {
                log.error("Cannot close connector", e);
            }
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {

        throw new UnsupportedOperationException("Implement me!");

    }
}
