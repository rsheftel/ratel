package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TbaSettlementBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TbaSettlementBase T_TBA_SETTLEMENT = new TbaSettlementBase("tba_settlementbase");

    public TbaSettlementBase(String alias) { super("TSDB..tba_settlement", alias); }

    public IntColumn C_TBA_ID = new IntColumn("tba_id", "int", this, NOT_NULL);
    public NcharColumn C_YEARMONTH = new NcharColumn("yearmonth", "nchar(6)", this, NOT_NULL);
    public DatetimeColumn C_NOTIFICATION_DATE = new DatetimeColumn("notification_date", "datetime", this, NOT_NULL);
    public DatetimeColumn C_SETTLEMENT_DATE = new DatetimeColumn("settlement_date", "datetime", this, NOT_NULL);


}

