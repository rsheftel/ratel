package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class TsamKeysBase extends Table {
    private static final long serialVersionUID = 1L;
    public TsamKeysBase(String alias) { super("TSDB..#tsam_keys", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public VarcharColumn C_TS_KEY = new VarcharColumn("ts_key", "varchar", this, NOT_NULL);
}

