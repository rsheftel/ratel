package com.malbec.tsdb.ivydb;

import static com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import static tsdb.AttributeValues.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Range.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import com.malbec.tsdb.loader.*;

import db.clause.*;

public class TestSecurityPrice extends LoaderTestCase {
	private static final int TEST_SECURITY_ID = 105169;
	private static final AttributeValue TEST_ATTRIBUTE_VALUE = SECURITY_ID.value(String.valueOf(TEST_SECURITY_ID));
	private static final Clause TEST_SECURITY_MATCHES = TEST_ATTRIBUTE_VALUE.matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
	private static final Date TEST_DATE = yyyyMmDd("2007/09/17");

	public void testCanLoadOneSecurity() throws Exception {
		SecurityPriceRow row = SECURITY_PRICE.row(TEST_SECURITY_ID, TEST_DATE);
		SecurityPriceLoader loader = new SecurityPriceLoader("jeff.bay@malbecpartners.com");
		loader.loadOne(row, TEST_SOURCE, TEST_DATE, TEST_SECURITY_MATCHES);
		AttributeValues values = values(
            SECURITY_ID.value(TEST_SECURITY_ID),
            QUOTE_CONVENTION.value("price"),
            QUOTE_TYPE.value("close")
        );
        assertEquals(40.18, observations(TEST_SOURCE, range(TEST_DATE), values).value());
        values.replace(QUOTE_TYPE.value("open"));
        assertEquals(40.08, observations(TEST_SOURCE, range(TEST_DATE), values).value());
        values.replace(QUOTE_TYPE.value("high"));
        assertEquals(40.35, observations(TEST_SOURCE, range(TEST_DATE), values).value());
        values.replace(QUOTE_TYPE.value("low"));
        assertEquals(40.04, observations(TEST_SOURCE, range(TEST_DATE), values).value());

		values.replace(QUOTE_TYPE.value("shares_outstanding"));
		values.remove(QUOTE_CONVENTION);
        assertEquals(10246180.0, observations(TEST_SOURCE, range(TEST_DATE), values).value());
        
        values.replace(QUOTE_TYPE.value("volume"));
        assertEquals(24279200.0, observations(TEST_SOURCE, range(TEST_DATE), values).value());
        
        values.remove(QUOTE_TYPE);
        values.add(QUOTE_CONVENTION.value("total_return_factor"));
		assertEquals(7.617258, observations(TEST_SOURCE, range(TEST_DATE), values).value());
		
		values.replace(QUOTE_CONVENTION.value("tri_daily_pct"));
		Observations triDailyPct = observations(TEST_SOURCE, range(TEST_DATE), values);
        assertEquals(-0.004213, triDailyPct.value());
        assertEquals(yyyyMmDdHhMmSs("2007/09/17 16:00:00"), triDailyPct.time());
	}

	public void slowtestLoadAllFast() throws Exception {
//		Log.debugSql(false);
		new SecurityPriceLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, TEST_DATE);
//		Log.debugSql(false);
		SecurityPriceRow row = SECURITY_PRICE.row(105168, TEST_DATE);
		assertObservations(105168, row);
		requireNoFailures();
	}


	private void assertObservations(int id, SecurityPriceRow row) {
	    if (true) throw bomb("add assertions for observations of high low close and open"); 
		assertObservation(id + "_shares_outstanding", row.sharesOutstanding());
		assertObservation(id + "_volume", row.volume());
		assertObservation(id + "_total_return_factor_mid", row.totalReturnFactor());
		assertObservation(id + "_tri_daily_pct_mid", row.totalReturn());
	}

	private void assertObservation(String seriesName, Double value) {
		TimeSeries ts = series("ivydb_" + seriesName);
		Observations observations = ts.observations(TEST_SOURCE, range(TEST_DATE));
		assertSize(1, observations);
		assertEquals(value, observations.value());
	}
}
