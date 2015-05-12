package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesDataTransformationRollingRegression4Dot1Base extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesDataTransformationRollingRegression4Dot1Base T_TIME_SERIES_DATA_TRANSFORMATION_ROLLING_REGRESSION_4DOT1 = new TimeSeriesDataTransformationRollingRegression4Dot1Base("time_series_data_transformation_rolling_regression_4dot1base");

    public TimeSeriesDataTransformationRollingRegression4Dot1Base(String alias) { super("TSDB..time_series_data_transformation_rolling_regression_4dot1", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public IntColumn C_DATA_SOURCE_ID = new IntColumn("data_source_id", "int", this, NOT_NULL);
    public DatetimeColumn C_OBSERVATION_TIME = new DatetimeColumn("observation_time", "datetime", this, NOT_NULL);
    public FloatColumn C_OBSERVATION_VALUE = new FloatColumn("observation_value", "float(53)", this, NOT_NULL);


}

