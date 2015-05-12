package systemdb.metadata;

import db.*;

public abstract class MsivPvTestCase extends DbTestCase {

    protected static final MsivPv SP_1C_FAST = new MsivPv("TEST.SP.1C_TestSystem1_daily_1.0", "Fast");
    protected static final MsivPv SP_1C_SLOW = new MsivPv("TEST.SP.1C_TestSystem1_daily_1.0", "Slow");
    protected static final MsivPv US_1C_FAST = new MsivPv("TEST.US.1C_TestSystem1_daily_1.0", "Fast");
    protected static final MsivPv US_1C_SLOW = new MsivPv("TEST.US.1C_TestSystem1_daily_1.0", "Slow");

    public MsivPvTestCase() {
        super();
    }

}