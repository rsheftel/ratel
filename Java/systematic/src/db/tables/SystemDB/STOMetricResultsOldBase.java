package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class STOMetricResultsOldBase extends Table {

    private static final long serialVersionUID = 1L;    public static final STOMetricResultsOldBase T_STOMETRICRESULTSOLD = new STOMetricResultsOldBase("STOMetricResultsOldbase");

    public STOMetricResultsOldBase(String alias) { super("SystemDB..STOMetricResultsOld", alias); }

    public IntColumn C_SYSTEMID = new IntColumn("systemId", "int", this, NOT_NULL);
    public NvarcharColumn C_MARKET = new NvarcharColumn("market", "nvarchar(50)", this, NOT_NULL);
    public IntColumn C_RUN = new IntColumn("run", "int", this, NOT_NULL);
    public NvarcharColumn C_METRIC = new NvarcharColumn("metric", "nvarchar(200)", this, NOT_NULL);
    public FloatColumn C_VALUE = new FloatColumn("value", "float(53)", this, NOT_NULL);


}

