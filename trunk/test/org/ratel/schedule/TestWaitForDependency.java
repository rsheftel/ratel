package org.ratel.schedule;

import static org.ratel.schedule.JobStatus.*;
import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.schedule.dependency.*;

public class TestWaitForDependency extends AbstractJobTest {

    public void testWaitForDependency() throws Exception {
        Date asOfTime = date("2008/03/06 15:00:00");
        Job job = JOBS.insert("test job", new DoNothing(), FOUR_PM);
        Dependency time = WaitForTime.create(job, THREE_PM);
        Dependency dependency = WaitForDependency.create(job, time);
        Dependency anotherDependency = WaitForDependency.create(job, time, 600);
        freezeNow(daysAhead(1, asOfTime));
        assertTrue(dependency.isIncomplete(asOfTime));
        assertTrue(anotherDependency.isIncomplete(asOfTime));
        assertMatches("not marked as SUCCESS", anotherDependency.explain(asOfTime));
        time.setStatusAndCommit(SUCCESS, asOfTime);
        assertFalse(dependency.isIncomplete(asOfTime));
        assertTrue(anotherDependency.isIncomplete(asOfTime));
        assertMatches("finished less than 600", anotherDependency.explain(asOfTime));
        freezeNow(minutesAhead(10, now()));
        assertFalse(dependency.isIncomplete(asOfTime));
        assertFalse(anotherDependency.isIncomplete(asOfTime));
    }

}
