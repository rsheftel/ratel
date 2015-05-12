package systemdb.live;

import static systemdb.metadata.TestMsivLiveHistory.*;
import static systemdb.portfolio.Groups.*;

import java.util.*;

import systemdb.metadata.*;
import systemdb.portfolio.*;
import db.*;

public class TestPopulateGroupsForHypoPnl extends DbTestCase {
    public void slowtestCanPopulateOne() throws Exception {
        PnlGroupsPopulator.deleteAll();
        PnlGroupsPopulator.populate(TEST_LIVE_SYSTEM);
        Group top = GROUPS.forName("PnlTags_All");
        List<Group> tags = top.members();
        Group test = GROUPS.forName("QF.Example");
        assertContains(test, tags);
        List<Group> msivs = test.members();
        assertContains(new GroupLeaf(new MsivPv("TEST.SP.1C_TestSystem1_daily_1.0", "Fast"), 1), msivs);
    }
    
    public void testRunSlowTestSometimes() throws Exception {
    
    }
}
