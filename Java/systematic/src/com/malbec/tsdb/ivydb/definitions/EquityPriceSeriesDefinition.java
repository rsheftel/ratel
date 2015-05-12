package com.malbec.tsdb.ivydb.definitions;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import tsdb.*;

import com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import com.malbec.tsdb.loader.*;

public class EquityPriceSeriesDefinition extends TimeSeriesDefinition<SecurityPriceRow> {

    private final String quoteType;

    public EquityPriceSeriesDefinition(String quoteType) {
        this.quoteType = quoteType;
    }
    
	@Override public TimeSeriesDataPoint dataPoint(SecurityPriceRow row, TimeSeriesLookup lookup) {
        AttributeValues values = values(
			INSTRUMENT.value("equity"),
			SECURITY_ID.value(row.id()),
			QUOTE_CONVENTION.value("price"),
			QUOTE_SIDE.value("mid"),
			QUOTE_TYPE.value(quoteType)
		);
		int id = id(values, lookup); 
		return new TimeSeriesDataPoint(id, row.price(quoteType));  // with quote type!
	}

	@Override protected String name(AttributeValues values) {
		return "ivydb_" + values.join("_", SECURITY_ID, QUOTE_TYPE, QUOTE_CONVENTION, QUOTE_SIDE);
	}

}
