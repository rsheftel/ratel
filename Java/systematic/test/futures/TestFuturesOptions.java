package futures;

import static futures.BloombergField.*;
import static tsdb.AttributeValues.*;
import static futures.Expiry.*;
import static futures.FuturesTable.*;
import static tsdb.Attribute.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import db.*;

public class TestFuturesOptions extends DbTestCase {
	private static final Date TEST_DATE = yyyyMmDd("2008/03/07");

	public void testHistoricalOptionsTickerGeneration() throws Exception {
		ContractHistorical yyz = new ContractHistorical("yyz", 5, 0, "None");
		FUTURES.insert("yyz", 99, -17, "testExchange", "None", "treasury");
		// test quarterlies first
		OptionHistorical opt = new OptionHistorical(yyz, "yyz");
		assertSomeTickers(yyz, opt);
	}
	
	public void testSerialsBork() throws Exception {
		try {
			new ContractHistorical("unused", 5, 1, "None");
			fail("disallow serial specification");
		} catch (RuntimeException success) {
			assertMatches("can't specify serial", success);
		}
	}
	
	public void testTickerGeneration() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		// test quarterlies first
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		assertSomeTickers(yyz, opt);
	}

	private void assertSomeTickers(Contract yyz, Option opt) {
		opt.setDetails(5, 0, "Table");
		opt.setStrikes("quarterly", array(0.25, 0.25, 0.25, 0.5, 0.5), array(11, 11, 11, 7, 7));
		opt.setExpiry(yearMonth(TEST_DATE), date("2008/03/15"));
		populateTestCloses(yyz, TEST_DATE);
		List<OptionTicker> tickers = opt.tickers(TEST_DATE, BLOOMBERG);
		assertSize(47, tickers);
		assertEquals("yyz200803_99", first(tickers).toString());
		assertEquals("yyz200803_100.25", tickers.get(5).toString());
		assertEquals("yyz200803_101.5", tickers.get(10).toString());
		assertEquals("yyz200806_100", tickers.get(11).toString());
		assertEquals("yyz200812_101.5", tickers.get(33).toString());
		assertEquals("yyz200903_105.5", last(tickers).toString());
	}
	
	public void testTickerGenerationWithSerials() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		opt.setDetails(0, 5, "Table");
		opt.setStrikes("monthly", array(0.25, 0.25, 0.25, 0.25, 0.25), array(3,3,3,3,3));
		opt.setExpiry(yearMonth(TEST_DATE), date("2008/03/15"));
		populateTestCloses(yyz, TEST_DATE);
		List<OptionTicker> tickers = opt.tickers(TEST_DATE, BLOOMBERG);
		assertSize(15, tickers);
		assertEquals("yyz200804_101", first(tickers).toString());
		assertEquals("yyz200805_101", fourth(tickers).toString());
		assertEquals("yyz200807_102", tickers.get(6).toString());
		assertEquals("yyz200808_102", tickers.get(9).toString());
		assertEquals("yyz200810_103", tickers.get(12).toString());
		assertEquals("yyz200810_103.5", last(tickers).toString());
	}
	
	public void testTimeSeriesCreation() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		opt.setDetails(1, 1, "Table");
		opt.setExpiry("200802", date("2008/02/15"));
		opt.setExpiry("200803", date("2008/03/15"));
		FuturesTicker quarterly = new FuturesTicker("yyz200803");
		FuturesTicker monthly = new FuturesTicker("yyz200802");
		List<OptionTicker> tickers = list(monthly.optionTicker(100.0), quarterly.optionTicker(100.0));
		opt.createTimeSeries(tickers);
		assertSeries();
		// should not bork
		opt.createTimeSeries(tickers);
	}
	
	public void testEurodollarOptions() throws Exception {
		ContractCurrent ed = FUTURES.insert("tested", 4, 0, "testExchange", THIRD_WED_LESS_TWO, "eurodollar");
		OptionCurrent opt = new OptionCurrent(ed, "tested");
		opt.setDetails(1, 1, THIRD_WED_LESS_TWO, FRIDAY_BEFORE_THIRD_WED);
		opt.setStrikes("quarterly", array(1.0), array(1));
		opt.setStrikes("monthly", array(1.0), array(1));
		populateTestCloses(ed, TEST_DATE);
		List<OptionTicker> tickers = opt.tickers(TEST_DATE, BLOOMBERG);
		assertEquals(new OptionTicker(new FuturesTicker("tested200803"), 100), first(tickers));
		assertEquals(new OptionTicker(new FuturesTicker("tested200804"), 101), second(tickers));
		opt.createTimeSeries(tickers);
		assertAttribute(EXPIRY_DATE, date("2008/03/17"), series("tested200803_100_call_price_mid").attributes());
		assertAttribute(EXPIRY_DATE, date("2008/04/11"), series("tested200804_101_call_price_mid").attributes());
		assertEquals(date("2010/03/15"), opt.expiry("201003").expiration(yearMonth("201003")));

	}

	private void assertSeries() {
		assertTrue(new TimeSeries("yyz200802_100_call_price_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_put_price_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_call_vol_ln_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_call_vol_bp_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_call_vol_bp_daily_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_call_delta_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_put_vol_ln_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_put_vol_bp_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_put_vol_bp_daily_mid").exists());
		assertTrue(new TimeSeries("yyz200802_100_put_delta_mid").exists());
		
		assertTrue(new TimeSeries("yyz200803_100_call_price_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_put_price_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_call_vol_ln_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_call_vol_bp_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_call_vol_bp_daily_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_call_delta_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_put_vol_ln_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_put_vol_bp_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_put_vol_bp_daily_mid").exists());
		assertTrue(new TimeSeries("yyz200803_100_put_delta_mid").exists());
		
		AttributeValues expected = values(
			INSTRUMENT.value("futures_option"),
			TICKER.value("yyz200802"),
			QUOTE_TYPE.value("close"),
			QUOTE_SIDE.value("mid"),
			QUOTE_CONVENTION.value("price"),
			CONTRACT.value("yyz"),
			FUTURE_YEAR.value("2008"),
			FUTURE_MONTH.value("3"),
			FUTURE_MONTH_LETTER.value("h"),
			OPTION_CONTRACT.value("yyz"),
			OPTION_YEAR.value("2008"),
			OPTION_MONTH.value("2"),
			OPTION_MONTH_LETTER.value("g"),
			STRIKE.value("100"),
			EXPIRY.value("actual"),
			EXPIRY_DATE.value(date("2008/02/15")),
			OPTION_TYPE.value("call")
		);
		assertEquals(expected, new TimeSeries("yyz200802_100_call_price_mid").attributes());
	}
	
	public void testExpirationEdgeCases() throws Exception {
		assertContracts("2008/03/08", 0, 5, "yyz200804_101.25", "yyz200810_103.25", 5);
		assertContracts("2008/02/08", 0, 5, "yyz200802_100.25", "yyz200808_102.25", 5);
		assertContracts("2008/03/15", 5, 5, "yyz200803_100.25", "yyz200903_104.25", 10);
		assertContracts("2008/03/16", 5, 5, "yyz200804_101.25", "yyz200906_105.25", 10);
		assertContracts("2008/02/15", 5, 5, "yyz200802_100.25", "yyz200903_104.25", 10);
		assertContracts("2008/02/16", 5, 5, "yyz200803_100.25", "yyz200903_104.25", 10);
	}
	
	public void testOptionExpiresOneMonthEarly() throws Exception {
		Date date = yyyyMmDd("2008/03/15");
		OptionCurrent opt = option(1, 1, date);
		opt.setExpiry("200803", date("2008/02/14"));
		opt.setExpiry("200804", date("2008/03/14"));
		opt.setExpiry("200805", date("2008/04/14"));
		opt.setExpiry("200806", date("2008/05/14"));
		List<OptionTicker> tickers = opt.tickers(date, BLOOMBERG);
		assertEquals("yyz200805_101.25", first(tickers).toString());
	}
	
	private void assertContracts(String dateString, int quarterly, int monthly, String first, String last, int count) {
		Date date = yyyyMmDd(dateString);
		Date oneMonthForward = monthsAhead(1, date);
		OptionCurrent opt = option(quarterly, monthly, date);
		opt.setExpiry(yearMonth(date), date(dateString.substring(0,8) + "15"));
		opt.setExpiry(yearMonth(oneMonthForward), date(yyyyMmDd(oneMonthForward).substring(0,8) + "15"));
		List<OptionTicker> tickers = opt.tickers(date, BLOOMBERG);
		assertSize(count, tickers);
		assertEquals(first, first(tickers).toString());
		assertEquals(last, last(tickers).toString());
		Db.reallyRollback();
	}

	private OptionCurrent option(int quarterly, int monthly, Date date) {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		populateTestCloses(yyz, date);
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		opt.setDetails(quarterly, monthly, "Table");
		setStrikes("quarterly", quarterly, opt);
		setStrikes("monthly", monthly, opt);
		return opt;
	}
	
	
	public void testBloombergTicker() throws Exception {
		FuturesTicker t = new FuturesTicker("yyz200803");
		OptionTicker ot = new OptionTicker(t, 117.5);
		assertEquals("YYZH8C 117.5", ot.bloomberg("call"));
		assertEquals("YYZH8P 117.5", ot.bloomberg("put"));
	}
	
	
	public void testJobEntries() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		opt.setDetails(1, 1, "Table");
		opt.setExpiry("200802", date("2008/02/15"));
		opt.setExpiry("200803", date("2008/03/15"));
		opt.setStrikes("monthly", array(0.5), array(1));
		opt.setStrikes("quarterly", array(0.5), array(1));
		Date testDate = date("2008/02/07");
		populateTestCloses(yyz, testDate);
		TimeSeries priceSeries = new TimeSeries("yyz200802_100_call_price_mid");
		assertFalse(priceSeries.exists());
		List<BloombergJobEntry> entries = opt.jobEntries(testDate, FUTURES_PRICE);
		assertSeries();
		assertEquals(new BloombergJobEntry("YYZG8C 100 PIT Comdty", FUTURES_PRICE.bloomberg(), BLOOMBERG.with(priceSeries)), first(entries));
		assertEquals(new BloombergJobEntry("YYZG8P 100 PIT Comdty", FUTURES_PRICE.bloomberg(), BLOOMBERG.with("yyz200802_100_put_price_mid")), second(entries));
		assertEquals(new BloombergJobEntry("YYZH8C 100 PIT Comdty", FUTURES_PRICE.bloomberg(), BLOOMBERG.with("yyz200803_100_call_price_mid")), third(entries));
		assertEquals(new BloombergJobEntry("YYZH8P 100 PIT Comdty", FUTURES_PRICE.bloomberg(), BLOOMBERG.with("yyz200803_100_put_price_mid")), fourth(entries));
	}

	private void setStrikes(String type, int num, OptionCurrent opt) {
		if(num == 0) return;
		opt.setStrikes(
			type, 
			Collections.nCopies(num, 0.25).toArray(new Double[0]), 
			Collections.nCopies(num, 1).toArray(new Integer[0])
		);
	}

	private void populateTestCloses(Contract futures, Date date) {
		Date yesterday = businessDaysAgo(1, date, "nyb");
		yesterday = hoursAhead(15, midnight(yesterday));
		List<TimeSeries> serieses = futures.priceSeries(yesterday);
		double offset = 0.0;
		for (TimeSeries series : serieses) 
			BLOOMBERG.with(series).write(yesterday, 100.15 + offset++);
	}
	
	public void testOptionLookupByContract() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		ContractCurrent zzz = FUTURES.insert("zzz", 5, 0, "testExchange", "None", "treasury");
		ContractCurrent qqz = FUTURES.insert("qqz", 5, 0, "testExchange", "None", "treasury");
		OptionCurrent optYyz = new OptionCurrent(yyz, "yyz");
		optYyz.setDetails(1, 1, "Table");
		OptionCurrent opt0yz = new OptionCurrent(yyz, "0yz");
		opt0yz.setDetails(1, 1, "Table");
		OptionCurrent optZzz = new OptionCurrent(zzz, "zzz");
		optZzz.setDetails(1, 1, "Table");
		assertSize(2, yyz.options());
		assertSize(1, zzz.options());
		assertEquals(first(zzz.options()), optZzz);
		assertSize(0, qqz.options());
	}
	
	public void testNoFuturesClose() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		opt.setDetails(1, 1, "Table");
		opt.setStrikes("monthly", array(0.5), array(1));
		opt.setStrikes("quarterly", array(0.5), array(1));
		opt.setExpiry("200803", date("2008/03/15"));
		opt.setExpiry("200804", date("2008/04/15"));
		emailer.allowMessages();
		opt.jobEntries(TEST_DATE, FUTURES_PRICE);
		emailer.requireSent(2);
		assertMatches("time series does not exist", emailer.first().message);
		emailer.clear();
		yyz.createTimeSeries(yyz.futuresTickers(TEST_DATE));
		opt.jobEntries(TEST_DATE, FUTURES_PRICE);
		emailer.requireSent(2);
		assertMatches("Missing price", emailer.first().message);
		populateTestCloses(yyz, TEST_DATE);
		emailer.disallowMessages();
		opt.jobEntries(TEST_DATE, FUTURES_PRICE);
	}
	
	public void testNoExpirySet() throws Exception {
		ContractCurrent yyz = FUTURES.insert("yyz", 5, 0, "testExchange", "None", "treasury");
		OptionCurrent opt = new OptionCurrent(yyz, "yyz");
		opt.setDetails(1, 1, "Table");
		opt.setStrikes("monthly", array(0.5), array(1));
		opt.setStrikes("quarterly", array(0.5), array(1));
		populateTestCloses(yyz, TEST_DATE);
		emailer.allowMessages();
		opt.jobEntries(TEST_DATE, FUTURES_PRICE);
		assertMatches("Missing expiry for 200803 for", emailer.sent().message);
		emailer.clear();
		opt.setExpiry("200803", date("2008/03/15"));
		opt.jobEntries(TEST_DATE, FUTURES_PRICE);
		assertMatches("The following options cannot have time series created", emailer.sent().message);
	}
	
	public void testMidcurves() throws Exception {
		ContractCurrent ed = FUTURES.insert("tested", 12, 0, "testExchange", THIRD_WED_LESS_TWO, "eurodollar");
		OptionCurrent opt = new OptionCurrent(ed, "0ested");
		opt.setDetails(1, 1, FRIDAY_BEFORE_THIRD_WED, FRIDAY_BEFORE_THIRD_WED, 12);
		opt.setStrikes("quarterly", array(1.0), array(1));
		opt.setStrikes("monthly", array(1.0), array(1));
		populateTestCloses(ed, TEST_DATE);
		List<OptionTicker> tickers = opt.tickers(TEST_DATE, BLOOMBERG);
		assertEquals(new OptionTicker(new FuturesTicker("0ested200803"), 104), first(tickers));
		assertEquals(new OptionTicker(new FuturesTicker("0ested200804"), 105), second(tickers));
		opt.createTimeSeries(tickers);
		assertAttribute(EXPIRY_DATE, date("2008/03/14"), series("0ested200803_104_call_price_mid"));
		assertAttribute(EXPIRY_DATE, date("2008/04/11"), series("0ested200804_105_call_price_mid"));
		assertAttribute(FUTURE_YEAR, "2009", series("0ested200803_104_call_price_mid"));
		assertAttribute(FUTURE_YEAR, "2009", series("0ested200804_105_call_price_mid"));
		assertAttribute(FUTURE_MONTH, "3", series("0ested200803_104_call_price_mid"));
		assertAttribute(FUTURE_MONTH, "6", series("0ested200804_105_call_price_mid"));
		assertAttribute(OPTION_MONTH, "3", series("0ested200803_104_call_price_mid"));
		assertAttribute(OPTION_MONTH, "4", series("0ested200804_105_call_price_mid"));
		assertAttribute(CONTRACT, "tested", series("0ested200803_104_call_price_mid"));
		assertAttribute(CONTRACT, "tested", series("0ested200804_105_call_price_mid"));
		assertEquals(date("2009/03/13"), opt.expiry("200903").expiration(yearMonth("200903")));
	}
	
}
