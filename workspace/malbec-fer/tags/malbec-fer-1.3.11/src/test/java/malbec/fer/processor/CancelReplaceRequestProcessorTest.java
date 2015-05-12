package malbec.fer.processor;

import static malbec.fer.FerretState.*;
import static malbec.fer.OrderTest.generateUserOrderId;
import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Map;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class CancelReplaceRequestProcessorTest extends AbstractProcessorTest {
    
    @Test(groups = { "unittest", "unittest-crp" })
    public void testNewOrderDmaReplaceDma() {
        DatabaseMapper dbm = createDatabaseMapper();
        AbstractOrderRequestProcessor crp = new CancelReplaceRequestProcessor(dbm);
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel/replace
        Map<String, String> sourceMessage = OrderTest.createLimitOrder("TEST").toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, DMA);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status.toUpperCase(), "SENT", "Status not set to 'SENT'");

        MessageUtil.setOriginalUserOrderId(sourceMessage, MessageUtil.getUserOrderId(sourceMessage));
        MessageUtil.setClientOrderId(sourceMessage, null);
        MessageUtil.setUserOrderId(sourceMessage, generateUserOrderId("R"));
        //Create the cancel replace request
        CancelReplaceRequest cr = new CancelReplaceRequest(sourceMessage);
        cr.setQuantity(BigDecimal.valueOf(cr.getQuantity().longValue() + 20));
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, DMA);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "CANCELREPLACEREQUESTED", "Status not set to 'CANCELREPLACEREQUESTED'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
    }
    
    @Test(groups = { "unittest" })
    public void testNewOrderStageReplaceStage() {
        DatabaseMapper dbm = createDatabaseMapper();
        AbstractOrderRequestProcessor crp = new CancelReplaceRequestProcessor(dbm);
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel/replace
        Map<String, String> sourceMessage = OrderTest.createLimitOrder("TEST").toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Stage);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "New");

        MessageUtil.setOriginalUserOrderId(sourceMessage, MessageUtil.getUserOrderId(sourceMessage));
        MessageUtil.setClientOrderId(sourceMessage, null);
        MessageUtil.setUserOrderId(sourceMessage, generateUserOrderId("R"));
        //Create the cancel replace request
        CancelReplaceRequest cr = new CancelReplaceRequest(sourceMessage);
        cr.setQuantity(BigDecimal.valueOf(cr.getQuantity().longValue() + 20));
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, Stage);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1, "New");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
        // Get the status of the original orders, as it needs to be set to Cancelled
        String originalOrderStatus = processResults.get("ORIGINALORDERSTATUS");
        assertEquals(originalOrderStatus, "Cancelled");
    }

    @Test(groups = { "unittest" })
    public void testNewOrderStageReplaceTicket() {
        DatabaseMapper dbm = createDatabaseMapper();
        AbstractOrderRequestProcessor crp = new CancelReplaceRequestProcessor(dbm);
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel/replace
        Map<String, String> sourceMessage = OrderTest.createLimitOrder("TEST").toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Stage);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "New");

        MessageUtil.setOriginalUserOrderId(sourceMessage, MessageUtil.getUserOrderId(sourceMessage));
        MessageUtil.setClientOrderId(sourceMessage, null);
        MessageUtil.setUserOrderId(sourceMessage, generateUserOrderId("R"));
        //Create the cancel replace request
        CancelReplaceRequest cr = new CancelReplaceRequest(sourceMessage);
        cr.setQuantity(BigDecimal.valueOf(cr.getQuantity().longValue() + 20));
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, Ticket);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1, "New");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
        // Get the status of the original orders, as it needs to be set to Cancelled
        String originalOrderStatus = processResults.get("ORIGINALORDERSTATUS");
        assertEquals(originalOrderStatus, "Cancelled");
    }

    @Test(groups = { "unittest" })
    public void testNewOrderTicketReplaceTicket() {
        DatabaseMapper dbm = createDatabaseMapper();
        AbstractOrderRequestProcessor crp = new CancelReplaceRequestProcessor(dbm);
        AbstractOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel/replace
        Map<String, String> sourceMessage = OrderTest.createLimitOrder("TEST").toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Ticket);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");

        MessageUtil.setOriginalUserOrderId(sourceMessage, MessageUtil.getUserOrderId(sourceMessage));
        MessageUtil.setClientOrderId(sourceMessage, null);
        MessageUtil.setUserOrderId(sourceMessage, generateUserOrderId("R"));
        //Create the cancel replace request
        CancelReplaceRequest cr = new CancelReplaceRequest(sourceMessage);
        cr.setQuantity(BigDecimal.valueOf(cr.getQuantity().longValue() + 20));
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations, Ticket);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1, "CancelReplaceRequested");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
    }

}
