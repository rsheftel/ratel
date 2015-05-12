package activemq.jms;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.testng.annotations.Test;

import activemq.ActiveMQTest;

public class QueueMonitorTest extends ActiveMQTest {


    @Test(groups = { "unittest" })
    public void monitorQueue() throws Exception {

        QueueMonitor monitor = new QueueMonitor(BROKER_URL);
        monitor.startup();

        assertTrue(monitor.isConnected(), "Monitor is not connected");

        // create a queue, publish messages and then browse the messages
        Queue testQueue = createTestQueue(monitor, "UnitTestQueue");
        
//        drainTestQueue(monitor, testQueue);
        
        assertNotNull(testQueue, "Failed to create test queue for unit test");

        List<Message> queueMessages = monitor.browserMessages("UnitTestQueue");

        assertTrue(queueMessages.size() == 0, "Found messages on an empty queue");

        publishQueueMessage(monitor, testQueue);

        queueMessages = monitor.browserMessages("UnitTestQueue");

        assertTrue(queueMessages.size() == 1, "Failed to find a message on the test queue");

        drainTestQueue(monitor, testQueue);
        monitor.shutdown();
    }

    private void drainTestQueue(QueueMonitor monitor, Queue testQueue) throws JMSException {
        Session jmsSession = monitor.connectionTask.getSession();

        MessageConsumer consumer = jmsSession.createConsumer(testQueue);

        while (consumer.receive(1000) != null) {
            System.out.println("Removed message");
        }

        consumer.close();
    }

    private Queue createTestQueue(QueueMonitor monitor, String queueName) throws JMSException {
        Session jmsSession = monitor.connectionTask.getSession();
        return jmsSession.createQueue(queueName);
    }

    private void publishQueueMessage(QueueMonitor monitor, Queue testQueue) throws JMSException {
        Session jmsSession = monitor.connectionTask.getSession();

        MessageProducer producer = jmsSession.createProducer(testQueue);

        TextMessage textMessage = jmsSession.createTextMessage("Message=Test");
        producer.send(testQueue, textMessage);

        producer.close();
    }

}
