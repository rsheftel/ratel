package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MarketTickersBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MarketTickersBase T_MARKETTICKERS = new MarketTickersBase("MarketTickersbase");

    public MarketTickersBase(String alias) { super("SystemDB..MarketTickers", alias); }

    public NvarcharColumn C_MARKET = new NvarcharColumn("Market", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_BLOOMBERG = new NvarcharColumn("Bloomberg", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_YELLOWKEY = new NvarcharColumn("YellowKey", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_TSDB = new NvarcharColumn("TSDB", "nvarchar(200)", this, NULL);
    public DatetimeColumn C_TIMESTAMP = new DatetimeColumn("Timestamp", "datetime", this, NOT_NULL);
    public NvarcharColumn C_BLOOMBERGROOT = new NvarcharColumn("BloombergRoot", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_RICROOT = new NvarcharColumn("RICRoot", "nvarchar(50)", this, NULL);


}

