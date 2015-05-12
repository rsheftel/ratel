package futures;

import static futures.BloombergJobTable.*;
import static tsdb.DataSource.*;

import java.util.*;

import db.*;

public class TestBloombergFeedDBPopulator extends DbTestCase {
	
	private static final String TEST_JOB = "bloomberg_test_autogen_foo";

	public void testOldRecordsGetDeleted() throws Exception {
		BloombergJob testJob = BLOOMBERG_JOBS.insert(TEST_JOB, "14:00:00", true, "0800");
		testJob.add(new BloombergJobEntry("testTicker", "testField", BLOOMBERG.with("testTimeSeries")));
		testJob.add(new BloombergJobEntry("testTicker2", "testField2", BLOOMBERG.with("testTimeSeries2")));
		List<BloombergJobEntry> data = testJob.entries();
		assertSize(2, data);
		BLOOMBERG_JOBS.deleteAllEntries("%autogen%");
		assertEmpty(testJob.entries());
		
	}

}
