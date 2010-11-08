package org.ratel.schedule;

import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.RunCalendar.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.schedule.StatusHistoryTable.*;
import static org.ratel.schedule.JobStatus.*;

public class TestStatus extends AbstractJobTest {
    public void testStatus() throws Exception {
        RunMe runMe = new RunMe();
        Job item = JOBS.insert("no holidays", runMe, THREE_PM, WEEKDAYS);
        freezeNow(date("2008/04/18 15:00:00"));
        item = JOBS.forName("no holidays");
        assertStatus(NOT_STARTED, item);
        assertTrue(item.needsRun(now()));
        item.setStatusAndCommit(now(), IN_PROGRESS);
        assertFalse(item.needsRun(now()));
        item.setStatusAndCommit(now(), SUCCESS);
        assertFalse(item.needsRun(now()));
        item.setStatusAndCommit(now(), FAILED);
        assertFalse(item.needsRun(now()));
        List<StatusEntry> statuses =  item.statusHistory();
        assertSize(3, statuses);
        assertEquals(IN_PROGRESS, first(statuses).status());
        assertEquals(SUCCESS, second(statuses).status());
        assertEquals(FAILED, third(statuses).status());
        
    }

    private void assertStatus(JobStatus status, Job item) {
        assertEquals(status, item.status(now()));
    }

}
