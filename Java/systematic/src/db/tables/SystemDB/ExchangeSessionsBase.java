package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ExchangeSessionsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ExchangeSessionsBase T_EXCHANGESESSIONS = new ExchangeSessionsBase("ExchangeSessionsbase");

    public ExchangeSessionsBase(String alias) { super("SystemDB..ExchangeSessions", alias); }

    public NvarcharColumn C_EXCHANGE = new NvarcharColumn("Exchange", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NcharColumn C_OPENTIME = new NcharColumn("OpenTime", "nchar(8)", this, NOT_NULL);
    public NcharColumn C_CLOSETIME = new NcharColumn("CloseTime", "nchar(8)", this, NOT_NULL);
    public IntColumn C_PROCESSCLOSEORDERSOFFSETSECONDS = new IntColumn("ProcessCloseOrdersOffsetSeconds", "int", this, NULL);
    public NvarcharColumn C_TIMEZONE = new NvarcharColumn("TimeZone", "nvarchar(200)", this, NOT_NULL);


}

