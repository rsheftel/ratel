package org.ratel.schedule.dependency;

import static org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.tsdb.TimeSeriesGroupTable.*;
import org.ratel.tsdb.*;

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