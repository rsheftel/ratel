package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class SystemBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SystemBase T_SYSTEM = new SystemBase("Systembase");

    public SystemBase(String alias) { super("SystemDB..System", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_DOCUMENTATION = new NvarcharColumn("Documentation", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_OWNER = new NvarcharColumn("Owner", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_QCLASSNAME = new NvarcharColumn("QClassName", "nvarchar(2000)", this, NULL);
    public RealColumn C_TYPICALMARKETBARSPERSECOND = new RealColumn("TypicalMarketBarsPerSecond", "real", this, NULL);


}

