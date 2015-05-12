package systemdb.metadata;

import static util.Dates.*;
import static util.Range.*;

import java.util.*;

import db.*;
import static systemdb.metadata.MsivBacktestTable.*;
import static systemdb.metadata.SystemDetailsTable.*;

public class TestMsivBacktestTable extends DbTestCase {

    public void testCanRetrieveStartAndEndForSystemIdAndMarket() throws Exception {
        Date fives = date("2005/05/05");
        BACKTEST.insert("TY.1C_NDayBreak_daily_1.0", "JEFFSTO", fives, fives);
        int details = DETAILS.insert("NDayBreak", "1.0", "daily", "BFBD30", "C:\nowhere", "JEFFSTO");
        assertEquals(range(fives, fives), BACKTEST.range(details, "TY.1C"));
    }
    
}
