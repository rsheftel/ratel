package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class PairWCcyNameBase extends Table {
    private static final long serialVersionUID = 1L;
    public PairWCcyNameBase(String alias) { super("TSDB..#pair_w_ccy_name", alias); }

    public IntIdentityColumn C_CCY_ID = new IntIdentityColumn("ccy_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_CCY_NAME = new VarcharColumn("ccy_name", "varchar", this, NOT_NULL);
    public FloatColumn C_PRECEDENCE = new FloatColumn("precedence", "float", this, NOT_NULL);
    public VarcharColumn C_DESCRIPTION = new VarcharColumn("description", "varchar", this, NOT_NULL);
    public VarcharColumn C_CCY_PAIR_NAME = new VarcharColumn("ccy_pair_name", "varchar", this, NOT_NULL);
    public IntIdentityColumn C_CCY_PAIR_ID = new IntIdentityColumn("ccy_pair_id", "int identity", this, NOT_NULL);

}

