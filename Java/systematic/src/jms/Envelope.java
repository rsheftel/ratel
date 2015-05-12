package jms;

import java.util.Map;

import javax.jms.*;

import static util.Errors.*;
import static util.Log.*;

public class Envelope {
	static final String IS_HEARTBEAT_PROPERTY = "isHeartbeat";
	static final String IS_ERROR_PROPERTY = "isError";
	private final Message message;
    private final String broker;

	public Envelope(Message m, String broker) {
		this.message = m;
        this.broker = broker;
	}

	public String text() {
        TextMessage tm = (TextMessage) message;
		try {
			return tm.getText();
		} catch (JMSException e) {
			throw bomb("cannot get text from " + message, e);
		}
	}

	public Channel returnAddress() {
		try {
			return new QQueue((Queue)message.getJMSReplyTo(), broker);
		} catch (JMSException e) {
			throw bomb("can't get reply Q from "  + message, e);
		}
	}

	private boolean isHeartbeat() {
		return hasProperty(IS_HEARTBEAT_PROPERTY);
	}

	private boolean hasProperty(String name) {
		try {
			return message.propertyExists(name);
		} catch (JMSException e) {
			throw bomb("failed to get " + isHeartbeat(), e);
		}
	}

	public int heartbeatFrequencyMillis() {
		try {
			return message.getIntProperty("heartbeatFrequency");
		} catch (JMSException e) {
			throw bomb("failed", e);
		}
	}

	public void sendBack(String reply, Map<String, String> properties) {
		try {
			returnAddress().send(reply,properties, message.getJMSCorrelationID());
		} catch(RuntimeException ex) {
			if(queueDeleted(ex))
				info("Discarding message \n" + text() + "\n\tbecause response queue has been deleted.");
			else
				throw ex;
		} catch (JMSException e) {
            throw bomb("jms exception on " + this + ": \n" + reply);
        }
	}
	
	private boolean queueDeleted(Exception ex) {
		return 
		    ex.getCause() != null && 
		    ex.getCause().getMessage().matches(".*Cannot publish to a deleted Destination: temp-queue.*");
	}

	private boolean isError() {
		return hasProperty(IS_ERROR_PROPERTY);
	}

	public void giveTo(Channel from, MessageReceiver receiver) {
	    try {
	        if (isError()) receiver.onError(this);
	        else if (isHeartbeat()) receiver.onHeartBeat(this);
	        else {
	            from.log('<', ":", text());
	            receiver.onMessage(this);
	        }
	    } catch (ClassCastException e) {
            try {
                from.log('?', ":", 
                    "wrong message type: " + message.getClass().getName() + " " + message.getJMSMessageID());
            } catch (Throwable inner) {
                throw bomb("Exception caught while constructing error message for ClassCastException", inner);
            }
        } catch (Throwable e) {
            bomb(from.logMessage('X', ":", 
        		message.getClass().getName() + " " + text() + "\nLOGGING TRACE: \n" + trace(e)));
	    }
	}

    public String correlationId() {
        try {
            return message.getJMSCorrelationID();
        } catch (JMSException e) {
            throw bomb("can't get message id", e);
        }
    }

}
