package db.tables.TSDB;

import db.*;
import db.columns.*;

public class FuturesDetailsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FuturesDetailsBase T_FUTURES_DETAILS = new FuturesDetailsBase("futures_detailsbase");

    public FuturesDetailsBase(String alias) { super("TSDB..futures_details", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_CONTRACT = new NvarcharColumn("contract", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_EXCHANGE = new NvarcharColumn("exchange", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_GROUP_NAME = new NvarcharColumn("group_name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_BBG_YELLOW_KEY = new NvarcharColumn("bbg_yellow_key", "nvarchar(10)", this, NOT_NULL);
    public IntColumn C_NUM_QUARTERLY = new IntColumn("num_quarterly", "int", this, NOT_NULL);
    public IntColumn C_NUM_MONTHLY = new IntColumn("num_monthly", "int", this, NOT_NULL);
    public NvarcharColumn C_EXPIRY_TYPE = new NvarcharColumn("expiry_type", "nvarchar(255)", this, NOT_NULL);


}

