package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesGroupBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesGroupBase T_TIME_SERIES_GROUP = new TimeSeriesGroupBase("time_series_groupbase");

    public TimeSeriesGroupBase(String alias) { super("TSDB..time_series_group", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_TYPE = new NvarcharColumn("type", "nvarchar(50)", this, NOT_NULL);


}

