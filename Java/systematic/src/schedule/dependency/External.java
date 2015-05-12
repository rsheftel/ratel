package schedule.dependency;

import static schedule.dependency.DependencyParameterTable.*;
import static schedule.dependency.DependencyTable.*;
import static util.Arguments.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;
import util.*;
import db.clause.*;

public class External extends Dependency {

    private String name;

    public External(Integer id, Map<String, String> parameters) {
        super(id);
        name = parameters.get("name");
    }
    
    @Override public String explain(Date asOf) {
        return "external dependency " + name + " has not been cleared.";
    }

    @Override public boolean isIncomplete(Date asOf) {
        return true;
    }

    public static void main(String[] args) {
        Arguments arguments = arguments(args, list("name", "status", "date"));
        String name = arguments.get("name");
        String statusString = arguments.get("status", "SUCCESS");
        Date asOf = arguments.date("date");
        JobStatus status = bombNull(JobStatus.valueOf(statusString), "invalid status " + statusString);
        Clause classMatches = DEPENDENCIES.C_DEPENDENCY.is(External.class.getCanonicalName());
        Clause nameMatches = DEPENDENCY_PARAMS.C_VALUE.is(name);
        Clause join = DEPENDENCIES.C_ID.is(DEPENDENCY_PARAMS.C_DEPENDENCY_ID);
        Clause matches = nameMatches.and(classMatches).and(join);
        int id = DEPENDENCY_PARAMS.C_DEPENDENCY_ID.value(matches);
        DEPENDENCIES.forId(id).setStatusAndCommit(status, asOf);
    }

    public static Dependency create(Job anItem, String name) {
        return anItem.insertDependency(External.class, map("name", name));
    }

}
