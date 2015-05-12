package db.tables.BloombergFeedDB;

import db.*;
import db.columns.*;

public class BloombergDataBase extends Table {

    private static final long serialVersionUID = 1L;    public static final BloombergDataBase T_BLOOMBERGDATA = new BloombergDataBase("BloombergDatabase");

    public BloombergDataBase(String alias) { super("BloombergFeedDB..BloombergData", alias); }

    public IntIdentityColumn C_IDBBDATA = new IntIdentityColumn("idBBData", "int identity", this, NOT_NULL);
    public VarcharColumn C_TICKERBB = new VarcharColumn("tickerBB", "varchar(50)", this, NOT_NULL);
    public VarcharColumn C_FIELDBB = new VarcharColumn("fieldBB", "varchar(50)", this, NOT_NULL);
    public VarcharColumn C_NAMETIMESERIES = new VarcharColumn("nameTimeSeries", "varchar(200)", this, NOT_NULL);


}

