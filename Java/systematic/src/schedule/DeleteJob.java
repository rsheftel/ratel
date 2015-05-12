package schedule;

import static util.Arguments.*;
import static util.Errors.*;
import static util.Objects.*;
import static schedule.dependency.DependencyParameterTable.*;
import schedule.JobTable.*;
import util.*;
import db.*;

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
