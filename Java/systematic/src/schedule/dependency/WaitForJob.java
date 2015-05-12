package schedule.dependency;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;

import static schedule.JobTable.*;
import static util.Errors.*;
import static util.Objects.*;

public class WaitForJob extends Dependency {

	private static final String PARAM = "parent_job_name";
	private String jobName;

	public WaitForJob(Integer id, Map<String, String> parameters) {
		super(id);
		jobName = parameters.get(PARAM);
	}
	
	@Override public String explain(Date asOf) {
		return "JobDependency: " + jobName + " is not marked as SUCCESS.  Status is: " + parentStatus(asOf) + "\n";
	}

	@Override public boolean isIncomplete(Date asOf) {
		return !parentStatus(asOf).isSuccess();
	}

	private JobStatus parentStatus(Date asOf) {
		try {
			return JOBS.forName(jobName).status(asOf);
		} catch (RuntimeException e) {
			throw bomb("parent job " + jobName + " could not be looked up", e);
		}
	}

	public static Dependency create(Job child, Job parent) {
		return child.insertDependency(WaitForJob.class, map(PARAM, parent.name()));
	}

}
