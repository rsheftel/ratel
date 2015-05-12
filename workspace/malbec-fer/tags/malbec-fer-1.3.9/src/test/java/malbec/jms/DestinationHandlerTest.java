package malbec.jms;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import malbec.util.DateTimeUtil;
import malbec.util.MessageUtil;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class DestinationHandlerTest extends AbstractJmsBaseTest {

    @Test(groups = { "unittest" })
    public void testConnectToTopic() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        DestinationHandler dh = new DestinationHandler(connection, "Test.Topic.Destination");
        dh.start();

        publishTestMessage(connection, "Test.Topic.Destination");
        sleep(100);
        assertTrue(dh.getTextReceivedCount() == 1);
    }

    @Test(groups = { "unittest" })
    public void testListenForFerretOrderResponse() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        final Map<String, String> statusMessage = new HashMap<String, String>();
        // Ferret publishes on FER.Order.Response.<date>.<user order id>
        DestinationHandler dh = new DestinationHandler(connection, "Test.FER.Order.Response.>");
        dh.setTextMessageProcessor(new DefaultTextMessageProcessor() {

            @Override
            protected void onTextMessage(TextMessage textMessage, Map<String, String> mapMessage) {
                statusMessage.putAll(mapMessage);
            }});
        dh.start();

        publishFerretStatusMessage(connection, "Test.FER.Order.Response", "TT0001");
        sleep(100);
        assertTrue(dh.getTextReceivedCount() == 1);
        assertEquals(MessageUtil.getStatus(statusMessage), "New");
        String localDate = new LocalDate().toString("yyyyMMdd");
        assertEquals(statusMessage.get("JMSTOPIC"), "Test.FER.Order.Response." + localDate + ".TT0001");
    }

    @Test(groups = { "unittest" })
    public void testListenForFerretState() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        final Map<String, String> stateMessage = new HashMap<String, String>();
        // Ferret publishes on FER.Order.Response.<date>.<user order id>
        DestinationHandler dh = new DestinationHandler(connection, "Test.FER.State");
        dh.setTextMessageProcessor(new ITextMessageProcessor() {
            @Override
            public void onTextMessage(TextMessage message) {
                try {
                    stateMessage.putAll(MessageUtil.extractRecord(message.getText()));
                    Destination dest = message.getJMSDestination();
                    if (dest instanceof Topic) {
                        Topic topic = (Topic) dest;
                        stateMessage.put("JMSTOPIC", topic.getTopicName());
                    } else {
                        stateMessage.put("JMSTOPIC", dest.toString());
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        dh.start();

        publishFerretStateMessage(connection, "Test.FER.State", "Reject");
        sleep(100);
        assertTrue(dh.getTextReceivedCount() == 1);
        assertEquals(stateMessage.get("FERRETSTATE"), "Reject");
        assertEquals(stateMessage.get("JMSTOPIC"), "Test.FER.State");
    }

    public static void publishFerretStateMessage(Connection connection, String topic, String state)
        throws JMSException {
        Map<String, String> ferretStateMessage = new HashMap<String, String>();
        MessageUtil.setFerretMode(ferretStateMessage, state);
        ferretStateMessage.put("TIMESTAMP", DateTimeUtil.format(new DateTime()));

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination d = session.createTopic(topic);
        MessageProducer mp = session.createProducer(d);
        TextMessage tm = session.createTextMessage(MessageUtil.createRecord(ferretStateMessage));

        mp.send(tm);
    }

    private void publishFerretStatusMessage(Connection connection, String topicBase, String userOrderId)
        throws JMSException {
        LocalDate localDate = new LocalDate();

        Map<String, String> ferretStatusMessage = new HashMap<String, String>();
        MessageUtil.setStatus(ferretStatusMessage, "New");
        MessageUtil.setDestination(ferretStatusMessage, "TICKET");
        MessageUtil.setOrderDate(ferretStatusMessage, localDate);
        MessageUtil.setUserOrderId(ferretStatusMessage, userOrderId);
        ferretStatusMessage.put("SYMBOL", "TYM9");
        ferretStatusMessage.put("TIMESTAMP", DateTimeUtil.format(new DateTime()));

        StringBuilder topic = new StringBuilder();
        topic.append(topicBase).append(".").append(localDate.toString("yyyyMMdd"));
        topic.append(".").append(userOrderId);

        System.err.println("Generated topic: " + topic.toString());
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination d = session.createTopic(topic.toString());
        MessageProducer mp = session.createProducer(d);
        TextMessage tm = session.createTextMessage(MessageUtil.createRecord(ferretStatusMessage));

        mp.send(tm);
    }

    private void publishTestMessage(Connection connection, String destination) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination d = session.createTopic(destination);
        MessageProducer mp = session.createProducer(d);
        TextMessage tm = session.createTextMessage("STATUS=Sent|MESSAGE=Hello from the test!");
        mp.send(tm);
    }
}
