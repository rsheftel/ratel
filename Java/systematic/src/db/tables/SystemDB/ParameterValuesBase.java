package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ParameterValuesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ParameterValuesBase T_PARAMETERVALUES = new ParameterValuesBase("ParameterValuesbase");

    public ParameterValuesBase(String alias) { super("SystemDB..ParameterValues", alias); }

    public NvarcharColumn C_SYSTEM = new NvarcharColumn("System", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_STRATEGY = new NvarcharColumn("Strategy", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PARAMETERNAME = new NvarcharColumn("ParameterName", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PARAMETERVALUE = new NvarcharColumn("ParameterValue", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_ASOFDATE = new DatetimeColumn("AsOfDate", "datetime", this, NOT_NULL);


}

