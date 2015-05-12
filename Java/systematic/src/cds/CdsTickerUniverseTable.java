package cds;

import static db.clause.Clause.*;
import static tsdb.Attribute.*;
import db.tables.TSDB.*;

public class CdsTickerUniverseTable extends CdsTickerUniverseBase {

    private static final long serialVersionUID = 1L;
    public static CdsTickerUniverseTable CDS_UNIVERSE = new CdsTickerUniverseTable();

    public CdsTickerUniverseTable() {
        super("cds_ticker_u");

    }

    public void resetUniverse(String ticker, String tier, String currency, String tenors) {
        deleteAll(TRUE);
        int tickerId = TICKER.value(ticker).id();
        int tierId = TIER.value(tier).id();
        int ccyId = CCY.value(currency).id();
        insert(
            C_TICKER_ID.with(tickerId),
            C_TIER_ID.with(tierId),
            C_CCY_ID.with(ccyId),
            C_TENORS.with(tenors)            
        );
    }

}
