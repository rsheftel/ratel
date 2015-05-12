package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class PATCHStdOptionPriceTEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PATCHStdOptionPriceTEMPBase T_PATCH_STDOPTIONPRICE_TEMP = new PATCHStdOptionPriceTEMPBase("PATCH_StdOptionPrice_TEMPbase");

    public PATCHStdOptionPriceTEMPBase(String alias) { super("IvyDB..PATCH_StdOptionPrice_TEMP", alias); }

    public VarcharColumn C_ACTION = new VarcharColumn("Action", "varchar(1)", this, NOT_NULL);
    public IntColumn C_SECURITYID = new IntColumn("securityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("days", "int", this, NOT_NULL);
    public RealColumn C_FORWARDPRICE = new RealColumn("forwardPrice", "real", this, NOT_NULL);
    public RealColumn C_STRIKE = new RealColumn("strike", "real", this, NOT_NULL);
    public CharColumn C_CALLPUTFLAG = new CharColumn("callPutFlag", "char(1)", this, NOT_NULL);
    public RealColumn C_PREMIUM = new RealColumn("premium", "real", this, NOT_NULL);
    public RealColumn C_IMPLIEDVOLATILITY = new RealColumn("impliedVolatility", "real", this, NOT_NULL);
    public RealColumn C_DELTA = new RealColumn("delta", "real", this, NOT_NULL);
    public RealColumn C_GAMMA = new RealColumn("gamma", "real", this, NOT_NULL);
    public RealColumn C_THETA = new RealColumn("theta", "real", this, NOT_NULL);
    public RealColumn C_VEGA = new RealColumn("vega", "real", this, NOT_NULL);


}

