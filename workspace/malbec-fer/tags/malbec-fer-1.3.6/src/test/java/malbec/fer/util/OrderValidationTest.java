package malbec.fer.util;

import static org.testng.Assert.*;

import malbec.fer.CancelRequest;
import malbec.fer.Order;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class OrderValidationTest {

    @Test(groups = { "unittest" })
    public void testUserOrderId() {
        
        String localDateString = new LocalDate().toString("YYYYMMdd");
        String userOrderId = "123456";
        
        String clientOrderId = OrderValidation.generateClientOrderId(new LocalDate(), Order.class, userOrderId);
        assertEquals(clientOrderId, localDateString + "-0" + userOrderId, "Unable to generated valid ClientOrderId");

        String clientOrderId2 = OrderValidation.generateClientOrderId(new LocalDate(), CancelRequest.class, userOrderId);
        assertEquals(clientOrderId2, localDateString + "-1" + userOrderId, "Unable to generated valid ClientOrderId");

        boolean invalidLength = OrderValidation.isValidUserOrderId(userOrderId + "7");
        assertFalse(invalidLength, "Failed to flag UserOrderId as valid");
        
        boolean invalidSpaces = OrderValidation.isValidUserOrderId("1 2 3 ");
        assertFalse(invalidSpaces, "Failed to flag UserOrderId as valid");
        
        boolean invalidChar = OrderValidation.isValidUserOrderId("1%23");
        assertFalse(invalidChar, "Failed to flag UserOrderId as valid");
        
        try {
            OrderValidation.generateClientOrderId(new LocalDate(), Order.class, userOrderId+"99");
            assertFalse(true, "Generation of ClientOrderId did not fail");
        } catch (IllegalArgumentException e) {
            
        }
    }
}
