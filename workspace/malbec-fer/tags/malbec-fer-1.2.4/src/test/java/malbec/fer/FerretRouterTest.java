package malbec.fer;

import static malbec.fer.FerretRouterTestHelper.createDatabaseMapper;
import static malbec.fer.FerretState.*;
import static org.testng.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.AbstractBaseTest;
import malbec.fer.fix.AbstractFixTest;
import malbec.fer.fix.FixDestination;
import malbec.fer.jms.AbstractJmsTest;
import malbec.fer.jms.JmsClientApp;
import malbec.fer.jms.JmsServerSessionApp;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fix.FixClient;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSettings;
import malbec.util.IWaitFor;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the routing logic.
 * 
 */
public class FerretRouterTest extends AbstractBaseTest {

    private boolean initializedJMS;
    DatabaseMapper dbm;
    private FerretRouter fr;

    TestMessageProcessorListener mpl = new TestMessageProcessorListener();

    AbstractJmsTest jmsHarness = new AbstractJmsTest() {};
    AbstractFixTest fixHarness = new AbstractFixTest() {};
    OrderTest ot = new OrderTest();

    @BeforeClass(groups = { "usejms", "unittest", "redi", "executor-jms" })
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

    @BeforeMethod(groups = { "unittest", "usefix", "development" })
    public void startFix() throws Exception {
        fr = createFerRouter();
    }

    @AfterMethod(groups = { "unittest", "usefix" })
    public void stopFix() {
        fr.stop();
    }

    @Test(groups = { "unittest", "usejms", "usefix" })
    public void testReceiveJmsSendViaFix() throws Exception {
        System.out.println("Starting 'testReceiveJmsSendViaFix'");
        // start the FIX server
        final FixClient fixServer = new FixClient("FixServer", AbstractFixTest.createInitiatorSession(),
            new EmailSettings());
        fixServer.start();
        fixHarness.waitForLogon(fixServer);
        assertTrue(fixServer.isLoggedOn(), "Session failed to logon");

        // start the JMS server
        final List<Map<String, String>> receivedMessages = new ArrayList<Map<String, String>>();

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());
        jsa.setMessageProcessor(new IMessageProcessor() {

            @Override
            public Map<String, String> processMessage(IMessageProcessListener mpl, Map<String, String> message) {
                receivedMessages.add(message);
                return message;
            }

        });
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
        String jmsMessageID = jca.sendOrder(OrderTest.createLimitOrder("FixServer"));
        assertNotNull(jmsMessageID, "Failed to send order");

        // Check that the JMS Server application received the order
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return jsa.getReceivedMessageCount() > 0;
            }
        }, true, 2000);

        assertTrue(jsa.getReceivedMessageCount() > 0, "Did not receive test order");
        assertTrue(receivedMessages.size() > 0, "Message processor not called");
        assertEquals(jsa.getReceivedMessageCount(), receivedMessages.size(),
            "JMS client and Message processor received different amount of messages");

        // shut everything down
        jca.stop();
        assertFalse(jca.isRunning(), "Failed to stop JMS client");
        jsa.stop();
        assertFalse(jsa.isRunning(), "Failed to stop JMS server");
        fixServer.stop();
        assertFalse(fixServer.isLoggedOn(), "Did not logoff and shutdown FIX");
    }

    @Test(groups = { "unittest", "usefix", "usejms" })
    public void testRouteOrder() throws Exception {
        System.out.println("Starting 'testRouteOrder'");

        DatabaseMapper dbm = createDatabaseMapper();
        dbm.addPlatformToClient(getHostName(), "FixServer");

        final FixDestination destination = new FixDestination("FixServer", AbstractFixTest
            .createInitiatorSession(), new EmailSettings(), dbm);
        fr.addOrderDestination(destination);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
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

        System.out.println("Waiting for response to: " + jmsMessageID);
        jmsHarness.waitForResponse(jca, jmsMessageID);
        System.out.println("Response received or timed-out: " + jmsMessageID);
        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        String errorResponse = response.get("ERROR_1");
        assertNotNull(errorResponse, "Response does not contain errors");

        // send a market order
        Order marketOrder = jmsHarness.createMarketOrder("FixServer");
        marketOrder.setStrategy("TESTE");

        jmsMessageID = jca.sendOrder(marketOrder);
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response for JMSMessageID: " + jmsMessageID);
        assertTrue(response.containsKey("ERROR_1"), "Response does not contain errors");

        // send a good order (limit)
        jmsMessageID = jca.sendOrder(OrderTest.createLimitOrder("FixServer"));
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        System.out.println(response);
        String error1 = response.get("ERROR_1");
        assertFalse(response.containsKey("ERROR_1"), "Response has errors ("+error1+")");

        // send a good order (stop/limit)
        jmsMessageID = jca.sendOrder(jmsHarness.createStopLimitOrder("FixServer"));
        assertNotNull(jmsMessageID, "Failed to send order");

        jmsHarness.waitForResponse(jca, jmsMessageID);
        response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        assertFalse(response.containsKey("ERROR_1"), "Response has errors" + response.get("ERROR_1"));

        // Send a Ferret Mode change message
        Map<String, String> stageStateMap = new HashMap<String, String>();
        MessageUtil.setFerretMode(stageStateMap, "Stage");
        jmsMessageID = jca.sendMessage(stageStateMap);
        assertNotNull(jmsMessageID, "Failed to send message");

        // There is no response, the status is either updated or not
//        response = jca.getResponseFor(jmsMessageID);
//        assertNotNull(response, "Did not receive response");
//        assertFalse(response.containsKey("ERROR_1"), "Response has errors" + response.get("ERROR_1"));
        
        // Clean up the test
        jca.stop();
        fr.stop();
    }

    @Test(groups = { "unittest", "usejms" })
    public void testQueryOrder() throws Exception {
        System.out.println("Starting 'queryOrder'");
        DatabaseMapper dbm = createDatabaseMapper();
        dbm.addPlatformToClient(getHostName(), "FixServer");

        final FixDestination destination = new FixDestination("FixServer", AbstractFixTest
            .createInitiatorSession(), new EmailSettings(), dbm);
        fr.addOrderDestination(destination);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
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
        Order queryOrder = OrderTest.createLimitOrder("FixServer");
        String jmsMessageID = jca.sendOrder(queryOrder);
        assertNotNull(jmsMessageID, "Failed to send order");
        System.out.println("Waiting for response to order: " + jmsMessageID);
        jmsHarness.waitForResponse(jca, jmsMessageID);
        System.out.println("Response received or timed-out: " + jmsMessageID);
        Map<String, String> response = jca.getResponseFor(jmsMessageID);
        assertNotNull(response, "Did not receive response");
        String errorMessage = response.get("ERROR_1");
        assertNull(errorMessage, "Response contain errors: " + errorMessage);

        // Start the query
        String queryMessageID = jca.queryOrder(queryOrder.getUserOrderId(), new LocalDate());
        System.out.println("Waiting for response to query: " + queryMessageID);
        assertNotNull(queryMessageID, "Failed to send query");
        jmsHarness.waitForResponse(jca, queryMessageID);
        Map<String, String> queryResponse = jca.getResponseFor(queryMessageID);
        assertNotNull(queryResponse, "Did not receive query response");
        assertFalse(queryResponse.containsKey("ERROR_1"), "Response contain errors:"
            + queryResponse.get("ERROR_1"));
        Order responseOrder = new Order(queryResponse);

        boolean foundOrder = queryOrder.getUserOrderId().equals(responseOrder.getUserOrderId());
        assertTrue(foundOrder, "Failed to query for order");

        // Query for a missing order
        String missingMessageID = jca.queryOrder("ShouldNotExist", new LocalDate());
        System.out.println("Waiting for response to query (missing order): " + missingMessageID);
        assertNotNull(missingMessageID, "Failed to send query");
        jmsHarness.waitForResponse(jca, missingMessageID);
        Map<String, String> missingQueryResponse = jca.getResponseFor(missingMessageID);
        assertNotNull(missingQueryResponse, "Did not receive query response");
        assertTrue(missingQueryResponse.containsKey("ERROR_1"), "Response does not contain errors");

        // Clean up the test
        jca.stop();
        fr.stop();
        System.out.println("Finished 'queryOrder'");
    }

    //@Test(groups = { "unittest-disabled" })
    public void testCancelNewOrder() throws Exception {
        System.out.println("Starting 'testCancelNewOrder'");

        DatabaseMapper dbm = createDatabaseMapper();
        dbm.addPlatformToClient(getHostName(), "FixServer");

        final FixDestination destination = new FixDestination("FixServer", AbstractFixTest
            .createInitiatorSession(), new EmailSettings(), dbm);
        fr.addOrderDestination(destination);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        // create the order and then cancel
        Order newOrder = OrderTest.createLimitOrder("FixServer");
        newOrder.setAccount("UT-TEST-ACCOUNT");
        Map<String, String> newOrderMap = newOrder.toMap();

        TestTransportableOrder tto = new TestTransportableOrder();

        assertTrue(newOrder.getId() != -1, "Failed to persist new order");
        assertTrue(tto.didTransport(), "Failed to transport");
        String status = newOrderMap.get("STATUS");
        assertNotNull(status, "Failed to update status on message");
        assertEquals(status, "SENT", "Status not set to 'SEND'");

        // Cancel the order
        CancelRequest cr = new CancelRequest(newOrderMap);
        cr.setOriginalUserOrderId(cr.getUserOrderId());

        // This is not the same as NewOrder and CancelReplace - should it be?
        // TODO change how this works so that we can test this
        Map<String, String> crMap = cr.toMap();

        // fr.processCancelOrder(jsa, cr, crMap);
        assertTrue(cr.getId() != -1, "Failed to instert CancelRequest");
        String crStatus = crMap.get("STATUS");
        assertNotNull(crStatus, "Failed to update status on message");
    }
    
    //@Test(groups = { "unittest-disabled" })
    public void testCancelReplaceOrder() throws Exception {
        System.out.println("Starting 'testCancelReplaceOrder'");

        DatabaseMapper dbm = createDatabaseMapper();
        dbm.addPlatformToClient(getHostName(), "FixServer");

        final FixDestination destination = new FixDestination("FixServer", AbstractFixTest
            .createInitiatorSession(), new EmailSettings(), dbm);
        fr.addOrderDestination(destination);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());

        fr.addJmsConnection(jsa);

        // create the order and then cancel
        Order newOrder = OrderTest.createLimitOrder("FixServer");
        newOrder.setAccount("UT-TEST-ACCOUNT");
        Map<String, String> newOrderMap = newOrder.toMap();

        TestTransportableOrder tto = new TestTransportableOrder();

        // fr.processNewOrder(jsa, newOrder, tto, newOrderMap);
        assertTrue(newOrder.getId() != -1, "Failed to persist new order");
        assertTrue(tto.didTransport(), "Failed to transport");
        String status = newOrderMap.get("STATUS");
        assertNotNull(status, "Failed to update status on message");
        assertEquals(status, "SENT", "Status not set to 'SEND'");

        // CancelReplace the order
        CancelReplaceRequest cr = new CancelReplaceRequest(newOrderMap);
        cr.setOriginalUserOrderId(cr.getUserOrderId());

        Map<String, String> crMap = cr.toMap();
        // fr.processReplaceOrder(jsa, cr, tto, crMap);
        assertTrue(cr.getId() != -1, "Failed to instert CancelReplaceRequest");
        String crStatus = crMap.get("STATUS");
        assertNotNull(crStatus, "Failed to update status on message");
    }

    @Test(groups = { "unittest-integration" })
    public void testCancelNewOrderIntegration() throws Exception {
        System.out.println("Starting 'testCancelNewOrderIntegration'");

        DatabaseMapper dbm = createDatabaseMapper();
        dbm.addPlatformToClient(getHostName(), "FixServer");

        final FixDestination destination = new FixDestination("FixServer", AbstractFixTest
            .createInitiatorSession(), new EmailSettings(), dbm);
        fr.addOrderDestination(destination);

        final JmsServerSessionApp jsa = new JmsServerSessionApp("JmsServer", new EmailSettings());
        jsa.setConfiguration(jmsHarness.createJmsTestProperties());
        fr.addJmsConnection(jsa);

        // Startup FERET
        fr.start();
        fixHarness.waitForLogon(destination.getFixClient());
        jmsHarness.waitForConnected(jsa);

        // Start the client application
        final JmsClientApp jca = new JmsClientApp("Test Session");
        jca.setConfiguration(jmsHarness.createJmsTestProperties());
        jca.start();
        jmsHarness.waitForConnected(jca);
        assertTrue(jca.isConnected(), "Did not connect to broker");

        // send a cancel for none existent order
        String invalidCancelRequest = jca.sendCancel(String.valueOf(System.currentTimeMillis()), String
            .valueOf(System.currentTimeMillis()));
        assertNotNull(invalidCancelRequest, "Failed to send order cancel");
        jmsHarness.waitForResponse(jca, invalidCancelRequest);
        Map<String, String> invalidResponse = jca.getResponseFor(invalidCancelRequest);
        assertNotNull(invalidResponse, "Did not receive response");
        String invalidMessage = invalidResponse.get("ERROR_1");
        assertNotNull(invalidMessage, "Should not have found order to cancel");

        // Send an order and then cancel it
        Order newOrder = OrderTest.createLimitOrder("FixServer");
        String jmsMessageID = jca.sendOrder(newOrder);
        System.err.println("UserOrderId of order to cancel: " + newOrder.getUserOrderId());
        assertNotNull(jmsMessageID, "Failed to send order");

        // Send the cancel request
        String cancelRequestMessageId = jca.sendCancel(String.valueOf(System.currentTimeMillis()), newOrder
            .getUserOrderId());
        assertNotNull(cancelRequestMessageId, "Failed to send order cancel");
        jmsHarness.waitForResponse(jca, cancelRequestMessageId);
        Map<String, String> cancelResponse = jca.getResponseFor(cancelRequestMessageId);
        assertNotNull(cancelResponse, "Did not receive response to cancel");
        String cancelErrorMessage = cancelResponse.get("ERROR_1");
        assertNull(cancelErrorMessage, "Response contain errors: " + cancelErrorMessage);

        String queryMessageId = jca.queryOrder(newOrder.getUserOrderId(), new LocalDate());
        jmsHarness.waitForResponse(jca, queryMessageId);
        Map<String, String> response = jca.getResponseFor(queryMessageId);
        assertNotNull(response, "Did not receive response");
        String errorMessage = response.get("ERROR_1");
        assertNull(errorMessage, "Response contain errors: " + errorMessage);
        // we really just want to ensure we sent the cancel request
        String orderStatus = MessageUtil.getStatus(response);
        assertTrue(orderStatus.contains("CANCEL"), "Failed to send cancel request, status is " + orderStatus);
        // ensure that the status is canceled

        // Clean up the test
        jca.stop();
        fr.stop();
    }

    @Test(groups = { "unittest" })
    public void testStartupStatus() {

        fr = createFerRouter("2009/03/17 03:00:00");
        assertEquals(fr.currentState(), Reject);

        fr = createFerRouter("2009/03/17 05:00:00");
        assertEquals(fr.currentState(), Stage);

        fr = createFerRouter("2009/03/17 10:00:00");
        assertEquals(fr.currentState(), Stage);

        fr = createFerRouter("2009/03/17 16:15:00");
        assertEquals(fr.currentState(), Stage);

        fr = createFerRouter("2009/03/17 16:45:00");
        assertEquals(fr.currentState(), Stage);

        fr = createFerRouter("2009/03/17 21:00:00");
        assertEquals(fr.currentState(), Reject);

        fr = createFerRouter("2009/03/20 21:00:00");
        assertEquals(fr.currentState(), Inactive);
    }

    @Test(groups = { "unittest" })
    public void testOrderForEachStatus() {

        fr = createFerRouter("2009/03/17 03:00:00");
        TestOrderDestination testDestination = new TestOrderDestination();
        fr.addOrderDestination(testDestination);

        assertEquals(fr.currentState(), Reject);
        TestOrderStatus rejectStatus = sendStatusTestOrder(testDestination);
        assertEquals(rejectStatus.status, "FerretRejected");

        // Test for Stage state
        DateTimeUtil.freezeTime("2009/03/17 05:00:00");
        assertEquals(fr.currentState(), Stage);
        TestOrderStatus stageStatus = sendStatusTestOrder(testDestination);
        assertEquals(stageStatus.status, "New");

        // Test for Ticket state
        DateTimeUtil.freezeTime("2009/03/17 06:30:00");
        assertEquals(fr.currentState(), Stage);
        assertTrue(fr.setStateToTicket());
        TestOrderStatus ticketStatus = sendStatusTestOrder(testDestination);

        assertEquals(ticketStatus.status, "Sent");
        assertEquals(ticketStatus.destination, "TICKET");
    }

    @Test(groups = { "unittest" })
    public void testReleaseStagedOrder() {
        TestOrderDestination testDestination = new TestOrderDestination();

        // Test for Stage state
        fr = createFerRouter("2009/03/17 05:00:00");
        fr.addOrderDestination(testDestination);
        assertEquals(fr.currentState(), Stage);
        TestOrderStatus stageStatus = sendStatusTestOrder(testDestination);
        assertEquals(stageStatus.status, "New");

        
        // Try to release a staged order to ticket
        DateTimeUtil.freezeTime("2009/03/17 06:01:00");
        assertTrue(fr.setStateToTicket());
        Map<String, String> sotr = new HashMap<String, String>();
        MessageUtil.setUserOrderId(sotr, stageStatus.userOrderId);
        MessageUtil.setOrderDate(sotr, new LocalDate());
        MessageUtil.setReleaseStagedOrder(sotr);
        
        Map<String, String> result = fr.processMessage(mpl, sotr);
        
        String error = result.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(result);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        String destination = MessageUtil.getDestination(result);
        assertEquals(destination, "TICKET");
    }
    
    @Test(groups = { "unittest"})
    public void testToTicketMessage() {
        // Start at Reject time
        fr = createFerRouter("2009/03/17 03:00:00");
        Map<String, String> ticketStateMap = new HashMap<String, String>();
        MessageUtil.setFerretMode(ticketStateMap, "Ticket");
        
        Map<String, String> result = fr.processMessage(mpl, ticketStateMap);
        assertEquals(result.get("FERRETSTATE"), "Reject");
        
        // Test during Stage time
        DateTimeUtil.freezeTime("2009/03/17 05:00:00");
        MessageUtil.setFerretMode(ticketStateMap, "Ticket");
        
        result = fr.processMessage(mpl, ticketStateMap);
        assertEquals(result.get("FERRETSTATE"), "Stage");
        
        // Test during Ticket time
        DateTimeUtil.freezeTime("2009/03/17 06:00:00");
        MessageUtil.setFerretMode(ticketStateMap, "Ticket");
        
        result = fr.processMessage(mpl, ticketStateMap);
        assertEquals(result.get("FERRETSTATE"), "Ticket");

        // Test during DMA time 
        DateTimeUtil.freezeTime("2009/03/17 07:00:00");
        MessageUtil.setFerretMode(ticketStateMap, "Ticket");
        
        result = fr.processMessage(mpl, ticketStateMap);
        assertEquals(result.get("FERRETSTATE"), "Ticket");

        // Advance to DMA and then test going off
        assertTrue(fr.setStateToDma());
        assertEquals(fr.currentState(), DMA);
        DateTimeUtil.freezeTime("2009/03/17 07:00:00");
        MessageUtil.setFerretMode(ticketStateMap, "Ticket");
        
        result = fr.processMessage(mpl, ticketStateMap);
        assertEquals(result.get("FERRETSTATE"), "Ticket");
    }
    
    @Test(groups = { "unittest" })
    public void testToStageMessage() {
        // Start at Reject time
        fr = createFerRouter("2009/03/17 03:00:00");
        Map<String, String> stageStateMap = new HashMap<String, String>();
        MessageUtil.setFerretMode(stageStateMap, "Stage");
        
        Map<String, String> result = fr.processMessage(mpl, stageStateMap);
        assertEquals(result.get("FERRETSTATE"), "Reject");
        
        // Test during Stage time
        DateTimeUtil.freezeTime("2009/03/17 05:00:00");
        MessageUtil.setFerretMode(stageStateMap, "Stage");
        
        result = fr.processMessage(mpl, stageStateMap);
        assertEquals(result.get("FERRETSTATE"), "Stage");
        
        // Test during Ticket time
        DateTimeUtil.freezeTime("2009/03/17 06:00:00");
        MessageUtil.setFerretMode(stageStateMap, "Stage");
        
        result = fr.processMessage(mpl, stageStateMap);
        assertEquals(result.get("FERRETSTATE"), "Stage");

        // Advance to Ticket and then test going off
        assertTrue(fr.setStateToTicket());
        assertEquals(fr.currentState(), Ticket);
        DateTimeUtil.freezeTime("2009/03/17 06:00:00");
        MessageUtil.setFerretMode(stageStateMap, "Stage");

        result = fr.processMessage(mpl, stageStateMap);
        assertEquals(result.get("FERRETSTATE"), "Stage");
        
        // Test during DMA time 
        DateTimeUtil.freezeTime("2009/03/17 07:00:00");
        MessageUtil.setFerretMode(stageStateMap, "Stage");
        
        result = fr.processMessage(mpl, stageStateMap);
        assertEquals(result.get("FERRETSTATE"), "Stage");

        // Advance to DMA and then test going off
        assertTrue(fr.setStateToTicket());
        assertTrue(fr.setStateToDma());
        assertEquals(fr.currentState(), DMA);
        DateTimeUtil.freezeTime("2009/03/17 07:00:00");
        MessageUtil.setFerretMode(stageStateMap, "Stage");
        
        result = fr.processMessage(mpl, stageStateMap);
        assertEquals(result.get("FERRETSTATE"), "Stage");
    }
    private TestOrderStatus sendStatusTestOrder(TestOrderDestination testDestination) {
        Order order = OrderTest.createLimitOrder(testDestination.getDestinationName());
        System.out.println("Sending limit order: " + order.getUserOrderId());

        Map<String, String> orderMap = order.toMap();
        MessageUtil.setNewOrder(orderMap);

        Map<String, String> result = fr.processMessage(mpl, orderMap);

        return new TestOrderStatus(MessageUtil.getStatus(result), MessageUtil.getDestination(result), order.getUserOrderId());
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "LOCALHOST";
        }
    }

    private FerretRouter createFerRouter(String freezeTime) {
        if (dbm == null) {
            dbm = createDatabaseMapper();
            dbm.addPlatformToClient(getHostName(), "FixServer");
        }
        
        return FerretRouterTestHelper.createFerRouter(freezeTime,dbm);
    }
 
    
    private FerretRouter createFerRouter() {
        if (dbm == null) {
            dbm = createDatabaseMapper();
            dbm.addPlatformToClient(getHostName(), "FixServer");
        }
        
        return FerretRouterTestHelper.createFerRouterDma(dbm);
    }

    private static class TestOrderStatus {
        String status;
        String destination;
        String userOrderId;

        TestOrderStatus(String s, String d, String id) {
            status = s;
            destination = d;
            userOrderId = id;
        }
    }
}
