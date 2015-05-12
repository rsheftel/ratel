package com.malbec.tsdb.markit;

import com.malbec.tsdb.loader.*;
import static tsdb.AttributeValues.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import tsdb.*;
import static tsdb.Attribute.*;
import static util.Strings.*;

public class SpreadTimeSeriesDefinition extends CdsTimeSeriesDefinition {

	private final String tenor;

	public SpreadTimeSeriesDefinition(String tenor) {
		this.tenor = tenor;
	}
	
	
	@Override protected String name(AttributeValues values) {
		return join("_", super.name(values), "spread", tenor);
	}

	@Override public TimeSeriesDataPoint dataPoint(MarkitRow row, TimeSeriesLookup lookup) {
		int seriesId = id(row, lookup, values(
			QUOTE_TYPE.value("spread"), 
			TENOR.value(tenor),
			CDS_STRIKE.value("par")
		));
		return new TimeSeriesDataPoint(seriesId, row.spread(tenor));
	}
}
