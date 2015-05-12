package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CdsIndexDetailsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CdsIndexDetailsBase T_CDS_INDEX_DETAILS = new CdsIndexDetailsBase("cds_index_detailsbase");

    public CdsIndexDetailsBase(String alias) { super("TSDB..cds_index_details", alias); }

    public VarcharColumn C_TICKER_NAME = new VarcharColumn("ticker_name", "varchar(200)", this, NOT_NULL);
    public IntColumn C_SERIES = new IntColumn("series", "int", this, NOT_NULL);
    public NvarcharColumn C_TENOR = new NvarcharColumn("tenor", "nvarchar(50)", this, NOT_NULL);
    public FloatColumn C_STRIKE_BP = new FloatColumn("strike_bp", "float(53)", this, NOT_NULL);
    public DatetimeColumn C_EFFECTIVE_DATE = new DatetimeColumn("effective_date", "datetime", this, NOT_NULL);
    public DatetimeColumn C_MATURITY_DATE = new DatetimeColumn("maturity_date", "datetime", this, NOT_NULL);


}

