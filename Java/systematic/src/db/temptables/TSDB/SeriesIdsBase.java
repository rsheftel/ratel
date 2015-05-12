package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class SeriesIdsBase extends Table {
    private static final long serialVersionUID = 1L;
    public static final SeriesIdsBase T_SERIES_IDS = new SeriesIdsBase("#series_idsbase");

    public SeriesIdsBase(String alias) { super("TSDB..#series_ids", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);


}

