package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class DependencyParametersBase extends Table {

    private static final long serialVersionUID = 1L;    public static final DependencyParametersBase T_DEPENDENCY_PARAMETERS = new DependencyParametersBase("dependency_parametersbase");

    public DependencyParametersBase(String alias) { super("ScheduleDB..dependency_parameters", alias); }

    public IntColumn C_DEPENDENCY_ID = new IntColumn("dependency_id", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_VALUE = new NvarcharColumn("value", "nvarchar(255)", this, NOT_NULL);


}

