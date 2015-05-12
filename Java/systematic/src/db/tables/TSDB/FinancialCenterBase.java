package db.tables.TSDB;

import db.*;
import db.columns.*;

public class FinancialCenterBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FinancialCenterBase T_FINANCIAL_CENTER = new FinancialCenterBase("financial_centerbase");

    public FinancialCenterBase(String alias) { super("TSDB..financial_center", alias); }

    public IntColumn C_ID = new IntColumn("id", "int", this, NOT_NULL);
    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(50)", this, NOT_NULL);


}

