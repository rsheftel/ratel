package schedule.dependency;

import static tsdb.TimeSeriesGroupTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import tsdb.TimeSeriesGroupTable.*;
import tsdb.*;

public class AllDataReady extends DataDependency {

	public AllDataReady(Integer id, Map<String, String> parameters) {
		super(id, parameters);
	}
	
	@Override public boolean isIncomplete(Date asOf) {
	    bombIf(group.isEmpty(asOf), "group " + group.name() + " is empty for " + ymdHuman(asOf));
		return !group.allPopulatedToday(asOf, source);
	}

	public static Dependency create(Job job, DataSource source, TimeSeries ... serieses) {
		return create(job.name(), job, source, serieses);
	}
	

	public static Dependency create(Job job, DataSource source, TimeSeriesGroup group) {
	    return job.insertDependency(AllDataReady.class, map(
            "source", source.name(), 
            "time_series_group", group.name()
        ));
	}

    public static Dependency create(Job job, DataSource source, AttributeValues attributes) {
        return create(job.name(), job, source, attributes);
    }

    public static Dependency create(String groupName, Job job, DataSource source, AttributeValues attributes) {
		TimeSeriesGroup group = GROUPS.insert(groupName, attributes);
		return create(job, source, group);
	}

	@Override public String explain(Date asOf) {
		StringBuilder buf = new StringBuilder();
		buf.append("DataAllDependency:\n");
		List<String> missingSeries = group.notPopulatedToday(asOf, source);
		for (String series : missingSeries)
			buf.append(series + ":" + source.name() + " missing\n");
		return buf.toString();
	}

    public static Dependency create(String groupName, Job job, DataSource source, TimeSeries ... serieses) {
        TimeSeriesGroup group = GROUPS.insert(groupName, serieses);
        return create(job, source, group);
    }

}
