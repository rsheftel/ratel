package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CcyPairBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CcyPairBase T_CCY_PAIR = new CcyPairBase("ccy_pairbase");

    public CcyPairBase(String alias) { super("TSDB..ccy_pair", alias); }

    public IntIdentityColumn C_CCY_PAIR_ID = new IntIdentityColumn("ccy_pair_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_CCY_PAIR_NAME = new VarcharColumn("ccy_pair_name", "varchar(200)", this, NOT_NULL);
    public IntColumn C_CCY_ID1 = new IntColumn("ccy_id1", "int", this, NOT_NULL);
    public IntColumn C_CCY_ID2 = new IntColumn("ccy_id2", "int", this, NOT_NULL);
    public BitColumn C_IS_ACTIVE = new BitColumn("is_active", "bit", this, NOT_NULL);


}

