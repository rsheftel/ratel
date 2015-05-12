package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class InvestorDataBase extends Table {

    private static final long serialVersionUID = 1L;    public static final InvestorDataBase T_INVESTORDATA = new InvestorDataBase("InvestorDatabase");

    public InvestorDataBase(String alias) { super("PerformanceDB..InvestorData", alias); }

    public IntIdentityColumn C_INVDATAID = new IntIdentityColumn("invDataId", "int identity", this, NOT_NULL);
    public IntColumn C_INVDATADEFID = new IntColumn("invDataDefId", "int", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);
    public DecimalColumn C_VALUE = new DecimalColumn("value", "decimal(15, 2)", this, NOT_NULL);


}

