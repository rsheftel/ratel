package schedule;

import static schedule.JobStatus.*;
import static schedule.JobTable.*;
import static util.Dates.*;
import static util.RunCalendar.*;
import static util.Times.*;

import java.util.*;

import schedule.JobTable.*;
import db.*;

public class TestMultiThreading extends AbstractJobTest {
	
	@Override public void setUp() throws Exception {
		super.setUp();
		deleteJob();
	}

	private void deleteJob() {
		JOBS.deleteAll(JOBS.C_NAME.is("sleeper"));
	}
	
	@Override public void tearDown() throws Exception {
		deleteJob();
		Db.commit();
		Db.beInNoCommitTestMode();
		super.tearDown();
	}
	
	static class SleepUntilReleased implements Schedulable {

		static boolean released = false;
		
		@Override public JobStatus run(Date asOf, Job item) {
			while(!released) sleep(10);
			return SUCCESS;
		}
	}
	
	public void functestMultiThreading() throws Exception {
	    // switch to DEV database before running this test
		Db.getOutOfNoCommitTestMode();
		Job job = JOBS.insert("sleeper", new SleepUntilReleased(), THREE_PM, WEEKDAYS);
		Db.commit();
		freezeNow("2008/02/18 14:59:00");
		new Scheduler().setMultiThreaded().start();
		sleepAndRefresh();
		assertEquals(IN_PROGRESS, job.status(now()));
		freezeNow("2008/02/18 15:00:01");
		emailer.allowMessages();
		new Scheduler().setMultiThreaded().start();
		sleepAndRefresh();
		assertEquals(IN_PROGRESS_LATE, job.status(now()));
		SleepUntilReleased.released = true;
		sleepAndRefresh();
		assertEquals(SUCCESS, job.status(now()));
	}
	
	public void functestLate() throws Exception {
	    // switch to DEV database before running this test
	    Db.getOutOfNoCommitTestMode();
	    Job job = JOBS.insert("sleeper", new SleepUntilReleased(), THREE_PM, WEEKDAYS);
	    job.setStatusAndCommit(now(), LATE);
	    freezeNow("2008/02/18 15:00:01");
	    new Scheduler().setMultiThreaded().start();
	    sleepAndRefresh();
	    assertEquals(IN_PROGRESS_LATE, job.status(now()));
	    SleepUntilReleased.released = true;
	    sleepAndRefresh();
	    assertEquals(SUCCESS, job.status(now()));
	}
	
	public void testNothing() throws Exception {
	
	}

	private void sleepAndRefresh() {
		sleep(4000);
	}
}
