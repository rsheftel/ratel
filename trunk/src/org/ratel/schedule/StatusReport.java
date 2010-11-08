package org.ratel.schedule;

import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Sequence.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;
import org.ratel.file.*;

public class StatusReport {

    private QDirectory dir;

    public StatusReport(String directory) {
        dir = new QDirectory(directory);
    }
    
    public void create(List<Job> jobs, int daysBack) {
        for (int i : sequence(daysBack, 0))
            new StatusReportFile(dir, jobs, daysAgo(i, now())).create();
    }
    
    @SuppressWarnings("deprecation") 
    public static void main(String[] args) {
        Arguments arguments = Arguments.arguments(args, list("dir", "daysBack"));
        debugSql(false);
        int daysBack = arguments.get("daysBack", 6);
        StatusReport report = new StatusReport(arguments.get("dir"));
        List<Job> reportJobs = empty();
        for (Job job : JOBS.jobs()) {
            if(!job.action().getClass().isAssignableFrom(DoNothing.class))
                reportJobs.add(job);
        }
        report.create(reportJobs, daysBack);
        System.exit(0);
    }

}
