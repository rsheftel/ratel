package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class PnlBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PnlBase T_PNL = new PnlBase("Pnlbase");

    public PnlBase(String alias) { super("PerformanceDB..Pnl", alias); }

    public IntColumn C_SOURCE_ID = new IntColumn("source_id", "int", this, NOT_NULL);
    public IntColumn C_TAG_ID = new IntColumn("tag_id", "int", this, NOT_NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("startDate", "datetime", this, NOT_NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("endDate", "datetime", this, NOT_NULL);
    public RealColumn C_PNL = new RealColumn("pnl", "real", this, NOT_NULL);


}

