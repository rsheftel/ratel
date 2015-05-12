package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class CcyTempTestBase extends Table {
    private static final long serialVersionUID = 1L;
    public CcyTempTestBase(String alias) { super("TSDB..#ccy_temp_test", alias); }

    public IntIdentityColumn C_CCY_ID = new IntIdentityColumn("ccy_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_CCY_NAME = new VarcharColumn("ccy_name", "varchar", this, NOT_NULL);
    public FloatColumn C_PRECEDENCE = new FloatColumn("precedence", "float", this, NOT_NULL);
    public VarcharColumn C_DESCRIPTION = new VarcharColumn("description", "varchar", this, NOT_NULL);

}

