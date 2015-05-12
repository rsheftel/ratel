package malbec.fer.rediplus;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Properties;

import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.util.EmailSettings;

import org.testng.annotations.Test;

public class RediPlusServerTest {

    @Test(groups = { "redi" })
    public void testSendOrder() {
        RediPlusServer rps = new RediPlusServer("UnitTestRedi", createUnitTestConfig(), new EmailSettings());

        BigDecimal quantity = new BigDecimal(1);
        
        // do a limit buy
        Order order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "BUY", "LIMIT", quantity, "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        assertTrue(rps.sendOrder(order), "Unable to send limit/buy order: " + order.getMessage());
        
        // limit sell
        order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "Sell", "LIMIT", quantity, "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        assertTrue(rps.sendOrder(order), "Unable to send limit/sell order: " + order.getMessage());

        // limit short sell
        order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "Sellshort", "LIMIT", quantity, "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account

        assertFalse(rps.sendOrder(order), "Sent short sell that usually fails");

    }

    @Test(groups = { "redi" })
    public void testDestination() {
        RediPlusDestination rpd = new RediPlusDestination("RediTest",createUnitTestConfig(), new EmailSettings());

        BigDecimal quantity = new BigDecimal(1);
        
        // do a limit buy
        Order order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "BUY", "LIMIT", quantity, "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");
        
        ITransportableOrder to = rpd.createOrder(order);
        assertNotNull(to, "Failed to create order");
        assertTrue(to.errors().size() == 0, "Order has errors");
        
        assertTrue(to.transport(), "Unable to send order:" + to.errors());
        
    }

    
    private Properties createUnitTestConfig() {
        Properties props = new Properties();
        props.setProperty("userID", "gc320050");
        props.setProperty("password", "mal200");

        return props;
    }
}
