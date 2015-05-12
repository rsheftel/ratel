package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class SecurityStageBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SecurityStageBase T_SECURITYSTAGE = new SecurityStageBase("SecurityStagebase");

    public SecurityStageBase(String alias) { super("IvyDB..SecurityStage", alias); }

    public IntColumn C_SECURITYID = new IntColumn("securityID", "int", this, NOT_NULL);
    public CharColumn C_CUSIP = new CharColumn("cusip", "char(8)", this, NOT_NULL);
    public CharColumn C_TICKER = new CharColumn("ticker", "char(6)", this, NULL);
    public CharColumn C_SIC = new CharColumn("sic", "char(4)", this, NULL);
    public CharColumn C_INDEXFLAG = new CharColumn("indexFlag", "char(1)", this, NOT_NULL);
    public IntColumn C_EXCHANGEDESIGNATOR = new IntColumn("exchangeDesignator", "int", this, NULL);
    public CharColumn C_CLASS = new CharColumn("class", "char(1)", this, NULL);
    public CharColumn C_ISSUETYPE = new CharColumn("issueType", "char(1)", this, NULL);
    public CharColumn C_INDUSTRYGROUP = new CharColumn("industryGroup", "char(3)", this, NULL);


}

