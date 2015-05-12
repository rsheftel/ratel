package com.malbec.tsdb.ivydb;


import static com.malbec.tsdb.ivydb.OptionVolumeTable.*;
import static tsdb.AttributeValues.*;
import static tsdb.Attribute.*;
import static tsdb.OptionType.*;
import static tsdb.TSAMTable.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Range.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.ivydb.OptionVolumeTable.*;
import com.malbec.tsdb.loader.*;

import db.clause.*;

public class TestOptionVolLoader extends LoaderTestCase {
	private static final int TEST_SECURITY_ID = 105169;
	private static final AttributeValue TEST_ATTRIBUTE_VALUE = SECURITY_ID.value(String.valueOf(TEST_SECURITY_ID));
	private static final Clause TEST_SECURITY_MATCHES = TEST_ATTRIBUTE_VALUE.matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
	private static final Date TEST_DATE = yyyyMmDd("2007/09/17");
	
	public void testCanLoadOneSecurity() throws Exception {
		OptionVolumeRow call = VOLUMES.row(TEST_SECURITY_ID, TEST_DATE, CALL);
		OptionVolumeRow put = VOLUMES.row(TEST_SECURITY_ID, TEST_DATE, PUT);
		OptionVolumeLoader loader = new OptionVolumeLoader("jeff.bay@malbecpartners.com");
		loader.loadOne(call, TEST_SOURCE, TEST_DATE, TEST_SECURITY_MATCHES);
		loader.loadOne(put, TEST_SOURCE, TEST_DATE, TEST_SECURITY_MATCHES);
		
		AttributeValues values = values(
            SECURITY_ID.value(TEST_SECURITY_ID),
            QUOTE_CONVENTION.value("volume"),
            INSTRUMENT.value("std_equity_option"),
            CALL.value()
		);
		Observations callVolume = observations(TEST_SOURCE, range(TEST_DATE), values);
        assertEquals(26229.0, callVolume.value());
		assertEquals(date("2007/09/17 16:00:00"), the(callVolume.times()));

		values.replace(PUT.value());
		assertEquals(25888.0, observations(TEST_SOURCE, range(TEST_DATE), values).value());
		values.replace(CALL.value());
		values.replace(QUOTE_CONVENTION.value("openinterest"));
		assertEquals(1655928.0, observations(TEST_SOURCE, range(TEST_DATE), values).value());
		values.replace(PUT.value());
		assertEquals(1174187.0, observations(TEST_SOURCE, range(TEST_DATE), values).value());
	}
	
	public void slowtestLoadAllFast() throws Exception {
//		Log.debugSql(false);
		new OptionVolumeLoader("jeff.bay@malbecpartners.com").loadAll(TEST_SOURCE, TEST_DATE);
//		Log.debugSql(true);
		OptionVolumeRow call = VOLUMES.row(105168, TEST_DATE, CALL);
		OptionVolumeRow put = VOLUMES.row(105168, TEST_DATE, PUT);
		assertObservation(105168 + "_call_all_volume", call.volume());
		assertObservation(105168 + "_call_all_openinterest", call.openInterest());
		assertObservation(105168 + "_put_all_volume", put.volume());
		assertObservation(105168 + "_put_all_openinterest", put.openInterest());
		requireNoFailures();
	}
	
	private void assertObservation(String seriesName, Double value) {
		TimeSeries ts = series("ivydb_" + seriesName);
		Observations observations = ts.observations(TEST_SOURCE, range(TEST_DATE));
		assertSize(1, observations);
		assertEquals(value, observations.value());
	}
}
