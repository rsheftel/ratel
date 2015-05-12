package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesMigrationCompleteBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesMigrationCompleteBase T_TIME_SERIES_MIGRATION_COMPLETE = new TimeSeriesMigrationCompleteBase("time_series_migration_completebase");

    public TimeSeriesMigrationCompleteBase(String alias) { super("TSDB..time_series_migration_complete", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);


}

