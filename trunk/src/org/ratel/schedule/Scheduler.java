package org.ratel.schedule;
import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.jms.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;
import org.ratel.db.*;
import org.ratel.file.*;
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
