package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CdsTickerUniverseOLDBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CdsTickerUniverseOLDBase T_CDS_TICKER_UNIVERSE_OLD = new CdsTickerUniverseOLDBase("cds_ticker_universe_OLDbase");

    public CdsTickerUniverseOLDBase(String alias) { super("TSDB..cds_ticker_universe_OLD", alias); }

    public IntColumn C_CDS_TICKER_ID = new IntColumn("cds_ticker_id", "int", this, NOT_NULL);


}

