package org.ratel.schedule.dependency;

import static org.ratel.schedule.dependency.DependencyParameterTable.*;
import static org.ratel.schedule.dependency.DependencyTable.*;
import static org.ratel.util.Arguments.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.schedule.*;
import org.ratel.schedule.JobTable.*;
import org.ratel.schedule.StatusHistoryTable.*;
import org.ratel.util.*;
import static org.ratel.schedule.StatusHistoryTable.*;

public abstract class Dependency {

    protected final int id;

    public abstract boolean isIncomplete(Date asOf);
    
    @Override public String toString() {
        return paren("" + id);
    }

    public Dependency(int id) {
        this.id = id;
    }

    public void setStatusAndCommit(JobStatus status, Date asOf) {
        STATUS.setStatusAndCommit("dependency", id, status, asOf);
    }

    public Date statusTime(Date asOf) {
        return STATUS.updateTime("dependency", id, asOf);
    }
    
    public boolean hasStatusEntry(Date asOf) {
        return STATUS.hasEntry("dependency", id, asOf);
    }

    public JobStatus status(Date asOf) {
        return STATUS.status("dependency", id, asOf);
    }
    
    public Map<String, String> parameters() {
        return DEPENDENCY_PARAMS.parameters(id());
    }

    public Integer id() {
        return id;
    }

    public abstract String explain(Date asOf);

    
    public Job job() {
        return DEPENDENCIES.job(id);
    }

    public void delete() {
        DEPENDENCIES.delete(id);
    }

    public List<StatusEntry> statusHistory(Date asOf) {
        return STATUS.statuses(asOf, this);
    }
    
    public static void main(String[] args) {
        doNotDebugSqlForever();
        Arguments arguments = arguments(args, list("id", "date"));
        int id = Integer.parseInt(arguments.get("id"));
        Date asOf = arguments.get("date", midnight());
        Dependency dep = DEPENDENCIES.forId(id);
        if(dep.isIncomplete(asOf))
            info(dep.explain(asOf));
        else
            info("SUCCESS");
        
    }


}