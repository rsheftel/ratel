package malbec.fer;

import static org.testng.Assert.*;
import static malbec.fer.FerRouterTestHelper.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.Test;

import malbec.AbstractBaseTest;
import malbec.fer.jms.AbstractJmsTest;
import malbec.fer.jms.JmsClientApp;
import malbec.fer.jms.JmsServerSessionApp;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.fer.rediplus.RediPlusDestination;
import malbec.util.EmailSettings;
import malbec.util.MessageUtil;

public class FerRouterRediTest extends AbstractBaseTest {

    AbstractJmsTest jmsHarness = new AbstractJmsTest() {};
    OrderTest ot = new OrderTest();

    @Test(groups = { "redi", "usejms" })
    public void receiveJmsSendViaRedi() throws Exception {
        System.out.println("Starting 'receiveJmsSendViaRedi'");

        IDatabaseMapper sam = createDatabaseMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

        final IOrderDestination rediDestination = new RediPlusDestination("RediServer", createRediConfig(),
                new EmailSettings());
        fr.addOrderDestination(rediDestination);
        // final ComClient comServer = new RediPlusServer("RediServer", createRediConfig(), new
        // EmailSettings());
        // fr.addComClient(comServer);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        fr.start();
        jmsHarness.waitForConnected(jsa);

        // Send an order from JMS - should show up in the FIX engine
        // create a JMS client to send the order with
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");

        // send a market order
        Order marketOrder = jmsHarness.createMarketOrder("RediServer");
        marketOrder.setStrategy("TESTE");

        String jmsMessageID = jca.sendOrder(marketOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response for JMSMessageID: " + jmsMessageID);
        assertTrue(response.containsKey("ERROR_1"), "Response does not contain errors");

        // send a good order (limit)
        Order limitOrder = ot.createLimitOrder("RediServer");
        limitOrder.setExchange("ticket");
        limitOrder.setLimitPrice(new BigDecimal("10.01"));
        jmsMessageID = jca.sendOrder(limitOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        System.out.println(response);

        assertFalse(response.containsKey("ERROR_1"), "Response has errors " + extractError(response));

        // send a good order (stop/limit)
        Order stopLimitOrder = jmsHarness.createStopLimitOrder("RediServer");
        stopLimitOrder.setExchange("ticket");
        stopLimitOrder.setLimitPrice(new BigDecimal("9.99"));
        stopLimitOrder.setStopPrice(new BigDecimal("9.97"));
        jmsMessageID = jca.sendOrder(stopLimitOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertFalse(response.containsKey("ERROR_1"), "Response has errors " + response.get("ERROR_1"));

        // Clean up the test
        jca.stop();
        fr.stop();
    }

    @Test(groups = { "redi-todo", "usejms", "spreadtrade" })
    public void receiveJmsSendSpreadViaRedi() throws Exception {
        // TODO
        System.out.println("Starting 'receiveJmsSendViaRediSpread'");

        IDatabaseMapper sam = createDatabaseMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

        final IOrderDestination rediDestination = new RediPlusDestination("RediServer", createRediConfig(),
                new EmailSettings());
        fr.addOrderDestination(rediDestination);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        fr.start();
        jmsHarness.waitForConnected(jsa);

        // Send an order from JMS - should show up in the FIX engine
        // create a JMS client to send the order with
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");

        // send a spread trade
        SpreadTrade spreadTrade = SpreadTradeTest.createTestSpreadTrade("RediServer", 1000, 1200);
        spreadTrade.setStrategy("TESTE");

        String jmsMessageID = jca.sendSpreadTrade(spreadTrade);
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response for JMSMessageID: " + jmsMessageID);
        assertFalse(response.containsKey("ERROR_1"), "Response does not contain errors");

        // Clean up the test
        jca.stop();
        fr.stop();
    }

    @Test(groups = { "redi-todo", "usejms", "spreadtrade" })
    public void testProcessSpreadTrade() {
        // TODO
        IDatabaseMapper sam = createDatabaseMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

        SpreadTrade spreadTrade = SpreadTradeTest.createTestSpreadTrade("RediServer", 1000, 1200);
        // over-ride the strategy
        spreadTrade.setStrategy("TESTE");
        Map<String, String> spreadMap = spreadTrade.toMap();
        MessageUtil.setNewSpreadTrade(spreadMap);

        //fr.processSpreadTrade(null, spreadMap);
        JmsServerSessionApp app = new JmsServerSessionApp("TESTAPP", new EmailSettings());
        
        fr.processMessage(app, spreadMap);
        
        app.getReceivedMessageCount();
        
    }
    
    private Properties createRediConfig() {
        Properties props = new Properties();
        props.setProperty("userID", "gc320050");
        props.setProperty("password", "sun1Darwin");

        return props;
    }

    private String extractError(Map<String, String> response) {
        if (response.containsKey("ERROR_1")) {
            return response.get("ERROR_1");
        }

        return "";
    }
}
