package malbec.fer.fix;

import static malbec.fer.CancelRequestTest.generateCancelReplaceRequestFromOrder;
import static malbec.fer.CancelRequestTest.generateCancelRequestFromOrder;
import static malbec.fer.FerretRouterTestHelper.createDatabaseMapper;
import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.position.PositionCache;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;
import malbec.util.InvalidConfigurationException;

import org.testng.annotations.Test;

import quickfix.Message;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Side;
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

    // @Test(groups = { "unittest" })
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

    // @Test(groups = { "unittest-disabled" })
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
        // sell short 11
        // buy to cover 11
        // buy 11
        // sell 11
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping("REDI", "TESTE", "EQUITY", "TESTEQUITY");
        PositionCache pc = PositionCache.getInstance();

        final FixDestination fd = new GoldmanSachsDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        dbm.addShortShares("MFPB", "ZVZZT", 11);

        Order sellShort = ot.createStopLimitOrder();
        sellShort.setSide("Sell");
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(sellShort);

        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0, listError(to.errors()));
        Message fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");
        assertEquals(fixMessage.getChar(Side.FIELD), '5');
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 0);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", -11);

        Order buyCover = ot.createStopLimitOrder();
        to = (FixTransportableOrder) fd.createOrder(buyCover);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");
        assertEquals(fixMessage.getChar(Side.FIELD), '1');
        assertEquals(fixMessage.getChar(9020), 'C');
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 11);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", 0);

        Order buy = ot.createStopLimitOrder();
        to = (FixTransportableOrder) fd.createOrder(buy);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");
        assertEquals(fixMessage.getChar(Side.FIELD), '1');
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 22);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", 11);

        Order sell = ot.createStopLimitOrder();
        sell.setSide("Sell");
        to = (FixTransportableOrder) fd.createOrder(sell);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");
        assertEquals(fixMessage.getChar(Side.FIELD), '2');
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 22);

        fc.stop();
        waitForLogoff(fc);
    }

    @Test(groups = { "unittest" })
    public void testGoldmanSachsEquityCreateFailure() throws Exception {
        // current position short 10
        // buy 11
        // current position long 10
        // sell 11
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping("REDI", "TESTE", "EQUITY", "TESTEQUITY");

        final FixDestination fd = new GoldmanSachsDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        PositionCache pc = PositionCache.getInstance();
        pc.updatePosition("MFPB", "ZVZZT", -10);
        Order buyCover = ot.createStopLimitOrder();
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(buyCover);
        List<String> errors = to.errors();
        assertNotNull(errors);
        assertEquals(errors.size(), 1);

        pc.updatePosition("MFPB", "ZVZZT", 10);

        Order sell = ot.createStopLimitOrder();
        sell.setSide("Sell");

        to = (FixTransportableOrder) fd.createOrder(sell);
        errors = to.errors();
        assertNotNull(errors);
        assertEquals(errors.size(), 1);

        fc.stop();
        waitForLogoff(fc);
    }

    @Test(groups = { "unittest" })
    public void testGoldmanSachsFuturesCreate() throws Exception {
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping("REDI", "TESTF", "FUTURES", "TEST");
        
        // bb -> ts
        dbm.addFuturesSymbolMapping("US", "US", "US");
        dbm.addFuturesMultiplier("REDI", "US", "US", BigDecimal.TEN);
        dbm.addMarketMapping("US.1C", "USM9", "Comdty", "US200906");

        // 12.54
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
        assertEquals(fixMessage.getString(SecurityIDSource.FIELD), SecurityIDSource.RIC_CODE,
            "Incorrect value for ID source");
        assertTrue(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
        // This is a sale, so it is rounding up 
        assertEquals(fixMessage.getDouble(Price.FIELD), 125.40625d);

        fc.stop();
        waitForLogoff(fc);
    }
}
