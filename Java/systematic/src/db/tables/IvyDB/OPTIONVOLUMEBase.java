package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class OPTIONVOLUMEBase extends Table {

    private static final long serialVersionUID = 1L;    public static final OPTIONVOLUMEBase T_OPTION_VOLUME = new OPTIONVOLUMEBase("OPTION_VOLUMEbase");

    public OPTIONVOLUMEBase(String alias) { super("IvyDB..OPTION_VOLUME", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public CharColumn C_CALLPUT = new CharColumn("CallPut", "char(1)", this, NOT_NULL);
    public IntColumn C_VOLUME = new IntColumn("Volume", "int", this, NOT_NULL);
    public IntColumn C_OPENINTEREST = new IntColumn("OpenInterest", "int", this, NOT_NULL);


}

