package schedule;

import static schedule.JobStatus.*;
import static schedule.JobTable.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeriesGroupTable.*;
import static util.Dates.*;
import static util.Errors.*;

import java.util.*;

import db.*;

import schedule.JobTable.*;
import tsdb.TimeSeriesGroupTable.*;
import tsdb.*;

public class Arbitrate implements Schedulable {

	public static final String GROUP_PARAM = "time_series_group";
	public static final String FROM_PARAM = "from_source";
	public static final String TO_PARAM = "to_source";
	public static final String FORCE_HOUR = "force_hour";

	@Override public JobStatus run(Date asOf, Job item) {
		DataSource from = source(item.parameter(FROM_PARAM));
		DataSource to = item.hasParameter(TO_PARAM) ? source(item.parameter(TO_PARAM)) : INTERNAL;
		bombIf(from.equals(to), "cannot arbitrate to and from the same source: " + from);
		TimeSeriesGroup group = GROUPS.get(item.parameter(GROUP_PARAM));
		if(item.parameter("delete_existing", "false").equals("true"))
		    group.purge(asOf, to);
		if(item.hasParameter(FORCE_HOUR))
		    group.arbitrate(asOf, from, to, Integer.valueOf(item.parameter(FORCE_HOUR)));
		else
		    group.arbitrate(asOf, from, to);
		return SUCCESS;
	}
	
	public static void main(String[] args) {
	    Date d = date("2008/11/03");
	    Job job = JOBS.forName("arb tba partial durations");
	    job.setTempParameter("delete_existing", "true");
	    while(d.before(now())) {
	        JobStatus status = job.run(d);
	        bombUnless(status.isSuccess(), "non-success status: " + status);
	        d = businessDaysAhead(1, d, "nyb");
	    }
	    Db.commit();
    }

}
