package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MarketSessionsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MarketSessionsBase T_MARKETSESSIONS = new MarketSessionsBase("MarketSessionsbase");

    public MarketSessionsBase(String alias) { super("SystemDB..MarketSessions", alias); }

    public NvarcharColumn C_MARKET = new NvarcharColumn("Market", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NcharColumn C_OPENTIME = new NcharColumn("OpenTime", "nchar(8)", this, NOT_NULL);
    public NcharColumn C_CLOSETIME = new NcharColumn("CloseTime", "nchar(8)", this, NOT_NULL);
    public IntColumn C_PROCESSCLOSEORDERSOFFSETSECONDS = new IntColumn("ProcessCloseOrdersOffsetSeconds", "int", this, NULL);
    public NvarcharColumn C_TIMEZONE = new NvarcharColumn("TimeZone", "nvarchar(200)", this, NOT_NULL);


}

