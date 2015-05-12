package malbec.jms;

import javax.jms.MessageListener;
import javax.jms.Message;
import java.util.Observer;
import java.util.Observable;
import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class ActiveMQListener implements MessageListener, Observer {

    private static ActiveMQListener instance;

    private static Map<InstanceKey, ActiveMQListener> instanceMap = new HashMap<InstanceKey, ActiveMQListener>();


    public synchronized static ActiveMQListener getInstance(String brokerUrl, String topic) {

        InstanceKey key = new InstanceKey(brokerUrl, topic);
        //ActiveMQListener listener = instanceMap.get()
        if (instance == null) {
            instance = new ActiveMQListener();
        }


        return instance;
    }

    public void onMessage(Message message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(Observable observed, Object event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Used as the key for the internal map of instances.
     */
    private static class InstanceKey {
        final String brokerUrl;
        final String topic;

        InstanceKey(String bu, String t) {
            this.brokerUrl = bu;
            this.topic = t;
        }

        public int hashCode() {
            return brokerUrl.hashCode() + topic.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof InstanceKey) {
                InstanceKey other = (InstanceKey) obj;
                return brokerUrl.equals(other.brokerUrl) && topic.equals(other.topic);
            } else {
                return false;
            }
        }

        public String toString() {
            return brokerUrl + ", " + topic;
        }
    }
}
