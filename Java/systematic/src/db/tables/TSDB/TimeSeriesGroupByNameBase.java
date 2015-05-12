package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesGroupByNameBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesGroupByNameBase T_TIME_SERIES_GROUP_BY_NAME = new TimeSeriesGroupByNameBase("time_series_group_by_namebase");

    public TimeSeriesGroupByNameBase(String alias) { super("TSDB..time_series_group_by_name", alias); }

    public IntColumn C_TIME_SERIES_GROUP_ID = new IntColumn("time_series_group_id", "int", this, NOT_NULL);
    public NvarcharColumn C_TIME_SERIES_NAME = new NvarcharColumn("time_series_name", "nvarchar(200)", this, NOT_NULL);


}

