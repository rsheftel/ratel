package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class OpenInterestBase extends Table {

    private static final long serialVersionUID = 1L;    public static final OpenInterestBase T_OPENINTEREST = new OpenInterestBase("OpenInterestbase");

    public OpenInterestBase(String alias) { super("IvyDB..OpenInterest", alias); }

    public IntColumn C_SECURITYID = new IntColumn("securityID", "int", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);
    public CharColumn C_ROOT = new CharColumn("root", "char(5)", this, NOT_NULL);
    public CharColumn C_SUFFIX = new CharColumn("suffix", "char(2)", this, NOT_NULL);
    public IntColumn C_OPENINTEREST = new IntColumn("openInterest", "int", this, NOT_NULL);


}

