package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class MaxSeriesVersionBase extends Table {
    private static final long serialVersionUID = 1L;
    public static final MaxSeriesVersionBase T_MAXSERIESVERSION = new MaxSeriesVersionBase("#maxSeriesVersionbase");

    public MaxSeriesVersionBase(String alias) { super("TSDB..#maxSeriesVersion", alias); }

    public VarcharColumn C_NAME = new VarcharColumn("name", "varchar", this, NOT_NULL);
    public IntColumn C_SERIES = new IntColumn("series", "int", this, NOT_NULL);
    public IntColumn C_VERSION = new IntColumn("version", "int", this, NOT_NULL);

}

