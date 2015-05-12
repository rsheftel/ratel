package com.malbec.tsdb.ivydb;


import static com.malbec.tsdb.ivydb.OptionVolumeTable.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static util.Dates.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.ivydb.OptionVolumeTable.*;
import com.malbec.tsdb.ivydb.definitions.*;
import com.malbec.tsdb.loader.*;

import db.clause.*;

public class OptionVolumeLoader extends Loader<OptionVolumeRow> {

	public OptionVolumeLoader(String address) {
		super("Option Volume", address); 
		add(new OptionVolumeSeriesDefinition());
		add(new OptionOpenInterestSeriesDefinition());
	}

	@Override protected Date adjustDate(Date date, TimeSeriesDataPoint point) {
		return setHour(date, 16);
	}
	
	@Override protected List<OptionVolumeRow> inputRows(Date date) {
		return VOLUMES.rows(date);
	}

	@Override protected TimeSeriesLookup seriesLookup(Clause filter) {
		TSAMTable isEquity = TSAMTable.alias("isOption");
		Clause instrumentMatches = isEquity.attributeMatches(INSTRUMENT.value("std_equity_option"));
		Clause join = TSAM.C_TIME_SERIES_ID.joinOn(isEquity);
		Clause matches = filter.and(instrumentMatches).and(join);
		return new TimeSeriesLookup(matches, SECURITY_ID, OPTION_TYPE, EXPIRY, STRIKE, QUOTE_CONVENTION);
	}
	
	public static void main(String[] args) {
		Loader.usage(args);
		new OptionVolumeLoader(args[0]).run(args, "ivydb");
	}
}
