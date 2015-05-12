package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CdsIndexTickerBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CdsIndexTickerBase T_CDS_INDEX_TICKER = new CdsIndexTickerBase("cds_index_tickerbase");

    public CdsIndexTickerBase(String alias) { super("TSDB..cds_index_ticker", alias); }

    public VarcharColumn C_TICKER_NAME = new VarcharColumn("ticker_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_MARKIT_NAME = new VarcharColumn("markit_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_FINANCIAL_CENTER = new VarcharColumn("financial_center", "varchar(50)", this, NOT_NULL);
    public IntColumn C_CLOSING_OFFSET_HOURS = new IntColumn("closing_offset_hours", "int", this, NOT_NULL);


}

