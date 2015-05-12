package schedule;

import static schedule.JobTable.*;
import static schedule.JobStatus.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.StatusHistoryTable.*;
import schedule.dependency.*;

public class TestJobDependency extends AbstractJobTest {
	@Override public void setUp() throws Exception {
		super.setUp();
		JOBS.setAllSuccess("2008/02/15");
	}
	
	public void testJobDependency() throws Exception {
		run.clear();
		Job first = JOBS.insert("first job", new RunMe(), FOUR_PM);
		Job second = JOBS.insert("second job", new RunMe(), FOUR_PM);
		Dependency dependencyOnFirst = WaitForJob.create(second, first);
		WaitForTime.create(first, THREE_PM);
		freezeNow("2008/02/15 14:59:59");
		first.setStatusAndCommit(date("2008/02/14"), SUCCESS);
		second.setStatusAndCommit(date("2008/02/14"), SUCCESS);
		new Scheduler().run();
		assertTrue(run.isEmpty());
		first.setStatusAndCommit(now(), SUCCESS);
		new Scheduler().run();
		assertEquals(date("2008/02/15"), run.get("second job"));
		List<StatusEntry> status = dependencyOnFirst.statusHistory(date("2008/02/15"));
		assertSize(2, status);
	}
	
	public void testDependencyIsSkippedAfterSuccess() throws Exception {
		Job first = JOBS.insert("first job", new RunMe(), FOUR_PM);
		Job second = JOBS.insert("second job", new RunMe(), FOUR_PM);
		Dependency dependency = WaitForJob.create(second, first);
		WaitForTime.create(second, THREE_PM);
		freezeNow("2008/02/15 14:59:59");
		first.setStatusAndCommit(now(), SUCCESS);
		new Scheduler().run();
		assertTrue(dependency.status(now()).isSuccess());
		Date time = dependency.statusTime(now());
		runAt("2008/02/15 14:59:59");
		assertTrue(dependency.status(now()).isSuccess());
		assertEquals(time, dependency.statusTime(now()));
	}
	
	public void testJobDependencyMissingJob() throws Exception {
		Job first = JOBS.insert("first job", new RunMe(), FOUR_PM);
		Job second = JOBS.insert("second job", new RunMe(), FOUR_PM);
		first.setParameters(map("foo", "bar"));
		second.runAfter(first);
		first.delete();
		second.setStatusAndCommit(date("2008/02/14"), SUCCESS);
		emailer.allowMessages();
		runAt("2008/02/15 14:59:59");
		emailer.sent().hasContent("parent job first job could not be looked up");
	}
	
	public void testDependencyCanBeLookedUpAndDeleted() throws Exception {
	    Job first = JOBS.insert("first job", new RunMe(), FOUR_PM);
	    Dependency after = first.runAfter("15:00:00");
	    after.delete();
	    runAt("2008/02/15 14:59:59");
        assertTrue(first.status(midnight()).isSuccess());
	    after = first.runAfter("15:00:00");
    }

    private void runAt(String at) {
        freezeNow(at);
        new Scheduler().run();
    }
	
	public void testDeleteJob() throws Exception {
	    Job first = JOBS.insert("first job", new RunMe(), FOUR_PM);
	    Job second = JOBS.insert("second job", new RunMe(), FOUR_PM);
	    first.setParameters(map("foo", "bar"));
	    second.runAfter(first);
	    first.delete();
	    second.delete();
    }
	
	
}
