package jms;

import javax.jms.*;
import javax.jms.Message;

import org.apache.activemq.*;
import static util.Errors.*;

import static org.apache.activemq.ActiveMQConnection.*;

public class SubscriberSpike implements ExceptionListener, MessageListener {

	private static final String BROKER_URL = "tcp://nyux51:61616";

	public static void main(String[] args) throws Exception {
		new SubscriberSpike().run();
	}

	private void run() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(DEFAULT_USER, DEFAULT_PASSWORD, BROKER_URL);
		Connection connection = factory.createConnection();
		connection.setExceptionListener(this);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(session.createQueue("test"));
		consumer.setMessageListener(this);
	}

	@Override public void onException(JMSException e) {
		bomb(e);
	}

	@Override public void onMessage(Message mess) {
		TextMessage message = (TextMessage) mess;
		try {
			System.out.println(message.getText());
		} catch (JMSException e) {
			throw bomb("couldn't get text", e);
		}
	}

}
