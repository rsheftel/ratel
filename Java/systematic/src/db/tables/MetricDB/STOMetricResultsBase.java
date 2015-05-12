package db.tables.MetricDB;

import db.*;
import db.columns.*;

public class STOMetricResultsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final STOMetricResultsBase T_STOMETRICRESULTS = new STOMetricResultsBase("STOMetricResultsbase");

    public STOMetricResultsBase(String alias) { super("MetricDB..STOMetricResults", alias); }

    public IntColumn C_SYSTEMID = new IntColumn("systemId", "int", this, NOT_NULL);
    public NvarcharColumn C_MARKET = new NvarcharColumn("market", "nvarchar(50)", this, NOT_NULL);
    public IntColumn C_RUN = new IntColumn("run", "int", this, NOT_NULL);
    public NvarcharColumn C_METRIC = new NvarcharColumn("metric", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_VALUE = new NvarcharColumn("value", "nvarchar(50)", this, NOT_NULL);


}

