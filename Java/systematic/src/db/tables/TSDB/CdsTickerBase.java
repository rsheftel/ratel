package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CdsTickerBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CdsTickerBase T_CDS_TICKER = new CdsTickerBase("cds_tickerbase");

    public CdsTickerBase(String alias) { super("TSDB..cds_ticker", alias); }

    public NumericIdentityColumn C_CDS_TICKER_ID = new NumericIdentityColumn("cds_ticker_id", "numeric() identity", this, NOT_NULL);
    public VarcharColumn C_CDS_TICKER_NAME = new VarcharColumn("cds_ticker_name", "varchar(200)", this, NOT_NULL);
    public NumericColumn C_TICKER_ID = new NumericColumn("ticker_id", "numeric(9)", this, NOT_NULL);
    public IntColumn C_TIER_ID = new IntColumn("tier_id", "int", this, NOT_NULL);
    public IntColumn C_CCY_ID = new IntColumn("ccy_id", "int", this, NOT_NULL);
    public IntColumn C_DOC_CLAUSE_ID = new IntColumn("doc_clause_id", "int", this, NOT_NULL);


}

