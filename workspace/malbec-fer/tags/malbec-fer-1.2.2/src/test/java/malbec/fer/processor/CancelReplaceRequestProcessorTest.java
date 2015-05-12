package malbec.fer.processor;

import static malbec.fer.OrderTest.*;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Map;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class CancelReplaceRequestProcessorTest extends OrderProcessorTest {

    
    @Test(groups = { "unittest", "unittest-crp" })
    public void testReplaceNewOrder() {
        DatabaseMapper dbm = new DatabaseMapper(true);
        dbm.addAccountMapping("TEST", "TEST.EQUITY", "EQUITY", "TEST-ACCOUNT");
        OrderProcessor crp = new CancelReplaceRequestProcessor(dbm);
        BaseOrderRequestProcessor op = new OrderProcessor(dbm);
        
        Map<String, IOrderDestination>  orderDestinations = createTestDestinations();

        // Create an order to cancel/replace
        Map<String, String> sourceMessage = ot.createLimitOrder("TEST").toMap();
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations);
        
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
        
        processResults= crp.process(sourceMessage, orderDestinations);
        
        String error1 = processResults.get("ERROR_1");
        assertNull(error1, "There was an un-expected error processing order: " + error);
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "CANCELREPLACEREQUESTED", "Status not set to 'CANCELREPLACEREQUESTED'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");
    }
}
