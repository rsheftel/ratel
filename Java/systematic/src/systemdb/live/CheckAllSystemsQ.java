package systemdb.live;

import static systemdb.live.Exceptions.*;
import static systemdb.metadata.MsivPv.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import systemdb.metadata.*;
import systemdb.portfolio.*;
import util.*;

public class CheckAllSystemsQ {

    public static void main(String[] args) {
        doNotDebugSqlForever();
        List<MsivPv> liveHistoryMarkets = empty();
        List<LiveSystem> liveSystems = MsivLiveHistory.LIVE.liveSystems();
        for (LiveSystem liveSystem : liveSystems)
            liveHistoryMarkets.addAll(liveSystem.liveMarkets());
        List<MsivPv> allSystemsQMarkets = Groups.GROUPS.liveMarkets("AllSystemsQ");
        if(allSystemsQMarkets.equals(liveHistoryMarkets)) return;
        
        List<MsivPv> extraQ = copy(allSystemsQMarkets);
        extraQ.removeAll(liveHistoryMarkets);
        EXCEPTIONS.removeAllowedMissingFromLiveHistory(extraQ);
        List<MsivPv> extraLiveHistory = copy(liveHistoryMarkets);
        extraLiveHistory.removeAll(allSystemsQMarkets);
        EXCEPTIONS.removeAllowedMissingFromAllSystemsQ(extraLiveHistory);
        
        for (MsivPv msivPv : copy(extraQ)) {
            Log.info("checking " + msivPv);
            String market = msivPv.market();
            List<MarketPeriod> activePeriods = new Market(market).activePeriods();
            if(hasContent(activePeriods) && last(activePeriods).hasEnd()) extraQ.remove(msivPv);
        }
        
        if(Log.verbose()) {
            Log.info("AllSystemsQ:" + allSystemsQMarkets);
            Log.info("MsivLiveHistory: " + liveHistoryMarkets);
        }
        
        if(hasContent(extraQ))
            info("The following MsivPvs are in AllSystemsQ, but not MsivLiveHistory:\n" + Strings.join("\n", names(extraQ)));
        if(hasContent(extraLiveHistory))
        info("The following MsivPvs are in MsivLiveHistory, but not AllSystemsQ:\n" + Strings.join("\n", names(extraLiveHistory)));
        
        bombIf(hasContent(extraQ) || hasContent(extraLiveHistory), "markets do not match!");
        info("Success!");
    }

}
