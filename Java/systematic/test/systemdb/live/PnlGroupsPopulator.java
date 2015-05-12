package systemdb.live;

import static systemdb.portfolio.GroupLeafs.*;
import static systemdb.portfolio.Groups.*;
import static db.tables.SystemDB.GroupsBase.*;
import static util.Errors.*;

import java.util.*;

import db.*;

import systemdb.metadata.*;
import systemdb.portfolio.*;
import static systemdb.metadata.MsivLiveHistory.*;

public class PnlGroupsPopulator {

    public static void deleteAll() {
        LEAFS.deleteAll(LEAFS.C_GROUPNAME.like("QF.%"));
        GROUPS.deleteAll(GROUPS.C_GROUPNAME.like("QF.%"));
        GROUPS.deleteAll(GROUPS.C_MEMBERGROUPNAME.like("QF.%"));
        T_GROUPS.deleteAll(T_GROUPS.C_NAME.like("QF.%"));
    }

    public static void populate(LiveSystem liveSystem) {
        Group top = GROUPS.forName("PnlTags_All");
        String tag = liveSystem.bloombergTag();
        bombUnless(tag.matches("^QF\\..*"), tag + "does not start with QF. ");
        GROUPS.insert(tag);
        List<Group> allTags = top.members();
        if(!allTags.contains(GROUPS.forName(tag)))
            GROUPS.insert(top.name(), tag, 1.0);
        for (MsivPv market : liveSystem.liveMarkets())
            LEAFS.insert(tag, market, 1.0);
    }
    
    public static void main(String[] args) {
        deleteAll();
        List<LiveSystem> liveSystems = LIVE.liveSystems();
        for (LiveSystem liveSystem : liveSystems)
            populate(liveSystem);
        Db.commit();
    }

}
