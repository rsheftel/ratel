package com.malbec.tsdb.markit;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import tsdb.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static util.Strings.*;


public class CompositeDepthTimeSeriesDefinition extends CdsTimeSeriesDefinition {

	@Override protected String name(AttributeValues values) {
		return join("_", super.name(values), "composite_depth_5y");
	}

	@Override public TimeSeriesDataPoint dataPoint(MarkitRow row, TimeSeriesLookup lookup) {
		int id = id(row, lookup, values(
			QUOTE_TYPE.value("composite_depth"), 
			TENOR.value("5y")
		));
		return new TimeSeriesDataPoint(id, row.compositeDepth5y());
	}
}
