package malbec.fer.jms;

import java.util.Properties;

import malbec.fer.IConnectable;
import malbec.util.InvalidConfigurationException;

public abstract class AbstractJmsApp implements IConnectable {

    protected final IJmsSession jmsSession;
    protected final String sessionName;

    public AbstractJmsApp(String sessionName, IJmsSession jmsSession) {
        this.jmsSession = jmsSession;
        this.sessionName = sessionName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void start() {
        jmsSession.start();
    }

    abstract public void setConfiguration(Properties props) throws InvalidConfigurationException;

    public boolean isRunning() {
        return jmsSession.isRunning();
    }

    public void stop() {
        jmsSession.stop();
    }

    public boolean isConnected() {
        return jmsSession.isConnected();
    }

    abstract public int getUnprocessedMessageCount();
    
    abstract public int getReceivedMessageCount();

}
