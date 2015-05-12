package db.tables.TSDB;

import db.*;
import db.columns.*;

public class DtpropertiesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final DtpropertiesBase T_DTPROPERTIES = new DtpropertiesBase("dtpropertiesbase");

    public DtpropertiesBase(String alias) { super("TSDB..dtproperties", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public IntColumn C_OBJECTID = new IntColumn("objectid", "int", this, NULL);
    public VarcharColumn C_PROPERTY = new VarcharColumn("property", "varchar(64)", this, NOT_NULL);
    public VarcharColumn C_VALUE = new VarcharColumn("value", "varchar(255)", this, NULL);
    public NvarcharColumn C_UVALUE = new NvarcharColumn("uvalue", "nvarchar(255)", this, NULL);
    public ImageColumn C_LVALUE = new ImageColumn("lvalue", "image", this, NULL);
    public IntColumn C_VERSION = new IntColumn("version", "int", this, NOT_NULL);


}

