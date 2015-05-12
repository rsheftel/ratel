package activemq.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.IJmsObserver;
import activemq.JmsException;

public class TopicMonitor extends AbstractTopicMonitor<IJmsObserver<Message>, Message> implements MessageListener {

    public final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private Map<String, Pair> consumers = new HashMap<String, Pair>();

    /**
     * Create a new instance with a new broker connection.
     * 
     * @param brokerUrl
     */
    public TopicMonitor(String brokerUrl) {
        connectionTask = new ConnectionTask(brokerUrl);
    }

    /**
     *  Create a new instance that is using a (possibly) shared broker connection.
     *  
     * @param connectionTask
     */
    public TopicMonitor(ConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
    }
    
    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            onMessage((TextMessage) message);
        } else if (message instanceof ActiveMQMessage) {
            onMessage((ActiveMQMessage) message);
        } else {
            // If we get other types of messages, we will need to enhance this
            log.warn("Ignoring message type: " + message.getClass());
        }
    }

    
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Messages from the Advisory
     * 
     * @param message
     */
    private void onMessage(ActiveMQMessage message) {
        setUpdated(true);
        notifyObservers(null, message);
    }

    /**
     * Text based messages.
     * 
     * We should only be receiving text messages. The bulk of the messages
     * should flow through here.
     * 
     * @param message
     */
    private void onMessage(TextMessage message) {
        setUpdated(true);
        notifyObservers(null, message);
    }


    public void listenTo(String topicString, boolean withRetro) {
        try {
            if (withRetro) {
                topicString = topicString + "?consumer.retroactive=true";
            }
            synchronized (consumers) {
                if (!consumers.containsKey(topicString)) {
                    Topic topic = connectionTask.getSession().createTopic(topicString);
                    MessageConsumer mc = connectionTask.getSession().createConsumer(topic);
                    mc.setMessageListener(this);
                    consumers.put(topicString, new Pair(mc, 1));
                } else {
                    Pair pair = consumers.get(topicString);
                    pair.count++;
                }
            }
        } catch (JMSException e) {
            log.error("Unable to subscribe to topic " + topicString, e);
            throw new JmsException("Unable to subscribe to topic " + topicString, e);
        }
    }
            
    public void listenTo(String topicString) {
        listenTo(topicString, false);
    }

    public void stopListeningTo(String topicString) {
        stopListeningTo(topicString, false);
    }
    
    public void stopListeningTo(String topicString, boolean withRetro) {

        if (withRetro) {
            topicString = topicString + "?consumer.retroactive=true";
        }

        synchronized (consumers) {
            if (consumers.containsKey(topicString)) {
                Pair pair = consumers.get(topicString);
                if (pair.count == 1) {
                    consumers.remove(topicString);
                    try {
                        pair.consumer.close();
                    } catch (JMSException e) {
                        log.error("Tried to close consumer", e);
                    }
                } else {
                    pair.count--;
                }
            }
        }
    }

    public int listenerCount() {
        int count = 0;
        
        for (Pair pair : consumers.values()) {
            count = count + pair.count;
        }
        return count;
    }
    
    private static class Pair {
        MessageConsumer consumer;
        int count;
        
        private Pair(MessageConsumer c, int count) {
            this.consumer = c;
            this.count = count;
        }
    }
}
