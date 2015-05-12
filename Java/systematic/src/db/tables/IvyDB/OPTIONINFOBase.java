package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class OPTIONINFOBase extends Table {

    private static final long serialVersionUID = 1L;    public static final OPTIONINFOBase T_OPTION_INFO = new OPTIONINFOBase("OPTION_INFObase");

    public OPTIONINFOBase(String alias) { super("IvyDB..OPTION_INFO", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public CharColumn C_DIVIDENDCONVENTION = new CharColumn("DividendConvention", "char(1)", this, NULL);
    public CharColumn C_EXERCISESTYLE = new CharColumn("ExerciseStyle", "char(1)", this, NULL);
    public IntColumn C_AMSETTLEMENTFLAG = new IntColumn("AMSettlementFlag", "int", this, NULL);


}

