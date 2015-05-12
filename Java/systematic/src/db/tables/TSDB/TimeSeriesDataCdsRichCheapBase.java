package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesDataCdsRichCheapBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesDataCdsRichCheapBase T_TIME_SERIES_DATA_CDS_RICH_CHEAP = new TimeSeriesDataCdsRichCheapBase("time_series_data_cds_rich_cheapbase");

    public TimeSeriesDataCdsRichCheapBase(String alias) { super("TSDB..time_series_data_cds_rich_cheap", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public IntColumn C_DATA_SOURCE_ID = new IntColumn("data_source_id", "int", this, NOT_NULL);
    public DatetimeColumn C_OBSERVATION_TIME = new DatetimeColumn("observation_time", "datetime", this, NOT_NULL);
    public FloatColumn C_OBSERVATION_VALUE = new FloatColumn("observation_value", "float(53)", this, NOT_NULL);


}

