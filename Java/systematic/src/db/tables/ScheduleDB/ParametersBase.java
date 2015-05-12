package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class ParametersBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ParametersBase T_PARAMETERS = new ParametersBase("parametersbase");

    public ParametersBase(String alias) { super("ScheduleDB..parameters", alias); }

    public IntColumn C_JOB_ID = new IntColumn("job_id", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_VALUE = new NvarcharColumn("value", "nvarchar(255)", this, NOT_NULL);


}

