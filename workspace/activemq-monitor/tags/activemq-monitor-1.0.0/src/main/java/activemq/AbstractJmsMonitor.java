package activemq;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Message;

public abstract class AbstractJmsMonitor<O extends IJmsObserver<M>, M extends Message> {

    private final Object observerLock = new Object();
    private List<O> observers = new LinkedList<O>();

    private boolean updated;

    protected ConnectionTask connectionTask;

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
}
