package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MSIVParameterValuesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MSIVParameterValuesBase T_MSIVPARAMETERVALUES = new MSIVParameterValuesBase("MSIVParameterValuesbase");

    public MSIVParameterValuesBase(String alias) { super("SystemDB..MSIVParameterValues", alias); }

    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("PV_Name", "nvarchar(50)", this, NOT_NULL);


}

