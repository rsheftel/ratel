package com.malbec.tsdb.markit;

import static com.malbec.tsdb.markit.IndexTable.*;
import static db.clause.Clause.*;
import static db.tables.TSDB.CdsIndexTickerBase.*;
import static tsdb.Attribute.*;

import java.util.*;

import mail.*;
import tsdb.*;
import util.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.IndexTable.*;

import db.tables.TSDB.*;

public abstract class MarkitIndexLoader extends Loader<IndexRow> {

	public MarkitIndexLoader(String name, String failureAddresses) {
		super(name, failureAddresses);
	}
	
	@Override protected Date adjustDate(Date date, TimeSeriesDataPoint point) {
		TimeSeries series = TimeSeries.series(point.id());
		String ticker = series.attributes().get(TICKER).name();
		CdsIndexTickerBase t = T_CDS_INDEX_TICKER;
		Integer value = t.C_CLOSING_OFFSET_HOURS.value(t.C_TICKER_NAME.is(ticker));
		return Dates.hoursAhead(value, date);
	}
	
	@Override protected void checkDate(Date runDate) {}
	
	@Override protected void noData(Email failureMessage, Date date) {
		if(!T_CDS_INDEX_TICKER.rowExists(not(MARKIT_INDEX.tickerHoliday(date)))) return;
		Log.info("No data for date " + Dates.yyyyMmDd(date));
		super.noData(failureMessage, date);
	}

	public static void main(String[] args) {
		oneDay(args);
	}

	private static void oneDay(String[] args) {
		Loader.usage(args);
		new MarkitIndexRawLoader(args[0]).run(args, "markit");
		new MarkitIndexOTRLoader(args[0]).run(args, "markit");
	}
}
