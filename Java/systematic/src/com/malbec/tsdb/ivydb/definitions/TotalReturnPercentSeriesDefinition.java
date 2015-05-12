package com.malbec.tsdb.ivydb.definitions;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import tsdb.*;

import com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import com.malbec.tsdb.loader.*;

public class TotalReturnPercentSeriesDefinition extends TimeSeriesDefinition<SecurityPriceRow> {

	
	@Override public TimeSeriesDataPoint dataPoint(SecurityPriceRow row, TimeSeriesLookup lookup) {
		AttributeValues values = values(
			INSTRUMENT.value("equity"),
			SECURITY_ID.value(row.id()),
			QUOTE_CONVENTION.value("tri_daily_pct"),
			QUOTE_SIDE.value("mid"),
			QUOTE_TYPE.value("close")
		);
		int id = id(values, lookup); 
		return new TimeSeriesDataPoint(id, row.totalReturn());
	}

	@Override protected String name(AttributeValues values) {
		return "ivydb_" + values.join("_", SECURITY_ID, QUOTE_CONVENTION, QUOTE_SIDE);
	}

}
