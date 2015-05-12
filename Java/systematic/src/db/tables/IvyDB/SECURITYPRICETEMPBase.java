package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class SECURITYPRICETEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SECURITYPRICETEMPBase T_SECURITY_PRICE_TEMP = new SECURITYPRICETEMPBase("SECURITY_PRICE_TEMPbase");

    public SECURITYPRICETEMPBase(String alias) { super("IvyDB..SECURITY_PRICE_TEMP", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public RealColumn C_BIDLOW = new RealColumn("BidLow", "real", this, NOT_NULL);
    public RealColumn C_ASKHIGH = new RealColumn("AskHigh", "real", this, NOT_NULL);
    public RealColumn C_CLOSEPRICE = new RealColumn("ClosePrice", "real", this, NOT_NULL);
    public IntColumn C_VOLUME = new IntColumn("Volume", "int", this, NOT_NULL);
    public RealColumn C_TOTALRETURN = new RealColumn("TotalReturn", "real", this, NOT_NULL);
    public RealColumn C_ADJUSTMENTFACTOR = new RealColumn("AdjustmentFactor", "real", this, NOT_NULL);
    public RealColumn C_OPENPRICE = new RealColumn("OpenPrice", "real", this, NULL);
    public IntColumn C_SHARESOUTSTANDING = new IntColumn("SharesOutstanding", "int", this, NOT_NULL);
    public RealColumn C_ADJUSTMENTFACTOR2 = new RealColumn("AdjustmentFactor2", "real", this, NOT_NULL);


}

