package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ExecutionConfigurationBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ExecutionConfigurationBase T_EXECUTIONCONFIGURATION = new ExecutionConfigurationBase("ExecutionConfigurationbase");

    public ExecutionConfigurationBase(String alias) { super("SystemDB..ExecutionConfiguration", alias); }

    public NvarcharColumn C_TYPE = new NvarcharColumn("Type", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PLATFORM = new NvarcharColumn("Platform", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_ROUTE = new NvarcharColumn("Route", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_SETBY = new NvarcharColumn("SetBy", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_ASOF = new DatetimeColumn("AsOf", "datetime", this, NOT_NULL);


}

