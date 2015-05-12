package systemdb.metadata;

import static systemdb.metadata.MarketTable.*;
import db.*;

public class TestMarket extends DbTestCase {

    @Override public void setUp() throws Exception {
        super.setUp();
        insertTestData();
    }

    public static void insertTestData() {
        ExchangesTable.insert("EX1", 17, 19, "16:00:00", 3600);
        ExchangesTable.insert("EX2", 18, 20, "16:00:61", 3600);
        MARKET.insert("MKT1", "EX1", 170.0, null, null);
        MARKET.insert("MKT2", "EX1", null, "13:00:00", 1800);
        MARKET.insert("MKT3", "EX2", 170.0, null, null); // exchange defaulted to invalid value
    }

    public void testCanGetDefaultedSlippageCorrectly() throws Exception {
        assertEquals(170.0, new Market("MKT1").fixedSlippage());
        assertEquals(19.0, new Market("MKT2").fixedSlippage());
    }
        

}
