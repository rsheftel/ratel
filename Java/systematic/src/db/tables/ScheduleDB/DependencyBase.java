package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class DependencyBase extends Table {

    private static final long serialVersionUID = 1L;    public static final DependencyBase T_DEPENDENCY = new DependencyBase("dependencybase");

    public DependencyBase(String alias) { super("ScheduleDB..dependency", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public IntColumn C_JOB_ID = new IntColumn("job_id", "int", this, NOT_NULL);
    public NvarcharColumn C_DEPENDENCY = new NvarcharColumn("dependency", "nvarchar(255)", this, NOT_NULL);
    public DatetimeColumn C_LAST_STATUS_UPDATE = new DatetimeColumn("last_status_update", "datetime", this, NULL);
    public NvarcharColumn C_STATUS = new NvarcharColumn("status", "nvarchar(50)", this, NOT_NULL);


}

