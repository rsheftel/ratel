package com.malbec.tsdb.markit;
import static com.malbec.tsdb.markit.MarkitTable.*;
import static tsdb.AttributeValues.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import mail.MockEmailer.*;
import tsdb.*;
import util.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import db.clause.*;
import db.tables.TSDB.*;

public class TestMarkit extends LoaderTestCase {
	private static final Clause TEST_CDS_AA_MATCHES = CDS_TICKER.value("aa_snrfor_usd_mr").matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
	private static final AttributeValue MR = DOC_CLAUSE.value("mr");
	private static final AttributeValue USD = CCY.value("usd");
	private static final AttributeValue ALCOA = TICKER.value("aa");
	private static final AttributeValue GE = TICKER.value("ge");
	private static final AttributeValue SNRFOR = TIER.value("snrfor");
	private static final Date TEST_DATE = yyyyMmDd("2007/09/14");
	
	public void testCanLoadOneTicker() throws Exception {
		MarkitRow row = MARKIT_CDS.markitRow(TEST_DATE, ALCOA, SNRFOR, USD, MR);
		Clause filter = TEST_CDS_AA_MATCHES;
		assertFalse(loadOne(row, filter));
		assertObservations("aa", row);
		requireNoFailures();
	}
	
	public void testCanCreateTimeSeries() throws Exception {
		MarkitRow row = MARKIT_CDS.markitRow(TEST_DATE, ALCOA, SNRFOR, USD, MR);
		row.put(MARKIT_CDS.C_TICKER.with("testTicker"));
		MARKIT_CDS.insert(row);
		assertFalse(loadOne(row, Clause.FALSE));
		assertObservations("testticker", row);
		requireNoFailures();
		TimeSeries series = series("testticker_snrfor_usd_mr_spread_5y");
		series.requireHas(TICKER.value("testticker"));
		series.requireHas(CDS_STRIKE.value("par"));
		series("testticker_snrfor_usd_mr_av_rating").requireMissing(CDS_STRIKE);
	}
	
	public void slowtestLoadAllFast() throws Exception {
//		Log.debugSql(false);
		new MarkitLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, TEST_DATE);
//		Log.debugSql(false);
		MarkitRow row = MARKIT_CDS.markitRow(TEST_DATE, GE, SNRFOR, USD, MR);
		assertObservations("ge", row);
		assertEquals("Gen Elec Co", TickerTable.TICKER.C_TICKER_DESCRIPTION.value(TickerTable.TICKER.C_TICKER_NAME.is("ge")));
		requireNoFailures();
	}


	private boolean loadOne(MarkitRow row, Clause filter) {
		MarkitLoader loader = new MarkitLoader("jeff.bay@malbecpartners.com");
		return loader.loadOne(row, TEST_SOURCE, TEST_DATE, filter);
	}

	private void assertObservations(String tickerName, MarkitRow row) {
		assertObservation(tickerName + "_snrfor_usd_mr_spread_5y", row.spread("5y"), TEST_DATE);
		assertObservation(tickerName + "_snrfor_usd_mr_recovery", row.recovery(), TEST_DATE);
		assertObservation(tickerName + "_snrfor_usd_mr_av_rating", row.avRating(), TEST_DATE);
		assertObservation(tickerName + "_snrfor_usd_mr_composite_depth_5y", row.compositeDepth5y(), TEST_DATE);
	}

	public void testBadCurrencyInMarkitLoadSendsEmail() throws Exception {
		MarkitRow row = MARKIT_CDS.markitRow(TEST_DATE, ALCOA, SNRFOR, USD, MR);
		row.put(MARKIT_CDS.C_CCY.with("ZZZ"));
		MARKIT_CDS.insert(row);
		emailer.allowMessages();
		assertFalse(loadOne(row, Clause.FALSE));
		String message = emailer.message();
		assertMatches("zzz id\\(", message);
		AttributeValue ccy = CCY.value("zzz");
		assertTrue(ccy.exists());
		assertEquals("created by markit loader", ccy.value(CcyBase.T_CCY.C_DESCRIPTION));
	}
	
	public void testBadTimeSeriesInDBSendsMail() throws Exception {
		MarkitRow row = MARKIT_CDS.markitRow(TEST_DATE, ALCOA, SNRFOR, USD, MR);
		TSAM.deleteAttributes(series("aa_snrfor_usd_mr_av_rating").id());
		emailer.allowMessages();
		assertTrue(loadOne(row, TEST_CDS_AA_MATCHES));
		Sent sent = emailer.sent();
		assertMatches("aa_snrfor_usd_mr", sent.message);
		assertEquals("jeff.bay@malbecpartners.com", the(sent.sentTo));
	}
	
	public void testNoRowsHasErrorAndSendsEmail() throws Exception {
		List<MarkitRow> noRows = empty();
		MarkitLoader loader = new MarkitLoader("asdf@foo.com");
		TimeSeriesLookup seriesIds = TimeSeriesLookup.empty();
		emailer.allowMessages();
		assertTrue(loader.loadRows(TEST_SOURCE, TEST_DATE, seriesIds , noRows));
		emailer.requireSent(1);
	}
	
	public void donotruntestFindBadTimeSeriesDefinitions() throws Exception {
		List<TimeSeries> multiSeries = TimeSeries.multiSeries(values(
			INSTRUMENT.value("cds"),
			QUOTE_TYPE.value("spread", "composite_depth")
		));
		List<TimeSeries> bad = empty();
		int i = multiSeries.size();
		for (TimeSeries series : multiSeries) {
			System.out.println(series.name() + " " + i--);
			if (!series.name().equals("aind-baslhold_secdom_eur_mm_composite_depth_5y")) continue;
			AttributeValues attributes = series.attributes();
			String expectedTenor = series.name().replaceAll(".*_(\\d+[ym])$", "$1");
			String expectedQuoteType = series.name().replaceAll(".*_(spread|composite_depth|av_rating|recovery).*", "$1");
			String attributeName = "tenor";
			requireAttribute(bad, series, attributes, expectedTenor, attributeName);
			requireAttribute(bad, series, attributes, expectedQuoteType, "quote_type");
		}
		Log.info("bad: " + bad);
	}

	private void requireAttribute(List<TimeSeries> bad, TimeSeries series,
			AttributeValues attributes, String expectedValue,
			String attributeName) {
		System.out.println("looking for " + expectedValue);
		Attribute tenor = attribute(attributeName);
		AttributeValue attributeValue = attributes.get(tenor);
		if (attributeValue == null || !attributeValue.name().equals(expectedValue)) 
			bad.add(series);
	}
}
