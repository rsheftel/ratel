package util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AbstractObservable<O extends IObserver<N>, N> {

    private boolean updated;
    private List<O> observers = new LinkedList<O>();

    
    
    public synchronized boolean isUpdated() {
        return updated;
    }

    public synchronized void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public synchronized void setChanged() {
        setUpdated(true);
    }
    
    public synchronized void addObserver(O observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void removeObserver(O observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    public void notifyObservers(N notificationObject) {
        List<O> toBeNotified = new ArrayList<O>(observers.size());
        synchronized (this) {
            if (isUpdated()) {
                toBeNotified.addAll(observers);
                setUpdated(false);
            }
        }
        for (O observer : toBeNotified) {
            observer.onUpdate(notificationObject);
        }
    }

}
