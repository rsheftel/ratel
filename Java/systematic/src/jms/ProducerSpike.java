package jms;

import static org.apache.activemq.ActiveMQConnection.*;
import static util.Errors.*;

import javax.jms.*;

import org.apache.activemq.*;

public class ProducerSpike implements ExceptionListener {

	private static final String BROKER_URL = "tcp://nyux51:61616";

	public static void main(String[] args) throws Exception {
		new ProducerSpike().run();
	}

	private void run() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(DEFAULT_USER, DEFAULT_PASSWORD, BROKER_URL);
		Connection connection = factory.createConnection();
		connection.setExceptionListener(this);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = session.createProducer(session.createQueue("test"));
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		TextMessage message = session.createTextMessage("Rock of Love ROCKS!");
		producer.send(message);
	}

	@Override public void onException(JMSException e) {
		bomb(e);
	}

}
