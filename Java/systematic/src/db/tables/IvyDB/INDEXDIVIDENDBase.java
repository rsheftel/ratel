package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class INDEXDIVIDENDBase extends Table {

    private static final long serialVersionUID = 1L;    public static final INDEXDIVIDENDBase T_INDEX_DIVIDEND = new INDEXDIVIDENDBase("INDEX_DIVIDENDbase");

    public INDEXDIVIDENDBase(String alias) { super("IvyDB..INDEX_DIVIDEND", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public RealColumn C_RATE = new RealColumn("Rate", "real", this, NOT_NULL);


}

