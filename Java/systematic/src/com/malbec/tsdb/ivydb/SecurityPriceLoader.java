package com.malbec.tsdb.ivydb;

import static com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;

import java.util.*;

import tsdb.*;
import util.*;

import com.malbec.tsdb.ivydb.SecurityPriceTable.*;
import com.malbec.tsdb.ivydb.definitions.*;
import com.malbec.tsdb.loader.*;

import db.clause.*;

public class SecurityPriceLoader extends Loader<SecurityPriceRow> {

	public SecurityPriceLoader(String failureAddresses) {
		super("IvyDB", failureAddresses);
		add(new EquityPriceSeriesDefinition("close"));
		add(new EquityPriceSeriesDefinition("open"));
		add(new EquityPriceSeriesDefinition("high"));
		add(new EquityPriceSeriesDefinition("low"));
		add(new SharesOutstandingSeriesDefinition());
		add(new TotalReturnFactorSeriesDefinition());
		add(new TotalReturnPercentSeriesDefinition());
		add(new VolumeSeriesDefinition());
	}

	@Override protected List<SecurityPriceRow> inputRows(Date date) {
		return SECURITY_PRICE.rows(date);
	}
	
	@Override protected Date adjustDate(Date date, TimeSeriesDataPoint point) {
		return Dates.setHour(date, 16);
	}

	@Override protected TimeSeriesLookup seriesLookup(Clause filter) { 
		TSAMTable isEquity = TSAMTable.alias("isEquity");
		Clause instrumentMatches = isEquity.attributeMatches(INSTRUMENT.value("equity"));
		Clause join = TSAM.C_TIME_SERIES_ID.joinOn(isEquity);
		Clause matches = filter.and(instrumentMatches).and(join);
		return new TimeSeriesLookup(matches, SECURITY_ID, QUOTE_TYPE, QUOTE_CONVENTION, QUOTE_SIDE, TRANSFORMATION, TRANSFORMATION_OUTPUT);
	}
	
	public static void main(String[] args) {
		Loader.usage(args);
		new SecurityPriceLoader(args[0]).run(args, "ivydb");
	}

}
