package com.fftw.metadb.domain.jms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.metadb.service.LiveListener;

/**
 * The ActiveMQ listener/subscribe interface for EasyLanguage.
 * 
 * Re-use the JmsLiveSubscriber.
 */
public class JmsELSubscriber {

    private static final String SUBSCRIBER_NAME = "EL-Subscriber";

    final static Logger staticLogger = LoggerFactory.getLogger(JmsELSubscriber.class);

    private static final Map<String, ELLiveListener> listeners = new HashMap<String, ELLiveListener>();

    /**
     * Get the value for the topic and field.
     * 
     * This uses the retro subscriber by default.
     * 
     * @param topicName
     * @param field
     * @return
     * @throws Exception
     */
    public static synchronized String getString(final String topicName, final String field) throws Exception {
        return getString(topicName, field, true);
    }

    public static synchronized double getDouble(final String topicName, final String field) throws Exception {
        String doubleValue = getString(topicName, field, true);
        if (doubleValue == null) {
            return -999999;
        } else {
            return Double.parseDouble(doubleValue);
        }
    }
    
    /**
     * 
     * @param topicName
     * @param field
     * @param useRetro
     * @return
     * @throws Exception
     */
    public static synchronized String getString(final String topicName, final String field, boolean useRetro) throws Exception {
        // check if we are subscribed to this topic, if not, subscribe.
        // Wait for data to arrive.
        try {
            //staticLogger.info("Get for " + topicName +" -- " + field);
            ELLiveListener listener = null;
            synchronized (JmsELSubscriber.class) {
                listener = listeners.get(topicName);
                if (listener == null) {
                    listener = new ELLiveListener();
                    JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance(SUBSCRIBER_NAME);
                    subscriber.subscribeToJms(topicName, listener, useRetro);
                    listeners.put(topicName, listener);
                }
            }
            synchronized (listener) {
               // Map<String, String> lastMessage = Collections.<String, String> emptyMap();
                String fieldLastValue = null;
                
                int waitAttempts = 0;
                try {
                    while ((fieldLastValue = listener.getLastMessage().get(field)) == null 
                            && waitAttempts < 10) {
                        listener.wait(1000); // wait for 1 second (a total of 10)
                        waitAttempts++;
                    }
                } catch (InterruptedException e) {
                    staticLogger.error("InterruptedException", e);
                }
               
                return fieldLastValue;
            }
        } catch (Exception e) {
            staticLogger.error("Unable to get field", e);
            throw e;
        }
    }

    public static void unsubscribe(String topicName) throws Exception {
        try {
            synchronized (JmsELSubscriber.class) {
                JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance(SUBSCRIBER_NAME);
                subscriber.unsubscribeFromJms(topicName, true);
            }
        } catch (Exception e) {
            staticLogger.error("Unable to unsubscribe", e);
        }
    }
    
    private static final class ELLiveListener implements LiveListener {

        private Map<String, String> lastMessage = Collections.emptyMap();

        @Override
        public void onData(Map<String, String> dataMap) {
            synchronized (this) {
                if (lastMessage == Collections.<String, String>emptyMap()) {
                    lastMessage = new HashMap<String, String>();
                }
                lastMessage.putAll(dataMap);
                notifyAll();
            }
        }

        private Map<String, String> getLastMessage() {
            synchronized (this) {
                return lastMessage;
            }
        }
    }

}
