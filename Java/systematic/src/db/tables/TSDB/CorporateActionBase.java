package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CorporateActionBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CorporateActionBase T_CORPORATE_ACTION = new CorporateActionBase("corporate_actionbase");

    public CorporateActionBase(String alias) { super("TSDB..corporate_action", alias); }

    public NumericColumn C_CDS_TICKER_ID = new NumericColumn("cds_ticker_id", "numeric(9)", this, NOT_NULL);
    public NumericColumn C_RELATED_CDS_TICKER_ID = new NumericColumn("related_cds_ticker_id", "numeric(9)", this, NOT_NULL);
    public IntColumn C_SERIES_ORDER = new IntColumn("series_order", "int", this, NOT_NULL);
    public DatetimeColumn C_EFFECTIVE_DATE = new DatetimeColumn("effective_date", "datetime", this, NULL);
    public VarcharColumn C_COMMENT = new VarcharColumn("comment", "varchar(256)", this, NULL);


}

