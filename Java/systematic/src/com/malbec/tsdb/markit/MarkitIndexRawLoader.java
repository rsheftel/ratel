package com.malbec.tsdb.markit;

import static com.malbec.tsdb.markit.IndexTable.*;
import static db.tables.TSDB.CdsIndexTickerBase.*;
import static db.tables.TSDB.TickerBase.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;

import java.math.*;
import java.util.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.IndexTable.*;

import db.clause.*;

public class MarkitIndexRawLoader extends MarkitIndexLoader {

	public MarkitIndexRawLoader(String failureAddresses) {
		super("MarkitIndexRawLoader", failureAddresses);
		add(new MarketIndexSeriesDefinition<Double>(MARKIT_INDEX.C_COMPOSITESPREAD, "market_spread"));
		add(new MarketIndexSeriesDefinition<Double>(MARKIT_INDEX.C_COMPOSITEPRICE, "market_price"));
		add(new MarketIndexSeriesDefinition<Double>(MARKIT_INDEX.C_MODELSPREAD, "model_spread"));
		add(new MarketIndexSeriesDefinition<Double>(MARKIT_INDEX.C_MODELPRICE, "model_price"));
	}

	@Override protected List<IndexRow> inputRows(Date date) {
		MARKIT_INDEX.emailHolidaySkippedTickers(failureAddresses(), date);
		return MARKIT_INDEX.indexRows(date);
	}

	@Override protected TimeSeriesLookup seriesLookup(Clause filter) {
		return new TimeSeriesLookup(filter, TICKER, TENOR, QUOTE_CONVENTION, QUOTE_TYPE, INDEX_SERIES, INDEX_VERSION, CCY);
	}
	
	@Override protected TimeSeriesLookup seriesLookup() {
		Clause tickerMatches = TSAM.C_ATTRIBUTE_VALUE_ID.is(new ConvertColumn<Integer, BigDecimal>("int", T_TICKER.C_TICKER_ID, "intTickerId"));
		tickerMatches = tickerMatches.and(T_TICKER.C_TICKER_NAME.is(T_CDS_INDEX_TICKER.C_TICKER_NAME));
		Clause matches = TICKER.matches(TSAM.C_ATTRIBUTE_ID).and(tickerMatches);
		return seriesLookup(matches);
	}

}
