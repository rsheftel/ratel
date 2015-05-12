package jms;

import static org.apache.activemq.ActiveMQConnection.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import javax.jms.*;
import javax.jms.Queue;

public class QQueue extends Channel {

    static Map<String, QQueue> responseQs = emptyMap();
    static Map<String, MessageReceiver> responseReceivers = emptyMap();
    
	final ThreadLocal<Queue> q = local();
	
	private boolean registered; 

    public QQueue(String name) {
        this(name, defaultBroker);
    }
    
    public QQueue(String name, String broker) {
        super(name + "?consumer.prefetchSize=1&broker.persistent=false", broker);
    }
	
	public QQueue(Queue q, String broker) {
		super(name(q), broker);
		this.q.set(q);
	}
	

    protected static String name(Queue q) {
		try {
			return q.getQueueName();
		} catch (JMSException e) {
			throw bomb("can't get queue name from " + q, e);
		}
	}

	private Queue q() throws JMSException {
		if (q.get() == null) q.set(session().createQueue(name));
		return q.get();
	}
	
	public static void main(String[] args) {
		info("broker: " + DEFAULT_BROKER_URL);
		// Log.setVerboseLogging(true);
		QQueue queue = new QQueue("jefftest");
		queue.response("hello", new ValidMessageReceiver() {
			@Override public void onHeartBeat(Envelope envelope) {}
			@Override public void onMessage(Envelope envelope) {
				info("world!");
			} 
		}, 1000);
	}

	@Override
	protected Destination destination() throws JMSException {
		return q();
	}
	
	public void response(String text, final MessageReceiver receiver, int heartbeatFrequencyMillis) {
		log('>', ":", text);
		TextMessage message;
		MessageProducer producer = null;
		try {
			message = session().createTextMessage(text);
			message.setJMSReplyTo(responseQ().q());
			String messageId = guid(false);
			message.setJMSCorrelationID(messageId); 
            responseReceivers.put(messageId, receiver);
            message.setIntProperty("heartbeatFrequency", heartbeatFrequencyMillis);
			producer = session().createProducer(destination());
			producer.send(message);
		} catch (JMSException e) {
			throw bomb("failed", e);
		} finally {
			closeProducer(producer);
		}
	}

	private QQueue responseQ() throws JMSException {
	    if (!responseQs.containsKey(broker())) {
    		Destination tempDest = session().createTemporaryQueue();
    		QQueue tempQ = new QQueue((Queue) tempDest, broker());
            tempQ.register(new MessageReceiver() {
                private MessageReceiver receiver(Envelope envelope) {
                    return responseReceivers.get(envelope.correlationId());
                }
                
                @Override public void onMessage(Envelope envelope) {
                    receiver(envelope).onMessage(envelope);
                }
                
                @Override public void onError(Envelope envelope) {
                    receiver(envelope).onError(envelope);
                }
                
                @Override public void onHeartBeat(Envelope envelope) {
                    receiver(envelope).onHeartBeat(envelope);
                } 
            });
            responseQs.put(broker(), tempQ);
	    }
		return responseQs.get(broker());
	}

    @Override
    public synchronized <T extends MessageReceiver> T register(T receiver) {
        bombIf(registered, "can't reregister receiver for QQueue");
        super.register(receiver);
        registered = true;
        return receiver;
    }

	@Override
	public void shutdown() {
		super.shutdown();
		registered = false;
	}

}
