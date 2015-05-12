package com.malbec.tsdb.ivydb.definitions;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import tsdb.*;

import com.malbec.tsdb.ivydb.*;
import com.malbec.tsdb.loader.*;

public class OptionOpenInterestSeriesDefinition extends TimeSeriesDefinition<OptionVolumeTable.OptionVolumeRow> {
	
	@Override public TimeSeriesDataPoint dataPoint(OptionVolumeTable.OptionVolumeRow row, TimeSeriesLookup lookup) {
		AttributeValues values = values(
			INSTRUMENT.value("std_equity_option"),
			SECURITY_ID.value(row.id()),
			QUOTE_CONVENTION.value("openinterest"),
			EXPIRY.value("all"),
			STRIKE.value("all"),
			row.optionType().value()
		);
		int id = id(values, lookup); 
		return new TimeSeriesDataPoint(id, row.openInterest());
	}

	@Override protected String name(AttributeValues values) {
		return "ivydb_" + values.join("_", SECURITY_ID, OPTION_TYPE, EXPIRY, QUOTE_CONVENTION);

	}

}
