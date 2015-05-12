package db.tables.TSDB;

import db.*;
import db.columns.*;

public class FuturesOptionDetailsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FuturesOptionDetailsBase T_FUTURES_OPTION_DETAILS = new FuturesOptionDetailsBase("futures_option_detailsbase");

    public FuturesOptionDetailsBase(String alias) { super("TSDB..futures_option_details", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public IntColumn C_FUTURES_ID = new IntColumn("futures_id", "int", this, NOT_NULL);
    public NvarcharColumn C_CONTRACT = new NvarcharColumn("contract", "nvarchar(50)", this, NOT_NULL);
    public IntColumn C_NUM_QUARTERLY = new IntColumn("num_quarterly", "int", this, NOT_NULL);
    public IntColumn C_NUM_MONTHLY = new IntColumn("num_monthly", "int", this, NOT_NULL);
    public NvarcharColumn C_EXPIRY_QUARTERLY = new NvarcharColumn("expiry_quarterly", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_EXPIRY_MONTHLY = new NvarcharColumn("expiry_monthly", "nvarchar(255)", this, NOT_NULL);
    public IntColumn C_MONTHS_LAG = new IntColumn("months_lag", "int", this, NOT_NULL);


}

