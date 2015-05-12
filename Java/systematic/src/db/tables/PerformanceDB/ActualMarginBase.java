package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class ActualMarginBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ActualMarginBase T_ACTUALMARGIN = new ActualMarginBase("ActualMarginbase");

    public ActualMarginBase(String alias) { super("PerformanceDB..ActualMargin", alias); }

    public IntColumn C_FIRMPRODUCTID = new IntColumn("FirmProductId", "int", this, NOT_NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("StartDate", "datetime", this, NOT_NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("EndDate", "datetime", this, NULL);
    public FloatColumn C_VALUE = new FloatColumn("Value", "float(53)", this, NOT_NULL);


}

