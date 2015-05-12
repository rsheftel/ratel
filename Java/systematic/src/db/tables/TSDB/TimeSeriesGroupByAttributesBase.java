package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesGroupByAttributesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesGroupByAttributesBase T_TIME_SERIES_GROUP_BY_ATTRIBUTES = new TimeSeriesGroupByAttributesBase("time_series_group_by_attributesbase");

    public TimeSeriesGroupByAttributesBase(String alias) { super("TSDB..time_series_group_by_attributes", alias); }

    public IntColumn C_TIME_SERIES_GROUP_ID = new IntColumn("time_series_group_id", "int", this, NOT_NULL);
    public VarcharColumn C_ATTRIBUTE_NAME = new VarcharColumn("attribute_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_ATTRIBUTE_VALUE_NAME = new VarcharColumn("attribute_value_name", "varchar(200)", this, NOT_NULL);


}

