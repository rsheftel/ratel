package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class ZEROCURVETEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ZEROCURVETEMPBase T_ZERO_CURVE_TEMP = new ZEROCURVETEMPBase("ZERO_CURVE_TEMPbase");

    public ZEROCURVETEMPBase(String alias) { super("IvyDB..ZERO_CURVE_TEMP", alias); }

    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public IntColumn C_DAYS = new IntColumn("Days", "int", this, NOT_NULL);
    public RealColumn C_RATE = new RealColumn("Rate", "real", this, NOT_NULL);


}

