package malbec.fer.jms;

import java.util.Properties;

import malbec.fer.IConnectable;

public abstract class AbstractJmsApp implements IConnectable {

    protected final JmsSession jmsSession;
    protected final String sessionName;

    public AbstractJmsApp(String sessionName, JmsSession jmsSession) {
        this.jmsSession = jmsSession;
        this.sessionName = sessionName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void start() {
        jmsSession.start();
    }

    abstract public void setConfiguration(Properties props);

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

}
