package schedule;

import static schedule.JobTable.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.dependency.*;

public class TestSymbolDependency extends AbstractJobTest {

    public void testSymbolDependency() throws Exception {
        Date asOfTime = date("2008/04/18");
        Job job = JOBS.insert("test job", new DoNothing(), FOUR_PM);
        AllSymbolsReady.create(list("TY.1C", "RE.TEST.TY.1C"), job);
        assertTrue(job.needsRun(asOfTime));
        asOfTime = date("2008/04/21");
        assertFalse(job.needsRun(asOfTime));
        
	}

}
