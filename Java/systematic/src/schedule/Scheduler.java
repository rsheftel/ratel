package schedule;
import static schedule.JobTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import jms.*;

import schedule.JobTable.*;
import util.*;
import db.*;
import file.*;
public class Scheduler extends Thread {

	private final QDirectory log;
	private boolean singleThread = true;
    private static final QTopic JOB_REPORTS = new QTopic("nightvision.status");

	public Scheduler() {
		log = null;
	}
	
	public Scheduler(String logDir) {
		log = new QDirectory(logDir);
	}
	
	public Scheduler setMultiThreaded() {
		singleThread = false;
		return this;
	}

	@Override public void run() {
	    Log.setContext("scheduler");
		final Date now = now();
		List<JobDate> toRun = JOBS.jobsToRun(now);
		JOBS.checkDeadlines(now);
		List<Thread> threads = empty();
		for (final JobDate jobDate : toRun) {
		    jobDate.markInProgress();
			Thread thread = new Thread() {
				@Override public void run() {
					setLogger(jobDate.job());
					Db.setQueryTimeout(300);
					jobDate.run();
				}
			};
			if(singleThread) thread.run();
			else {
				threads.add(thread);
				thread.start();
			}
		}
		Objects.join(threads);
		resetLogger();
	}


	private void resetLogger() {
		Log.setToSystem();
		Log.setContext("");
	}

	private void setLogger(Job check) {
		if (log == null) return;
		Log.setContext(check.name());
		Log.setFile(check.logFile(log));
	}
	
	

	public static void main(String[] args) {
		if(args.length != 1) usage();
		String logDir = args[0];
		new Scheduler(logDir).setMultiThreaded().run();
		Db.commit();
		System.exit(0);
	}

	private static void usage() {
		bomb("Usage: java schedule.Scheduler <logDir>\n");
	}

    public static void publish(Row data) {
        JOB_REPORTS.send(data);
    }

    public static void onStatusUpdated(MessageReceiver newStatus) {
        JOB_REPORTS.register(newStatus);
    }


}
