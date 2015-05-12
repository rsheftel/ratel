package malbec.fer.processor;

import static malbec.fer.CancelRequestTest.generateCancelRequestFromOrder;
import static malbec.fer.FerretState.*;
import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Map;

import malbec.fer.CancelRequest;
import malbec.fer.FerretRouter;
import malbec.fer.IOrderDestination;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class CancelRequestProcessorTest extends AbstractProcessorTest {

    
    @Test(groups = { "unittest" })
    public void testCancelReplaceNewOrderDMA() {
        DatabaseMapper dbm = createDatabaseMapper();
        OrderProcessor crp = new CancelRequestProcessor(dbm);
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel
        Order order = OrderTest.createLimitOrder("TEST");
        Map<String, String> sourceMessage = order.toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, DMA);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status.toUpperCase(), "SENT", "Status not set to 'SENT'");
        
        CancelRequest cr = generateCancelRequestFromOrder(order);
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, DMA);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = MessageUtil.getStatus(processResults);
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "CANCELREQUESTED", "Status not set to 'CANCELREQUESTED'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
    }
    
    @Test(groups = { "unittest" })
    public void testCancelReplaceNewOrderTicket() {
        DatabaseMapper dbm = createDatabaseMapper();
        OrderProcessor crp = new CancelRequestProcessor(dbm);
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel
        Order order = OrderTest.createLimitOrder("TEST");
        Map<String, String> sourceMessage = order.toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Ticket);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status.toUpperCase(), "SENT", "Status not set to 'SENT'");
        
        CancelRequest cr = generateCancelRequestFromOrder(order);
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, Ticket);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = MessageUtil.getStatus(processResults);
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "CANCELREQUESTED", "Status not set to 'CANCELREQUESTED'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
    }
    
    @Test(groups = { "unittest" })
    public void testCancelReplaceNewOrderStage() {
        DatabaseMapper dbm = createDatabaseMapper();
        OrderProcessor crp = new CancelRequestProcessor(dbm, new FerretRouter(null, dbm));
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel
        Order order = OrderTest.createLimitOrder("TEST");
        order.setLimitPrice(new BigDecimal("99.99"));
        Map<String, String> sourceMessage = order.toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Stage);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "New");
        assertEquals(MessageUtil.getDestination(processResults), "TICKET");
        
        CancelRequest cr = generateCancelRequestFromOrder(order);
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, Stage);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = MessageUtil.getStatus(processResults);
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1, "Accepted");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
        // Get the status of the original orders, as it needs to be set to Cancelled
        String originalOrderStatus = processResults.get("ORIGINALORDERSTATUS");
        assertEquals(originalOrderStatus, "Cancelled");
    }

}
