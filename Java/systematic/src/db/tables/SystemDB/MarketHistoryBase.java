package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MarketHistoryBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MarketHistoryBase T_MARKETHISTORY = new MarketHistoryBase("MarketHistorybase");

    public MarketHistoryBase(String alias) { super("SystemDB..MarketHistory", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("ID", "int identity", this, NOT_NULL);
    public NvarcharColumn C_MARKET = new NvarcharColumn("Market", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("StartDate", "datetime", this, NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("EndDate", "datetime", this, NULL);


}

