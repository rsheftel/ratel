package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class SourceBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SourceBase T_SOURCE = new SourceBase("Sourcebase");

    public SourceBase(String alias) { super("PerformanceDB..Source", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(100)", this, NOT_NULL);
    public NvarcharColumn C_INTERVAL = new NvarcharColumn("interval", "nvarchar(50)", this, NOT_NULL);


}

