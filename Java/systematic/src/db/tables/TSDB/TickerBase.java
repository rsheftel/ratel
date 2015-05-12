package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TickerBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TickerBase T_TICKER = new TickerBase("tickerbase");

    public TickerBase(String alias) { super("TSDB..ticker", alias); }

    public NumericIdentityColumn C_TICKER_ID = new NumericIdentityColumn("ticker_id", "numeric() identity", this, NOT_NULL);
    public VarcharColumn C_TICKER_NAME = new VarcharColumn("ticker_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_TICKER_DESCRIPTION = new VarcharColumn("ticker_description", "varchar(200)", this, NOT_NULL);


}

