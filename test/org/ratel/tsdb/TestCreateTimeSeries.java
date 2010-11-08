package org.ratel.tsdb;

import static org.ratel.db.clause.Clause.*;
import static org.ratel.db.tables.TSDB.TimeSeriesDataBase.*;
import static org.ratel.tsdb.AttributeValues.*;
import static org.ratel.db.tables.TSDB.TimeSeriesDataScratchBase.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TSAMTable.*;
import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.tsdb.TimeSeriesTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;

import java.sql.*;
import java.util.*;

import org.ratel.db.*;
import org.ratel.db.tables.TSDB.*;
import org.ratel.file.*;

public class TestCreateTimeSeries extends DbTestCase {

    private static final String TEST_SERIES_NAME = "testCreateTs";
    private static final String TEST_SERIES_NAME2 = "testCreateTs2";
    private static final AttributeValues TEST_ATTRIBUTES = values(
        TICKER.value("test-quantys"),
        QUOTE_TYPE.value("close")
    );
    private QDirectory testFiles = new QDirectory("test/tsdb");
    private final QFile sitRate10yFile = testFiles.file("test_sit_rate_10y_mid.def.csv");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sitRate10yFile.deleteIfExists();
    }
    
    public void testCanCreateSimpleTimeSeries() throws Exception {
        TimeSeries series = attemptSeriesCreation(TEST_SERIES_NAME, TEST_ATTRIBUTES);
        List<Integer> timeSeriesIds = timeSeriesIds(TEST_ATTRIBUTES);
        assertContains(series.id(), timeSeriesIds);
        series.delete();
    }

    public void testTimeSeriesAndAttributesAreCreatedInOneTransaction() throws Exception {
        AttributeValues values = values(
            TICKER.value("test-quantys", "test-quantys"),
            QUOTE_TYPE.value("close")
        );
        try {
            attemptSeriesCreation(TEST_SERIES_NAME, values);
            fail("created series with duplicate values");
        } catch (RuntimeException success) {
            Db.reallyRollback();
            assertFalse(new TimeSeries(TEST_SERIES_NAME).exists());
            assertMatches("failed to create testCreateTs", success);
        }
    }
    
    public void testCannotCreateTimeSeriesAsSubsetOfAnother() throws Exception {
        attemptSeriesCreation(TEST_SERIES_NAME, TEST_ATTRIBUTES);
        try {
            attemptSeriesCreation(TEST_SERIES_NAME2, TEST_ATTRIBUTES);
            fail("created two series with same attributes");
        } catch (Exception e) {
            assertMatches("already exists", e.getCause());
            series(TEST_SERIES_NAME2).delete();
        }
        try {
            attemptSeriesCreation(TEST_SERIES_NAME2, values(TICKER.value("test-quantys")));
            fail("created a series with a subset of attributes");
        } catch (Exception e) {
            assertMatches("already exists", e.getCause());
        }
    }
    
    public void testCreateFromFile() throws Exception {
        TimeSeries.createFromFile("test/tsdb/test_time_series_defs.csv");
        assertEquals("40y", series("test_sit_rate_40y_mid").attributes().get(TENOR).name());
        assertEquals("30y", series("test_sit_rate_30y_mid").attributes().get(TENOR).name());
        assertEquals("sit", series("test_sit_rate_10y_mid").attributes().get(CCY).name());
    }
    
    public void testCreateFile() throws Exception {
        TimeSeries.createFromFile(testFiles.file("test_time_series_defs2.csv").path());
        TimeSeries series = series("test_sit_rate_10y_mid");
        assertEquals("10y", series.attributes().get(TENOR).name());
        series.createFile(new QDirectory("test/tsdb"));
        assertEquals(
            testFiles.file("test_time_series_defs2.csv").text().replace("\r", ""), 
            sitRate10yFile.text()
        );
    }
    
    public void testCreateInScratch() throws Exception {
        TIME_SERIES.create("test_series", TEST_ATTRIBUTES, TimeSeriesDataTable.withName("time_series_data_scratch"));
        SeriesSource ss = series("test_series").with(TEST_SOURCE);
        ss.write("2008/04/03",  69.0);
        assertEquals(69.0, ss.latestObservation().value());
        assertEquals(date("2008/04/03"), ss.latestObservation().time());
        TimeSeriesDataScratchBase scratch = T_TIME_SERIES_DATA_SCRATCH;
        assertTrue(scratch.rowExists(scratch.C_TIME_SERIES_ID.is(ss.series().id())));
        TimeSeriesDataBase data = T_TIME_SERIES_DATA;
        assertFalse(data.rowExists(data.C_TIME_SERIES_ID.is(ss.series().id())));
    }
    
    private TimeSeries attemptSeriesCreation(String name, AttributeValues values) {
        String testSeriesName = name;
        TimeSeries series = new TimeSeries(testSeriesName);
        assertFalse(series.exists());
        series.create(values);
        assertTrue(series.exists());
        return series;
    }
    
    public void testCreateNewDataTableGetsForeignKeysRight() throws Exception {
        TimeSeriesDataTable data = TimeSeriesDataTable.withName("time_series_data_test");
        data.schemaTable().destroyIfExists();
        assertFalse(data.schemaTable().exists());
        TIME_SERIES.create("this is a test", values(INSTRUMENT.value("test")));
        assertInsertFails(data, -7, 1);
        assertInsertFails(data, 1, -1);
        
    }
    
    public void testInsertWorksWithMultipleDataTables() throws Exception {
        INSTRUMENT.createValues(list("test1", "test2"));
        TIME_SERIES.create("this is a test1", values(INSTRUMENT.value("test1")));
        TIME_SERIES.create("this is a test2", values(INSTRUMENT.value("test2")));
        List<Row> rows = empty();
        TimeSeries test1 = series("this is a test1");
        rows.add(new Row(
            T_TIME_SERIES_DATA.C_TIME_SERIES_ID.with(test1.id()),
            T_TIME_SERIES_DATA.C_DATA_SOURCE_ID.with(TEST_SOURCE.id()),
            T_TIME_SERIES_DATA.C_OBSERVATION_TIME.with(midnight()),
            T_TIME_SERIES_DATA.C_OBSERVATION_VALUE.with(99.0)
        ));
        TimeSeries test2 = series("this is a test2");
        rows.add(new Row(
            T_TIME_SERIES_DATA.C_TIME_SERIES_ID.with(test2.id()),
            T_TIME_SERIES_DATA.C_DATA_SOURCE_ID.with(TEST_SOURCE.id()),
            T_TIME_SERIES_DATA.C_OBSERVATION_TIME.with(midnight()),
            T_TIME_SERIES_DATA.C_OBSERVATION_VALUE.with(99.0)
        ));
        TimeSeriesDataTable.writeUsingTempWithCommits(rows );
        assertEquals(1, TimeSeriesDataTable.withName("time_series_data_test1").count(TRUE));
        assertEquals(1, TimeSeriesDataTable.withName("time_series_data_test2").count(TRUE));
    }

    @SuppressWarnings("unchecked") private void assertInsertFails(TimeSeriesDataTable data, int seriesId, int sourceId) {
        try {
            ConcreteColumn<Integer> id = (ConcreteColumn<Integer>) data.column("time_series_id");
            ConcreteColumn<Integer> ds = (ConcreteColumn<Integer>) data.column("data_source_id");
            ConcreteColumn<Timestamp> date = (ConcreteColumn<java.sql.Timestamp>) data.column("observation_time");
            ConcreteColumn<Double> value = (ConcreteColumn<Double>) data.column("observation_value");
            data.insert(
                id.with(seriesId),
                ds.with(sourceId),
                date.with(new java.sql.Timestamp(now().getTime())),
                value.with(1.0)
            );
            fail();
        } catch (Exception success) {
            assertMatches("FOREIGN KEY", success.getCause());
        }
    }
    
    @Override protected void tearDown() throws Exception {
        TimeSeries series = new TimeSeries(TEST_SERIES_NAME);
        if (series.exists()) series.delete();
        series = new TimeSeries(TEST_SERIES_NAME2);
        if (series.exists()) series.delete();
        super.tearDown();
    }
}
