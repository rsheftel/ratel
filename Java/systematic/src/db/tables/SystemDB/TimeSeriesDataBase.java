package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class TimeSeriesDataBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesDataBase T_TIME_SERIES_DATA = new TimeSeriesDataBase("Time_series_database");

    public TimeSeriesDataBase(String alias) { super("SystemDB..Time_series_data", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_LONG_NAME = new NvarcharColumn("Long_name", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_SUBSECTOR = new NvarcharColumn("SubSector", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_CURRENCY = new NvarcharColumn("Currency", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_TYPE = new NvarcharColumn("Type", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_EXCHANGE = new NvarcharColumn("Exchange", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_EXPIRY = new NvarcharColumn("Expiry", "nvarchar(50)", this, NULL);
    public BitColumn C_OPTIONFLAG = new BitColumn("OptionFlag", "bit", this, NULL);
    public BitColumn C_TEMPLATEFLAG = new BitColumn("TemplateFlag", "bit", this, NULL);
    public NvarcharColumn C_HISTDAILY = new NvarcharColumn("HistDaily", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_HISTINTRADAY = new NvarcharColumn("HistIntraday", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_HISTTICK = new NvarcharColumn("HistTick", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_TODAYTICK = new NvarcharColumn("TodayTick", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_LIVE = new NvarcharColumn("Live", "nvarchar(50)", this, NULL);
    public BitColumn C_USETSDBADJUSTMENT = new BitColumn("UseTSDBAdjustment", "bit", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(1073741823)", this, NULL);
    public NvarcharColumn C_OWNER = new NvarcharColumn("Owner", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_ASOF = new DatetimeColumn("AsOf", "datetime", this, NULL);


}

