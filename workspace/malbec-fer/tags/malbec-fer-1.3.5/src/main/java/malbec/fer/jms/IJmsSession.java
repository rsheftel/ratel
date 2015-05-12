package malbec.fer.jms;

import javax.jms.JMSException;

import org.apache.activemq.transport.TransportListener;

public interface IJmsSession {

    void start();

    boolean isRunning();

    void stop();

    boolean isConnected();

    void setBrokerUrl(String brokerUrl);

    void connect() throws JMSException;

    void connect(TransportListener transportListener) throws JMSException;

    String getBrokerUrl();

}
