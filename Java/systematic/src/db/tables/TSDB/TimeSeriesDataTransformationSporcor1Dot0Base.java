package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesDataTransformationSporcor1Dot0Base extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesDataTransformationSporcor1Dot0Base T_TIME_SERIES_DATA_TRANSFORMATION_SPORCOR_1DOT0 = new TimeSeriesDataTransformationSporcor1Dot0Base("time_series_data_transformation_sporcor_1dot0base");

    public TimeSeriesDataTransformationSporcor1Dot0Base(String alias) { super("TSDB..time_series_data_transformation_sporcor_1dot0", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public IntColumn C_DATA_SOURCE_ID = new IntColumn("data_source_id", "int", this, NOT_NULL);
    public DatetimeColumn C_OBSERVATION_TIME = new DatetimeColumn("observation_time", "datetime", this, NOT_NULL);
    public FloatColumn C_OBSERVATION_VALUE = new FloatColumn("observation_value", "float(53)", this, NOT_NULL);


}

