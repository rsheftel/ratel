package org.ratel.schedule.dependency;

import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;

public class WaitForTime extends Dependency {

    private final String time;
    public WaitForTime(Integer id, Map<String, String> parameters) {
        super(id);
        bombUnless(parameters.size() == 1, "should be only one parameter (time) for TimeDependency in " + parameters);
        time = bombNull(parameters.get("time"), "no time param in " + parameters);
    }

    @Override public boolean isIncomplete(Date asOf) {
        return job().calendar().isBeforeTime(asOf, time);
    }

    public static Dependency create(Job anItem, String time) {
        return anItem.insertDependency(WaitForTime.class, map("time", time));
    }

    @Override public String explain(Date asOf) {
        return "TimeDependency: asOf date " + paren(yyyyMmDdHhMmSs(asOf)) + " is before " + time;
    }

}
