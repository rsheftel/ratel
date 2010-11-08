package org.ratel.schedule;

import static org.ratel.schedule.JobTable.*;
import static org.ratel.tsdb.AttributeValues.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.tsdb.TimeSeriesTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.schedule.Arbitrate.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;
import org.ratel.db.*;
import org.ratel.schedule.JobTable.*;
import org.ratel.tsdb.*;
import org.ratel.util.*;

public class TestArbitration extends AbstractJobTest {
    private static final String ARBITRATION_DATE = "1999/12/30";
    private static final Range ARBITRATION_RANGE = range(ARBITRATION_DATE);
    private static final TimeSeries AAPL_CLOSE = series("aapl close");
    private static final TimeSeries AAPL_OPEN = series("aapl open");
    private static final SeriesSource AAPL_CLOSE_YAHOO = YAHOO.with(AAPL_CLOSE);
    private static final SeriesSource AAPL_OPEN_YAHOO = YAHOO.with(AAPL_OPEN);
    private static final SeriesSource AAPL_CLOSE_TEST = TEST_SOURCE.with(AAPL_CLOSE);
    
    @Override public void setUp() throws Exception {
        super.setUp();
        freezeNow(hoursAhead(15, date(ARBITRATION_DATE)));
    }

    
    public void testArbitrate() throws Exception {
        Job job = insertAaplArbitration(YAHOO, TEST_SOURCE);
        job.run(now());
        assertObservationsMatch(ARBITRATION_RANGE);
    }

    public void testCannotArbitrateToFromSameSource() throws Exception {
        Job job = insertAaplArbitration(YAHOO, YAHOO);
        emailer.allowMessages();
        job.run(now());
        emailer.sent().hasContent("same source");
    }
    
    public void testNoDataCausesEmailToBeSent() throws Exception {
        Job job = insertAaplArbitration(YAHOO, TEST_SOURCE);
        emailer.allowMessages();
        AAPL_CLOSE_YAHOO.purge();
        AAPL_OPEN_YAHOO.purge();
        job.run(now());
        emailer.sent().hasContent("no data");
    }
    
    public void testNoDataInOneTableIsOk() throws Exception {
        TIME_SERIES.updateOne(new Row(TIME_SERIES.C_DATA_TABLE.with("time_series_data_equity_open")), TIME_SERIES.C_TIME_SERIES_ID.is(AAPL_OPEN.id()));
        Job job = insertAaplArbitration(YAHOO, TEST_SOURCE);
        job.run(now());
        assertEquals(100.31, AAPL_CLOSE_TEST.observations().value());
    }
    
    public void testCanOverwriteDataInDeleteExistingMode() throws Exception {
        Job job = insertAaplArbitration(YAHOO, TEST_SOURCE);
        job.setTempParameter("delete_existing", "true");
        changeObservation();
        job.run(now());
        assertEquals(100.31, AAPL_CLOSE_TEST.observations().value());
    }
    
    public void testCannotOverwriteDataInNormalMode() throws Exception {
        Job job = insertAaplArbitration(YAHOO, TEST_SOURCE);
        changeObservation();
        emailer.allowMessages();
        job.run(now());
        emailer.sent().hasContent("cannot arbitrate over existing data");
        assertEquals(18.0, AAPL_CLOSE_TEST.observations().value());
    }
    
    public void testCannotAbritrateWithBadTimeSeriesGroup() throws Exception {
        GROUPS.insert("bad aapl", new AttributeValues());
        Job job = JOBS.insert("aapl arbitration", new Arbitrate(), THREE_PM);
        job.setParameters(map(
            FROM_PARAM, YAHOO.name(), 
            TO_PARAM, TEST_SOURCE.name(), 
            GROUP_PARAM, "bad aapl"
        ));
        emailer.allowMessages();
        job.run(now());
        emailer.sent().hasContent("no attribute values to lookup from");
    }

    public void testCanArbitrateWithTimeSeriesNames() throws Exception {
        GROUPS.insert("bad aapl", series("aapl high"), series("aapl close"));
        Job job = JOBS.insert("aapl arbitration", new Arbitrate(), THREE_PM);
        job.setParameters(map(
            FROM_PARAM, YAHOO.name(), 
            TO_PARAM, TEST_SOURCE.name(), 
            GROUP_PARAM, "bad aapl"
        ));
        job.run(now());
        assertObservationsMatch(ARBITRATION_RANGE);
    }
    
    private void changeObservation() {
        Observations obs = AAPL_CLOSE_YAHOO.observations(ARBITRATION_RANGE);
        obs.set(obs.time(), 18);
        obs.write(AAPL_CLOSE_TEST);
    }
    
    private Job insertAaplArbitration(DataSource from, DataSource to) {
        GROUPS.insert("aapl test", values(
            TICKER.value("aapl"), 
            QUOTE_TYPE.value("close", "open")
        ));
        Job job = JOBS.insert("aapl arbitration", new Arbitrate(), THREE_PM);
        job.setParameters(map(
            FROM_PARAM, from.name(), 
            TO_PARAM, to.name(), 
            GROUP_PARAM, "aapl test"
        ));
        return job;
    }
    
    private void assertObservationsMatch(Range range) {
        assertEquals(
            AAPL_CLOSE_YAHOO.observations(range),
            AAPL_CLOSE_TEST.observations(range)
        );
        assertEmpty(AAPL_CLOSE_TEST.observations(range(daysAhead(1, date(ARBITRATION_DATE)))));
        assertEmpty(AAPL_CLOSE_TEST.observations(range(daysAgo(1, date(ARBITRATION_DATE)))));
    }
}
