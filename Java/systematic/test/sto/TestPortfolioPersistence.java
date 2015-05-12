package sto;

import static systemdb.metadata.SystemDetailsTable.*;
import static util.Objects.*;
import static systemdb.metadata.MsivBacktestTable.*;
import junit.framework.*;

public class TestPortfolioPersistence extends TestCase {

    private static final String MSIV2 = "TEST.US.1C_TestSystem1_daily_1.0";
    private static final String MSIV1 = "TEST.SP.1C_TestSystem1_daily_1.0";

    public void testCanSaveAndLoadAPortfolio() throws Exception {
        int systemId = DETAILS.insert("TestSystem1", "daily", "1.0", "NA", "NA", "NA");
        BACKTEST.insert(MSIV1, "NA");
        BACKTEST.insert(MSIV2, "NA");
        Portfolio p = new Portfolio("test", list(
            new WeightedMsiv(MSIV1, 1.0),
            new WeightedMsiv(MSIV2, 2.0)
        ));
        p.store(systemId);
        assertEquals(p, the(Portfolio.portfolios(systemId)));
    }
    
}
