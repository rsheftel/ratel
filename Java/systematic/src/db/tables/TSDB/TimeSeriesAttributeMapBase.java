package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesAttributeMapBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesAttributeMapBase T_TIME_SERIES_ATTRIBUTE_MAP = new TimeSeriesAttributeMapBase("time_series_attribute_mapbase");

    public TimeSeriesAttributeMapBase(String alias) { super("TSDB..time_series_attribute_map", alias); }

    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public IntColumn C_ATTRIBUTE_ID = new IntColumn("attribute_id", "int", this, NOT_NULL);
    public IntColumn C_ATTRIBUTE_VALUE_ID = new IntColumn("attribute_value_id", "int", this, NOT_NULL);


}

