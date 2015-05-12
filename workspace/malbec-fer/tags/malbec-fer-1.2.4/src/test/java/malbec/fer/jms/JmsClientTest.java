package malbec.fer.jms;

import static malbec.fer.OrderStatus.*;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import malbec.fer.OrderTest;
import malbec.fer.SpreadTrade;
import malbec.fer.SpreadTradeTest;
import malbec.util.InvalidConfigurationException;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

/**
 * Test the JMS Client Application.
 * 
 */
public class JmsClientTest extends AbstractJmsTest {

    OrderTest ot = new OrderTest();
    
    @Test(groups = { "unittest" })
    public void testClientStartup() throws JMSException, InvalidConfigurationException {
        final JmsClientApp jc = new JmsClientApp("Test Session");

        jc.setConfiguration(createJmsTestProperties());
        jc.start();

        waitForConnected(jc);

        assertTrue(jc.isConnected(), "Did not connect to broker");

        jc.stop();
        assertFalse(jc.isRunning(), "Client did not stop");
    }

    @Test(groups = { "unittest" })
    public void testSendOrder() throws JMSException, InvalidConfigurationException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.sendOrder(OrderTest.createLimitOrder("TEST-ORDER-SEND"));

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
    public void testQueryOrders() throws JMSException, InvalidConfigurationException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.queryOrder("TestClientID", new LocalDate());

        assertNotNull(jmsMessageID, "Failed to send order");

        processQuery();
        // wait for a response using the JMS Message ID
        waitForResponse(jca);

        assertTrue(jca.getUnprocessedMessageCount() > 0, "Did not receive a response");

        assertNotNull(jca.getResponseFor(jmsMessageID), "Did not receive query response");
        jca.stop();
        assertFalse(jca.isRunning(), "Client did not stop");

    }

    @Test(groups = { "unittest" })
    public void testSendSpreadTrade() throws JMSException, InvalidConfigurationException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.sendSpreadTrade(createSpreadTrade("TEST-ORDER-SEND"));

        assertNotNull(jmsMessageID, "Failed to send order");

        processOrder();
        // wait for a response using the JMS Message ID
        waitForResponse(jca);

        assertTrue(jca.getUnprocessedMessageCount() > 0, "Did not receive a response");

        assertNotNull(jca.getResponseFor(jmsMessageID), "Did not receive order response");
        jca.stop();
        assertFalse(jca.isRunning(), "Client did not stop");
    }
    
    private SpreadTrade createSpreadTrade(String platform) {
        return SpreadTradeTest.createTestSpreadTrade(platform, 1000, 1100);
    }

    private void processQuery() throws JMSException {
        connect();
        MessageConsumer consumer = createQueueConsumer(FER_COMMAND);
        

        // Receive all messages that have been sent. We may have orphaned messages to consume
        TextMessage textMessage = (TextMessage) consumer.receive(1000);
        while (textMessage != null) {
            Map<String, String> record = MessageUtil.extractRecord(textMessage.getText());
            if (MessageUtil.isOrderQuery(record)) {
                System.out.println("Received message of: " + record);

                // String clientID = MessageUtil.getClientID(record);
                Destination replyDestination = textMessage.getJMSReplyTo();
                
                String originalMessageID = textMessage.getJMSMessageID();

                Map<String, String> response = new HashMap<String, String>();
                MessageUtil.setStatus(response, Accepted.toString());
                MessageUtil.setOrderQueryResponse(response);

                TextMessage textResponse = createTextMessage(MessageUtil.createRecord(response));

                textResponse.setStringProperty("JmsOriginalMessageID", originalMessageID);

                MessageProducer producer = createProducerFor(replyDestination);
                producer.send(textResponse);
                System.out.println("Sent " + textResponse.getJMSMessageID() + " response for "
                        + originalMessageID + " using replyTo=" + replyDestination);
                
                producer.close();
            } else {
                System.out.println("Received non-orderQuery message");
            }
            textMessage = (TextMessage) consumer.receive(1000);
        }
        consumer.close();
       

        disconnect();
    }



    @Test(groups = { "integration" })
    public void testIntegrationSendOrder() throws JMSException, InvalidConfigurationException {
        final JmsClientApp jca = new JmsClientApp("Test Session");

        jca.setConfiguration(createJmsTestProperties());
        jca.start();

        waitForConnected(jca);

        assertTrue(jca.isConnected(), "Did not connect to broker");

        String jmsMessageID = jca.sendOrder(OrderTest.createLimitOrder("TESTSERVER"));

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
       

        // Receive all messages that have been sent. We may have orphaned messages to consume
        TextMessage textMessage = (TextMessage) consumer.receive(1000);
        while (textMessage != null) {
            Map<String, String> record = MessageUtil.extractRecord(textMessage.getText());
            System.out.println("Received message of: " + record);

            // String clientID = MessageUtil.getClientID(record);
//            String replyTo = textMessage.getStringProperty("ReplyTo");
            Destination replyDestination = textMessage.getJMSReplyTo();
            String originalMessageID = textMessage.getJMSMessageID();

            Map<String, String> response = new HashMap<String, String>();
            response.put("TestResponse", "Responding");

            TextMessage textResponse = createTextMessage(MessageUtil.createRecord(response));

//            textResponse.setJMSCorrelationID(replyTo);
            textResponse.setStringProperty("JmsOriginalMessageID", originalMessageID);

            MessageProducer producer = createProducerFor(replyDestination);
            producer.send(textResponse);
            producer.close();
            
            System.out.println("Sent " + textResponse.getJMSMessageID() + " response for "
                    + originalMessageID + " using replyTo=" + replyDestination);
            textMessage = (TextMessage) consumer.receive(1000);
        }
        consumer.close();
      

        disconnect();
    }
}
