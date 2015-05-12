package tsdb;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static util.Range.*;
import static tsdb.TimeSeriesDataTable.*;
import util.*;
import db.*;

public class TestObservationsErrorConditions extends DbTestCase {
	public void testObservationsCanBeEmptySometimes() throws Exception {
		TimeSeries series = new TimeSeries("javaTestTs");
		DataSource source = new DataSource("test");
		AttributeValues values = values(
			TICKER.value("test-quantys"),
			QUOTE_TYPE.value("close")
		);
		Range dateRange = range("2001/01/01", "2001/01/02");
		series.create(values);
		assertEmpty(observations(source, values));
		assertEmpty(observations(source, series));
		assertEmpty(observations(source, dateRange, values));
		assertEmpty(observations(source, dateRange, series));
		
		series = new TimeSeries("asdfghkjasdf");
		values = values(
			TICKER.value("asdfjkhasdf"),
			QUOTE_TYPE.value("close")
		);
		try {
			observations(source, values);
			fail();
		} catch (RuntimeException e) {
			assertMatches("cannot find id in attribute ticker, table: TSDB..ticker", e);
		}
		try {
			observations(source, series);
			fail();
		} catch (RuntimeException e) {
			assertMatches("time series does not exist", e);
		}
		try {
			observations(source, dateRange, values);
			fail();
		} catch (RuntimeException e) {
			assertMatches("cannot find id in attribute ticker, table: TSDB..ticker", e);
		}
		try {
			observations(source, dateRange, series);
			fail();
		} catch (RuntimeException e) {
			assertMatches("time series does not exist", e);
		}
		series = new TimeSeries("javaTestTs");
		values = values(
			TICKER.value("test-quantys"),
			QUOTE_TYPE.value("close")
		);
		source = new DataSource("asdfasdjghasd");
		try {
			observations(source, values);
			fail();
		} catch (RuntimeException e) {
			assertMatches("data source does not exist", e);
		}
		try {
			observations(source, series);
			fail();
		} catch (RuntimeException e) {
			assertMatches("data source does not exist", e);
		}
		try {
			observations(source, dateRange, values);
			fail();
		} catch (RuntimeException e) {
			assertMatches("data source does not exist", e);
		}
		try {
			observations(source, dateRange, series);
			fail();
		} catch (RuntimeException e) {
			assertMatches("data source does not exist", e);
		}	
		try {
		    observationsMap(source, 50, values(TICKER.value("aapl")));
		    fail();
		} catch (RuntimeException e) {
		    assertMatches("single series", e);
		}	
		
		source = new DataSource("test");
		Observations small = new Observations();
		small.set("2001/01/01", 123.45);
		small.set("2001/01/02", 9876.54);
		write(series.id(), source.id(), small);
		assertSize(2, observations(source, values));
		assertSize(2, observations(source, series));
		assertSize(2, observations(source, dateRange, values));
		assertSize(2, observations(source, dateRange, series));
	}

}
