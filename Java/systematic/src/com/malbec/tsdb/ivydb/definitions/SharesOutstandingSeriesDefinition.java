package com.malbec.tsdb.ivydb.definitions;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import tsdb.*;

import com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import com.malbec.tsdb.loader.*;

public class SharesOutstandingSeriesDefinition extends TimeSeriesDefinition<SecurityPriceRow> {

	@Override public TimeSeriesDataPoint dataPoint(SecurityPriceRow row, TimeSeriesLookup lookup) {
		AttributeValues values = values(
			INSTRUMENT.value("equity"),
			SECURITY_ID.value(row.id()),
			QUOTE_TYPE.value("shares_outstanding")
		);
		int id = id(values, lookup); 
		return new TimeSeriesDataPoint(id, row.sharesOutstanding());
	}

	@Override protected String name(AttributeValues values) {
		return "ivydb_" + values.join("_", SECURITY_ID, QUOTE_TYPE);
	}

}
