package malbec.fer.jms;

import javax.jms.JMSException;

public interface IJmsSession {

    void start();

    boolean isRunning();

    void stop();

    boolean isConnected();

    void setBrokerUrl(String brokerUrl);
    
    void connect() throws JMSException;

}
