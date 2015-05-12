package db.tables.TSDB;

import db.*;
import db.columns.*;

public class SettleDateBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SettleDateBase T_SETTLE_DATE = new SettleDateBase("settle_datebase");

    public SettleDateBase(String alias) { super("TSDB..settle_date", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);


}

