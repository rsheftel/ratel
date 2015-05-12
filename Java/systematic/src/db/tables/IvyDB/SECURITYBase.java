package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class SECURITYBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SECURITYBase T_SECURITY = new SECURITYBase("SECURITYbase");

    public SECURITYBase(String alias) { super("IvyDB..SECURITY", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public CharColumn C_CUSIP = new CharColumn("CUSIP", "char(8)", this, NULL);
    public CharColumn C_TICKER = new CharColumn("Ticker", "char(6)", this, NULL);
    public CharColumn C_SIC = new CharColumn("SIC", "char(4)", this, NULL);
    public CharColumn C_INDEXFLAG = new CharColumn("IndexFlag", "char(1)", this, NULL);
    public IntColumn C_EXCHANGEFLAGS = new IntColumn("ExchangeFlags", "int", this, NOT_NULL);
    public CharColumn C_CLASS = new CharColumn("Class", "char(1)", this, NULL);
    public CharColumn C_ISSUETYPE = new CharColumn("IssueType", "char(1)", this, NULL);
    public CharColumn C_INDUSTRYGROUP = new CharColumn("IndustryGroup", "char(3)", this, NULL);


}

