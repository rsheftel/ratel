package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class STDOPTIONPRICETEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final STDOPTIONPRICETEMPBase T_STD_OPTION_PRICE_TEMP = new STDOPTIONPRICETEMPBase("STD_OPTION_PRICE_TEMPbase");

    public STDOPTIONPRICETEMPBase(String alias) { super("IvyDB..STD_OPTION_PRICE_TEMP", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("Days", "int", this, NOT_NULL);
    public RealColumn C_FORWARDPRICE = new RealColumn("ForwardPrice", "real", this, NOT_NULL);
    public RealColumn C_STRIKE = new RealColumn("Strike", "real", this, NOT_NULL);
    public CharColumn C_CALLPUT = new CharColumn("CallPut", "char(1)", this, NOT_NULL);
    public RealColumn C_PREMIUM = new RealColumn("Premium", "real", this, NOT_NULL);
    public RealColumn C_IMPLIEDVOLATILITY = new RealColumn("ImpliedVolatility", "real", this, NOT_NULL);
    public RealColumn C_DELTA = new RealColumn("Delta", "real", this, NOT_NULL);
    public RealColumn C_GAMMA = new RealColumn("Gamma", "real", this, NOT_NULL);
    public RealColumn C_THETA = new RealColumn("Theta", "real", this, NOT_NULL);
    public RealColumn C_VEGA = new RealColumn("Vega", "real", this, NOT_NULL);


}

