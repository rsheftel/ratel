package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MarketConstituentBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MarketConstituentBase T_MARKET_CONSTITUENT = new MarketConstituentBase("Market_constituentbase");

    public MarketConstituentBase(String alias) { super("SystemDB..Market_constituent", alias); }

    public NvarcharColumn C_MARKET_NAME = new NvarcharColumn("Market_name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_REAL_SECURITY_NAME = new NvarcharColumn("Real_security_name", "nvarchar(50)", this, NOT_NULL);


}

