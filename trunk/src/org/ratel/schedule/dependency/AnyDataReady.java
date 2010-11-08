package org.ratel.schedule.dependency;

import static org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.tsdb.*;

public class AnyDataReady extends DataDependency {

    public AnyDataReady(Integer id, Map<String, String> parameters) {
        super(id, parameters);
    }

    @Override public boolean isIncomplete(Date asOf) {
        return !group.anyPopulatedToday(asOf, source);
    }


    public static void create(Job item, DataSource source, AttributeValues attributes) {
        create(item.name(), item, source, attributes);
    }

    public static void create(String groupName, Job item, DataSource source, AttributeValues attributes) {
        GROUPS.insert(groupName, attributes);
        create(groupName, item, source);
    }

    public static void create(String groupName, Job item, DataSource source) {
        bombUnless(GROUPS.has(groupName), "no time series group named " + groupName);
        item.insertDependency(AnyDataReady.class, map(
            "source", source.name(), 
            "time_series_group", groupName
        ));
    }

    @Override public String explain(Date asOf) {
        return "DataOneDependency: no data for group " + paren(group.name());
    }

    
}
