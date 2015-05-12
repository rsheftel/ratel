package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CcyBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CcyBase T_CCY = new CcyBase("ccybase");

    public CcyBase(String alias) { super("TSDB..ccy", alias); }

    public IntIdentityColumn C_CCY_ID = new IntIdentityColumn("ccy_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_CCY_NAME = new VarcharColumn("ccy_name", "varchar(200)", this, NOT_NULL);
    public FloatColumn C_PRECEDENCE = new FloatColumn("precedence", "float(53)", this, NOT_NULL);
    public VarcharColumn C_DESCRIPTION = new VarcharColumn("description", "varchar(200)", this, NULL);


}

