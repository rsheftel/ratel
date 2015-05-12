package com.fftw.metadb.domain.jms;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.testng.TestException;
import org.testng.annotations.BeforeTest;

import com.fftw.metadb.util.MessageUtil;
import com.fftw.util.PropertyLoader;

public class ActiveMQTest {

    final static class MyMessageListener implements MessageListener {
        Map<String, String> message;

        @Override
        public void onMessage(Message message) {
            try {
                TextMessage textMessage = (TextMessage) message;
                String rawString = textMessage.getText();
                this.message = MessageUtil.extractRecord(rawString);
            } catch (JMSException e) {
                throw new TestException("Failed to extract text message");
            }
        }
    }

    private static final int TCP_PORT = 60606;
    protected static final String BROKER_URL = "tcp://localhost:" + TCP_PORT;

    @BeforeTest(groups = { "unittest" })
    public void setUp() throws Exception {
        try {
            URL url = getClass().getClassLoader().getResource("default.conf");
            PropertyLoader.setPropertyFile(url.getPath());
            // we need to have ActiveMQ running within the JVM for testing
            BrokerService broker = new BrokerService();
            broker.setUseJmx(false);
            broker.setBrokerName("UnitTestBroker");
            // configure the broker
            broker.addConnector(BROKER_URL);
            broker.start();
        } catch (IOException e) {
            throw new TestException("Unable to start embedded broker");
        }
    }

    public Topic publish(String topicName, String message) throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);

        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        MessageProducer mp = session.createProducer(topic);
        TextMessage textMsg = session.createTextMessage(message);
        mp.send(textMsg);
        mp.close();
        
        return topic;
    }
    
    public void attachListener(String topicName, MessageListener listener) throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);

        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        
        MessageConsumer mc = session.createConsumer(topic);
        mc.setMessageListener(listener);
        connection.start();
    }
    
    static void localSleep(long delay) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }
}
