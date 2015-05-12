package schedule;

import static schedule.JobStatus.*;
import static schedule.JobTable.*;
import static schedule.Schedulable.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.RunCalendar.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.dependency.*;
import tsdb.*;
import amazon.monitor.*;


public class TestScheduler extends AbstractJobTest {

	@Override public void setUp() throws Exception {
		super.setUp();
		freezeNow("2008/02/15");
		JOBS.setAllSuccess("2008/02/15");
	}
	
	public void testTimeDependency() throws Exception {
		RunMe runMe = new RunMe();
		Job anItem = JOBS.insert("some item", runMe, FOUR_PM);
		anItem.runAfter(THREE_PM);
		RecentFieldsKeeper fakeRecon = new RecentFieldsKeeper();
        Scheduler.onStatusUpdated(fakeRecon);
		anItem.setStatusAndCommit(anItem.prior(now()), SUCCESS);
		fakeRecon.waitForMessage(1000);
		assertEquals("SUCCESS", fakeRecon.string("status"));
		Job anotherItem = JOBS.insert("some other item", runMe, FOUR_PM);
		anotherItem.runAfter(THREE_PM);
		anotherItem.setStatusAndCommit(anotherItem.prior(now()), SUCCESS);
		

		freezeNow(todayAt("14:59:59"));
		assertEmpty(JOBS.jobsToRun(now()));
		assertEmpty(JOBS.jobsToRun(midnight(anItem.prior(now()))));
		freezeNow(todayAt("15:00:00"));
		List<JobDate> dls = assertSize(2, JOBS.jobsToRun(now()));
		assertContains(new JobDate(anItem, midnight()), dls);
		assertContains(new JobDate(anotherItem, midnight()), dls);
	}
	

	
	public void testSendsEmailWhenDataReadyAndThenDoesNotNeedChecking() throws Exception {
		Job ready = JOBS.insert("all ready", EMAIL, FOUR_PM);
		ready.setParameters(map("subject", "data is ready"));
		AllDataReady.create(ready, TEST_SOURCE, AAPL, AAPL_HIGH);
		assertAllDataTriggersReady();
	}
	
	public void testSendsEmailWhenDataReadyAndThenDoesNotNeedCheckingAttributeValues() throws Exception {
		Job ready = JOBS.insert("all ready", EMAIL, FOUR_PM);
		ready.setParameters(map("subject", "data is ready"));
		AllDataReady.create(ready, TEST_SOURCE, values(
			TICKER.value("aapl"),
			QUOTE_TYPE.value("close", "high")
		));
		assertAllDataTriggersReady();
	}

	private void assertAllDataTriggersReady() {
		new Scheduler().run();
		observeOne(AAPL);
		new Scheduler().run();
		emailer.allowMessages();
		observeOne(AAPL_HIGH);
		new Scheduler().run();
		emailer.sent().hasSubject("data is ready.*all ready");
		assertCommitted();
		emailer.disallowMessages();
		// runChecks resets the status, which invalidates this test
		new Scheduler().run();
	}
	
	public void testInProgress() throws Exception {
		RunMe runMe = new RunMe();
		Job job = JOBS.insert("no holidays", runMe, THREE_PM, WEEKDAYS);
		freezeNow("2008/02/18 15:01:00");
		emailer.allowMessages();
		Date now = midnight(now());
		job.checkDeadline(now);
		emailer.requireSent(1);
		assertEquals(LATE, job.status(now()));
		job.setStatusAndCommit(now(), IN_PROGRESS);
		job.checkDeadline(now);
		assertEquals(IN_PROGRESS_LATE, job.status(now()));
		emailer.disallowMessages();
		job.checkDeadline(now);
	}
	
	public void testDoNothingDoesNotSendEmail() throws Exception {
	    DoNothing nothing = new DoNothing();
	    Job job = JOBS.insert("do nothing", nothing, THREE_PM, WEEKDAYS);
        freezeNow("2008/02/18 15:01:00");
        Date now = midnight(now());
        job.checkDeadline(now);
        assertEquals(LATE, job.status(now()));
    }
	
	public void testFailedCausesDeadlineCheckToSkip() throws Exception {
	    RunMe runMe = new RunMe();
	    Job job = JOBS.insert("no holidays", runMe, THREE_PM, WEEKDAYS);
	    freezeNow("2008/02/18 15:01:00");
	    job.setStatusAndCommit(now(), FAILED);
        job.checkDeadline(now());
        assertTrue(job.status(now()).isFailed());
    }

	public void testDataReadyByAttributeValues() throws Exception {
		Job ready = JOBS.insert("some ready", EMAIL, FOUR_PM);
		ready.setParameters(map("subject", "data is ready"));
		AnyDataReady.create(ready, TEST_SOURCE, values(
			TICKER.value("aapl"),
			QUOTE_TYPE.value("close", "high")
		));
		new Scheduler().run();
		observeOne(AAPL);
		emailer.allowMessages();
		new Scheduler().run();
		emailer.sent().hasSubject("data is ready.*some ready");
		assertCommitted();
		observeOne(AAPL_HIGH);
		emailer.disallowMessages();
		// runChecks resets the status, which invalidates this test
		new Scheduler().run();
	}

	private void observeOne(TimeSeries series) {
		TEST_SOURCE.with(series).write("2008/02/15", 19.0);
	}
	
	
	class NoConstructor implements Schedulable {
		NoConstructor(int i) { i++; }
		@Override public JobStatus run(Date asOf, Job deadLine) { return SUCCESS; }
	}
	public void testSendsEmailOnBadClass() throws Exception {
		JOBS.insert("bad class name", new NoConstructor(1), FOUR_PM);
		emailer.allowMessages();
		new Scheduler().run();
		emailer.requireSent(1);
	}
	class Fails implements Schedulable {
		@Override public JobStatus run(Date asOf, Job deadLine) {throw bomb("this is a bomb");}
	}
	public void testSendsEmailOnFailedRun() throws Exception {
		JOBS.insert("bad class name", new Fails(), FOUR_PM);
		emailer.allowMessages();
		new Scheduler().run();
		emailer.requireSent(1);
	}
}
