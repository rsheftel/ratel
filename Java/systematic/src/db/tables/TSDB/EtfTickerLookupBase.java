package db.tables.TSDB;

import db.*;
import db.columns.*;

public class EtfTickerLookupBase extends Table {

    private static final long serialVersionUID = 1L;    public static final EtfTickerLookupBase T_ETF_TICKER_LOOKUP = new EtfTickerLookupBase("etf_ticker_lookupbase");

    public EtfTickerLookupBase(String alias) { super("TSDB..etf_ticker_lookup", alias); }

    public VarcharColumn C_BLOOMBERG = new VarcharColumn("bloomberg", "varchar(200)", this, NOT_NULL);
    public IntColumn C_OPTION_METRICS_ID = new IntColumn("option_metrics_id", "int", this, NULL);


}

