package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class PATCHSecurityPriceTEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PATCHSecurityPriceTEMPBase T_PATCH_SECURITYPRICE_TEMP = new PATCHSecurityPriceTEMPBase("PATCH_SecurityPrice_TEMPbase");

    public PATCHSecurityPriceTEMPBase(String alias) { super("IvyDB..PATCH_SecurityPrice_TEMP", alias); }

    public VarcharColumn C_ACTION = new VarcharColumn("Action", "varchar(1)", this, NOT_NULL);
    public IntColumn C_SECURITYID = new IntColumn("securityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("date", "smalldatetime", this, NOT_NULL);
    public RealColumn C_BID_LOW = new RealColumn("bid_Low", "real", this, NOT_NULL);
    public RealColumn C_ASK_HIGH = new RealColumn("ask_High", "real", this, NOT_NULL);
    public RealColumn C_CLOSEPRICE = new RealColumn("closePrice", "real", this, NOT_NULL);
    public IntColumn C_VOLUME = new IntColumn("volume", "int", this, NOT_NULL);
    public RealColumn C_TOTALRETURN = new RealColumn("totalReturn", "real", this, NOT_NULL);
    public RealColumn C_CUMULATIVEADJUSTMENTFACTOR = new RealColumn("cumulativeAdjustmentFactor", "real", this, NOT_NULL);
    public RealColumn C_OPENPRICE = new RealColumn("openPrice", "real", this, NULL);
    public IntColumn C_SHAREOUTSTANDING = new IntColumn("shareOutstanding", "int", this, NOT_NULL);
    public RealColumn C_CUMULATIVETOTALRETURNFACTOR = new RealColumn("cumulativeTotalReturnFactor", "real", this, NOT_NULL);


}

