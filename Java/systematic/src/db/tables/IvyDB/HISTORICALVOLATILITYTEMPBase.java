package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class HISTORICALVOLATILITYTEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final HISTORICALVOLATILITYTEMPBase T_HISTORICAL_VOLATILITY_TEMP = new HISTORICALVOLATILITYTEMPBase("HISTORICAL_VOLATILITY_TEMPbase");

    public HISTORICALVOLATILITYTEMPBase(String alias) { super("IvyDB..HISTORICAL_VOLATILITY_TEMP", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("Days", "int", this, NOT_NULL);
    public RealColumn C_VOLATILITY = new RealColumn("Volatility", "real", this, NULL);


}

