package activemq;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;

public abstract class AbstractJmsMonitor<O extends IJmsObserver<M>, M extends Message> {

    private final Object observerLock = new Object();
    private List<O> observers = new LinkedList<O>();

    private boolean updated;

    protected ConnectionTask connectionTask;

    protected abstract Logger getLogger();
    
    public void startup() {
        connectionTask.connect();
    }

    public boolean isConnected() {
        return connectionTask.isConnected();
    }

    public void shutdown() {
        connectionTask.stop();
    }

    /**
     * Add an observer for the specified type.
     * 
     * @param observer
     */
    public void addObserver(O observer) {
        synchronized (observerLock) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public int observerCount() {
        synchronized (observerLock) {
            return observers.size();
        }
    }

    protected void setUpdated(boolean updated) {
        synchronized (observerLock) {
            this.updated = updated;
        }
    }

    public void removeObserver(O observer) {
        synchronized (observerLock) {
            if (observers.contains(observer)) {
                observers.remove(observer);
            }
        }
    }

    protected void notifyObservers(M message) {
        List<O> toBeNotified = new ArrayList<O>(observers.size());
        synchronized (observerLock) {
            if (isUpdate()) {
                toBeNotified.addAll(observers);
                setUpdated(false);
            }
        }
        for (O observer : toBeNotified) {
            observer.onUpdate(message);
        }
    }

    private boolean isUpdate() {
        synchronized (observerLock) {
            return updated;
        }
    }

    public Topic createTempTopic() {
        try {
            return connectionTask.getSession().createTemporaryTopic();
        } catch (JMSException e) {
            getLogger().error("Cannot create temporary topic", e);
            throw new JmsException("Cannot create temporary topic", e);
        }
    }
    
    public Topic createTopic(String topicName) {
        try {
            return connectionTask.getSession().createTopic(topicName);
        } catch (JMSException e) {
            getLogger().error("Cannot create temporary topic", e);
            throw new JmsException("Cannot create temporary topic", e);
        }
    }

    public Topic publish(String topicName, String message) throws JMSException {
        Session localSession = connectionTask.getSession(); 
        Topic topic = localSession.createTopic(topicName);
        MessageProducer mp = localSession.createProducer(topic);
        TextMessage textMsg = localSession.createTextMessage(message);
        mp.send(textMsg);
        mp.close();
        return topic;
    }

    public Topic publishWithNewSession(String topicName, String message) throws JMSException {
        Session newSession = connectionTask.getNewSession();
        Topic topic = newSession.createTopic(topicName);
        MessageProducer mp = newSession.createProducer(topic);
        TextMessage textMsg = newSession.createTextMessage(message);
        mp.send(textMsg);
        mp.close();
        newSession.close();
        return topic;
    }

    public Topic publishWithNewConnection(String brokerUrl, String topicName, String message) throws JMSException {
        ConnectionTask connectionTask = new ConnectionTask(brokerUrl);
        connectionTask.connect();
        
        Session newSession = connectionTask.getNewSession();
        Topic topic = newSession.createTopic(topicName);
        MessageProducer mp = newSession.createProducer(topic);
        TextMessage textMsg = newSession.createTextMessage(message);
        mp.send(textMsg);
        mp.close();
        newSession.close();
        connectionTask.stop();
        return topic;
    }

    public ConnectionTask getConnectionTask() {
        return connectionTask;
    }
    
}
