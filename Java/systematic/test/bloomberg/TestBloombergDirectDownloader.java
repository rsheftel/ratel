package bloomberg;

import static db.clause.Clause.*;
import static futures.BloombergJobTable.*;
import static util.Dates.*;
import tsdb.*;
import db.*;
import db.tables.BloombergFeedDB.*;
import futures.*;

public class TestBloombergDirectDownloader extends DbTestCase {

    private SeriesSource TEST_SS = new SeriesSource("irs_usd_rate_10y_mid:bloomberg_test");
    private BloombergDirectDownloader downloader = new BloombergDirectDownloader();

    @Override protected void setUp() throws Exception {
        super.setUp();
        JobBloombergDataBase.T_JOBBLOOMBERGDATA.deleteAll(TRUE);
        BLOOMBERG_JOBS.deleteAll(TRUE);
    }
    
    public void testCanDownloadOneItem() throws Exception {
        BloombergJob job = BLOOMBERG_JOBS.insert("test", "12:34:56", true, "14:30");
        job.add(new BloombergJobEntry("USSW10 CMN3 Index", "LAST_PRICE", TEST_SS));
        TEST_SS.purge();
        runAtTime("2009/05/05 00:00:00");
        assertNoObservations(TEST_SS);
        runAtTime("2009/05/05 14:29:59");
        assertNoObservations(TEST_SS);
        runAtTime("2009/05/05 14:30:00");
        assertHasObservation(TEST_SS);
        TEST_SS.purge();
        runAtTime("2009/05/05 15:00:00");
        assertHasObservation(TEST_SS);
    }
    
    public void testRolloverNotAtMidnight() throws Exception {
        BloombergJob job = BLOOMBERG_JOBS.insert("test", "12:34:56", true, "01:30");
        job.add(new BloombergJobEntry("USSW10 CMN3 Index", "LAST_PRICE", TEST_SS));
        TEST_SS.purge();
        runAtTime("2009/05/05 23:59:59");
        assertNoObservations(TEST_SS);
        runAtTime("2009/05/06 00:00:00");
        assertNoObservations(TEST_SS);
        runAtTime("2009/05/06 01:30:00");
        assertHasObservation(TEST_SS);
        TEST_SS.purge();
        runAtTime("2009/05/06 05:59:59");
        assertHasObservation(TEST_SS);
        TEST_SS.purge();
        runAtTime("2009/05/06 06:00:00");
        assertNoObservations(TEST_SS);
    }

    private void assertHasObservation(SeriesSource ss) {
        Observations latest = ss.latestObservation();
        assertEquals(3.2950, latest.value());
        assertEquals(date("2009/05/05 12:34:56"), latest.time());
    }

    private void assertNoObservations(SeriesSource ss) {
        assertFalse(ss.hasObservationToday(date("2009/05/05")));
    }

    private void runAtTime(String time) {
        freezeNow(time);
        downloader.run();
    }
    
}
