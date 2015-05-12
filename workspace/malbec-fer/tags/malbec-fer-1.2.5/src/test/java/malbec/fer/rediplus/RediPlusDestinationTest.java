package malbec.fer.rediplus;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Properties;

import malbec.AbstractBaseTest;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.util.EmailSettings;
import malbec.util.IWaitFor;

import org.testng.annotations.Test;

public class RediPlusDestinationTest extends AbstractBaseTest {

    @Test(groups = { "redi" })
    public void testSendOrderEquity() {
        RediPlusServer rps = new RediPlusServer("UnitTestRedi", createUnitTestConfig(), new EmailSettings());

        BigDecimal quantity = new BigDecimal(1);

        // do a limit buy
        Order order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "BUY", "LIMIT", quantity,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        assertTrue(rps.sendOrder(order), "Unable to send limit/buy order: " + order.getMessage());

        // limit sell
        order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "Sell", "LIMIT", quantity,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        assertTrue(rps.sendOrder(order), "Unable to send limit/sell order: " + order.getMessage());

        // limit short sell
        order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "Sellshort", "LIMIT", quantity,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account

        assertFalse(rps.sendOrder(order), "Sent short sell that usually fails");
    }

    @Test(groups = { "redi", "redidebug" })
    public void testSendOrderEquityBasket() {
        String basketName = String.valueOf(System.currentTimeMillis() / 1000);
        
        RediPlusServer rps = new RediPlusServer("UnitTestRedi", createUnitTestConfig(), new EmailSettings());

        BigDecimal quantity = new BigDecimal(100);

        // do a limit buy
        Order order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "SELL", "LIMIT", quantity,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        //order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setAccount("MSCO"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        order.setBasketName(basketName);
        assertTrue(rps.sendOrder(order), "Unable to send limit/buy order: " + order.getMessage());

        // limit sell
        order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "BUY", "LIMIT", new BigDecimal(200),
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.56"));
        order.setAccount("MSCO"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        order.setBasketName(basketName);
        assertTrue(rps.sendOrder(order), "Unable to send limit/sell order: " + order.getMessage());

        // limit short sell
        order = new Order("IT-" + System.nanoTime(), "ZXZZT", "EQUITY", "SELLSHORT", "LIMIT", BigDecimal.ONE,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.55"));
        order.setAccount("MSCO"); // should be strategy for a real order and not account
        order.setBasketName(basketName);
        order.setExchange("ticket");
        assertTrue(rps.sendOrder(order), "Sent short sell failed");
        System.out.println(order.getMessage());
    }

    @Test(groups = { "redi" })
    public void testSendOrderFutures() {
        RediPlusServer rps = new RediPlusServer("UnitTestRedi", createUnitTestConfig(), new EmailSettings());

        BigDecimal quantity = new BigDecimal(1);

        // do a limit buy
        Order order = new Order("IT-" + System.nanoTime(), "EDZ8", "FUTURES", "BUY", "LIMIT", quantity,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("0.01"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        assertTrue(rps.sendOrder(order), "Unable to send limit/buy order: " + order.getMessage());
        /*
         * // limit sell order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "Sell", "LIMIT",
         * quantity, "UnitTestRedi");
         * 
         * order.setLimitPrice(new BigDecimal("9.54")); order.setAccount("5CX19915"); // should be strategy
         * for a real order and not account order.setExchange("ticket");
         * 
         * assertTrue(rps.sendOrder(order), "Unable to send limit/sell order: " + order.getMessage());
         * 
         * // limit short sell order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "Sellshort",
         * "LIMIT", quantity, "UnitTestRedi");
         * 
         * order.setLimitPrice(new BigDecimal("9.54")); order.setAccount("5CX19915"); // should be strategy
         * for a real order and not account
         * 
         * assertFalse(rps.sendOrder(order), "Sent short sell that usually fails");
         */
    }

    @Test(groups = { "redi" })
    public void testDestination() {
        RediPlusDestination rpd = new RediPlusDestination("RediTest", createUnitTestConfig(),
                new EmailSettings());

        rpd.start();

        waitForListener(rpd);
        assertTrue(rpd.isListeningToMessages(), "Failed to start message listener");

        BigDecimal quantity = new BigDecimal(1);

        // do a limit buy
        Order order = new Order("IT-" + System.nanoTime(), "ZVZZT", "EQUITY", "BUY", "LIMIT", quantity,
                "UnitTestRedi");

        order.setLimitPrice(new BigDecimal("9.54"));
        order.setAccount("5CX19915"); // should be strategy for a real order and not account
        order.setExchange("ticket");

        ITransportableOrder to = rpd.createOrder(order);
        assertNotNull(to, "Failed to create order");
        assertTrue(to.errors().size() == 0, "Order has errors");

        assertTrue(to.transport(), "Unable to send order:" + to.errors());

        rpd.stop();

        waitForListenerStop(rpd);

        assertFalse(rpd.isListeningToMessages(), "Failed to stop listener");
    }

    private void waitForListenerStop(final RediPlusDestination rpd) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return !rpd.isListeningToMessages();
            }
        }, true, 10 * 1000);

    }

    private void waitForListener(final RediPlusDestination rpd) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return rpd.isListeningToMessages();
            }
        }, true, 10 * 1000);

    }

    private Properties createUnitTestConfig() {
        Properties props = new Properties();
        props.setProperty("userID", "gc320050");
        props.setProperty("password", "mal200");

        return props;
    }
}
