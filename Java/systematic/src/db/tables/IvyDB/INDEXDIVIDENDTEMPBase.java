package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class INDEXDIVIDENDTEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final INDEXDIVIDENDTEMPBase T_INDEX_DIVIDEND_TEMP = new INDEXDIVIDENDTEMPBase("INDEX_DIVIDEND_TEMPbase");

    public INDEXDIVIDENDTEMPBase(String alias) { super("IvyDB..INDEX_DIVIDEND_TEMP", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public RealColumn C_RATE = new RealColumn("Rate", "real", this, NOT_NULL);


}

