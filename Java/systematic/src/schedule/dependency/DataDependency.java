package schedule.dependency;

import static tsdb.TimeSeriesGroupTable.*;
import static util.Strings.*;

import java.util.*;

import tsdb.TimeSeriesGroupTable.*;
import tsdb.*;

public abstract class DataDependency extends Dependency {

	protected DataSource source;
	protected TimeSeriesGroup group;

	@Override public String toString() {
		return paren("group: " + group.name() + ", " + "source: " + source);
	}
	
	public DataDependency(int id, Map<String, String> parameters) {
		super(id);
		source = new DataSource(parameters.get("source"));
		group = GROUPS.get(parameters.get("time_series_group"));
	}

}