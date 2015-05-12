package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class RequiredMarginBase extends Table {

    private static final long serialVersionUID = 1L;    public static final RequiredMarginBase T_REQUIREDMARGIN = new RequiredMarginBase("RequiredMarginbase");

    public RequiredMarginBase(String alias) { super("PerformanceDB..RequiredMargin", alias); }

    public IntColumn C_FIRMPRODUCTID = new IntColumn("FirmProductId", "int", this, NOT_NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("StartDate", "datetime", this, NOT_NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("EndDate", "datetime", this, NULL);
    public FloatColumn C_VALUE = new FloatColumn("Value", "float(53)", this, NOT_NULL);


}

