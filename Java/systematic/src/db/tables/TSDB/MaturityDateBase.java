package db.tables.TSDB;

import db.*;
import db.columns.*;

public class MaturityDateBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MaturityDateBase T_MATURITY_DATE = new MaturityDateBase("maturity_datebase");

    public MaturityDateBase(String alias) { super("TSDB..maturity_date", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);


}

