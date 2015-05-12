package malbec.fer.jms;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import malbec.fer.IMessageProcessListener;
import malbec.fer.IMessageProcessor;
import malbec.fer.Order;
import malbec.util.EmailSettings;
import malbec.util.IWaitFor;
import malbec.util.InvalidConfigurationException;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

/**
 * Test the JMS Session Server Application
 * 
 */
public class JmsServerSessionAppTest extends AbstractJmsTest {
    
    @Test(groups = { "unittest" })
    public void testServerStartup() throws JMSException, InvalidConfigurationException {
        final List<Map<String, String>> receivedMessages = new ArrayList<Map<String, String>>();
        final JmsServerSessionApp jsa = new JmsServerSessionApp("Test Session", new EmailSettings());

        jsa.setConfiguration(createJmsTestProperties());
        jsa.setMessageProcessor(new IMessageProcessor() {
            @Override
            public void processMessage(IMessageProcessListener jmsApp, Map<String, String> message) {
               receivedMessages.add(message);
            }
        });
        jsa.start();

        waitForConnected(jsa);
      
        assertTrue(jsa.isConnected(), "Did not connect to broker");

        jsa.stop();
        assertFalse(jsa.isRunning(), "Server did not stop");
    }
    
    @Test(groups = { "unittest" })
    public void testReceiveOrder() throws JMSException, InvalidConfigurationException {
        final List<Map<String, String>> receivedMessages = new ArrayList<Map<String, String>>();
        
        final JmsServerSessionApp jsa = new JmsServerSessionApp("Test Session", new EmailSettings());
        jsa.setConfiguration(createJmsTestProperties());
        jsa.setMessageProcessor(new IMessageProcessor() {
            @Override
            public void processMessage(IMessageProcessListener jmsApp, Map<String, String> message) {
               receivedMessages.add(message);
            }
        });
        jsa.start();

        waitForConnected(jsa);

        assertTrue(jsa.isConnected(), "Did not connect to broker");
        
        // send an order to be processed
        sendValidLimitOrder();

        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return receivedMessages.size() > 0;
            }
        }, true, 2000);

        assertTrue(receivedMessages.size() > 0, "Did not receive test order");

        jsa.stop();
    }
    
    @Test(groups = { "unittest" })
    public void testReceiveOrderSendUpdates() throws JMSException, InvalidConfigurationException {
        final List<Map<String, String>> receivedMessages = new ArrayList<Map<String, String>>();
        final JmsServerSessionApp jsa = new JmsServerSessionApp("Test Session", new EmailSettings());

        jsa.setConfiguration(createJmsTestProperties());
        jsa.setMessageProcessor(new IMessageProcessor() {
            @Override
            public void processMessage(IMessageProcessListener jmsApp, Map<String, String> message) {
               receivedMessages.add(message);
            }
        });
        jsa.start();

        waitForConnected(jsa);

        assertTrue(jsa.isConnected(), "Did not connect to broker");
        
        // send an order to be processed
        sendOrderAndWaitForResponse();

        //sleep(60 * 1000);
        
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return receivedMessages.size() > 0;
            }
        }, true, 2000);

        assertTrue(receivedMessages.size() > 0, "Did not receive test order");
        
        Map<String, String> testMessage = receivedMessages.get(0);
        
        String userOrderID = testMessage.get("USERORDERID");
        assertNotNull(userOrderID, "Message does not contain UserOrderID");
        
        // Send an updated status to every client that is interested in the order
        LocalDate orderDate = new LocalDate(MessageUtil.getOrderDate(testMessage));

        int sentCount = jsa.broadcastStatus(userOrderID, orderDate, testMessage);

        assertTrue(sentCount > 0, "No interested clients for userOrderID:" + userOrderID);
        
        // this closes the session created by the sendOrderAndWaitForResponse
        disconnect();
        
        jsa.stop();
    }

    private void sendOrderAndWaitForResponse() throws JMSException {
        Session session = connect();

        MessageProducer producer = createQueueProducer(FER_COMMAND);

        Order orderRecord = createLimitOrder();

        TextMessage textMessage = createTextMessage(MessageUtil.createRecord(orderRecord.toMap()));

        Queue reponseQueue = session.createTemporaryQueue();
        
        textMessage.setJMSReplyTo(reponseQueue);
        
        producer.send(textMessage);
        System.out.println("MessageID=" + textMessage.getJMSMessageID());
        System.out.println("Sent test Limit order");
        producer.close();
    }

}
