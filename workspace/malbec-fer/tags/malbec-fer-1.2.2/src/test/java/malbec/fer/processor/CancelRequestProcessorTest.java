package malbec.fer.processor;

import static malbec.fer.CancelRequestTest.generateCancelRequestFromOrder;
import static org.testng.Assert.*;

import java.util.Map;

import malbec.fer.CancelRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class CancelRequestProcessorTest extends OrderProcessorTest {

    
    @Test(groups = { "unittest", "unittest-crp" })
    public void testCancelReplaceNewOrder() {
        DatabaseMapper dbm = new DatabaseMapper(true);
        dbm.addAccountMapping("TEST", "TEST.EQUITY", "EQUITY", "TEST-ACCOUNT");
        OrderProcessor crp = new CancelRequestProcessor(dbm);
        BaseOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel
        Order order = ot.createLimitOrder("TEST");
        Map<String, String> sourceMessage = order.toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status.toUpperCase(), "SENT", "Status not set to 'SENT'");
        
        CancelRequest cr = generateCancelRequestFromOrder(order);
        
        sourceMessage = cr.toMap();
        
        processResults= crp.process(sourceMessage, orderDestinations);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "CANCELREQUESTED", "Status not set to 'CANCELREQUESTED'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
        
    }
}
