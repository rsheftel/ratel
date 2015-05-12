package com.malbec.tsdb.markit;

import static com.malbec.tsdb.markit.CdsIndexOtrOverride.*;
import static com.malbec.tsdb.markit.IndexTable.*;
import static db.tables.TSDB.CdsIndexTickerBase.*;
import static java.lang.Double.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static util.Dates.*;

import java.util.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.IndexTable.*;

import db.*;
import db.clause.*;

public class TestMarkitIndex extends LoaderTestCase {
	private static final String CDXNAIGHVOL = "cdx-na-ig-hvol";
	private static final String CDXNAIG = "cdx-na-ig";
	private static final String TEST_DATE_STRING = "2007/12/07";
	private static final Date TEST_DATE = yyyyMmDd(TEST_DATE_STRING);
	private static final Date TEST_DATE_NYB = yyyyMmDdHhMmSs(TEST_DATE_STRING + " 15:00:00");
	private static final Date TEST_DATE_LNB = yyyyMmDdHhMmSs(TEST_DATE_STRING + " 11:00:00");
	private static final Clause TEST_CDXNAIGHVOL_MATCHES = TICKER.value(CDXNAIGHVOL).matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
	
	public void testCanLoadOneIndex() throws Exception {
		IndexRow row = MARKIT_INDEX.indexRow(TEST_DATE, CDXNAIGHVOL, 9, 1, "5y");
		Clause filter = TEST_CDXNAIGHVOL_MATCHES;
		assertFalse(loadOne(row, filter));
		assertObservations(TEST_DATE_NYB, CDXNAIGHVOL, row);
		requireNoFailures();
	}

	public void testLondonTimeSetProperly() throws Exception {
		updateFinancialCenter("lnb", CDXNAIGHVOL, 11);
		IndexRow row = MARKIT_INDEX.indexRow(TEST_DATE, CDXNAIGHVOL, 9, 1, "5y");
		Clause filter = TEST_CDXNAIGHVOL_MATCHES;
		assertFalse(loadOne(row, filter));
		assertObservations(TEST_DATE_LNB, CDXNAIGHVOL, row);
	}
	
	public void testLoadAll() throws Exception {
		updateFinancialCenter("nyb", CDXNAIGHVOL, 15);
		updateFinancialCenter("lnb", CDXNAIG, 11);
		Date noHoliday = yyyyMmDd("2007/12/07");
		Date londonHoliday = yyyyMmDd("2007/08/27");
		Date bothHolidays = yyyyMmDd("2007/01/01");
		new MarkitIndexRawLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, noHoliday);
		requireNoFailures();
		IndexRow row = MARKIT_INDEX.indexRow(noHoliday, CDXNAIG, 7, 1, "5y");
		assertObservations(noHoliday, CDXNAIG, row);
		row = MARKIT_INDEX.indexRow(noHoliday, CDXNAIGHVOL, 9, 1, "5y");
		assertObservations(noHoliday, CDXNAIGHVOL, row);

		emailer.allowMessages();
		new MarkitIndexRawLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, londonHoliday);
		assertNoObservations(londonHoliday, CDXNAIG, "7", "1", false);
		row = MARKIT_INDEX.indexRow(londonHoliday, CDXNAIGHVOL, 8, 1, "5y");
		assertObservations(londonHoliday, CDXNAIGHVOL, row);
		assertMatches("ticker cdx-na-ig in center lnb was skipped", emailer.sent().message);
		emailer.clear();

		new MarkitIndexOTRLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, londonHoliday);
		assertNoObservations(londonHoliday, CDXNAIG, "7", "1", true);
		row = MARKIT_INDEX.indexRow(londonHoliday, CDXNAIGHVOL, 8, 1, "5y");
		assertOtrObservations(londonHoliday, CDXNAIGHVOL, row);
		
		new MarkitIndexRawLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, bothHolidays);
		assertNoObservations(bothHolidays, CDXNAIG, "7", "1", false);
		assertNoObservations(bothHolidays, CDXNAIGHVOL, "9", "1", false);
		assertMatches("ticker cdx-na-ig in center lnb was skipped", emailer.sent().message);
		assertMatches("ticker cdx-na-ig-hvol in center nyb was skipped", emailer.sent().message);
	}

	private void updateFinancialCenter(String center, String ticker, int offset) {
		T_CDS_INDEX_TICKER.updateOne(
			new Row(
				T_CDS_INDEX_TICKER.C_FINANCIAL_CENTER.with(center),
				T_CDS_INDEX_TICKER.C_CLOSING_OFFSET_HOURS.with(offset)
			),
			T_CDS_INDEX_TICKER.C_TICKER_NAME.is(ticker)
		);
	}
	
	public void testCanLoadOneOTRIndex() throws Exception {
		IndexRow row = MARKIT_INDEX.indexRow(TEST_DATE, CDXNAIGHVOL, 9, 1, "5y");
		new MarkitIndexOTRLoader("foo@asdf.com").loadAll(TEST_SOURCE, TEST_DATE);
		assertOtrObservations(TEST_DATE_NYB, CDXNAIGHVOL, row);
	}
	
	public void testRollWorksForCdx() throws Exception {
        Date firstOveriddenDate = date("2007/09/21"); // and then 9/24
        Date firstDayWithSecondOverride = date("2007/09/25");
        OVERRIDE.insert("CDXNAIGHVOL", firstOveriddenDate, daysAgo(1, firstDayWithSecondOverride), 8, 1);
        OVERRIDE.insert("CDXNAIGHVOL", firstDayWithSecondOverride, null, 3, 2);
        
		Date dayBeforeRollDate = date("2007/09/19");
		IndexRow series8 = MARKIT_INDEX.indexRow(dayBeforeRollDate, CDXNAIGHVOL, 8, 1, "5y");
		new MarkitIndexOTRLoader("foo@asdf.com").loadAll(TEST_SOURCE, dayBeforeRollDate);
		assertOtrObservations(hoursAhead(15, dayBeforeRollDate), CDXNAIGHVOL, series8);

        Date rollDate = date("2007/09/20");
        IndexRow series9 = MARKIT_INDEX.indexRow(rollDate, CDXNAIGHVOL, 9, 1, "5y");
        new MarkitIndexOTRLoader("foo@asdf.com").loadAll(TEST_SOURCE, rollDate);
        assertOtrObservations(hoursAhead(15, rollDate), CDXNAIGHVOL, series9);
        
        IndexRow seriesWithOverride = MARKIT_INDEX.indexRow(firstOveriddenDate, CDXNAIGHVOL, 8, 1, "5y");
        new MarkitIndexOTRLoader("foo@asdf.com").loadAll(TEST_SOURCE, firstOveriddenDate);
        assertOtrObservations(hoursAhead(15, firstOveriddenDate), CDXNAIGHVOL, seriesWithOverride);
        

        IndexRow seriesWithSecondOverride = MARKIT_INDEX.indexRow(firstDayWithSecondOverride, CDXNAIGHVOL, 3, 2, "5y");
        new MarkitIndexOTRLoader("foo@asdf.com").loadAll(TEST_SOURCE, firstDayWithSecondOverride);
        assertObservation("cdx-na-ig-hvol_model_spread_5y_otr", seriesWithSecondOverride.modelSpread(), firstDayWithSecondOverride);
		
	}
    
    private void assertObservations(Date date, String tickerName, IndexRow row) { // false
        String seriesVersion = row.series() + "_" + row.version();
        assertObservations(date, tickerName, row, seriesVersion);
    }
    
    private void assertOtrObservations(Date date, String tickerName, IndexRow row) {
        assertObservations(date, tickerName, row, "otr");
        assertObservation(tickerName + "_series_5y_otr", parseDouble(row.series()), date);
        assertObservation(tickerName + "_version_5y_otr", parseDouble(row.version()), date);
    }

    private void assertObservations(Date date, String tickerName, IndexRow row, String seriesVersion) {
        assertObservation(tickerName + "_market_spread_5y_" + seriesVersion, row.marketSpread(), date);
		assertObservation(tickerName + "_market_price_5y_" + seriesVersion, row.marketPrice(), date);
		assertObservation(tickerName + "_model_spread_5y_" + seriesVersion, row.modelSpread(), date);
		assertObservation(tickerName + "_model_price_5y_" + seriesVersion, row.modelPrice(), date);
    }
	
	private void assertNoObservations(Date date, String tickerName, String series, String version, boolean otr) {
		String seriesVersion = otr ? "otr" : series + "_" + version;
		assertNoObservation(tickerName + "_market_spread_5y_" + seriesVersion, date);
		assertNoObservation(tickerName + "_market_price_5y_" + seriesVersion, date);
		assertNoObservation(tickerName + "_model_spread_5y_" + seriesVersion, date);
		assertNoObservation(tickerName + "_model_price_5y_" + seriesVersion, date);
	}

	private boolean loadOne(IndexRow row, Clause filter) {
		MarkitIndexRawLoader loader = new MarkitIndexRawLoader("jeff.bay@malbecpartners.com");
		return loader.loadOne(row, TEST_SOURCE, TEST_DATE, filter);
	}
}