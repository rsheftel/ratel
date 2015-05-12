package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesDataMbsTbaFnciBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesDataMbsTbaFnciBase T_TIME_SERIES_DATA_MBS_TBA_FNCI = new TimeSeriesDataMbsTbaFnciBase("time_series_data_mbs_tba_fncibase");

    public TimeSeriesDataMbsTbaFnciBase(String alias) { super("TSDB..time_series_data_mbs_tba_fnci", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public IntColumn C_DATA_SOURCE_ID = new IntColumn("data_source_id", "int", this, NOT_NULL);
    public DatetimeColumn C_OBSERVATION_TIME = new DatetimeColumn("observation_time", "datetime", this, NOT_NULL);
    public FloatColumn C_OBSERVATION_VALUE = new FloatColumn("observation_value", "float(53)", this, NOT_NULL);


}

