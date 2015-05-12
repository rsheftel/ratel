package com.malbec.tsdb.markit;

import db.tables.TSDB.*;

class TickerTable extends TickerBase {
    private static final long serialVersionUID = 1L;
    static TickerTable TICKER = new TickerTable();
	protected TickerTable() {
		super("ticker");
	}
	public void create(String name, String description) {
		insert(
			C_TICKER_NAME.with(name), 
			C_TICKER_DESCRIPTION.with(description)
		);
	}
}