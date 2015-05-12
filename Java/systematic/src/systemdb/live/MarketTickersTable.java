package systemdb.live;

import db.*;
import db.tables.SystemDB.*;

public class MarketTickersTable extends MarketTickersBase {
    private static final long serialVersionUID = 1L;
    public static MarketTickersTable TICKERS = new MarketTickersTable();
    public MarketTickersTable() {
        super("market_tickers");
    }
    
    public void insert(String name) {
        insert(C_MARKET.with(name), C_BLOOMBERG.with("ASDF"), C_YELLOWKEY.with("Garbage"), C_TIMESTAMP.now());
    }

    public String lookup(String security) {
        Row row = row(C_MARKET.is(security));
        return row.value(C_BLOOMBERG) + " " + row.value(C_YELLOWKEY);
    }

    public boolean has(String security) {
        return rowExists(C_MARKET.is(security));
    }
}