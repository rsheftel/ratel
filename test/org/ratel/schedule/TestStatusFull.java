package org.ratel.schedule;

import static java.util.Calendar.*;
import static org.ratel.schedule.JobStatus.*;
import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.RunCalendar.*;
import static org.ratel.util.Times.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;
import org.ratel.db.*;

public class TestStatusFull extends DbTestCase {
    
    private Job runsLong;
    private Job afterMidnight;
    private Job neverRuns;
    private Job failsOnThursdays;
    private Job runsTPlusOne;
    private Job runsFriday;
    private String tuesday = "2008/07/01";
    private String wednesday = "2008/07/02";
    private String thursday = "2008/07/03";
    private String friday = "2008/07/04";
    private String saturday = "2008/07/05";
    private String sunday = "2008/07/06";
    private String monday = "2008/07/07";
    private Job neverStops;
    private Map<String, Map<Job, JobStatus>> expected;

    @Override protected void tearDown() throws Exception {
        SleepUntilReleased.releaseAll();
        super.tearDown();
    }

    static class FailsOnThursday implements Schedulable {
        @Override public JobStatus run(Date asOf, Job job) {
            bombIf(calendar(asOf).get(DAY_OF_WEEK) == THURSDAY, "it's Thursday!"); 
            return SUCCESS;
        }
    }

    static class SleepUntilReleased implements Schedulable {

        static Map<String, Map<Date, Boolean>> released = emptyMap();
        static boolean releaseAll = false;
        
        @Override public JobStatus run(Date asOf, Job item) {
            String name = item.parameter("name");
            info("started " + ymdHuman(asOf) + " " + name);
            while(!released(name, asOf)) sleep(10);
            info("released " + ymdHuman(asOf) + " " + name);
            return SUCCESS;
        }

        private boolean released(String name, Date asOf) {
            if(releaseAll) return true;
            if(!released.containsKey(name)) return false;
            return released.get(name).containsKey(midnight(asOf));
        }
        
        public static void release(String name, Date asOf) {
            Map<Date, Boolean> empty = emptyMap();
            if(!released.containsKey(name)) released.put(name, empty);
            released.get(name).put(midnight(asOf), true);
        }
        
        public static void releaseAll() { 
            releaseAll = true;
        }

        public static void release(Job job, String asOf) {
            release(job.name(), date(asOf));
        }
    }
    
    private void assertStatus(Job job, String asOf, JobStatus status) {
        assertEquals("job " + job.name() + " had incorrect status for " + asOf, status, job.status(date(asOf)));
    }

    public void functestRealWorldTestCase() throws Exception {
        Db.getOutOfNoCommitTestMode();
        turnOffSqlLoggingForever();
        JOBS.deleteAll();
        Db.commit();
        // friday is a US holiday - 7/4/2008
        setupJobs();
        setupExpected();
        wednesday();
        thursday();
        friday();
        saturday();
        sunday();
        monday();
        tuesday();
    }

    @SuppressWarnings("deprecation") private void turnOffSqlLoggingForever() {
        Log.debugSql(false);
    }

    



    private void wednesday() {
        runSleepAndRefresh("2008/07/02 00:00:00");
        changed(wednesday, runsLong, IN_PROGRESS);
        changed(wednesday, neverStops, IN_PROGRESS);
        changed(tuesday, runsFriday, IN_PROGRESS);
        changed(tuesday, runsLong, CANCELLED);
        changed(tuesday, afterMidnight, IN_PROGRESS);
        changed(tuesday, neverStops, CANCELLED);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/02 04:00:00");
        emailer.disallowMessages();
        changed(tuesday, runsTPlusOne, LATE);
        assertStatus();

        SleepUntilReleased.release(runsFriday, "2008/07/01");
        SleepUntilReleased.release(afterMidnight, "2008/07/01");
        sleep(4000);
        changed(tuesday, runsFriday, SUCCESS);
        changed(tuesday, afterMidnight, SUCCESS);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/02 13:30:00");
        emailer.disallowMessages();
        changed(wednesday, runsFriday, IN_PROGRESS);
        changed(wednesday, failsOnThursdays, LATE);
        assertStatus();

        SleepUntilReleased.release(runsFriday, wednesday);
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/02 23:45:00");
        emailer.disallowMessages();
        changed(wednesday, runsFriday, SUCCESS);
        changed(wednesday, runsLong, IN_PROGRESS_LATE);
        changed(wednesday, afterMidnight, IN_PROGRESS);
        changed(wednesday, neverStops, IN_PROGRESS_LATE);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/02 23:50:00");
        emailer.disallowMessages();
        changed(wednesday, afterMidnight, IN_PROGRESS_LATE);
        changed(wednesday, neverRuns, LATE);
        assertStatus();
        
        runSleepAndRefresh("2008/07/02 23:59:59");
        assertStatus();
    }
    
    private void thursday() {
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/03 00:00:00");
        emailer.disallowMessages();
        changed(thursday, runsLong, BLOCKED);
        changed(thursday, neverStops, BLOCKED);
        assertStatus();
        
        runSleepAndRefresh("2008/07/03 00:05:00");
        assertStatus();
        
        SleepUntilReleased.release(runsLong, wednesday);
        SleepUntilReleased.release(afterMidnight, wednesday);
        sleep(1000);
        runSleepAndRefresh("2008/07/03 00:10:00");
        changed(wednesday, runsLong, SUCCESS);
        changed(wednesday, afterMidnight, SUCCESS);
        changed(wednesday, failsOnThursdays, SUCCESS);
        changed(thursday, runsLong, IN_PROGRESS);
        assertStatus();
        
        runSleepAndRefresh("2008/07/03 02:00:00");
        changed(tuesday, runsTPlusOne, CANCELLED);
        changed(wednesday, runsTPlusOne, IN_PROGRESS);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/03 04:00:00");
        emailer.disallowMessages();
        changed(wednesday, runsTPlusOne, IN_PROGRESS_LATE);
        assertStatus();
        
        SleepUntilReleased.release(runsTPlusOne, wednesday);
        runSleepAndRefresh("2008/07/03 04:05:00");
        changed(wednesday, runsTPlusOne, SUCCESS);
        assertStatus();
        
        SleepUntilReleased.release(runsFriday, thursday);
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/03 13:30:00");
        emailer.disallowMessages();
        changed(thursday, runsFriday, SUCCESS);
        changed(thursday, failsOnThursdays, LATE);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/03 23:45:00");
        emailer.disallowMessages();
        changed(thursday, neverStops, BLOCKED_LATE); 
        changed(thursday, afterMidnight, IN_PROGRESS);
        changed(thursday, runsLong, IN_PROGRESS_LATE);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/03 23:50:00");
        emailer.disallowMessages();
        changed(thursday, afterMidnight, IN_PROGRESS_LATE);
        changed(thursday, neverRuns, LATE);
        assertStatus();
        
        runSleepAndRefresh("2008/07/03 23:59:59");
        assertStatus();
    }

    private void friday() {
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/04 00:00:00");
        emailer.disallowMessages();
        changed(thursday, neverStops, BLOCKED_LATE);
        changed(friday, neverStops, BLOCKED);
        changed(friday, runsLong, NOT_BUSINESS_DAY);
        changed(friday, afterMidnight, NOT_BUSINESS_DAY);
        changed(friday, neverRuns, NOT_BUSINESS_DAY);
        changed(friday, failsOnThursdays, NOT_BUSINESS_DAY);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/04 04:00:00");
        emailer.disallowMessages();
        changed(thursday, runsTPlusOne, LATE);
        assertStatus();
        
        SleepUntilReleased.release(runsLong, thursday);
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/04 09:00:00");
        emailer.disallowMessages();
        changed(thursday, runsLong, SUCCESS);
        changed(thursday, failsOnThursdays, FAILED);
        assertStatus();
        
        
        SleepUntilReleased.release(afterMidnight, thursday);
        SleepUntilReleased.release(runsFriday, friday);
        runSleepAndRefresh("2008/07/04 13:30:00");
        changed(friday, runsFriday, SUCCESS);
        changed(thursday, afterMidnight, SUCCESS);
        assertStatus();
        
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/04 23:59:59");
        emailer.disallowMessages();
        changed(friday, neverStops, BLOCKED_LATE);
        assertStatus();
    }
    
    private void saturday() {
        runSleepAndRefresh("2008/07/05 00:00:00");
        changed(friday, runsTPlusOne, NOT_BUSINESS_DAY);
        changed(saturday, neverStops, NOT_BUSINESS_DAY);
        changed(saturday, runsLong, NOT_BUSINESS_DAY);
        changed(saturday, afterMidnight, NOT_BUSINESS_DAY);
        changed(saturday, neverRuns, NOT_BUSINESS_DAY);
        changed(saturday, failsOnThursdays, NOT_BUSINESS_DAY);
        changed(saturday, runsFriday, NOT_BUSINESS_DAY);
        assertStatus();
        
        runSleepAndRefresh("2008/07/05 23:59:59");
        assertStatus();
    }

    private void sunday() {
        runSleepAndRefresh("2008/07/06 00:00:00");
        changed(saturday, runsTPlusOne, NOT_BUSINESS_DAY);
        changed(sunday, neverStops, NOT_BUSINESS_DAY);
        changed(sunday, runsLong, NOT_BUSINESS_DAY);
        changed(sunday, afterMidnight, NOT_BUSINESS_DAY);
        changed(sunday, neverRuns, NOT_BUSINESS_DAY);
        changed(sunday, failsOnThursdays, NOT_BUSINESS_DAY);
        changed(sunday, runsFriday, NOT_BUSINESS_DAY);
        assertStatus();
        
        runSleepAndRefresh("2008/07/06 23:59:59");
        assertStatus();
    }
    
    private void monday() {
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/07 00:00:00");
        emailer.disallowMessages();
        changed(sunday, runsTPlusOne, NOT_BUSINESS_DAY);
        changed(monday, neverStops, BLOCKED);
        changed(monday, runsLong, IN_PROGRESS);
        assertStatus();
        
        SleepUntilReleased.release(runsLong, monday);
        SleepUntilReleased.release(afterMidnight, monday);
        SleepUntilReleased.release(failsOnThursdays, monday);
        SleepUntilReleased.release(runsFriday, monday);
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/07 23:59:59");
        emailer.disallowMessages();
        changed(monday, runsLong, SUCCESS);
        changed(monday, neverStops, BLOCKED_LATE);
        changed(monday, afterMidnight, SUCCESS);
        changed(monday, neverRuns, LATE);
        changed(monday, failsOnThursdays, SUCCESS);
        changed(monday, runsFriday, SUCCESS);
        assertStatus();
    }
    
    private void tuesday() {
        emailer.allowMessages();
        runSleepAndRefresh("2008/07/08 00:00:00");
        emailer.disallowMessages();
        assertStatus();
        
        SleepUntilReleased.release(runsTPlusOne, monday);
        runSleepAndRefresh("2008/07/08 02:00:00");
        changed(thursday, runsTPlusOne, CANCELLED);
        changed(monday, runsTPlusOne, SUCCESS);
        assertStatus();
    }

    
    
    private void setupJobs() {
        runsLong = insertJob("long running job", "23:00:00", NYB);
        neverStops = insertJob("never stops", "23:00:00", WEEKDAYS);
        afterMidnight = insertJob("runs past midnight", "23:50:00", NYB);
        afterMidnight.runAfter("23:45:00");
        neverRuns = insertJob("never runs", "23:50:00", NYB);
        neverRuns.runAfter(neverRuns);
        failsOnThursdays = JOBS.insert("fails on Thursday", new FailsOnThursday(), "12:00:00", NYB);
        failsOnThursdays.runAfter(runsLong);
        runsTPlusOne = insertJob("runsTPlusOne", "04:00:00", RunCalendar.from("nyb+1"));
        runsTPlusOne.runAfter(failsOnThursdays);
        runsTPlusOne.runAfter("02:00:00");
        runsFriday = insertJob("runs on friday", "14:00:00", LNB);
        runsFriday.runAfter("13:30:00");
        Db.commit();
    }

    private void setupExpected() {
        List<String> days = list(tuesday, wednesday, thursday, friday, saturday, sunday, monday);
        List<Job> jobs = list(runsLong, neverStops, afterMidnight, neverRuns, failsOnThursdays, runsTPlusOne, runsFriday);
        expected = emptyMap();
        for (String day : days) {
            Map<Job, JobStatus> empty = emptyMap();
            expected.put(day, empty);
            for (Job job : jobs)
                empty.put(job, NOT_STARTED);
        }
    }

    private void changed(String day, Job job, JobStatus status) {
        expected.get(day).put(job, status);
    }
    
    private void assertStatus() {
        for (String day : expected.keySet())
            for (Job job : expected.get(day).keySet())
                assertStatus(job, day, expected.get(day).get(job));
    }

    private Job insertJob(String name, String deadline, RunCalendar center) {
        Job job = JOBS.insert(name, new SleepUntilReleased(), deadline, center);
        job.setParameters(map("name", name));
        return job;
    }
    

    private void runSleepAndRefresh(String now) {
        freezeNow(date(now));
        new Scheduler().setMultiThreaded().start();
        sleep(6000);
        new StatusReport("C:/").create(JOBS.jobs(), 6);
        
    }
    
    public void testNothing() throws Exception {
    
    }
}
