package malbec.fer.fix;

import static malbec.fer.FerretRouterTestHelper.*;
import static malbec.fer.CancelRequestTest.generateCancelReplaceRequestFromOrder;
import static malbec.fer.CancelRequestTest.generateCancelRequestFromOrder;
import static org.testng.Assert.*;

import java.math.BigDecimal;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;
import malbec.util.InvalidConfigurationException;

import org.testng.annotations.Test;

import quickfix.Message;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;

public class FixDestinationTest extends AbstractFixTest {

    private OrderTest ot = new OrderTest();

    @Test(groups = { "unittest" })
    public void testFixDestinationCreateOrder() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationCreateOrder'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), createDatabaseMapper());
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        // Send a valid order
        ITransportableOrder to = fd.createOrder(ot.createStopLimitOrder());
        assertTrue(to.errors().size() == 0, "Converted order has errors");
        assertTrue(to.transport(), "Failed to transport order");

        // Send a invalid order
        to = fd.createOrder(ot.createMarketOrder());
        assertTrue(to.errors().size() > 0, "Order should have an error");
        assertFalse(to.transport(), "Transported an order that contained errors");

        fc.stop();
        waitForLogoff(fc);

        assertFalse(fc.isLoggedOn(), "Logout did not occur on session");
        assertFalse(fc.isRunning(), "FixClient is running, after stop requested");
        System.out.println("Finished 'testFixDestinationStartup'");
    }

    @Test(groups = { "unittest" })
    public void testFixDestinationCancelOrder() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationCancelOrder'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), createDatabaseMapper());
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        // Send a valid order
        Order order = ot.createStopLimitOrder();
        ITransportableOrder to = fd.createOrder(order);
        assertTrue(to.errors().size() == 0, "Converted order has errors");
        assertTrue(to.transport(), "Failed to transport order");

        // Send a cancel request
        CancelRequest cr = generateCancelRequestFromOrder(order);
        cr.setOriginalUserOrderId(order.getUserOrderId());
        fd.createCancelOrder(cr);
        assertTrue(to.errors().size() == 0, "Converted cancel request has errors");
        assertTrue(to.transport(), "Failed to transport cancel request");

        fc.stop();
        waitForLogoff(fc);

        assertFalse(fc.isLoggedOn(), "Logout did not occur on session");
        assertFalse(fc.isRunning(), "FixClient is running, after stop requested");
        System.out.println("Finished 'testFixDestinationStartup'");
    }

    @Test(groups = { "unittest" })
    public void testFixDestinationCancelReplaceOrder() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationCancelReplaceOrder'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), createDatabaseMapper());
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        // Send a valid order
        Order order = ot.createStopLimitOrder();
        ITransportableOrder to = fd.createOrder(order);
        assertTrue(to.errors().size() == 0, "Converted order has errors");
        assertTrue(to.transport(), "Failed to transport order");

        // Send a cancel request
        CancelReplaceRequest cr = generateCancelReplaceRequestFromOrder(order);
        cr.setOriginalUserOrderId(order.getUserOrderId());

        cr.setQuantity(BigDecimal.valueOf(89));
        fd.createReplaceOrder(cr);
        assertTrue(to.errors().size() == 0, "Converted cancel request has errors");
        assertTrue(to.transport(), "Failed to transport cancel request");

        fc.stop();
        waitForLogoff(fc);

        assertFalse(fc.isLoggedOn(), "Logout did not occur on session");
        assertFalse(fc.isRunning(), "FixClient is running, after stop requested");
        System.out.println("Finished 'testFixDestinationCancelReplaceOrder'");
    }

    //@Test(groups = { "unittest" })
    public void testTradeStationEquityCreate() throws Exception {

        DatabaseMapper dbm = new DatabaseMapper();

        final FixDestination fd = new TradeStationDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isSessionTime());
        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(ot.createStopLimitOrder());

        Message fixMessage = to.getFixMessage();

        assertEquals("EQU", fixMessage.getString(SecurityType.FIELD),
            "Incorrect security type for Tradestation");

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");

        fc.stop();
        waitForLogoff(fc);
    }

    //@Test(groups = { "unittest-disabled" })
    public void testTradeStationFuturesCreate() throws Exception {
        DatabaseMapper dbm = new DatabaseMapper();
        // bb -> ts
        dbm.addFuturesSymbolMapping("TS", "AD", "6A");

        final FixDestination fd = new TradeStationDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        Order testOrder = OrderTest.createFuturesOrder();
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(testOrder);

        Message fixMessage = to.getFixMessage();

        assertEquals(SecurityType.FUTURE, fixMessage.getString(SecurityType.FIELD),
            "Incorrect security type for Tradestation");

        assertEquals(fixMessage.getString(SecurityID.FIELD), "USM09", "Incorrect value for symbol/SecurityID");
        assertEquals("100", fixMessage.getString(SecurityIDSource.FIELD), "Incorrect value for ID source");
        assertFalse(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");

        // testing mapping logic
        /*
         * testOrder.setSymbol("ADZ9"); to = (FixTransportableOrder) fd.createOrder(testOrder);
         * 
         * fixMessage = to.getFixMessage();
         * 
         * assertEquals(SecurityType.FUTURE, fixMessage.getString(SecurityType.FIELD),
         * "Incorrect security type for Tradestation");
         * 
         * assertEquals("6AZ09", fixMessage.getString(SecurityID.FIELD),
         * "Incorrect value for symbol/SecurityID"); assertEquals("100",
         * fixMessage.getString(SecurityIDSource.FIELD), "Incorrect value for ID source");
         * assertFalse(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
         */
        fc.stop();
        waitForLogoff(fc);
    }

    @Test(groups = { "unittest" })
    public void testGoldmanSachsEquityCreate() throws Exception {

        DatabaseMapper dbm = new DatabaseMapper();

        final FixDestination fd = new GoldmanSachsDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(ot.createStopLimitOrder());

        Message fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");

        fc.stop();
        waitForLogoff(fc);
    }

    @Test(groups = { "unittest" })
    public void testGoldmanSachsFuturesCreate() throws Exception {
        DatabaseMapper dbm = new DatabaseMapper();
        // bb -> ts
        dbm.addFuturesSymbolMapping("US", "US", "US");
        dbm.addFuturesMultiplier("REDI", "US", "US", BigDecimal.TEN);
        dbm.addMarketMapping("US.1C", "USM9", "US200906");

      //12.54
        final FixDestination fd = new GoldmanSachsDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        Order testOrder = OrderTest.createFuturesOrder();
        testOrder.setSymbol("USM9");
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(testOrder);
        Message fixMessage = to.getFixMessage();

        assertEquals(fixMessage.getString(SecurityID.FIELD), "USM9", "Incorrect value for symbol/SecurityID");
        assertEquals(fixMessage.getString(SecurityIDSource.FIELD), SecurityIDSource.RIC_CODE, "Incorrect value for ID source");
        assertTrue(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
        assertEquals(fixMessage.getDouble(Price.FIELD), 125.4d);

        fc.stop();
        waitForLogoff(fc);
    }
}
