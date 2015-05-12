package db.tables.TSDB;

import db.*;
import db.columns.*;

public class IssueDateBase extends Table {

    private static final long serialVersionUID = 1L;    public static final IssueDateBase T_ISSUE_DATE = new IssueDateBase("issue_datebase");

    public IssueDateBase(String alias) { super("TSDB..issue_date", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);


}

