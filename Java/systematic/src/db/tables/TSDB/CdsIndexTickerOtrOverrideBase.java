package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CdsIndexTickerOtrOverrideBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CdsIndexTickerOtrOverrideBase T_CDS_INDEX_TICKER_OTR_OVERRIDE = new CdsIndexTickerOtrOverrideBase("cds_index_ticker_otr_overridebase");

    public CdsIndexTickerOtrOverrideBase(String alias) { super("TSDB..cds_index_ticker_otr_override", alias); }

    public VarcharColumn C_MARKIT_NAME = new VarcharColumn("markit_name", "varchar(200)", this, NOT_NULL);
    public IntColumn C_OTR_SERIES = new IntColumn("otr_series", "int", this, NOT_NULL);
    public IntColumn C_OTR_VERSION = new IntColumn("otr_version", "int", this, NOT_NULL);
    public DatetimeColumn C_START_DATE = new DatetimeColumn("start_date", "datetime", this, NOT_NULL);
    public DatetimeColumn C_END_DATE = new DatetimeColumn("end_date", "datetime", this, NULL);


}

