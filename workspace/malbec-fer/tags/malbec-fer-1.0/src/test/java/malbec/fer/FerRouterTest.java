package malbec.fer;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import malbec.AbstractBaseTest;
import malbec.fer.fix.AbstractFixTest;
import malbec.fer.fix.FixDestination;
import malbec.fer.jms.AbstractJmsApp;
import malbec.fer.jms.AbstractJmsTest;
import malbec.fer.jms.JmsClientApp;
import malbec.fer.jms.JmsServerApp;
import malbec.fer.rediplus.RediPlusDestination;
import malbec.fix.FixClient;
import malbec.fix.util.QfjHelper;
import malbec.util.EmailSettings;
import malbec.util.IWaitFor;
import malbec.util.StrategyAccountMapper;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import quickfix.SessionSettings;
import quickfix.examples.executor.Executor;

/**
 * Test the routing logic.
 * 
 */
public class FerRouterTest extends AbstractBaseTest {

    private boolean initializedJMS;

    static {
        TaskService.getInstance().addExecutor("QuickFIXJ",
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("QuickFIXEngine")));
    }

    AbstractJmsTest jmsHarness = new AbstractJmsTest() {};
    AbstractFixTest fixHarness = new AbstractFixTest() {};

    @BeforeClass(groups = { "usejms", "unittest", "redi" })
    protected void initJms() {
        synchronized (jmsHarness) {
            if (!initializedJMS) {
                jmsHarness.init();
                initializedJMS = true;
            }
        }
    }

    @AfterClass(groups = { "usejms", "unittest", "redi" })
    public void shutdown() {
        synchronized (jmsHarness) {
            if (initializedJMS) {
                jmsHarness.shutdown();
                initializedJMS = false;
            }
        }
    }

    @BeforeMethod(groups = { "unittest", "usefix" })
    public void startFix() throws Exception {
        fixHarness.startQuickFix();
    }

    @AfterMethod(groups = { "unittest", "usefix" })
    public void stopFix() {
        fixHarness.stopQuickFix();
    }

    @Test(groups = { "unittest", "usejms", "usefix" })
    public void receiveJmsSendViaFix() throws Exception {
        System.out.println("Starting 'receiveJmsSendViaFix'");
        // start the FIX server
        final FixClient fixServer = new FixClient("FixServer", fixHarness.createInitiatorSession(),
                new EmailSettings());
        fixServer.start();
        fixHarness.waitForLogon(fixServer);
        assertTrue(fixServer.isLoggedOn(), "Session failed to logon");

        // start the JMS server
        final AbstractJmsApp jsa = new JmsServerApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());
        jsa.start();
        jmsHarness.waitForConnected(jsa);

        assertTrue(jsa.isConnected(), "Did not connect to broker");

        // Send an order from JMS - should show up in the FIX engine
        // create a JMS client to send the order with
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");
        String jmsMessageID = jca.sendOrder(jmsHarness.createLimitOrder("FixServer"));
        assertNotNull(jmsMessageID, "Failed to send order");

        // Check that the JMS Server application received the order
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return jsa.getUnprocessedMessageCount() > 0;
            }
        }, true, 2000);

        assertTrue(jsa.getUnprocessedMessageCount() > 0, "Did not receive test order");

        // shut everything down
        jca.stop();
        assertFalse(jca.isRunning(), "Failed to stop JMS client");
        jsa.stop();
        assertFalse(jsa.isRunning(), "Failed to stop JMS server");
        fixServer.stop();
        assertFalse(fixServer.isLoggedOn(), "Did not logoff and shutdown FIX");
    }

    
    @Test(groups = { "redi", "usejms" })
    public void receiveJmsSendViaRedi() throws Exception {
        System.out.println("Starting 'receiveJmsSendViaRedi'");

        StrategyAccountMapper sam = createStrategyAccountMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

        final IOrderDestination rediDestination = new RediPlusDestination("RediServer", createRediConfig(), new EmailSettings());
        fr.addOrderDestination(rediDestination);
//        final ComClient comServer = new RediPlusServer("RediServer", createRediConfig(), new EmailSettings());
//        fr.addComClient(comServer);
        
        final JmsServerApp jsa = new JmsServerApp("JmsServer", new EmailSettings());
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
        Order limitOrder = jmsHarness.createLimitOrder("RediServer");
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
    
    private String extractError(Map<String, String> response) {
        if (response.containsKey("ERROR_1")) {
            return response.get("ERROR_1");
        }
        
        return "";
    }

    private Properties createRediConfig() {
        Properties props = new Properties();
        props.setProperty("userID", "gc320050");
        props.setProperty("password", "mal200");
        
        return props;
    }

    @Test(groups = { "unittest", "usefix", "usejms" })
    public void routeOrder() throws Exception {
        System.out.println("Starting 'routeOrder'");

        StrategyAccountMapper sam = createStrategyAccountMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

//        final FixClient fixServer = new FixClient("FixServer", fixHarness.createInitiatorSession(), new EmailSettings());
//        fr.addFixConnection(fixServer);

        final FixDestination destination = new FixDestination("FixServer", fixHarness.createInitiatorSession(), new EmailSettings());
        fr.addOrderDestination(destination);
        
        final JmsServerApp jsa = new JmsServerApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        fr.start();
        fixHarness.waitForLogon(destination.getFixClient());
        jmsHarness.waitForConnected(jsa);

        // Send an order from JMS - should show up in the FIX engine
        // create a JMS client to send the order with
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");

        // send a bad order, no destination
        Order noPlatformOrder = jmsHarness.createLimitOrder();
        noPlatformOrder.setStrategy("TESTE");

        String jmsMessageID = jca.sendOrder(noPlatformOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        // This is the main logic to be tested - moved to be executed by a separate thread
        // fr.process();

        System.out.println("Waiting for response to: " + jmsMessageID);
        jmsHarness.waitForResponse(jca, jmsMessageID);
        System.out.println("Response received: " + jmsMessageID);
        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertTrue(response.containsKey("ERROR_1"), "Response does not contain errors");

        // send a market order
        Order marketOrder = jmsHarness.createMarketOrder("FixServer");
        marketOrder.setStrategy("TESTE");

        jmsMessageID = jca.sendOrder(marketOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        // fr.process();

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response for JMSMessageID: " + jmsMessageID);
        assertTrue(response.containsKey("ERROR_1"), "Response does not contain errors");

        // send a good order (limit)
        jmsMessageID = jca.sendOrder(jmsHarness.createLimitOrder("FixServer"));
        assertNotNull(jmsMessageID, "Failed to send order");

        // fr.process();

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        System.out.println(response);
        assertFalse(response.containsKey("ERROR_1"), "Response has errors");

        // send a good order (stop/limit)
        jmsMessageID = jca.sendOrder(jmsHarness.createStopLimitOrder("FixServer"));
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertFalse(response.containsKey("ERROR_1"), "Response has errors" + response.get("ERROR_1"));

        // Clean up the test
        jca.stop();
        fr.stop();
    }

    @Test(groups = { "unittest", "usejms" })
    public void queryOrder() throws Exception {
        StrategyAccountMapper sam = createStrategyAccountMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

//        final FixClient fixServer = new FixClient("FixServer", fixHarness.createInitiatorSession(), new EmailSettings());
//        fr.addFixConnection(fixServer);
        
        final FixDestination destination = new FixDestination("FixServer", fixHarness.createInitiatorSession(), new EmailSettings());
        fr.addOrderDestination(destination);


        final JmsServerApp jsa = new JmsServerApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        fr.start();
        fixHarness.waitForLogon(destination.getFixClient());
        jmsHarness.waitForConnected(jsa);
        
        // Query the server for an order
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");

        // send an order so we can query later
        Order queryOrder = jmsHarness.createLimitOrder("FixServer");
        String jmsMessageID = jca.sendOrder(queryOrder);
        assertNotNull(jmsMessageID, "Failed to send order");
        System.out.println("Waiting for response to: " + jmsMessageID);
        jmsHarness.waitForResponse(jca, jmsMessageID);
        System.out.println("Response received: " + jmsMessageID);
        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertFalse(response.containsKey("ERROR_1"), "Response contain errors");
        
        // Start the query
        String queryMessageID = jca.queryOrder(queryOrder.getClientOrderID());
        assertNotNull(queryMessageID, "Failed to send query");
        jmsHarness.waitForResponse(jca, queryMessageID);
        Map<String, String> queryResponse = jca.getResponseFor(queryMessageID);
        assertNotNull(queryResponse, "Did not receive query response");
        assertFalse(queryResponse.containsKey("ERROR_1"), "Response contain errors:" + queryResponse.get("ERROR_1"));
        Order responseOrder = new Order(queryResponse);
        
        boolean foundOrder = queryOrder.getClientOrderID().equals(responseOrder.getClientOrderID());
        assertTrue(foundOrder, "Failed to query for order");
        
        // Query for a missing order
        String missingMessageID = jca.queryOrder("ShouldNotExist");
        assertNotNull(missingMessageID, "Failed to send query");
        jmsHarness.waitForResponse(jca, missingMessageID);
        Map<String, String> missingQueryResponse = jca.getResponseFor(missingMessageID);
        assertNotNull(missingQueryResponse, "Did not receive query response");
        assertTrue(missingQueryResponse.containsKey("ERROR_1"), "Response does not contain errors");

        // Clean up the test
        jca.stop();
        fr.stop();
    }
    
    private StrategyAccountMapper createStrategyAccountMapper() {
        StrategyAccountMapper sam = new StrategyAccountMapper(true);
        sam.addMapping("FixServer", "TEST.EQUITY", "Equity", "FIX-TEST");
        sam.addMapping("FixServer", "TESTE", "Equity", "FIX-TEST");
        sam.addMapping("FixServer", "TESTF", "Futures", "FIX-TEST");
        
        sam.addMapping("RediServer", "TESTF", "Futures", "5CX19915");
        sam.addMapping("RediServer", "TESTE", "Equity", "5CX19915");
        sam.addMapping("RediServer", "TEST.EQUITY", "Equity", "5CX19915");
        
        sam.addMapping("Executor", "TEST.EQUITY", "Equity", "EXEC-TEST");
        sam.addMapping("Executor", "TESTE", "Equity", "EXEC-TEST");
        sam.addMapping("Executor", "TESTF", "Futures", "EXEC-TEST");

        sam.addMapping("DATABASE", "TEST.EQUITY", "Equity", "DB-TEST");

        return sam;
    }

    @Test(groups = { "unittest" })
    public void testDBUpdates() {
        StrategyAccountMapper sam = createStrategyAccountMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);

        Order order = jmsHarness.createLimitOrder("DATABASE");
        order.setStrategy("DB-TEST-STRATEGY");
        order.setAccount("DB-TEST-ACCOUNT");
        order.setSecurityType("Equity");

        long id = fr.persistOrder(order);
        assertNotSame(id, -1, "Failed to insert test order");

        boolean rt = fr.updateAcceptedOrder(order.getClientOrderID(), String.valueOf(System
                .nanoTime()));

        assertTrue(rt, "Failed to update order to accepted");
    }

    /**
     * If these fail, make sure that the executor is configured to have ResetOnLogon=Y
     * 
     * @throws Exception
     */
    @Test(groups = { "executor" })
    public void testWithExecutor() throws Exception {
        // startup the executor in a different thread so we can continue
        ScheduledExecutorService executor = TaskService.getInstance().getExecutor("QuickFIXJ");
        // Send a 'CR' to the stdin to kill this
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Starting executor");
//                    SessionSettings settings = fixHarness.createExecutorSessionSettings();
//                    ExecutorRunner.startExecutor(settings);
                    
                    Executor.main(new String[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        StrategyAccountMapper sam = createStrategyAccountMapper();

        FerRouter fr = new FerRouter(new EmailSettings(), sam);
//        final FixClient fixServer = new FixClient("Executor", fixHarness.createInitiatorSession(),new EmailSettings());
//        fr.addFixConnection(fixServer);

        final FixDestination destination = new FixDestination("Executor", fixHarness.createInitiatorSession(), new EmailSettings());
        fr.addOrderDestination(destination);

        
        final JmsServerApp jsa = new JmsServerApp("JmsServer", jmsHarness.createJmsTestProperties());
        // jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        fr.start();
        fixHarness.waitForLogon(destination.getFixClient());

        // Send an order that should make it to the execution engine
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");

        // Limit Order
        Order limitOrder = jmsHarness.createLimitOrder("Executor");
        setExecutorClientID(limitOrder);
        System.out.println("Sending limit order: " + limitOrder.getClientOrderID());
        String jmsMessageID = jca.sendOrder(limitOrder);
        assertNotNull(jmsMessageID, "Failed to send order");
        waitForOrderAccepted(fr, limitOrder);
        Order limit = fr.findOrderByClientOrderID(limitOrder.getClientOrderID());
        assertNotNull(limit, "Did not select order by 'ClientOrderID' " + limitOrder.getClientOrderID());
        assertNotNull(limit.getOrderID(), "Limit Order not accepted by engine");
        if (jca.getUnprocessedMessageCount() > 0) {
            jca.dumpUnprocessedMessages();
        }
        System.out.println("Limit order unprocessed message count=" + jca.getUnprocessedMessageCount());

        System.out.println("Sending stop/limit order");
        // send a good order (stop/limit)
        Order stopLimitOrder = jmsHarness.createStopLimitOrder("Executor");
        setExecutorClientID(stopLimitOrder);
        jmsMessageID = jca.sendOrder(stopLimitOrder);
        assertNotNull(jmsMessageID, "Failed to send order");
        waitForOrderAccepted(fr, stopLimitOrder);
        System.out.println("Finished waiting");
        Order stopLimit = fr.findOrderByClientOrderID(stopLimitOrder.getClientOrderID());
        assertNotNull(stopLimit.getOrderID(), "StopLimit Order not accepted by engine");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        Map<String, String> response = jca.removeResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response for " + jmsMessageID);
        assertFalse(response.containsKey("ERROR_1"), "Response does not contain errors");

        // send a good order (stop)
        System.out.println("Sending stop order");
        Order stopOrder = jmsHarness.createStopOrder("Executor");
        setExecutorClientID(stopOrder);
        jmsMessageID = jca.sendOrder(stopOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.removeResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertFalse(response.containsKey("ERROR_1"), "Response contain errors:" + response.get("ERROR_1"));

        waitForOrderAccepted(fr, stopOrder);
        System.out.println("Finished waiting");
        System.out.println("Looking up order: " + stopOrder.getClientOrderID());
        Order stop = fr.findOrderByClientOrderID(stopOrder.getClientOrderID());
        assertNotNull(stop.getOrderID(), "Stop Order not accepted by engine");

        // Send a futures order
        Order futuresOrder = jmsHarness.createFuturesOrder("Executor");
        setExecutorClientID(futuresOrder);
        jmsMessageID = jca.sendOrder(futuresOrder);
        assertNotNull(jmsMessageID, "Failed to send order");
        waitForOrderAccepted(fr, futuresOrder);
        System.out.println("Finished waiting");
        Order futures = fr.findOrderByClientOrderID(futuresOrder.getClientOrderID());
        assertNotNull(futures.getOrderID(), "Futures order not accepted by engine");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.removeResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertFalse(response.containsKey("ERROR_1"), "Response does not contain errors");

        // sleep(30000);

        // Clean up the test
        jca.stop();
        fr.stop();
    }

    private void setExecutorClientID(Order limitOrder) {
        limitOrder.setClientOrderID("EX-" + System.nanoTime());
    }

    private void waitForOrderAccepted(final FerRouter fr, final Order order) {

        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                Order foundOrder = fr.findOrderByClientOrderID(order.getClientOrderID());
                return (foundOrder != null && foundOrder.getOrderID() != null);

            }
        }, true, 10000);
    }

    static class ExecutorRunner {

        public static void startExecutor(SessionSettings settings) throws Exception {
            try {
                Executor executor = new Executor(settings);
                QfjHelper.executeVoidNoArgMethod(executor, "start");
                // executor.start();

                System.out.println("press <enter> to quit");
                System.in.read();

                QfjHelper.executeVoidNoArgMethod(executor, "stop");
                // executor.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
