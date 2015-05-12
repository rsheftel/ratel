package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class RateDataBase extends Table {

    private static final long serialVersionUID = 1L;    public static final RateDataBase T_RATEDATA = new RateDataBase("RateDatabase");

    public RateDataBase(String alias) { super("PerformanceDB..RateData", alias); }

    public IntIdentityColumn C_RATEDATAID = new IntIdentityColumn("rateDataId", "int identity", this, NOT_NULL);
    public IntColumn C_RATEDEFID = new IntColumn("rateDefId", "int", this, NOT_NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("startDate", "datetime", this, NOT_NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("endDate", "datetime", this, NOT_NULL);
    public FloatColumn C_VALUE = new FloatColumn("value", "float(53)", this, NOT_NULL);


}

