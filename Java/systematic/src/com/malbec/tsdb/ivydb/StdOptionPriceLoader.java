package com.malbec.tsdb.ivydb;

import static com.malbec.tsdb.ivydb.StdOptionPriceTable.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;

import java.util.*;

import tsdb.*;
import util.*;

import com.malbec.tsdb.ivydb.StdOptionPriceTable.*;
import com.malbec.tsdb.ivydb.definitions.*;
import com.malbec.tsdb.loader.*;

import db.clause.*;

public class StdOptionPriceLoader extends Loader<StdOptionPriceRow> {

	public StdOptionPriceLoader(String failureAddresses) {
		super("StdOptionPrice", failureAddresses);
		add(new StdDeltaSeriesDefinition());
		add(new StdImpliedVolSeriesDefinition());
	}

	@Override protected List<StdOptionPriceRow> inputRows(Date date) {
		return STD_OPTION_PRICE.rows(date);
	}
	
	@Override protected Date adjustDate(Date date, TimeSeriesDataPoint point) {
		return Dates.setHour(date, 16);
	}
	
	@Override protected TimeSeriesLookup seriesLookup(Clause filter) {
		TSAMTable isEquity = TSAMTable.alias("isOption");
		Clause instrumentMatches = isEquity.attributeMatches(INSTRUMENT.value("std_equity_option"));
		Clause join = TSAM.C_TIME_SERIES_ID.joinOn(isEquity);
		Clause matches = filter.and(instrumentMatches).and(join);
		return new TimeSeriesLookup(matches, SECURITY_ID, QUOTE_CONVENTION, OPTION_TYPE, EXPIRY, STRIKE);
	}
	
	public static void main(String[] args) {
		Loader.usage(args);
		new StdOptionPriceLoader(args[0]).run(args, "ivydb");
	}

}
