package com.malbec.tsdb.ivydb;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import tsdb.*;

import com.malbec.tsdb.ivydb.StdOptionPriceTable.*;
import com.malbec.tsdb.loader.*;

public abstract class StdOptionSeriesDefinition extends TimeSeriesDefinition<StdOptionPriceRow> {

	private final String quoteConvention;

	public StdOptionSeriesDefinition(String quoteConvention) {
		this.quoteConvention = quoteConvention;
	}
	
	@Override public TimeSeriesDataPoint dataPoint(StdOptionPriceRow row, TimeSeriesLookup lookup) {
		AttributeValues values = values(
			INSTRUMENT.value("std_equity_option"),
			SECURITY_ID.value(row.id()),
			QUOTE_TYPE.value("close"),
			row.optionType().value(),
			STRIKE.value("atm"),
			QUOTE_SIDE.value("mid"),
			QUOTE_CONVENTION.value(quoteConvention),
			EXPIRY.value(row.expiry())
		);
		int id = id(values, lookup); 
		return new TimeSeriesDataPoint(id, seriesValue(row));
	}

	protected abstract Double seriesValue(StdOptionPriceRow row);

	@Override protected String name(AttributeValues values) {
		return "ivydb_" + values.join("_", SECURITY_ID, EXPIRY, STRIKE, OPTION_TYPE, QUOTE_CONVENTION, QUOTE_SIDE);
	}


}
