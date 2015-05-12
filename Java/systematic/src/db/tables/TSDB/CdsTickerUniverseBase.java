package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CdsTickerUniverseBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CdsTickerUniverseBase T_CDS_TICKER_UNIVERSE = new CdsTickerUniverseBase("cds_ticker_universebase");

    public CdsTickerUniverseBase(String alias) { super("TSDB..cds_ticker_universe", alias); }

    public NumericColumn C_TICKER_ID = new NumericColumn("ticker_id", "numeric(9)", this, NOT_NULL);
    public IntColumn C_TIER_ID = new IntColumn("tier_id", "int", this, NOT_NULL);
    public IntColumn C_CCY_ID = new IntColumn("ccy_id", "int", this, NOT_NULL);
    public NvarcharColumn C_TENORS = new NvarcharColumn("tenors", "nvarchar(255)", this, NOT_NULL);


}

