package jms;

import static org.apache.activemq.ActiveMQConnection.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import javax.jms.*;

import org.apache.activemq.*;
import org.apache.activemq.transport.*;

import systemdb.data.*;
import util.*;

public abstract class Channel {

    private static Map<String, Connection> connection = emptyMap();
    private static final Map<String, ThreadLocal<Session>> session = emptyMap();
    protected static String defaultBroker = "tcp://amqmktdata:63636"; 
    
	protected final String name;
    private final String broker;
    private static Map<String, Boolean> inReadonlyMode = emptyMap();
    
    static {
        TransportFactory.registerTransportFactory("tcp", new QTcpTransportFactory());
    }

	public Channel(String name) {
		this(name, defaultBroker);
	}

    public Channel(String name, String broker) {
        this.name = name;
        this.broker = broker;
    }

    public static void setDefaultBroker(String broker) {
        if (Log.verbose()) 
            info("changing default broker from " + defaultBroker + " to " + broker);
        defaultBroker = broker;
    }
    
    public static String defaultBroker() {
        return defaultBroker;
    }

	public void connect() {
		connection();
	}    
	
	public void log(char mark, String divider, String message) {
	    if (Log.verbose()) info(logMessage(mark, divider, message));
	}

    public String logMessage(char mark, String divider, String message) {
        return "JMS" + mark + " " + this + divider + message;
    }

	public <T extends MessageReceiver> T register(final T receiver) {
		try {
		    log('=', "><", receiver.getClass().getName() + " on " + broker);
			MessageConsumer consumer = session().createConsumer(destination());
			consumer.setMessageListener(new MessageListener() {
				@Override public void onMessage(javax.jms.Message message) {
					new Envelope(message, Channel.this.broker()).giveTo(Channel.this, receiver);
				}


			});
			return receiver;
		} catch (JMSException e) {
			closeResources();
			throw bomb("failed creating connection/session/consumer on " + this, e);
		} 
	}
	
    protected String broker() {
        return broker;
    } 

	public static void closeResources() {
		try {
		    session.clear();
		    for (Connection conn : connection.values())
                conn.close();
		    connection.clear();
		} catch (JMSException e) {
			throw bomb("failed to close resource!", e);
		}
	}

	private Connection connection() {
	    bombIf(broker.startsWith("fail"), "failover is supplied by framework!");
		if (!connection.containsKey(broker)) {
			try {
			    String failoverUrl = failoverUrl();
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(DEFAULT_USER, DEFAULT_PASSWORD, failoverUrl);
                log('C', ":", failoverUrl);
                Connection newConnection = factory.createConnection();
                connection.put(broker, newConnection);
                newConnection.setExceptionListener(new ExceptionListener() {
					@Override public void onException(JMSException e) {
						bomb("connection exception", e);
					} 
				});
                newConnection.start();
			} catch (JMSException e1) {
				throw bomb("cannot create connection", e1);
			}
		}
		return connection.get(broker);
	}

    private String failoverUrl() {
        return "failover:(" + broker + ")?initialReconnectDelay=100&maxReconnectAttempts=10";
    }

	protected Session session() throws JMSException {
		if (!session.containsKey(broker)) 
		    session.put(broker, new ThreadLocal<Session>());
		if(session.get(broker).get() == null){
			Session s = newSession();
			session.get(broker).set(s);
		}
		return session.get(broker).get();
	}

	private Session newSession() throws JMSException {
		return connection().createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	@Override
	public String toString() {
	    return name;
	}

	public void send(String message) {
		Map<String, String> none = emptyMap();
		send(message, none);
	}
	

    public void send(Fields fields) {
        fields = fields.copy();
        fields.put("MSTopicName", name());
        fields.put("MSTimestamp", yyyyMmDdHhMmSs(now()));
        send(fields.messageText());
    }

    public void send(String text, Map<String, String> properties, String correlationId) {
        bombIf(inReadonlyMode.containsKey(name()), logMessage('X', " ", "in readonly: can't send " + text));
        MessageProducer producer = null;
        try {
            producer = session().createProducer(destination());
            TextMessage message = session().createTextMessage(text);
            if (correlationId != null) message.setJMSCorrelationID(correlationId);
            for (String key : properties.keySet())
                message.setStringProperty(key, properties.get(key));
            producer.send(message);
            if (Log.verbose()) info("JMS>: " + text + " to " + name());
        } catch (JMSException e) {
            throw bomb("failed to write " + text + "\nto queue " + this, e);
        } finally {
            closeProducer(producer);
        }
    }
    
    public void send(String text, Map<String, String> properties) {
        send(text, properties, null);
	}

	private static void closeSession(Session s) {
		try {
			if (s != null) s.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		closeResources();
	}


	protected void closeProducer(MessageProducer producer) {
		try {
			if(producer != null) producer.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		Session synchronousSession = null;
		try {
			synchronousSession = newSession();
			MessageConsumer consumer = synchronousSession.createConsumer(destination());
			while (consumer.receiveNoWait() != null) {}
		} catch (JMSException e) {
			throw bomb("failed to clear queue " + this, e);
		} finally {
			if(synchronousSession != null) closeSession(synchronousSession);
		}
	}

	protected abstract Destination destination() throws JMSException;

	public String name() {
		return name;
	}

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Channel other = (Channel) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }
    
    public void setReadonly(boolean beReadonly) {
        if(beReadonly)
            inReadonlyMode.put(name(), true);
        else
            inReadonlyMode.remove(name());
    }
    
    public static void main(String[] args) {
        new QTopic("TEST.TEST.TEST").send("TEST=WORKS");
    }
}