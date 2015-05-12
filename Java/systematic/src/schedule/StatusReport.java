package schedule;

import static schedule.JobTable.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Sequence.*;

import java.util.*;

import schedule.JobTable.*;
import util.*;
import file.*;

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
