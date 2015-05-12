package malbec.fer.jms;

import static org.testng.Assert.*;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;

import malbec.util.EmailSettings;

import org.testng.annotations.Test;

public class JmsServerSessionTest extends AbstractJmsTest {

    @Test(groups = { "unittest" })
    public void testStartup() throws JMSException {
        final JmsServerSession jmsSession = new JmsServerSession(BROKER_URL, new EmailSettings());

        jmsSession.start();
        waitForConnected(jmsSession);
        assertTrue(jmsSession.isConnected(), "Did not connect to broker");

        jmsSession.stop();
        waitForDisconnect(jmsSession);
        assertFalse(jmsSession.isConnected(), "Client did not stop");
    }

    @Test(groups = { "unittest" })
    public void testManageProducer() throws JMSException {
        final JmsServerSession jmsSession = new JmsServerSession(BROKER_URL,  new EmailSettings());

        jmsSession.start();
        waitForConnected(jmsSession);
        assertTrue(jmsSession.isConnected(), "Did not connect to broker");

        Destination tempDest = jmsSession.createTempQueue();
        MessageProducer tempProducer = jmsSession.createProducerFor(tempDest);

        jmsSession.manageProducer("TestProducer", tempProducer);

        MessageProducer retrievedProducer = jmsSession.getProducer("TestProducer");
        assertNotNull(retrievedProducer, "Failed to retrieve producer");
        assertEquals(tempDest, retrievedProducer.getDestination(), "Producer destination does not match original");

        retrievedProducer.setTimeToLive(10000);
        retrievedProducer.send(jmsSession.createTextMessage("TestMessage"));

        MessageProducer removedProducer = jmsSession.removeProducer("TestProducer");

        assertNotNull(removedProducer, "Failed to remove managed producer");

        jmsSession.stop();
        waitForDisconnect(jmsSession);
        assertFalse(jmsSession.isConnected(), "Client did not stop");
    }

    @Test(groups = { "unittest" })
    public void testManageConsumer() throws JMSException {
        final JmsServerSession jmsSession = new JmsServerSession(BROKER_URL,  new EmailSettings());

        jmsSession.start();
        waitForConnected(jmsSession);
        assertTrue(jmsSession.isConnected(), "Did not connect to broker");

        Destination tempDest = jmsSession.createTempQueue();
        MessageConsumer tempConsumer = jmsSession.createConsumerFor(tempDest);

        tempConsumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message message) {
                try {
                    Destination dest = message.getJMSReplyTo();

                    if (dest != null) {
                        System.out.println("have replyto destination");
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

        jmsSession.manageConsumer("TestConsumer", tempConsumer);

        MessageConsumer retrievedConsumer = jmsSession.getConsumer("TestConsumer");
        assertNotNull(retrievedConsumer, "Failed to retrieve consumer");
        assertNotNull(retrievedConsumer.getMessageListener(), "Consumer listener does not match original");

        // TODO Retrieve something!!
        //        retrievedConsumer.send(jmsSession.createTextMessage("TestMessage"));

        MessageConsumer removedConsumer = jmsSession.removeConsumer("TestConsumer");

        assertNotNull(removedConsumer, "Failed to remove managed producer");

        removedConsumer.close();

        jmsSession.stop();
        waitForDisconnect(jmsSession);
        assertFalse(jmsSession.isConnected(), "Client did not stop");
    }

}
