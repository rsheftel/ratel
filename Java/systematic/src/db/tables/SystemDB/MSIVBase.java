package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MSIVBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MSIVBase T_MSIV = new MSIVBase("MSIVbase");

    public MSIVBase(String alias) { super("SystemDB..MSIV", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_MARKET = new NvarcharColumn("Market", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_SYSTEM = new NvarcharColumn("System", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_INTERVAL = new NvarcharColumn("Interval", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_VERSION = new NvarcharColumn("Version", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_INSAMPLESTOID = new NvarcharColumn("InSampleSTOid", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_OUTSAMPLESTOID = new NvarcharColumn("OutSampleSTOid", "nvarchar(50)", this, NULL);


}

