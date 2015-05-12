package com.malbec.tsdb.markit;

import static tsdb.Attribute.*;
import static util.Strings.*;
import tsdb.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import static tsdb.AttributeValues.*;

public class AvRatingTimeSeriesDefinition extends CdsTimeSeriesDefinition {

	@Override protected String name(AttributeValues values) {
		return join("_", super.name(values), "av_rating");
	}

	@Override public TimeSeriesDataPoint dataPoint(MarkitRow row, TimeSeriesLookup lookup) {
		return new TimeSeriesDataPoint(id(row, lookup, values(QUOTE_TYPE.value("av_rating"))), row.avRating());
	}


}
