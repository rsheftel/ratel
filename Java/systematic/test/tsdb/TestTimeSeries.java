package tsdb;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Range.*;
import static util.Sequence.*;

import java.util.*;

import util.*;
import db.*;
import db.tables.TSDB.*;
public class TestTimeSeries extends DbTestCase {

	public void testTimeSeriesByName() throws Exception {
		TimeSeries aaplClose = series("aapl close");
		requireEquals(4, aaplClose.id());
		
		DataSource yahoo = new DataSource("bogus");
		requireEquals(2, yahoo.id());
		
		Observations yahooAaplClose = aaplClose.observations(yahoo);
		assertEquals(yahooAaplClose.size(), 7);
		assertEquals(87.97, yahooAaplClose.value("2006/07/06"));
		assertEquals(yahooAaplClose, yahoo.observations(aaplClose));
		List<Date> times = yahooAaplClose.times();
        assertContains(yyyyMmDd("2006/07/06"), times);
        double[] values = yahooAaplClose.values();
        for (int i : along(times))
            assertEquals(values[i], yahooAaplClose.value(times.get(i)));
	}
	
	public void testLatestObservation() throws Exception {
		SeriesSource open = new SeriesSource(series("aapl open"), YAHOO);
		Observations latest = open.latestObservation();
		assertEquals(date("2007/03/26 14:00:00"), latest.time());
		assertEquals(93.99, latest.value());
	}
	
	public void testFindByAttributeList() throws Exception {
		TimeSeries aaplClose = series(values(
			TICKER.value("aapl"),
			QUOTE_TYPE.value("close")
		));
		assertEquals(aaplClose.id(), 4);
		assertEquals(aaplClose.name(), "aapl close");
		List<TimeSeries> series = multiSeries(values(
			TICKER.value("aapl")
		));
		assertContains(aaplClose, series);
		assertContains(series("aapl open"), series);
	}
	
	public void testTimeSeriesByAttributeList() throws Exception {
		Observations aaplClose = loadOneTimeSeries();
		assertEquals(aaplClose.size(), 7);
		Observations ivyClose = observations(
			new DataSource("ivydb"),
			range("2006-12-29", "2006-12-29"),
			values(
    			SECURITY_ID.value("101594"),
    			QUOTE_TYPE.value("close"),
    			QUOTE_CONVENTION.value("price"),
    			QUOTE_SIDE.value("mid"),
    			INSTRUMENT.value("equity")
    		)
		);
		assertEquals(1, ivyClose.size());
		
		try {
			observations(
				new DataSource("yahoo"),
				Range.range("2000/01/01", "2000/02/01"),
				values(TICKER.value("aapl"))
			);
			fail();
		} catch(Exception e) {
			assertMatches("multiple elements", e);
		}
	}

	private Observations loadOneTimeSeries() {
		Observations aaplClose = observations(new DataSource("bogus"), values(
			TICKER.value("aapl"),
			QUOTE_TYPE.value("close")
		));
		return aaplClose;
	}
	
	public void testReadMultipleTimeSeries() throws Exception {
	    TsdbObservations observations = loadTestTimeSeries("yahoo");
		assertEquals(5, observations.size());
		SeriesSource seriesSource = new SeriesSource("aapl close", "yahoo");
		assertTrue(observations.has(seriesSource));
		assertEquals(55.77, observations.get(seriesSource).value("2006/07/06 14:00:00"));
		Range range = range("2006/07/03", "2006/08/01");
        assertEquals(range, observations.range());
		for(SeriesSource ss : observations) {
		    assertEquals(range, observations.get(ss).dateRange());
		}
	}
	
	public void testReadTimeSeriesMultipleAttributeValues() throws Exception {
		 TsdbObservations observations = observationsMap(
				new DataSource("yahoo"),
				Range.range("2006/07/01", "2006/08/01"),
				values(
				    TICKER.value("aapl"), 
				    QUOTE_TYPE.value("close", "open", "volume")
				)
		);
		assertEquals(3, observations.size());
	}
	
	private TsdbObservations loadTestTimeSeries(String sourceName) {
		return observationsMap(
			new DataSource(sourceName),
			Range.range("2006/07/01", "2006/08/01"),
			values(TICKER.value("aapl"))
		);
	}
	
	class TSDB extends TimeSeriesDataBase {
	    private static final long serialVersionUID = 1L;
	    public TSDB() { super("data"); }
		void deleteOne(SeriesSource ss, Date time) {
		    deleteOne(ss.matches(C_TIME_SERIES_ID, C_DATA_SOURCE_ID).and(C_OBSERVATION_TIME.is(time)));
		}
	}
	
	public void testWriteMultipleTimeSeries() throws Exception {
	    TsdbObservations observations = loadTestTimeSeries("yahoo");
	    TsdbObservations writeThese = new TsdbObservations();
		DataSource test = new DataSource("test");
		for (SeriesSource s : observations) 
		    writeThese.add(new SeriesSource(s.series(), test), observations.get(s));
		Observations.write(writeThese);
		TsdbObservations result = loadTestTimeSeries("test");
		assertSetEquals(observations.observationses(), result.observationses());
	}

	public void testWriteTimeSeries() throws Exception {
		Observations observations = loadOneTimeSeries();
		SeriesSource seriesSource = new SeriesSource("aapl close", "test");
		assertTrue(seriesSource.observationsMap().isEmpty());
		
		checkWriteRead(observations, seriesSource);
		// check that we can write the same data again without borking
		checkWriteRead(observations, seriesSource);
		
		new TSDB().deleteOne(seriesSource, first(observations.times()));
		checkWriteRead(observations, seriesSource);
		
		observations.set(first(observations.times()), 9999);
		checkWriteRead(observations, seriesSource);
	}
	
	public void testCanWriteZeroTimeSeries() throws Exception {
		checkWriteRead(new Observations(), new SeriesSource("aapl close", "test"));
	}

	private void checkWriteRead(Observations observations,
			SeriesSource seriesSource) {
		seriesSource.write(observations);
		Observations result = seriesSource.observations();
		assertEquals(result, observations);
	}
	
	
	public void testPurgeTimeSeries() throws Exception {
		Observations observations = loadOneTimeSeries();
		SeriesSource seriesSource = new SeriesSource("aapl close", "test");
		seriesSource.write(observations);
		seriesSource.purge();
		Observations result = seriesSource.observations();
		assertEquals(0, result.size());
	}
	
	public void perfTestWriteTimeSeries() throws Exception {
		// Dev baseline: 3203
		// With bulk insert: 782
		long start = System.currentTimeMillis();
		Observations observations = new SeriesSource("aapl close", "yahoo").observations();
		System.out.println("Time taken for load: " + Times.reallyMillisSince(start));
		start = System.currentTimeMillis();
		new SeriesSource("aapl close", "test").write(observations);
		System.out.println("Time taken for write: " + Times.reallyMillisSince(start));
		System.out.println("Rows: " + observations.size());
	}
	
	public void perfTestAlvinUseCase() throws Exception {
		long start = System.currentTimeMillis(); 
		DataSource yahoo = new DataSource("yahoo"); 
		Range range = Range.range("2000/03/06", "2007/09/05");
		yahoo.observations(series("aapl volume"), range);
		yahoo.observations(series("aapl open"), range);
		yahoo.observations(series("aapl low"), range);
		yahoo.observations(series("aapl high"), range);
		yahoo.observations(series("aapl close"), range);
		System.out.println("Time taken: " + Times.reallyMillisSince(start));
	}
	
	
	public void testTimeSeriesAttributeReplace() throws Exception {
	    TimeSeries series = series("aapl close");
	    Observations o = series.observations(YAHOO);
	    AttributeValues attributes = series.attributes();
	    attributes.replace(QUOTE_TYPE.value("test"));
	    series.replaceAll(attributes);
	    assertEquals(attributes, series("aapl close").attributes());
	    assertEquals(o, series.observations(YAHOO));
    }
}
