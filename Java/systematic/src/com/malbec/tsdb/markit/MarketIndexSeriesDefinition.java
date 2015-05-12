package com.malbec.tsdb.markit;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.IndexTable.*;

import db.*;

public class MarketIndexSeriesDefinition<T extends Number> extends TimeSeriesDefinition<IndexRow> {

	private final Column<T> from;
	private final String toQuoteConvention;
	private final boolean useOnTheRun;

	public MarketIndexSeriesDefinition(Column<T> from, String toQuoteConvention) {
		this(from, toQuoteConvention, false);
	}

	public MarketIndexSeriesDefinition(Column<T> from, String toQuoteConvention, boolean useOnTheRun) {
		this.from = from;
		this.toQuoteConvention = toQuoteConvention;
		this.useOnTheRun = useOnTheRun;
	}

	@Override public TimeSeriesDataPoint dataPoint(IndexRow row, TimeSeriesLookup lookup) {
		AttributeValues values = values(
			INSTRUMENT.value("cds_index"),
			QUOTE_TYPE.value("close"),
			QUOTE_CONVENTION.value(toQuoteConvention),
			TENOR.value(row.tenor()),
			TICKER.value(row.ticker())
		);
		values.add(INDEX_SERIES.value(useOnTheRun ? "otr" : row.series()));
		values.add(INDEX_VERSION.value(useOnTheRun ? "otr" : row.version()));
		T raw = row.value(from);
        Double value = raw == null ? null : raw.doubleValue();
        return new TimeSeriesDataPoint(id(values, lookup), value);
	}

	@Override protected String name(AttributeValues values) {
		List<Attribute> attributes = empty(); 
		attributes.addAll(list(TICKER, QUOTE_CONVENTION, TENOR, INDEX_SERIES));
		if (!useOnTheRun) attributes.add(INDEX_VERSION);
		return values.join("_", attributes.toArray(new Attribute[0]));
	}

}
