package org.ratel.schedule;

import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.tables.ScheduleDB.*;

public class ParametersTable extends ParametersBase {
    private static final long serialVersionUID = 1L;
    public static final ParametersTable PARAMETERS = new ParametersTable();
    
    public ParametersTable() {
        super("parameters");
    }

    public void insert(int id, Map<String, String> parameters) {
        for (String key : parameters.keySet())
            insert(
                C_JOB_ID.with(id),
                C_NAME.with(key),
                C_VALUE.with(parameters.get(key))
            );
    }

    public Map<String, String> parameters(int id) {
        Map<String, String> result = emptyMap();
        for(Row r : rows(C_JOB_ID.is(id)))
            result.put(r.value(C_NAME), r.value(C_VALUE));
        return result;
    }

    public String get(int id, String name) {
        return C_VALUE.value(idNameMatches(id, name));
    }

    private Clause idNameMatches(int id, String name) {
        return C_JOB_ID.is(id).and(C_NAME.is(name));
    }

    public boolean has(int id, String name) {
        return rowExists(idNameMatches(id, name));
    }

    public void delete(Integer jobId) {
        deleteAll(C_JOB_ID.is(jobId));
    }
}
