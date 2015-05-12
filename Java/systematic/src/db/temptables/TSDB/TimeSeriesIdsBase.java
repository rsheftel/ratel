package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesIdsBase extends Table {
    private static final long serialVersionUID = 1L;
    public TimeSeriesIdsBase(String alias) { super("TSDB..#time_series_ids", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);

}

