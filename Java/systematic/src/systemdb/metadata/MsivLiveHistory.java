package systemdb.metadata;


import static systemdb.metadata.MsivTable.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.MsivTable.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;

public class MsivLiveHistory extends MSIVLiveHistoryBase {
    private static final long serialVersionUID = 1L;
    public static final MsivLiveHistory LIVE = new MsivLiveHistory();
    public static final Siv TEST_SIV = new Siv("TestSystem1", "daily", "1.0");
    
    public MsivLiveHistory() {
        super("liveHistory");
    }

    public List<Pv> pvs(Siv siv) {
        Clause isSystem = MSIVS.matches(siv);
        return pvs(C_PV_NAME.distinct(isSystem.and(MSIVS.nameMatches(C_MSIV_NAME)).and(isLive())));
    }

    private Clause isLive() {
        return DatetimeColumn.dateInRange(now(), C_START_TRADING, C_END_TRADING);
    }

    public List<Market> markets(Siv siv, Pv pv) {
        Clause systemMatches = MSIVS.matches(siv);
        Clause pvMatches = pv.matches(C_PV_NAME);
        Clause joinClause = C_MSIV_NAME.is(MSIVS.C_NAME);
        return markets(MSIVS.C_MARKET.values(systemMatches.and(pvMatches).and(joinClause).and(isLive())));
    }

    private List<Market> markets(List<String> names) {
        List<Market> result = empty();
        for (String name : names)
            result.add(new Market(name));
        return result;
    }

    public List<Pv> pvsForMsiv(MsivRow msiv) {
        Clause isMsiv = msiv.matches(C_MSIV_NAME);
        return pvs(C_PV_NAME.distinct(isMsiv.and(isLive())));
        
    }

    private List<Pv> pvs(List<String> pvNames) {
        List<Pv> result = empty();
        for (String name : pvNames) {
            result.add(new Pv(name));
        }
        return result ;
    }

    public List<LiveSystem> systems() {
        Clause msivJoin = MSIVS.nameMatches(C_MSIV_NAME);
        List<LiveSystem> result = empty();
        Clause matches = isLive().and(msivJoin);
        SelectMultiple select = selectDistinct(Column.columns(
            MSIVS.C_SYSTEM,
            MSIVS.C_INTERVAL, 
            MSIVS.C_VERSION,
            C_PV_NAME
        ), matches);
        for(Row r : select.rows())
            result.add(liveSystem(r));
        return result;
    }
    
    public List<LiveSystem> liveSystems() {
        List<LiveSystem> systems = systems();
        for (LiveSystem testSystem : TEST_SIV.liveSystems()) 
            systems.remove(testSystem);
        return systems;
    }

    private LiveSystem liveSystem(Row r) {
        return new LiveSystem(
            MSIVS.siv(r),
            new Pv(r.value(C_PV_NAME))
        );
    }

    public void insert(LiveSystem liveSystem, String marketName, Date start, Date end) {
        Siv siv = liveSystem.siv();
        if (!MSIVS.exists(siv, marketName)) MSIVS.insert(marketName, siv);
        MsivRow msiv = siv.with(marketName);
        insert(
            C_MSIV_NAME.with(msiv.name()),
            C_PV_NAME.with(liveSystem.pv().name()),
            C_START_TRADING.with(start),
            C_END_TRADING.withMaybe(end)
        );
    }    
}
