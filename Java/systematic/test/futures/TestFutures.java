package futures;

import static futures.BloombergField.*;
import static tsdb.AttributeValues.*;
import static futures.Expiry.*;
import static futures.FuturesTable.*;
import static futures.BloombergJobTable.*;
import static tsdb.Attribute.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import db.*;
public class TestFutures extends DbTestCase {
	

	private static final Date TEST_DATE = yyyyMmDd("2008/02/07");

	public void testNonDbFutures() throws Exception {
		ContractHistorical yyz = new ContractHistorical("yyz", 5, 0, "None");
		assertTickers(yyz);
		List<FuturesTicker> tickers = yyz.futuresTickers(TEST_DATE);
		FUTURES.insert("yyz", 899, 1010348, "testExchange", "None", "treasury");
		yyz.createTimeSeries(tickers);
		assertSeries();
		// should not bork
		yyz.createTimeSeries(tickers);
	}
	
	public void testTickerGeneration() throws Exception {
		assertTickers(FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury"));
	}
	
	public void testEurodollarExpiry() throws Exception {
		ContractHistorical ed = new ContractHistorical("tested", 4, 0, THIRD_WED_LESS_TWO);
		List<FuturesTicker> tickers = ed.futuresTickers(date("2008/03/17"));
		assertSize(4, tickers);
		assertEquals("tested200803", first(tickers).name());
		tickers = ed.futuresTickers(date("2008/03/18"));
		assertSize(4, tickers);
		assertEquals("tested200806", first(tickers).name());
		tickers = ed.futuresTickers(date("2008/04/18"));
		assertSize(4, tickers);
		assertEquals("tested200806", first(tickers).name());
		tickers = ed.futuresTickers(date("2008/05/18"));
		assertSize(4, tickers);
		assertEquals("tested200806", first(tickers).name());
		tickers = ed.futuresTickers(date("2008/06/18"));
		assertSize(4, tickers);
		assertEquals("tested200809", first(tickers).name());
	}
	
	public void testExpiryOnTimeSeries() throws Exception {
		ContractCurrent ed = FUTURES.insert("tested", 4, 0, "testExchange", THIRD_WED_LESS_TWO, "eurodollar");
		List<FuturesTicker> tickers = ed.futuresTickers(date("2008/03/17"));
		ed.createTimeSeries(tickers);
		TimeSeries priceSeries = new TimeSeries("tested200803_price_mid");
		AttributeValues attributes = priceSeries.attributes();
		assertAttribute(EXPIRY, "actual", attributes);
		assertAttribute(EXPIRY_DATE, date("2008/03/17"), attributes);
	}

	private void assertTickers(Contract yyz) {
		List<FuturesTicker> tickers = yyz.futuresTickers(TEST_DATE);
		// yyzh08, yyzm08, yyzu08, yyzz08, yyzh09
		assertSize(5, tickers);
		assertEquals("yyz200803", first(tickers).name());
		assertEquals("yyz200903", last(tickers).name());
		
		tickers = yyz.futuresTickers(yyyyMmDd("2008/03/07"));
		// yyzh08, yyzm08, yyzu08, yyzz08, yyzh09, yyzm09
		assertSize(6, tickers);
		assertEquals("yyz200803", first(tickers).name());
		assertEquals("yyz200906", last(tickers).name());
		
		assertSize(5, yyz.futuresTickers(yyyyMmDd("2008/02/29")));
		assertSize(6, yyz.futuresTickers(yyyyMmDd("2008/03/01")));
		assertSize(6, yyz.futuresTickers(yyyyMmDd("2008/03/31")));
		assertSize(5, yyz.futuresTickers(yyyyMmDd("2008/04/01")));
	}
	
	public void testTimeSeriesCreation() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		List<FuturesTicker> tickers = yyz.futuresTickers(TEST_DATE);
		yyz.createTimeSeries(tickers);
		assertSeries();
		// should not bork
		yyz.createTimeSeries(tickers);
	}

	private void assertSeries() {
		TimeSeries priceSeries = new TimeSeries("yyz200803_price_mid");
		assertTrue(priceSeries.exists());
		assertTrue(new TimeSeries("yyz200803_volume").exists());
		assertTrue(new TimeSeries("yyz200803_dv01").exists());
		assertTrue(new TimeSeries("yyz200803_convexity").exists());
		assertTrue(new TimeSeries("yyz200903_price_mid").exists());
		AttributeValues expected = values(
			INSTRUMENT.value("futures"),
			TICKER.value("yyz200803"),
			QUOTE_TYPE.value("close"),
			QUOTE_SIDE.value("mid"),
			QUOTE_CONVENTION.value("price"),
			CONTRACT.value("yyz"),
			FUTURE_YEAR.value("2008"),
			FUTURE_MONTH.value("3"),
			FUTURE_MONTH_LETTER.value("h"),
			EXPIRY.value("actual")
		);
		assertEquals(expected, priceSeries.attributes());
	}
	
	public void testBloombergTicker() throws Exception {
		FuturesTicker t = new FuturesTicker("yyz200803");
		assertEquals("YYZH8", t.bloomberg());
		t = new FuturesTicker("ty200906");
		assertEquals("TYM9", t.bloomberg());
	}
	
	public void testJobEntries() throws Exception {
		BloombergLoadable yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		TimeSeries priceSeries = new TimeSeries("yyz200803_price_mid");
		assertFalse(priceSeries.exists());
		List<BloombergJobEntry> entries = yyz.jobEntries(TEST_DATE, FUTURES_PRICE);
		entries.addAll(yyz.jobEntries(TEST_DATE, VOLUME));
		assertSeries();
		SeriesSource ss = new SeriesSource(priceSeries, BLOOMBERG);
		assertEquals(new BloombergJobEntry("YYZH8 PIT Comdty", FUTURES_PRICE.bloomberg(), ss), first(entries));
		assertEquals(new BloombergJobEntry("YYZH8 Comdty", VOLUME.bloomberg(), BLOOMBERG.with("yyz200803_volume")), sixth(entries));
		assertEquals(new BloombergJobEntry("YYZH9 Comdty", VOLUME.bloomberg(), BLOOMBERG.with("yyz200903_volume")), last(entries));
	}
	
	@Deprecated // deprecated to avoid delete all warning
	public void testCreatesCorrectBloombergDbRows() throws Exception {
		FUTURES.deleteAll();
		FUTURES.insert("one", 2, 0, "testExchange", "None", "treasury");
		ContractCurrent contract2 = FUTURES.insert("two", 3, 0, "testExchange2", "None", "treasury");
		BloombergJob price1 = BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange_treasury_PX_SETTLE", "15:00:00", true, "0600");
		BloombergJob volume1 = BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange_treasury_VOLUME", "15:00:00", true, "0600");
		BloombergJob price2 = BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange2_treasury_PX_SETTLE", "15:00:00", true, "0600");
		BloombergJob volume2 = BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange2_treasury_VOLUME", "15:00:00", true, "0600");
		new FuturesBloombergLoader(TEST_DATE).loadAll();
		assertSize(2, price1.entries());
		assertSize(2, volume1.entries());
		assertSize(3, price2.entries());
		assertSize(3, volume2.entries());
		OptionCurrent option = new OptionCurrent(contract2, "imaoption");
		option.setDetails(1, 1, "Table");
		option.setStrikes("quarterly", array(0.5), array(1));
		option.setStrikes("monthly", array(0.5), array(1));
		option.setExpiry("200802", date("2008/02/15"));
		option.setExpiry("200803", date("2008/03/15"));
		first(price2.entries()).writeObservation(date("2008/02/06 15:00:00"), 100.0);
		BloombergJob optionPrice = BLOOMBERG_JOBS.insert("bloomberg_futures_option_autogen_testExchange2_treasury_PX_SETTLE", "15:00:00", true, "0600");
		BLOOMBERG_JOBS.deleteAllEntries("%autogen%");
		new FuturesBloombergLoader(TEST_DATE).loadAll();
		assertSize(4, optionPrice.entries());
	}

	@Deprecated // deprecated to avoid delete all warning
	public void testJobDoesNotExist() throws Exception {
		FUTURES.deleteAll();
		FUTURES.insert("one", 2, 0, "testExchange", "None", "treasury");
		ContractCurrent contract2 = FUTURES.insert("two", 2, 0, "testExchange2", "None", "treasury");
		new FuturesBloombergLoader(TEST_DATE).loadAll();
		OptionCurrent option = new OptionCurrent(contract2, "imaoption");
		option.setDetails(1, 1, "Table");
		BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange_treasury_PX_SETTLE", "15:00:00", true, "0600");
		BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange2_treasury_PX_SETTLE", "15:00:00", true, "0600");
		BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange_treasury_VOLUME", "15:00:00", true, "0600");
		BLOOMBERG_JOBS.insert("bloomberg_futures_autogen_testExchange2_treasury_VOLUME", "15:00:00", true, "0600");
		new FuturesBloombergLoader(TEST_DATE).loadAll();
	}
	
	public void testSerialContractsNotImplementedYet() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 4, 3, "testExchange", "None", "treasury");
		try {
			yyz.futuresTickers(TEST_DATE);
			fail();
		} catch(Exception success) {
			assertMatches("not implemented yet", success);
		}
	}
}
