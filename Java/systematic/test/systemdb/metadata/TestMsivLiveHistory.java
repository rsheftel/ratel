package systemdb.metadata;

import static systemdb.metadata.MsivLiveHistory.*;
import db.*;


public class TestMsivLiveHistory extends DbTestCase {

    public static final LiveSystem TEST_LIVE_SYSTEM = new LiveSystem(new Siv("TestSystem1", "daily", "1.0"), new Pv("Fast"));

    public void testSystemsIncludesTestSystem() throws Exception {
        assertContains(TEST_LIVE_SYSTEM, LIVE.systems());
        assertFalse(LIVE.liveSystems().contains(TEST_LIVE_SYSTEM));
    }
    
}
