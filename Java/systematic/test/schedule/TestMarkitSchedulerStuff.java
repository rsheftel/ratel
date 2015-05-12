package schedule;

import static com.malbec.tsdb.markit.IndexTable.*;
import static com.malbec.tsdb.markit.MarkitTable.*;
import static schedule.JobTable.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.dependency.*;
import tsdb.*;

import com.malbec.tsdb.markit.*;

public class TestMarkitSchedulerStuff extends AbstractJobTest {
	private static final Date TEST_DATE = date("2088/03/05");
    private Schedulable action;
    private Job job;
	
	@Override public void setUp() throws Exception {
	    super.setUp();
	    action = new MarkitLoaderAction();
	    job = JOBS.insert("test job", action, THREE_PM);
	}

    public void testMarkitDataReady() throws Exception {
        Date asOfTime = date("2088/03/05 02:01:01");
		Map<String, String> empty = emptyMap();
		Dependency dependency = job.insertDependency(MarkitDataReady.class, empty);
		assertTrue(dependency.isIncomplete(asOfTime));
		MARKIT_CDS.insertTestData(TEST_DATE);
		assertFalse(dependency.isIncomplete(asOfTime));
	}

	
	public void testMarkitIndexLoaderAction() throws Exception {
	    MARKIT_INDEX.insertTestData(TEST_DATE);
	    Schedulable indexLoad = new MarkitIndexLoaderAction();
	    Job loadJob = JOBS.insert("testmarkitindex", indexLoad, THREE_PM);
	    assertTrue(indexLoad.run(TEST_DATE, loadJob).isSuccess());
	    SeriesSource ss = series("cdx-na-ig-hvol_market_spread_5y_50_49").with(MARKIT);
	    assertEquals(97.0, ss.observationValue(TEST_DATE));
	    ss = series("cdx-na-ig-hvol_market_spread_5y_otr").with(MARKIT);
	    assertEquals(97.0, ss.observationValue(TEST_DATE));
	    
	    
    }
}
