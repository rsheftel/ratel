package malbec.fer.fix;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;
import quickfix.Message;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;

public class TradeStationDestinationTest extends AbstractFixTest {

    private OrderTest ot = new OrderTest();

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
        dbm.addFuturesSymbolMapping("TS", "AD", "6A", "AD", BigDecimal.ONE);

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
}
