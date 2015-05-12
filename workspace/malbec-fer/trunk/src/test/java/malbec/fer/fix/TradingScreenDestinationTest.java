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
import quickfix.field.ExecBroker;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.Side;
import quickfix.field.Symbol;

public class TradingScreenDestinationTest extends AbstractFixTest {

    private static final String PLATFORM = "TRADS";
    
    private OrderTest ot = new OrderTest();

    @Test(groups = { "unittest" })
    public void testEquityCreate() throws Exception {
        // sell short 100
        // buy to cover 100
        // buy 100
        // sell 100
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping(PLATFORM, "TESTE", "EQUITY", "TESTEQUITY");
        PositionCache pc = PositionCache.getInstance();

        final FixDestination fd = new TradingScreenDestination("Test Session", createInitiatorSession("FIX.4.4"),
            new EmailSettings(), dbm);
        fd.start();
        fd.setForceToTicket(false);

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        dbm.addShortShares("MFPB", "ZVZZT", 100);
        dbm.addRouteAccount("TRADS", "MANS", "TEST", "EQUITY");

        Order sellShort = ot.createStopLimitOrder();
        sellShort.setSide("Sell");
        sellShort.setRoute("MANS");
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(sellShort);

        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0, listError(to.errors()));
        Message fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertTrue(fixMessage.isSetField(SecurityIDSource.FIELD));
        assertTrue(fixMessage.isSetField(SecurityID.FIELD));
        assertEquals(fixMessage.getChar(Side.FIELD), '5');
        assertEquals(fixMessage.getHeader().getString(ExecBroker.FIELD), "MFGBLUS");
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 0);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", -100);

        Order buyCover = ot.createStopLimitOrder();
        buyCover.setRoute("MANS");
        to = (FixTransportableOrder) fd.createOrder(buyCover);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0, listError(to.errors()));

        fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertTrue(fixMessage.isSetField(SecurityIDSource.FIELD));
        assertTrue(fixMessage.isSetField(SecurityID.FIELD));
        assertEquals(fixMessage.getChar(Side.FIELD), '1');
        //  TradingScreen does not support buy-to-cover
        //assertEquals(fixMessage.getChar(9020), 'C');
        assertFalse(fixMessage.isSetField(9020));
        assertEquals(buyCover.getBuyToCover(), "Y");
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 100);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", 0);

        Order buy = ot.createStopLimitOrder();
        buy.setRoute("MANS");
        to = (FixTransportableOrder) fd.createOrder(buy);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertTrue(fixMessage.isSetField(SecurityIDSource.FIELD));
        assertTrue(fixMessage.isSetField(SecurityID.FIELD));
        assertEquals(fixMessage.getChar(Side.FIELD), '1');
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 200);
        // Update our positions
        pc.updatePosition("MFPB", "ZVZZT", 100);

        Order sell = ot.createStopLimitOrder();
        sell.setSide("Sell");
        sell.setRoute("MANS");
        to = (FixTransportableOrder) fd.createOrder(sell);
        assertNotNull(to.errors());
        assertEquals(to.errors().size(), 0);

        fixMessage = to.getFixMessage();

        assertEquals("ZVZZT", fixMessage.getString(Symbol.FIELD), "Incorrect value for symbol");
        assertTrue(fixMessage.isSetField(SecurityIDSource.FIELD));
        assertTrue(fixMessage.isSetField(SecurityID.FIELD));
        assertEquals(fixMessage.getChar(Side.FIELD), '2');
        assertEqualsBD(dbm.sharesToShort("MFPB", "ZVZZT"), 200);
        
        fc.stop();
        waitForLogoff(fc);
    }

    @Test(groups = { "unittest" })
    public void testEquityCreateFailure() throws Exception {
        // current position short 10
        // buy 11
        // current position long 10
        // sell 11
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping(PLATFORM, "TESTE", "EQUITY", "TESTEQUITY");
        dbm.addRouteAccount("TRADS", "MANS", "TEST", "EQUITY");

        final FixDestination fd = new TradingScreenDestination("Test Session", createInitiatorSession("FIX.4.4"),
            new EmailSettings(), dbm);
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        PositionCache pc = PositionCache.getInstance();
        pc.updatePosition("MFPB", "ZVZZT", -10);
        Order buyCover = ot.createStopLimitOrder();
        buyCover.setRoute("MANS");
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(buyCover);
        List<String> errors = to.errors();
        assertNotNull(errors);
        assertEquals(errors.size(), 1, listError(errors));;

        pc.updatePosition("MFPB", "ZVZZT", 10);

        Order sell = ot.createStopLimitOrder();
        sell.setSide("Sell");
        sell.setRoute("MANS");

        to = (FixTransportableOrder) fd.createOrder(sell);
        errors = to.errors();
        assertNotNull(errors);
        assertEquals(errors.size(), 1, listError(errors));

        fc.stop();
        waitForLogoff(fc);
    }

    @Test(groups = { "unittest" })
    public void testFuturesCreate() throws Exception {
        DatabaseMapper dbm = new DatabaseMapper();
        dbm.addAccountMapping(PLATFORM, "TESTF", "FUTURES", "TEST");
        
        // bb -> ts
        dbm.addFuturesSymbolMapping(PLATFORM, "US", "US", "ZB", BigDecimal.ONE);
        dbm.addMarketMapping("US.1C", "USM9", "Comdty", "US200906", "US");

        // 12.54
        final FixDestination fd = new TradingScreenDestination("Test Session", createInitiatorSession("FIX.4.4"),
            new EmailSettings(), dbm);
        fd.start();
        fd.setForceToTicket(false);

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        Order testOrder = OrderTest.createFuturesOrder();
        testOrder.setSymbol("USM9");
        testOrder.setRoute("MANS");
        FixTransportableOrder to = (FixTransportableOrder) fd.createOrder(testOrder);
        Message fixMessage = to.getFixMessage();

        assertEquals(fixMessage.getString(SecurityID.FIELD), "ZBM9", "Incorrect value for symbol/SecurityID");
        assertEquals(fixMessage.getString(SecurityIDSource.FIELD), SecurityIDSource.RIC_CODE,
            "Incorrect value for ID source");
        assertTrue(fixMessage.isSetField(Symbol.FIELD), "Symbol is set");
        assertEquals(fixMessage.getHeader().getString(ExecBroker.FIELD), "MFGBLFUT");
        // This is a sale, so it is rounding up 
        assertEqualsBD(fixMessage.getDecimal(Price.FIELD), "125.40625");

        fc.stop();
        waitForLogoff(fc);
    }
}
