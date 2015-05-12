package malbec.fer.position;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import malbec.jms.AbstractJmsBaseTest;
import malbec.util.IWaitFor;
import malbec.util.MessageUtil;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.annotations.Test;

public class PositionCacheTest extends AbstractJmsBaseTest {

    @Test(groups = ("unittest"))
    public void testLookupPosition() {
        PositionCache cache = PositionCache.getInstance();

        BigDecimal position = cache.positionFor("BP", "IBM");
        assertNotNull(position);
        assertEqualsBD(position, 0);

        cache.updatePosition("BP", "IBM", BigDecimal.TEN);
        position = cache.positionFor("BP", "IBM");
        assertNotNull(position);
        assertEqualsBD(position, 10);

        Collection<BigDecimal> positions = cache.positions();
        assertNotNull(positions);
        assertEquals(positions.size(), 1);

        assertEquals(cache.count(), 1);
        assertNotNull(cache.purgePosition("BP", "IBM"));
        assertEquals(cache.count(), 0);

        cache.updatePosition("BP", "IBM", BigDecimal.ONE);
        cache.purge();
        assertEquals(cache.count(), 0);
    }

    @Test(groups = ("unittest"))
    public void testListenToPositionTopic() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        // The Real-time Feed publishes on Positions.PrimeBroker.QMF.<prime broker>.EQUITY.<ticker>
        final PositionCache positionCache = PositionCache.getInstance();
        positionCache.setBrokerConnection(connection);
        positionCache.setPrimeBroker("TestBroker");
        positionCache.start();

        publishPositionMessage(connection, "Positions.PrimeBroker.QMF", "TestBroker");
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return positionCache.count() > 0;
            }
            
        }, true, 1000);
        
        assertTrue(positionCache.count() > 0);
        assertEqualsBD(positionCache.positionFor("TestBroker", "ZVZZT"), 200);
        
        positionCache.stop();
        
        connection.stop();
    }

    private void publishPositionMessage(Connection connection, String topicBase, String primeBroker)
        throws JMSException {

        Map<String, String> positionMessage = new HashMap<String, String>();
        positionMessage.put("primeBroker", "TestBroker");
        positionMessage.put("ticker", "ZVZZT");
        positionMessage.put("currentPosition", "200");

        StringBuilder topic = new StringBuilder();
        topic.append(topicBase).append(".").append(primeBroker);
        topic.append(".").append("EQUITY.ZVZZT");

        System.err.println("Generated topic: " + topic.toString());
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination d = session.createTopic(topic.toString());
        MessageProducer mp = session.createProducer(d);
        TextMessage tm = session.createTextMessage(MessageUtil.createRecord(positionMessage));

        mp.send(tm);
        mp.close();
    }
}
