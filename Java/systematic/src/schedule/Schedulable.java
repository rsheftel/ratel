package schedule;

import java.util.*;

import schedule.JobTable.*;

public interface Schedulable {
	public static final Schedulable EMAIL = new SendEmail();
	public static final Schedulable DO_NOTHING = new DoNothing();
	public static final Schedulable RUN_COMMAND = new RunCommand();
	JobStatus run(Date asOf, Job job);
	
	
}