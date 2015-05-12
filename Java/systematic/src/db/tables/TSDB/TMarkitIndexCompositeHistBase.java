package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TMarkitIndexCompositeHistBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TMarkitIndexCompositeHistBase T_T_MARKIT_INDEX_COMPOSITE_HIST = new TMarkitIndexCompositeHistBase("T_Markit_Index_Composite_Histbase");

    public TMarkitIndexCompositeHistBase(String alias) { super("TSDB..T_Markit_Index_Composite_Hist", alias); }

    public VarcharColumn C_HEADERNAME = new VarcharColumn("headerName", "varchar(40)", this, NOT_NULL);
    public VarcharColumn C_HEADERVERSION = new VarcharColumn("headerVersion", "varchar(40)", this, NOT_NULL);
    public DatetimeColumn C_HEADERDATE = new DatetimeColumn("headerDate", "datetime", this, NOT_NULL);
    public VarcharColumn C_INDEXFAMILY = new VarcharColumn("indexFamily", "varchar(40)", this, NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NULL);
    public VarcharColumn C_NAME = new VarcharColumn("name", "varchar(120)", this, NOT_NULL);
    public IntColumn C_SERIES = new IntColumn("series", "int", this, NOT_NULL);
    public IntColumn C_VERSION = new IntColumn("version", "int", this, NOT_NULL);
    public VarcharColumn C_TERM = new VarcharColumn("term", "varchar(10)", this, NOT_NULL);
    public VarcharColumn C_REDCODE = new VarcharColumn("redCode", "varchar(30)", this, NULL);
    public VarcharColumn C_INDEXID = new VarcharColumn("indexID", "varchar(100)", this, NULL);
    public DatetimeColumn C_MATURITY = new DatetimeColumn("maturity", "datetime", this, NULL);
    public CharColumn C_ONTHERUN = new CharColumn("onTheRun", "char(1)", this, NULL);
    public FloatColumn C_COMPOSITEPRICE = new FloatColumn("compositePrice", "float(53)", this, NULL);
    public FloatColumn C_COMPOSITESPREAD = new FloatColumn("compositeSpread", "float(53)", this, NULL);
    public FloatColumn C_MODELPRICE = new FloatColumn("modelPrice", "float(53)", this, NULL);
    public FloatColumn C_MODELSPREAD = new FloatColumn("modelSpread", "float(53)", this, NULL);
    public IntColumn C_DEPTH = new IntColumn("depth", "int", this, NULL);


}

