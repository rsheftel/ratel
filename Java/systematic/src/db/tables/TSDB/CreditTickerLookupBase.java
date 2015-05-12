package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CreditTickerLookupBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CreditTickerLookupBase T_CREDIT_TICKER_LOOKUP = new CreditTickerLookupBase("credit_ticker_lookupbase");

    public CreditTickerLookupBase(String alias) { super("TSDB..credit_ticker_lookup", alias); }

    public VarcharColumn C_MARKIT = new VarcharColumn("markit", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_BLOOMBERG = new VarcharColumn("bloomberg", "varchar(200)", this, NULL);
    public IntColumn C_OPTION_METRICS_ID = new IntColumn("option_metrics_id", "int", this, NULL);


}

