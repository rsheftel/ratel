package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class VOLATILITYSURFACE200208Base extends Table {

    private static final long serialVersionUID = 1L;    public static final VOLATILITYSURFACE200208Base T_VOLATILITY_SURFACE_2002_08 = new VOLATILITYSURFACE200208Base("VOLATILITY_SURFACE_2002_08base");

    public VOLATILITYSURFACE200208Base(String alias) { super("IvyDB..VOLATILITY_SURFACE_2002_08", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("Days", "int", this, NOT_NULL);
    public IntColumn C_DELTA = new IntColumn("Delta", "int", this, NOT_NULL);
    public CharColumn C_CALLPUT = new CharColumn("CallPut", "char(1)", this, NULL);
    public RealColumn C_IMPLIEDVOLATILITY = new RealColumn("ImpliedVolatility", "real", this, NULL);
    public RealColumn C_IMPLIEDSTRIKE = new RealColumn("ImpliedStrike", "real", this, NULL);
    public RealColumn C_IMPLIEDPREMIUM = new RealColumn("ImpliedPremium", "real", this, NULL);
    public RealColumn C_DISPERSION = new RealColumn("Dispersion", "real", this, NULL);


}

