package org.ratel.schedule;

import static org.ratel.util.Arguments.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.schedule.dependency.DependencyParameterTable.*;
import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;
import org.ratel.db.*;

public class DeleteJob {

    public static void main(String[] args) {
        Arguments arguments = arguments(args, list("id", "name"));
        Job job = JobTable.job(arguments);
        bombIf(
            DEPENDENCY_PARAMS.exists("parent_job_name", job.name()), 
            "cannot delete job with dependencies - review/remove dependencies first"
        );
        job.delete();
        Db.commit();
    }

}
