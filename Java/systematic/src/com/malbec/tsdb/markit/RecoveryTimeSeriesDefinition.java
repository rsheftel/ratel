package com.malbec.tsdb.markit;

import com.malbec.tsdb.loader.*;
import static tsdb.AttributeValues.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import tsdb.*;
import static tsdb.Attribute.*;
import static util.Strings.*;

public class RecoveryTimeSeriesDefinition extends CdsTimeSeriesDefinition {

	@Override protected String name(AttributeValues values) {
		return join("_", super.name(values), "recovery");
	}

	@Override public TimeSeriesDataPoint dataPoint(MarkitRow row, TimeSeriesLookup lookup) {
		int id = id(row, lookup, values(QUOTE_TYPE.value("recovery")));
		return new TimeSeriesDataPoint(id, row.recovery());
	}
}
