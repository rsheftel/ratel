package org.ratel.schedule.dependency;

import static org.ratel.schedule.dependency.DependencyTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.schedule.*;
import org.ratel.schedule.JobTable.*;

public class WaitForDependency extends Dependency {

    private static final String ID = "parent_dependency_id";
    private static final String DELAY = "delay_seconds";
    private int parentId;
    private int delay;

    public WaitForDependency(Integer id, Map<String, String> parameters) {
        super(id);
        parentId = Integer.valueOf(parameters.get(ID));
        delay = parameters.containsKey(DELAY) ? Integer.valueOf(parameters.get(DELAY)) : 0;
    }
    
    @Override public String explain(Date asOf) {
        if(parentStatus(asOf).isSuccess())
            return 
                "WaitForDependency: " + parentId + " finished less than " + delay + " seconds ago.\n" +
                "Finished at " + parentStatusTime(asOf) + ", asOf is " + now() + "\n";
            
        return "WaitForDependency: " + parentId + " is not marked as SUCCESS.  Status is: " + parentStatus(asOf) + "\n";
    }

    @Override public boolean isIncomplete(Date asOf) {
        return !parentStatus(asOf).isSuccess() || secondsAhead(delay, parentStatusTime(asOf)).after(now());
    }

    private Date parentStatusTime(Date asOf) {
        return DEPENDENCIES.forId(parentId).statusTime(asOf);
    }

    private JobStatus parentStatus(Date asOf) {
        try {
            return DEPENDENCIES.forId(parentId).status(asOf);
        } catch (RuntimeException e) {
            throw bomb("parent dependency " + parentId + " could not be looked up", e);
        }
    }

    public static Dependency create(Job job, Dependency parent) {
        return job.insertDependency(WaitForDependency.class, map(ID, parent.id().toString()));
    }

    public static Dependency create(Job job, Dependency parent, Integer secondsDelay) {
        return job.insertDependency(WaitForDependency.class, map(ID, parent.id().toString(), DELAY, secondsDelay.toString()));
    }

}
