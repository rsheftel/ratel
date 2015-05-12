package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TimeSeriesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TimeSeriesBase T_TIME_SERIES = new TimeSeriesBase("time_seriesbase");

    public TimeSeriesBase(String alias) { super("TSDB..time_series", alias); }

    public IntIdentityColumn C_TIME_SERIES_ID = new IntIdentityColumn("time_series_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_TIME_SERIES_NAME = new VarcharColumn("time_series_name", "varchar(200)", this, NOT_NULL);
    public NvarcharColumn C_DATA_TABLE = new NvarcharColumn("data_table", "nvarchar(255)", this, NOT_NULL);


}

