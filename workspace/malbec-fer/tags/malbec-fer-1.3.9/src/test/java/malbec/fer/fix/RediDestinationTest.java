package malbec.fer.fix;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.position.PositionCache;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;

import org.testng.annotations.Test;

import quickfix.Message;
import quickfix.field.OrdType;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;

public class RediDestinationTest extends AbstractFixTest {

    private OrderTest ot = new OrderTest();

    @Test(groups = { "unittest" })
    public void testGoldmanSachsEquityCreate() throws Exception {
        // sell short 100
        // buy to cover 100
        // buy 100
        // sell 100
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping("REDI", "TESTE", "EQUITY", "TESTEQUITY");
        PositionCache pc = PositionCache.getInstance();

        final FixDestination fd = new GoldmanSachsDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        dbm.addShortShares("MFPB", "ZVZZT", 100);

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
        pc.updatePosition("MFPB", "ZVZZT", -100);

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
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 100);
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
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 200);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", 100);

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
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 200);

        // Test the At Close orders
        Order buyMOC = ot.createStopLimitOrder();
        buyMOC.setTimeInForce("CLOSE");
        buyMOC.setOrderType("MARKET");
        
        to = (FixTransportableOrder) fd.createOrder(buyMOC);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();
System.err.println(fixMessage);

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");
        assertEquals(fixMessage.getChar(Side.FIELD), '1');
        assertEquals(OrdType.MARKET_ON_CLOSE, fixMessage.getChar(OrdType.FIELD));
        assertEquals(TimeInForce.DAY, fixMessage.getChar(TimeInForce.FIELD));
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 300);
        
        // Update our positions - I don't know what this should be at this point
//        pc.updatePosition("MFPB", "ZVZZT", 11);

        Order buyLOC = ot.createStopLimitOrder();
        buyLOC.setTimeInForce("CLOSE");
        buyLOC.setOrderType("LIMIT");
        
        to = (FixTransportableOrder) fd.createOrder(buyLOC);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();
System.err.println(fixMessage);
        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertFalse(fixMessage.isSetField(SecurityIDSource.FIELD), "SecurityIDSource is set");
        assertFalse(fixMessage.isSetField(SecurityID.FIELD), "SecurityID is set");
        assertEquals(fixMessage.getChar(Side.FIELD), '1');
        assertEquals(OrdType.LIMIT_ON_CLOSE, fixMessage.getChar(OrdType.FIELD));
        assertEquals(TimeInForce.DAY, fixMessage.getChar(TimeInForce.FIELD));
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 400);
        
        // Update our positions - I don't know what this should be at this point
//        pc.updatePosition("MFPB", "ZVZZT", 11);
        
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
        dbm.addFuturesSymbolMapping("REDI", "US", "US", "US", BigDecimal.ONE);
        dbm.addMarketMapping("US.1C", "USM9", "Comdty", "US200906", "US");

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
        assertEquals(fixMessage.getString(SecurityIDSource.FIELD), SecurityIDSource.RIC_CODE, "Incorrect value for ID source");
        assertTrue(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
        // This is a sale, so it is rounding up 
        assertEquals(fixMessage.getDouble(Price.FIELD), 125.40625d);

        
        // MOC and LOC orders
        Order moc = OrderTest.createFuturesOrder();
        moc.setSymbol("USM9");
        moc.setTimeInForce("CLOSE");
        moc.setOrderType("MARKET");
        to = (FixTransportableOrder) fd.createOrder(moc);
        fixMessage = to.getFixMessage();

        assertEquals(fixMessage.getString(SecurityID.FIELD), "USM9", "Incorrect value for symbol/SecurityID");
        assertEquals(fixMessage.getString(SecurityIDSource.FIELD), SecurityIDSource.RIC_CODE, "Incorrect value for ID source");
        assertTrue(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
        assertEquals(OrdType.MARKET_ON_CLOSE, fixMessage.getChar(OrdType.FIELD));
        assertEquals(TimeInForce.DAY, fixMessage.getChar(TimeInForce.FIELD));

        Order loc = OrderTest.createFuturesOrder();
        loc.setSymbol("USM9");
        loc.setTimeInForce("CLOSE");
        loc.setOrderType("LIMIT");
        to = (FixTransportableOrder) fd.createOrder(loc);
        fixMessage = to.getFixMessage();

        assertEquals(fixMessage.getString(SecurityID.FIELD), "USM9", "Incorrect value for symbol/SecurityID");
        assertEquals(fixMessage.getString(SecurityIDSource.FIELD), SecurityIDSource.RIC_CODE, "Incorrect value for ID source");
        assertTrue(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
        assertEquals(OrdType.LIMIT_ON_CLOSE, fixMessage.getChar(OrdType.FIELD));
        assertEquals(TimeInForce.DAY, fixMessage.getChar(TimeInForce.FIELD));
        // This is a sale, so it is rounding up 
        assertEquals(fixMessage.getDouble(Price.FIELD), 125.40625d);

        fc.stop();
        waitForLogoff(fc);
    }
}
