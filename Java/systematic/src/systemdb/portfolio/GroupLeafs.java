package systemdb.portfolio;

import static util.Objects.*;

import java.util.*;

import systemdb.metadata.*;
import db.*;
import db.tables.SystemDB.*;

public class GroupLeafs extends GroupMemberMSIVPVsBase {
    private static final long serialVersionUID = 1L;
    public static final GroupLeafs LEAFS = new GroupLeafs();
	
	public GroupLeafs() {
		super("leaf");
	}

	public void insert(String group, MsivPv msivpv, double weight) {
		insert(
			C_GROUPNAME.with(group),
			msivpv.msivCell(C_MSIV_NAME),
			msivpv.pvCell(C_PV_NAME),
			C_WEIGHT.with(weight)
		);
	}

	public Map<MsivPv, Double> weighting(String group, double weighting) {
		Map<MsivPv, Double> result = emptyMap();
		for (Row row : rows(C_GROUPNAME.is(group))) {
			MsivPv msivPv = msivPv(row);
			result.put(msivPv, row.value(C_WEIGHT) * weighting);
		}
		return result;
	}

    private MsivPv msivPv(Row row) {
        return new MsivPv(row.value(C_MSIV_NAME), row.value(C_PV_NAME));
    }

    public Group group(Row row) {
        return new GroupLeaf(msivPv(row), row.value(C_WEIGHT));
    }

    public void delete(String group) {
        deleteAll(C_GROUPNAME.is(group));
    }
}