package com.malbec.tsdb.markit;

import tsdb.*;
import db.tables.TSDB.*;

class CdsTickerTable extends CdsTickerBase {
    private static final long serialVersionUID = 1L;
    public static final CdsTickerTable CDS_TICKER = new CdsTickerTable();
	protected CdsTickerTable() {
		super("cdsTicker");
	}
	public void create(String name, AttributeValue ticker, AttributeValue tier, AttributeValue ccy, AttributeValue docClause) {
		insert(
			C_TICKER_ID.with(ticker.id()), 
			C_DOC_CLAUSE_ID.with(docClause.id()),
			C_TIER_ID.with(tier.id()),
			C_CCY_ID.with(ccy.id()),
			C_CDS_TICKER_NAME.with(name)
		);
	}
}