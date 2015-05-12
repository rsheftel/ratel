package schedule;

import static schedule.JobTable.*;
import static util.Dates.*;
import static util.RunCalendar.*;

import java.util.*;

public class TestRunDateStuff extends AbstractJobTest {
	public void testHolidays() throws Exception {
	    JOBS.setAllSuccess("2008/02/18");
	    JOBS.setAllSuccess("2008/03/24");
	    JOBS.setAllSuccess("2008/03/20");
	    JOBS.setAllSuccess("2008/03/06");
	    
		// Note:  RunMe also tests that IN_PROGRESS status is set properly
		RunMe runMe = new RunMe();
		JOBS.insert("ny holidays", runMe, FOUR_PM, NYB);
		JOBS.insert("london holidays", runMe, FOUR_PM, LNB);
		JOBS.insert("no holidays", runMe, FOUR_PM, WEEKDAYS);
		freezeNow("2008/02/18 15:00:00"); // ny holiday
        populateRun();
		assertEquals(date("2008/02/18"), run.get("london holidays"));
		assertEquals(date("2008/02/18"), run.get("no holidays"));
		assertEquals(date("2008/02/15"), run.get("ny holidays")); // runs because prior day status is clear
		run.clear();
		freezeNow("2008/03/24 15:00:00"); // london holiday
		populateRun();
		assertEquals(date("2008/03/20"), run.get("london holidays")); // runs because prior day status is clear
		assertEquals(date("2008/03/24"), run.get("no holidays"));
		assertEquals(date("2008/03/24"), run.get("ny holidays"));
		run.clear();
		freezeNow("2008/03/06 15:00:00"); // no holiday
		populateRun();
	    assertEquals(date("2008/03/06"), run.get("london holidays")); 
	    assertEquals(date("2008/03/06"), run.get("no holidays"));
	    assertEquals(date("2008/03/06"), run.get("ny holidays"));
		run.clear();
	}

    private void populateRun() {
        List<JobDate> jobs = JOBS.jobsToRun(now());
        for (JobDate jobDate : jobs) 
            jobDate.addToMap(run);
    }
}
