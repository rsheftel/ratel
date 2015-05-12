package com.malbec.tsdb.markit;

import static com.malbec.tsdb.markit.MarkitTable.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import util.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import db.clause.*;

public class MarkitLoader extends Loader<MarkitRow> {

	public static final String[] TENORS = array("6m", "1y", "2y", "3y", "4y", "5y", "7y", "10y", "15y", "20y", "30y");

	public MarkitLoader(String failureAddresses) {
		super("Markit", failureAddresses);
		add(new AvRatingTimeSeriesDefinition());
		add(new CompositeDepthTimeSeriesDefinition());
		add(new RecoveryTimeSeriesDefinition());
		for (String tenor : TENORS)
			add(new SpreadTimeSeriesDefinition(tenor));
	}
	
	@Override protected Date adjustDate(Date date, TimeSeriesDataPoint point) {
		return Dates.setHour(date, 15);
	}

	@Override public boolean loadAll(DataSource targetSource, Date date) {
		Log.info("cache cds tickers");
		attribute("cds_ticker").cacheAllValues();
		return super.loadAll(targetSource, date);
	}

	@Override protected List<MarkitRow> inputRows(Date date) {
		Log.info("retrieve rows from Markit for " + date);
		List<MarkitRow> markitRows = MARKIT_CDS.markitRows(date);
		return markitRows;
	}
	
	@Override protected boolean loadRows(DataSource targetSource, Date date, TimeSeriesLookup lookup,
		List<MarkitRow> inputRows) {
		boolean result = super.loadRows(targetSource, date, lookup, inputRows);
		CcyTable.CCY.emailBadPrecedenceRows(failureAddresses());
		return result;
	}
	
	public static void main(String[] args) {
		Loader.usage(args);
		new MarkitLoader(args[0]).run(args, "markit");
	}

	@Override protected TimeSeriesLookup seriesLookup(Clause filter) { 
		return cdsSeriesLookup(filter);
	}

    static TimeSeriesLookup cdsSeriesLookup(Clause filter) {
        TSAMTable isCds = TSAMTable.alias("isCds");
		Clause cdsMatches = isCds.attributeMatches(INSTRUMENT.value("cds"));
		Clause join = TSAM.C_TIME_SERIES_ID.joinOn(isCds);
		Clause matches = filter.and(cdsMatches).and(join);
		return new TimeSeriesLookup(matches, CDS_TICKER, QUOTE_TYPE, TENOR, CDS_STRIKE);
    }
	
}
