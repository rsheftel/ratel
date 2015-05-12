package com.malbec.tsdb.ivydb;

import static com.malbec.tsdb.ivydb.StdOptionPriceTable.*;
import static tsdb.AttributeValues.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Dates.*;
import static util.Range.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.ivydb.StdOptionPriceTable.*;
import com.malbec.tsdb.loader.*;

import db.clause.*;

public class TestStdOptionPrice extends LoaderTestCase {
	private static final int TEST_SECURITY_ID = 105169;
	private static final AttributeValue TEST_ATTRIBUTE_VALUE = SECURITY_ID.value(String.valueOf(TEST_SECURITY_ID));
	private static final Clause TEST_SECURITY_MATCHES = TEST_ATTRIBUTE_VALUE.matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
	private static final Date TEST_DATE = yyyyMmDd("2007/09/17");
	private static final int EXPIRY_DAYS = 60;
	private static final String CALL_PUT = "C";
	
	public void testCanLoadOneOption() throws Exception {
		StdOptionPriceRow row = STD_OPTION_PRICE.row(TEST_SECURITY_ID, EXPIRY_DAYS, CALL_PUT, TEST_DATE);
		StdOptionPriceLoader loader = new StdOptionPriceLoader("jeff.bay@malbecpartners.com");
		loader.loadOne(row, TEST_SOURCE, TEST_DATE, TEST_SECURITY_MATCHES);
		assertEquals(0.514297, observations(TEST_SOURCE, range(TEST_DATE),  values(
			SECURITY_ID.value(TEST_SECURITY_ID),
			QUOTE_CONVENTION.value("delta")
		)).value());
		Observations vol = observations(TEST_SOURCE, range(TEST_DATE), values(
			SECURITY_ID.value(TEST_SECURITY_ID),
			QUOTE_CONVENTION.value("vol_ln")
		));
        assertEquals(0.221386, vol.value());
		assertEquals(yyyyMmDdHhMmSs("2007/09/17 16:00:00"), vol.time());
	}
	
	public void slowtestLoadAllFast() throws Exception {
//		Log.debugSql(false);
		new StdOptionPriceLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, TEST_DATE);
//		Log.debugSql(false);
		StdOptionPriceRow row = STD_OPTION_PRICE.row(105168, EXPIRY_DAYS, CALL_PUT, TEST_DATE);
		assertObservations(105168, row);
		requireNoFailures();
	}
	
	private void assertObservations(int id, StdOptionPriceRow row) {
		assertObservation(id + "_60d_atm_call_delta_mid", row.delta());
		assertObservation(id + "_60d_atm_call_vol_ln_mid", row.impliedVol());
	}

	private void assertObservation(String seriesName, Double value) {
		TimeSeries ts = series("ivydb_" + seriesName);
		Observations observations = ts.observations(TEST_SOURCE, range(TEST_DATE));
		assertSize(1, observations);
		assertEquals(value, observations.value());
	}
}
