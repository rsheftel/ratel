package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class SECURITYNAMEBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SECURITYNAMEBase T_SECURITY_NAME = new SECURITYNAMEBase("SECURITY_NAMEbase");

    public SECURITYNAMEBase(String alias) { super("IvyDB..SECURITY_NAME", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public CharColumn C_CUSIP = new CharColumn("CUSIP", "char(8)", this, NULL);
    public CharColumn C_TICKER = new CharColumn("Ticker", "char(6)", this, NULL);
    public CharColumn C_CLASS = new CharColumn("Class", "char(1)", this, NULL);
    public CharColumn C_ISSUERDESCRIPTION = new CharColumn("IssuerDescription", "char(28)", this, NULL);
    public CharColumn C_ISSUEDESCRIPTION = new CharColumn("IssueDescription", "char(20)", this, NULL);
    public CharColumn C_SIC = new CharColumn("SIC", "char(4)", this, NULL);


}

