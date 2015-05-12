package malbec.fer.processor;


import static malbec.fer.FerretState.*;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import malbec.fer.IOrderDestination;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.DateTimeUtil;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.Test;

public class StagedOrderProcessorTest extends AbstractProcessorTest {

    @Test(groups = { "unittest"})
    public void testReleaseStagedOrder() {
        DateTimeUtil.freezeTime(new LocalTime(10,0,0,0).toDateTimeToday());
        DatabaseMapper dbm = createDatabaseMapper();
        Map<String, IOrderDestination> orderDestinations = createTestDestinations();
        String userOrderId = createStagedOrder(dbm);
        
        // Release the order for today
        StagedOrderProcessor sop = new StagedOrderProcessor(dbm);
        Map<String, String> releaseStagedOrderRequest = new HashMap<String, String>();
        MessageUtil.setReleaseStagedOrder(releaseStagedOrderRequest);
        MessageUtil.setOrderDate(releaseStagedOrderRequest, new LocalDate());
        MessageUtil.setUserOrderId(releaseStagedOrderRequest, userOrderId);
        
        Map<String, String> processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Ticket);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        String destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        

        // Try to release it again
        processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Ticket);
        
        error = processResults.get("ERROR_1");
        assertNotNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        
        processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Stage);
        
        error = processResults.get("ERROR_1");
        assertNotNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "FerretRejected");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");

        
        // try to release a staged order while we are in staged mode
        userOrderId = createStagedOrder(dbm);
        
        processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Stage);
        
        error = processResults.get("ERROR_1");
        assertNotNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "FerretRejected");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        
    }

    private String createStagedOrder(DatabaseMapper dbm) {
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
        
        return MessageUtil.getUserOrderId(sourceMessage);
    }
}
