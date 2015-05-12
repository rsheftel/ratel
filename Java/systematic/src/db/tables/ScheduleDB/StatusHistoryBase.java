package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class StatusHistoryBase extends Table {

    private static final long serialVersionUID = 1L;    public static final StatusHistoryBase T_STATUS_HISTORY = new StatusHistoryBase("status_historybase");

    public StatusHistoryBase(String alias) { super("ScheduleDB..status_history", alias); }

    public IntIdentityColumn C_RECORD_NUMBER = new IntIdentityColumn("record_number", "int identity", this, NOT_NULL);
    public NvarcharColumn C_TYPE = new NvarcharColumn("type", "nvarchar(50)", this, NOT_NULL);
    public IntColumn C_ID = new IntColumn("id", "int", this, NOT_NULL);
    public DatetimeColumn C_AS_OF = new DatetimeColumn("as_of", "datetime", this, NOT_NULL);
    public DatetimeColumn C_UPDATE_TIME = new DatetimeColumn("update_time", "datetime", this, NOT_NULL);
    public NvarcharColumn C_STATUS = new NvarcharColumn("status", "nvarchar(50)", this, NOT_NULL);


}

