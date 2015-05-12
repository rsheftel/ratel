package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MSIVPropertyBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MSIVPropertyBase T_MSIVPROPERTY = new MSIVPropertyBase("MSIVPropertybase");

    public MSIVPropertyBase(String alias) { super("SystemDB..MSIVProperty", alias); }

    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("PV_Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PROPERTY = new NvarcharColumn("Property", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_VALUE = new NvarcharColumn("Value", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_ASOFDATE = new DatetimeColumn("AsOfDate", "datetime", this, NOT_NULL);


}

