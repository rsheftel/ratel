package systemdb.portfolio;

import static systemdb.portfolio.GroupLeafs.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.*;
import util.*;
import db.*;
import db.tables.SystemDB.*;

public class Groups extends GroupMemberGroupsBase {
    private static final long serialVersionUID = 1L;
    public static final Groups GROUPS = new Groups();

	public Groups() {
		super("groups");
	}
	
	public void insert(String group, String memberGroup, double weight) {
		insert(
			C_GROUPNAME.with(group),
			C_MEMBERGROUPNAME.with(memberGroup),
			C_WEIGHT.with(weight)
		);
	}
	
	public Group forName(String group) {
        return new GroupNode(group, 1.0);
	}
	
	public Map<MsivPv, Double> weighting(String group) {
	    return weighting(forName(group));
	}
	
    public Map<MsivPv, Double> weighting(Group group) {
		Map<MsivPv, Double> weights = emptyMap();
        group.addWeights(weights , 1.0);
        return weights;
	}
    
    public List<MsivPv> liveMarkets(String group) {
        return list(weighting(group).keySet());
    }

	public WeightedMsivPvFiles rWeighting(String group) {
		return rWeighting(forName(group));
	}
	
	public WeightedMsivPvFiles rWeighting(Group group) {
	    return new WeightedMsivPvFiles(weighting(group));
	}

	public void insert(String group) {
		GroupsBase t = GroupsBase.T_GROUPS;
		Cell<String> cell = t.C_NAME.with(group);
		if (t.rowExists(cell.matches())) return;
        t.insert(cell);
	}

    public Group group(Row row) {
        return new GroupNode(row.value(C_MEMBERGROUPNAME), row.value(C_WEIGHT));
    }

    public List<Group> members(String group) {
        List<Group> result = empty();
        for (Row row : rows(C_GROUPNAME.is(group)))
            result.add(group(row));
        for (Row row : LEAFS.rows(LEAFS.C_GROUPNAME.is(group)))
            result.add(LEAFS.group(row));
        return result;
    }
    
    public static void main(String[] args) {
        Group nday = GROUPS.forName("NDayBreak");
        for(MsivPv mp : nday.weighting().keySet()) {
            Siv siv = mp.liveSystem().siv();
            info(siv.system());
        }
    }

    public Group load(Tag group, String groupPrefix) {
        String reloaded = groupPrefix + group.text("name");
        insert(reloaded);
        for(Tag childGroup : group.children("group")) {
            Group child = load(childGroup, groupPrefix);
            GROUPS.insert(reloaded, child.name(), child.weight());
        }
        for(Tag leaf : group.children("leaf")) {
            String market = leaf.text("market");
            Siv siv = Siv.fromSivName(leaf.text("siv"), "_");
            Pv pv = new Pv(leaf.text("pv"));
            GroupLeafs.LEAFS.insert(reloaded, new MsivPv(siv.with(market), pv), leaf.decimal("weight"));
        }
        return GROUPS.forName(reloaded);
    }

}







