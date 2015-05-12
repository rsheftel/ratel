package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class JobBase extends Table {

    private static final long serialVersionUID = 1L;    public static final JobBase T_JOB = new JobBase("jobbase");

    public JobBase(String alias) { super("ScheduleDB..job", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_ACTION = new NvarcharColumn("action", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_DEADLINE_TIME = new NvarcharColumn("deadline_time", "nvarchar(8)", this, NOT_NULL);
    public NvarcharColumn C_RECIPIENTS = new NvarcharColumn("recipients", "nvarchar(1000)", this, NOT_NULL);
    public NvarcharColumn C_FINANCIAL_CENTER = new NvarcharColumn("financial_center", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_LAST_STATUS_UPDATE = new DatetimeColumn("last_status_update", "datetime", this, NULL);
    public NvarcharColumn C_STATUS = new NvarcharColumn("status", "nvarchar(50)", this, NOT_NULL);


}

