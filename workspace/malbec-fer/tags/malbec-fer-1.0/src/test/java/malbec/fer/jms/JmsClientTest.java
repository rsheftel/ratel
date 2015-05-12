package malbec.fer.jms;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import malbec.util.MessageUtil;

import org.testng.annotations.Test;

/**
 * Test the JMS Client Application.
 * 
 */
public class JmsClientTest extends AbstractJmsTest {

    @Test(groups = { "unittest" })
    public void testClientStartup() throws JMSException {
        final JmsClientApp jc = new JmsClientApp("Test Session");

        jc.setConfiguration(createJmsTestProperties());
        jc.start();

        waitForConnected(jc);

        assertTrue(jc.isConnected(), "Did not connect to broker");

        jc.stop();
        assertFalse(jc.isRunning(), "Client did not stop");
    }

    @Test(groups = { "unittest" })
    public void testSendOrder() throws JMSException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.sendOrder(createLimitOrder("TEST-ORDER-SEND"));

        assertNotNull(jmsMessageID, "Failed to send order");

        processOrder();
        // wait for a response using the JMS Message ID
        waitForResponse(jca);

        assertTrue(jca.getUnprocessedMessageCount() > 0, "Did not receive a response");

        assertNotNull(jca.getResponseFor(jmsMessageID), "Did not receive order response");
        jca.stop();
        assertFalse(jca.isRunning(), "Client did not stop");
    }

    @Test(groups = { "unittest" })
    public void testQueryOrders() throws JMSException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.queryOrder("TestClientID");

        assertNotNull(jmsMessageID, "Failed to send order");

        processQuery();
        // wait for a response using the JMS Message ID
        waitForResponse(jca);

        assertTrue(jca.getUnprocessedMessageCount() > 0, "Did not receive a response");

        assertNotNull(jca.getResponseFor(jmsMessageID), "Did not receive query response");
        jca.stop();
        assertFalse(jca.isRunning(), "Client did not stop");

    }

    private void processQuery() throws JMSException {
        connect();
        MessageConsumer consumer = createQueueConsumer(FER_COMMAND);
        MessageProducer producer = createQueueProducer(FER_RESPONSE);

        // Receive all messages that have been sent. We may have orphaned messages to consume
        TextMessage textMessage = (TextMessage) consumer.receive(1000);
        while (textMessage != null) {
            Map<String, String> record = MessageUtil.extractRecord(textMessage.getText());
            if (MessageUtil.isOrderQuery(record)) {
                System.out.println("Received message of: " + record);

                // String clientID = MessageUtil.getClientID(record);
                String replyTo = textMessage.getStringProperty("ReplyTo");
                String originalMessageID = textMessage.getJMSMessageID();

                Map<String, String> response = new HashMap<String, String>();
                response.put("Status", "ACCEPTED");
                MessageUtil.setOrderQueryResponse(response);

                TextMessage textResponse = createTextMessage(MessageUtil.createRecord(response));

                textResponse.setJMSCorrelationID(replyTo);
                textResponse.setStringProperty("JmsOriginalMessageID", originalMessageID);

                producer.send(textResponse);
                System.out.println("Sent " + textResponse.getJMSMessageID() + " response for "
                        + originalMessageID + " using replyTo=" + replyTo);
            } else {
                System.out.println("Received non-orderQuery message");
            }
            textMessage = (TextMessage) consumer.receive(1000);
        }
        consumer.close();
        producer.close();

        disconnect();
    }

    @Test(groups = { "integration" })
    public void testIntegrationSendOrder() throws JMSException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.sendOrder(createLimitOrder("TESTSERVER"));

        assertNotNull(jmsMessageID, "Failed to send order");

        // wait for a response using the JMS Message ID
        waitForResponse(jca);

        assertTrue(jca.getUnprocessedMessageCount() > 0, "Did not receive a response");

        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response for JMSMessageID: " + jmsMessageID);
        System.out.println(response);
        assertFalse(response.containsKey("ERROR_1"), "Order was not sent");

        jca.stop();
        assertFalse(jca.isRunning(), "Client did not stop");
    }

    /**
     * Simulate the server logic.
     *  - extract the 'ReplyTo' string property to be used in the response's 'JMSCorrelationID' - extract the
     * 'JMSMessageID' to be sent back as the 'JmsOriginalMessageID' string property
     */
    private void processOrder() throws JMSException {
        connect();
        MessageConsumer consumer = createQueueConsumer(FER_COMMAND);
        MessageProducer producer = createQueueProducer(FER_RESPONSE);

        // Receive all messages that have been sent. We may have orphaned messages to consume
        TextMessage textMessage = (TextMessage) consumer.receive(1000);
        while (textMessage != null) {
            Map<String, String> record = MessageUtil.extractRecord(textMessage.getText());
            System.out.println("Received message of: " + record);

            // String clientID = MessageUtil.getClientID(record);
            String replyTo = textMessage.getStringProperty("ReplyTo");
            String originalMessageID = textMessage.getJMSMessageID();

            Map<String, String> response = new HashMap<String, String>();
            response.put("TestResponse", "Responding");

            TextMessage textResponse = createTextMessage(MessageUtil.createRecord(response));

            textResponse.setJMSCorrelationID(replyTo);
            textResponse.setStringProperty("JmsOriginalMessageID", originalMessageID);

            producer.send(textResponse);
            System.out.println("Sent " + textResponse.getJMSMessageID() + " response for "
                    + originalMessageID + " using replyTo=" + replyTo);
            textMessage = (TextMessage) consumer.receive(1000);
        }
        consumer.close();
        producer.close();

        disconnect();
    }
}
