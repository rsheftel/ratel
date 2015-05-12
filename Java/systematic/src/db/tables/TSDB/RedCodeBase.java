package db.tables.TSDB;

import db.*;
import db.columns.*;

public class RedCodeBase extends Table {

    private static final long serialVersionUID = 1L;    public static final RedCodeBase T_RED_CODE = new RedCodeBase("red_codebase");

    public RedCodeBase(String alias) { super("TSDB..red_code", alias); }

    public NumericIdentityColumn C_RED_CODE_ID = new NumericIdentityColumn("red_code_id", "numeric() identity", this, NOT_NULL);
    public VarcharColumn C_RED_CODE = new VarcharColumn("red_code", "varchar(32)", this, NOT_NULL);


}

