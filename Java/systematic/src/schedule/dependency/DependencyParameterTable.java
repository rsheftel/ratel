package schedule.dependency;

import static schedule.dependency.DependencyTable.*;
import static util.Objects.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.ScheduleDB.*;

public class DependencyParameterTable extends DependencyParametersBase {
    private static final long serialVersionUID = 1L;
	public static final DependencyParameterTable DEPENDENCY_PARAMS = new DependencyParameterTable();
	
	public DependencyParameterTable() {
		super("dep_params");
	}

	public void insert(int id, Map<String, String> parameters) {
		for (String name : parameters.keySet()) 
			insert(
				C_DEPENDENCY_ID.with(id), 
				C_NAME.with(name), 
				C_VALUE.with(parameters.get(name))
			);
	}

	public Map<String, String> parameters(int id) {
		Map<String, String> result = emptyMap();
		for (Row row : rows(C_DEPENDENCY_ID.is(id)))
			result.put(row.value(C_NAME), row.value(C_VALUE));
		return result;
	}

    public boolean exists(String paramName, String value) {
        return C_NAME.is(paramName).and(C_VALUE.is(value)).exists();
    }

    public void deleteParametersForJob(Integer jobId) {
        Clause jobMatches = DEPENDENCIES.C_JOB_ID.is(jobId);
        SelectOne<Integer> depIds = DEPENDENCIES.C_ID.select(jobMatches);
        deleteAll(C_DEPENDENCY_ID.in(depIds));
    }

    public void delete(int dependencyId) {
        deleteAll(C_DEPENDENCY_ID.is(dependencyId));
    }

}
