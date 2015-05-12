package malbec.fer.jms;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import malbec.AbstractBaseTest;
import malbec.fer.IConnectable;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.util.IWaitFor;
import malbec.util.MessageUtil;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.testng.TestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractJmsTest extends AbstractBaseTest {

    private static final int MAX_MESSAGE_REPONSE = 10000;
    protected static final String FER_RESPONSE = "FER.Response";
    protected static final String FER_COMMAND = "FER.Command";

    //private static final int TCP_PORT = 60606;
    private static final int TCP_PORT = 61616;
    private static final int JMX_PORT = 11099;

    protected static final String BROKER_URL = "tcp://localhost:" + TCP_PORT;
    protected static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:" + JMX_PORT + "/jmxrmi";
    static final int MAX_CONNECT_WAIT = 2000;

    private BrokerService broker;
    private Connection connection;
    private Session session;

    @BeforeClass(groups = { "unittest", "usejms" })
    public void init() {

        System.setProperty("com.sun.management.jmxremote.port", String.valueOf(JMX_PORT));
        try {
            broker = new BrokerService();
            // configure the broker
            broker.setBrokerName("UnitTestBroker");
            broker.addConnector(BROKER_URL);
            broker.setUseJmx(true);

            broker.getManagementContext().setConnectorPort(JMX_PORT);

            broker.start();

        } catch (Exception e) {
            throw new TestException("Unable to start embedded broker. "+ e.getMessage());
        }
    }

    @AfterClass(groups = { "unittest", "usejms" })
    public void shutdown() {
        try {
            broker.stop();
        } catch (Exception e) {
            throw new TestException("Unable to stop embedded broker");
        }
    }

    protected Session getSession() {
        return session;
    }
    
    protected MessageProducer createQueueProducer(String destination) throws JMSException {
        return session.createProducer(session.createQueue(destination));
    }

    protected MessageConsumer createQueueConsumer(String destination) throws JMSException {
        return session.createConsumer(session.createQueue(destination));
    }

    
    protected MessageProducer createProducerFor(Destination destination) throws JMSException {
        return session.createProducer(destination);
    }
    
    
    protected TextMessage createTextMessage(String messageBody) throws JMSException {
        return session.createTextMessage(messageBody);
    }

    public Properties createJmsTestProperties() {
        Properties props = new Properties();

        props.setProperty("jms.brokerurl", BROKER_URL);
        props.setProperty("jms.command.queue", FER_COMMAND);
        //props.setProperty("jms.response.queue", FER_RESPONSE);
        props.setProperty("jms.response.tempqueue", "true");
        props.setProperty("jms.response.topic.prefix", "fer.order.response");
        
        return props;
    }
    
    protected void sendValidLimitOrder() throws JMSException {
        connect();

        MessageProducer producer = createQueueProducer(FER_COMMAND);

        Order orderRecord = createLimitOrder();

        TextMessage textMessage = createTextMessage(MessageUtil.createRecord(orderRecord.toMap()));

        Queue reponseQueue = session.createTemporaryQueue();
        
        textMessage.setJMSReplyTo(reponseQueue);
        
        producer.send(textMessage);
        System.out.println("MessageID=" + textMessage.getJMSMessageID());
        System.out.println("Sent test Limit order");
        producer.close();

        disconnect();
    }

    public Order createLimitOrder() {
        OrderTest ot = new OrderTest();

        return ot.createLimitOrder();
    }

    public Order createMarketOrder(String platform) {
        OrderTest ot = new OrderTest();

        Order mo = ot.createMarketOrder();
        mo.setPlatform(platform);
        
        return mo;
    }

    void sendInvalidMarketOrder() throws JMSException {
        connect();

        MessageProducer producer = createQueueProducer(FER_COMMAND);

        Order orderRecord = createMarketOrder("PLATFORM");

        TextMessage textMessage = createTextMessage(MessageUtil.createRecord(orderRecord.toMap()));

        producer.send(textMessage);
        System.out.println("MessageID=" + textMessage.getJMSMessageID());
        System.out.println("Sent test Market order");
        producer.close();

        disconnect();
    }

    protected Session connect() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            return session;
        } catch (JMSException e) {}

        return null;
    }

    protected boolean disconnect() {
        try {
            session.close();
            connection.close();
            return true;
        } catch (JMSException e) {}
        return false;
    }

    public void waitForConnected(final IConnectable jc) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return jc.isConnected();
            }
        }, true, MAX_CONNECT_WAIT);
    }

    
    public void waitForDisconnect(final IConnectable jc) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return !jc.isConnected();
            }
        }, true, MAX_CONNECT_WAIT);
    }
    
    public void waitForResponse(final JmsClientApp jca) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return jca.getUnprocessedMessageCount() > 0;
            }
        }, true, MAX_MESSAGE_REPONSE);
    }

    public Order createStopLimitOrder(String platform) {
        Order order = createStopLimitOrder();

        order.setPlatform(platform);

        return order;
    }

    private Order createStopLimitOrder() {
        OrderTest ot = new OrderTest();

        return ot.createStopLimitOrder();
    }

    public Order createStopOrder(String platform) {
        OrderTest ot = new OrderTest();

        Order order = ot.createStopOrder();
        order.setPlatform(platform);

        return order;
    }

    public void waitForResponse(final JmsClientApp jca, final String jmsMessageID) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                boolean tempResult = jca.getResponseFor(jmsMessageID) != null;
                return tempResult;
            }
        }, true, MAX_MESSAGE_REPONSE);
    }

    public Order createFuturesOrder(String platform) {
        OrderTest ot = new OrderTest();

        Order order = ot.createFuturesOrder();
        order.setPlatform(platform);

        return order;
    
    }

}
