package jms;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import javax.jms.*;

import systemdb.data.*;

import db.*;

public class QTopic extends Channel {

	final ThreadLocal<Topic> topic = local();
	// for tests
	public static boolean useRetroactiveConsumer = true;
	public QTopic(String name) {
		this(name, true);
	}
	public QTopic(String name, boolean doRetroactiveConsumer) {
	    this(name, defaultBroker, doRetroactiveConsumer);
	}
	
	public QTopic(Topic topic) {
		this(name(topic));
		this.topic.set(topic);
	}
	
	public QTopic(String name, String broker) {
	    this(name, broker, true);
	}
	
    public QTopic(String name, String broker, boolean doRetroactiveConsumer) {
        super(name + (doRetroactiveConsumer && useRetroactiveConsumer ? "?consumer.retroactive=true" : ""), broker);
    }
    
    protected static String name(Topic topic) {
		try {
			return topic.getTopicName();
		} catch (JMSException e) {
			throw bomb("can't get name from " + topic, e);
		}
	}

	private Topic topic() throws JMSException {
		if (topic.get() == null) topic.set(session().createTopic(name));
		return topic.get();
	}

	@Override
	protected Destination destination() throws JMSException {
		return topic();
	}
    public void send(Row data) {
        Fields fields = new Fields();
        for(Column<?> c : data.columns())
            fields.put(javaIdentifier(c.name()), data.string(c));
        send(fields);
    }



	
}
