package org.ratel.schedule.dependency;

import static org.ratel.db.clause.Clause.*;
import static org.ratel.schedule.JobStatus.*;
import static org.ratel.schedule.StatusHistoryTable.*;
import static org.ratel.schedule.dependency.DependencyParameterTable.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.lang.reflect.*;
import java.util.*;

import org.ratel.schedule.*;
import org.ratel.schedule.JobTable.*;
import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.tables.ScheduleDB.*;

public class DependencyTable extends DependencyBase {
    private static final long serialVersionUID = 1L;
    private static final Class<?>[] DEPENDENCY_SIGNATURE = new Class<?>[] {Integer.class, Map.class};
    public static final DependencyTable DEPENDENCIES = new DependencyTable();
    
    public DependencyTable() {
        super("dependency");
    }

    public Dependency insert(int scheduleId, String name, Map<String, String> parameters) {
        insert(
            C_JOB_ID.with(scheduleId),
            C_DEPENDENCY.with(name),
            C_STATUS.with(NOT_STARTED.name())
        );
        int id = Db.identity();
        DEPENDENCY_PARAMS.insert(id, parameters);
        return dependency(id, parameters, name);
    }

    public List<Dependency> dependencies(int scheduleId) {
        List<Dependency> result = empty();
        for (Row row : rows(C_JOB_ID.is(scheduleId))) {
            Integer dependencyId = row.value(C_ID);
            Map<String, String> parameters = DEPENDENCY_PARAMS.parameters(dependencyId);
            String className = row.value(C_DEPENDENCY);
            result.add(dependency(dependencyId, parameters, className));
        }
        return result;
    }

    @SuppressWarnings("unchecked") private Dependency dependency(int dependencyId, Map<String, String> parameters, String className) {
        try {
            Class<? extends Dependency> clazz = (Class<? extends Dependency>) Class.forName(className);
            Constructor<? extends Dependency> constructor = clazz.getConstructor(DEPENDENCY_SIGNATURE);
            return constructor.newInstance(dependencyId, parameters);
        } catch (Exception e) {
            throw bomb("could not create dependency for " + className, e);
        }
    }

    public void updateStatus(int id, JobStatus status) {
        C_STATUS.updateOne(idMatches(id), status.name());
        C_LAST_STATUS_UPDATE.updateOne(idMatches(id));
    }

    private Clause idMatches(int id) {
        return C_ID.is(id);
    }

    public JobStatus status(int id) {
        String string = C_STATUS.value(idMatches(id));
        if (isEmpty(string)) return NOT_STARTED;
        return JobStatus.valueOf(string);
    }

    public Date statusTime(int id) {
        return C_LAST_STATUS_UPDATE.value(idMatches(id));
    }
    public void deleteAll() {
        DEPENDENCY_PARAMS.deleteAll(TRUE);
        deleteAll(TRUE);
    }

    public Dependency forId(int id) {
        return dependency(id, DEPENDENCY_PARAMS.parameters(id), C_DEPENDENCY.value(idMatches(id)));
    }
    
    public void delete(int id) {
        DEPENDENCY_PARAMS.delete(id);
        STATUS.delete("dependency", id);
        deleteOne(idMatches(id));
    }

    public void deleteJobDependecies(Integer jobId) {
        DEPENDENCY_PARAMS.deleteParametersForJob(jobId);
        for (int dependency : C_ID.values(jobMatches(jobId))) 
            STATUS.delete("dependency", dependency);
        deleteAll(jobMatches(jobId));
    }

    private Clause jobMatches(Integer jobId) {
        return C_JOB_ID.is(jobId);
    }

    public Job job(int dependencyId) {
        return JobTable.JOBS.forId(C_JOB_ID.value(C_ID.is(dependencyId)));
    }


}
