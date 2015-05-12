package db.tables.TSDB;

import db.*;
import db.columns.*;

public class FuturesOptionExpiryBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FuturesOptionExpiryBase T_FUTURES_OPTION_EXPIRY = new FuturesOptionExpiryBase("futures_option_expirybase");

    public FuturesOptionExpiryBase(String alias) { super("TSDB..futures_option_expiry", alias); }

    public IntColumn C_OPTION_ID = new IntColumn("option_id", "int", this, NOT_NULL);
    public NcharColumn C_YEARMONTH = new NcharColumn("yearmonth", "nchar(6)", this, NOT_NULL);
    public DatetimeColumn C_EXPIRY = new DatetimeColumn("expiry", "datetime", this, NOT_NULL);


}

