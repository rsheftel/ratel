package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class CreditingRatesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CreditingRatesBase T_CREDITINGRATES = new CreditingRatesBase("CreditingRatesbase");

    public CreditingRatesBase(String alias) { super("PerformanceDB..CreditingRates", alias); }

    public IntColumn C_FIRMPRODUCTID = new IntColumn("FirmProductId", "int", this, NOT_NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("StartDate", "datetime", this, NOT_NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("EndDate", "datetime", this, NULL);
    public FloatColumn C_DEBITRATE = new FloatColumn("DebitRate", "float(53)", this, NOT_NULL);
    public FloatColumn C_CREDITRATE = new FloatColumn("CreditRate", "float(53)", this, NOT_NULL);


}

