package schedule.dependency;

import static com.malbec.tsdb.markit.MarkitTable.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;

import db.*;


public class MarkitDataReady extends Dependency {

	public MarkitDataReady(Integer id, @SuppressWarnings("unused") Map<String, String> parameters) {
		super(id);
	}
	
	@Override public String explain(Date asOf) {
		return "no rows in T_Markit_Cds_Composite_Hist for date " + asOf;
	}

	@Override public boolean isIncomplete(Date asOf) {
	    try {
	        Db.execute("set transaction isolation level read uncommitted");
	        return MARKIT_CDS.matches(asOf).isEmpty();
	    } finally {
	        Db.execute("set transaction isolation level read committed");
	    }
	}

    public static Dependency create(Job job) {
        Map<String, String> empty = emptyMap();
        return job.insertDependency(MarkitDataReady.class, empty);
    }

}
