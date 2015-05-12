package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class HISTORICALVOLATILITYBase extends Table {

    private static final long serialVersionUID = 1L;    public static final HISTORICALVOLATILITYBase T_HISTORICAL_VOLATILITY = new HISTORICALVOLATILITYBase("HISTORICAL_VOLATILITYbase");

    public HISTORICALVOLATILITYBase(String alias) { super("IvyDB..HISTORICAL_VOLATILITY", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("Days", "int", this, NOT_NULL);
    public RealColumn C_VOLATILITY = new RealColumn("Volatility", "real", this, NULL);


}

