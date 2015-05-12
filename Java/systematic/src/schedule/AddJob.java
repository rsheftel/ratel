package schedule;

import static mail.EmailAddress.*;
import static schedule.JobTable.*;
import static util.Objects.*;
import static util.RunCalendar.*;
import schedule.JobTable.*;
import util.*;
import db.*;


public class AddJob {

    public static void main(String[] args) {
        Job esty = JOBS.forName("runSystemPairsTradingESTY");
        esty.deleteDependencies();
        esty.runAfter("19:00:00");
        Job dave = addRScript("update bloomberg liabilities", "updateBloombergLiabilities.r [[DATE]]", "20:00:00", NYB);
        dave.runAfter("19:00:00");
        Job dtd = JOBS.forName("run system DTD 2009");
        dtd.runAfter(dave);
        Db.commit();
        
    }
    
    @SuppressWarnings("unused")
    private static Job addJava(String jobName, String commandExtras, String late, RunCalendar calendar) {
        Job job = JOBS.insert(jobName, new RunCommand(), late, TEAM, calendar);
        job.setParameters(map("command", "cd /home/simdata/svn/systematic/Java/systematic/lib; java -classpath ./\\* " + commandExtras));
        return job;
    }

    @SuppressWarnings("unused")
	private static Job addRScript(String jobName, String commandExtras, String late, RunCalendar calendar) {
        Job wfoRun = JOBS.insert(jobName, new RunCommand(), late, TEAM, calendar);
        wfoRun.setParameters(map("command", "Rscript /home/simdata/svn/systematic/R/scripts/Jobs/" + commandExtras));
        return wfoRun;
    }

}
