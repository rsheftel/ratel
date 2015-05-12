package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class PATCHVolatilitySurfaceTEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PATCHVolatilitySurfaceTEMPBase T_PATCH_VOLATILITYSURFACE_TEMP = new PATCHVolatilitySurfaceTEMPBase("PATCH_VolatilitySurface_TEMPbase");

    public PATCHVolatilitySurfaceTEMPBase(String alias) { super("IvyDB..PATCH_VolatilitySurface_TEMP", alias); }

    public VarcharColumn C_ACTION = new VarcharColumn("Action", "varchar(1)", this, NOT_NULL);
    public IntColumn C_SECURITYID = new IntColumn("securityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("days", "int", this, NOT_NULL);
    public IntColumn C_DELTA = new IntColumn("delta", "int", this, NOT_NULL);
    public CharColumn C_CALLPUTFLAG = new CharColumn("callPutFlag", "char(1)", this, NULL);
    public RealColumn C_IMPLIEDVOLATILITY = new RealColumn("impliedVolatility", "real", this, NULL);
    public RealColumn C_IMPLIEDSTRIKE = new RealColumn("impliedStrike", "real", this, NULL);
    public RealColumn C_IMPLIEDPREMIUM = new RealColumn("impliedPremium", "real", this, NULL);
    public RealColumn C_DISPERSION = new RealColumn("dispersion", "real", this, NULL);


}

