package activemq;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create connections to ActiveMQ brokers
 * 
 */
public class ConnectionTask implements ExceptionListener {

    final Logger log = LoggerFactory.getLogger(getClass().getName());

    private String brokerUrl;

    private Session session;

    public ConnectionTask(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public Session getSession() {
        return session;
    }

    public boolean isConnected() {
        return (session != null);
    }

    public void connect() throws JmsException {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);

            Connection connection = factory.createConnection();
            connection.setExceptionListener(this);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            connection.start();
        } catch (JMSException e) {
            log.error("Unable to connect to broker: " + brokerUrl, e);
            throw new JmsException("Failed to connect to broker: " + brokerUrl, e);
        }

        // connection.setExceptionListener(exceptionListener);
    }

    public void stop() {

        if (session != null) {
            try {
                session.close();
                session = null;
            } catch (JMSException e) {
                log.error("Cannot close session", e);
                throw new JmsException("Cannot close session", e);
            }
        }
    }

    @Override
    public void onException(JMSException exception) {
        session = null;
    }
}
