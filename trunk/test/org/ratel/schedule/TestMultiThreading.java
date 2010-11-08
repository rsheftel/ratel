package org.ratel.schedule;

import static org.ratel.schedule.JobStatus.*;
import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.RunCalendar.*;
import static org.ratel.util.Times.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.db.*;

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
