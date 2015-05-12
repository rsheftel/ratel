package db.tables.TSDB;

import db.*;
import db.columns.*;

public class ExpiryDateBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ExpiryDateBase T_EXPIRY_DATE = new ExpiryDateBase("expiry_datebase");

    public ExpiryDateBase(String alias) { super("TSDB..expiry_date", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);


}

