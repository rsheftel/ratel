package systemdb.live;

import java.util.*;

import systemdb.metadata.*;
import db.*;

import static db.clause.Clause.*;
import static systemdb.live.Exceptions.*;
import static systemdb.metadata.MsivTable.*;
import static util.Objects.*;

public class TestExceptions extends DbTestCase {
    public void testExceptionsWork() throws Exception {
        EXCEPTIONS.deleteAll(TRUE);
        MsivPv both = new MsivPv(MSIVS.forName("TY.1C_NDayBreak_daily_1.0"), new Pv("BFBD30"));
        MsivPv allSystemQ = new MsivPv(MSIVS.forName("FV.1C_NDayBreak_daily_1.0"), new Pv("BFBD30"));
        MsivPv liveHistory = new MsivPv(MSIVS.forName("TU.1C_NDayBreak_daily_1.0"), new Pv("BFBD30"));
        MsivPv none = new MsivPv(MSIVS.forName("US.1C_NDayBreak_daily_1.0"), new Pv("BFBD30"));
        EXCEPTIONS.insert(both, true, true);
        EXCEPTIONS.insert(allSystemQ, true, false);
        EXCEPTIONS.insert(liveHistory, false, true);
        EXCEPTIONS.insert(none, false, false);
        
        List<MsivPv> all = list(both, allSystemQ, liveHistory, none);
        List<MsivPv> missingFromAllSystemsQ = copy(all);
        EXCEPTIONS.removeAllowedMissingFromAllSystemsQ(missingFromAllSystemsQ);
        assertEquals(list(liveHistory, none), missingFromAllSystemsQ);
        
        List<MsivPv> missingFromLiveHistory = copy(all);
        EXCEPTIONS.removeAllowedMissingFromLiveHistory(missingFromLiveHistory);
        assertEquals(list(allSystemQ, none), missingFromLiveHistory);
    }
}
