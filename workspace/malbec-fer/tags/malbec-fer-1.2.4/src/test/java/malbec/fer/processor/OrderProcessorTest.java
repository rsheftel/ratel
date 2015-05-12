package malbec.fer.processor;

import static malbec.fer.FerretState.*;
import static org.testng.Assert.*;

import java.util.Map;

import javax.persistence.PersistenceException;

import malbec.fer.IOrderDestination;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class OrderProcessorTest extends AbstractProcessorTest {

    protected OrderTest ot = new OrderTest();

    @Test(groups = { "unittest" })
    public void testCreateNewOrder() {
        DatabaseMapper dbm = createDatabaseMapper();
        OrderProcessor op = new OrderProcessor(dbm);
        Map<String, IOrderDestination> orderDestinations = createTestDestinations();

        // Process a valid limit order
        Map<String, String> sourceMessage = OrderTest.createLimitOrder("TEST").toMap();

        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, DMA);
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status.toUpperCase(), "SENT", "Status not set to 'SENT'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");

        // Process a duplicate order
        processResults = op.process(sourceMessage, orderDestinations, DMA);
        String error1 = processResults.get("ERROR_1");
        assertNotNull(error1, "Failed to generate duplicate order entry");
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "DUPLICATE", "Status not set to 'DUPLICATE'");
        String userOrderId = MessageUtil.getUserOrderId(processResults);
        assertNotNull(userOrderId, "Failed to set UserOrderId on result");
        assertEquals(userOrderId, MessageUtil.getUserOrderId(sourceMessage),
                "Returned UserOrderId != sent UserOrderId");

        // try to process an order with SQL error
        Order sqlErrorOrder = OrderTest.createLimitOrder("TEST");
        System.err.println("Trying to save order with null SIDE");
        sqlErrorOrder.setSide(null);
        sourceMessage = sqlErrorOrder.toMap();
        try {
            processResults = null;
            processResults = op.process(sourceMessage, orderDestinations, DMA);
            assertNull(processResults, "No exceptions thrown");
        } catch (PersistenceException e) {

        }
        assertNull(processResults, "Failed to blowup on processing order");
    }

    @Test(groups = { "unittest" })
    public void testOrderForEachStatus() {
        DatabaseMapper dbm = createDatabaseMapper();
        OrderProcessor op = new OrderProcessor(dbm);
        Map<String, IOrderDestination> orderDestinations = createTestDestinations();

        // Process a valid limit order
        Map<String, String> sourceMessage = OrderTest.createLimitOrder("TEST").toMap();

        // Stage state
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Stage);
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "New");
        String destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");

        // Test for Ticket state
        sourceMessage = OrderTest.createLimitOrder("TEST").toMap();
        processResults = op.process(sourceMessage, orderDestinations, Ticket);
        error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        
        sourceMessage = OrderTest.createLimitOrder("TEST").toMap();
        processResults = op.process(sourceMessage, orderDestinations, DMA);
        error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "DMA");

    }
}
