package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MarketBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MarketBase T_MARKET = new MarketBase("Marketbase");

    public MarketBase(String alias) { super("SystemDB..Market", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_WEIGHTING_FUNCTION = new NvarcharColumn("Weighting_function", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_REBALANCE_FUNCTION = new NvarcharColumn("Rebalance_function", "nvarchar(2000)", this, NULL);
    public IntIdentityColumn C_ID = new IntIdentityColumn("ID", "int identity", this, NOT_NULL);
    public FloatColumn C_BIGPOINTVALUE = new FloatColumn("BigPointValue", "float(53)", this, NULL);
    public FloatColumn C_SLIPPAGE = new FloatColumn("Slippage", "float(53)", this, NULL);
    public NvarcharColumn C_SLIPPAGECALCULATOR = new NvarcharColumn("SlippageCalculator", "nvarchar(255)", this, NULL);


}

