package com.malbec.tsdb.markit;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static util.Strings.*;
import tsdb.*;

import com.malbec.tsdb.loader.*;
import com.malbec.tsdb.markit.MarkitTable.*;

import db.tables.TSDB.*;

public abstract class CdsTimeSeriesDefinition extends TimeSeriesDefinition<MarkitRow> {

	@Override protected String name(AttributeValues values) {
		return cdsBaseName(values);
	}

    public static String cdsBaseName(AttributeValues values) {
        return values.join("_", TICKER, TIER, CCY, DOC_CLAUSE);
    }

	protected int id(CdsData row, TimeSeriesLookup lookup, AttributeValues someValues) {
	    AttributeValues values = cdsValues(row, someValues);
	    return id(values, lookup);
	}

    public static AttributeValues cdsValues(CdsData row, AttributeValues someValues) {
        AttributeValues values = values(
            INSTRUMENT.value("cds"), 
            CDS_TICKER.value(row.cdsTicker().name()),
            TICKER.value(row.ticker().name()),
            TIER.value(row.tier().name()),
            CCY.value(row.ccy().name()),
            DOC_CLAUSE.value(row.docClause().name())
	    );
	    values.add(someValues);
        return values;
    }

    public static AttributeValue cdsTicker(CdsData data) {
        AttributeValue ticker = data.ticker();
        AttributeValue tier = data.tier();
        AttributeValue ccy = data.ccy();
        AttributeValue docClause = data.docClause();
        String cdsTickerName = join("_", ticker.name(), tier.name(), ccy.name(), docClause.name()).toLowerCase();
        AttributeValue value = CDS_TICKER.value(cdsTickerName);
        if (!value.exists()) {
            CdsTickerBase t = CdsTickerTable.CDS_TICKER;
            value.create(
                t.C_TICKER_ID.with(ticker.id()),
                t.C_TIER_ID.with(tier.id()),
                t.C_CCY_ID.with(ccy.id()),
                t.C_DOC_CLAUSE_ID.with(docClause.id())
            );
        }
        return value;

    }

	
}
