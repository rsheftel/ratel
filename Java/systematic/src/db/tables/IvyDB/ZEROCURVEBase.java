package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class ZEROCURVEBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ZEROCURVEBase T_ZERO_CURVE = new ZEROCURVEBase("ZERO_CURVEbase");

    public ZEROCURVEBase(String alias) { super("IvyDB..ZERO_CURVE", alias); }

    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("Days", "int", this, NOT_NULL);
    public RealColumn C_RATE = new RealColumn("Rate", "real", this, NOT_NULL);


}

