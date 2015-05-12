package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class SystemDetailsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SystemDetailsBase T_SYSTEMDETAILS = new SystemDetailsBase("SystemDetailsbase");

    public SystemDetailsBase(String alias) { super("SystemDB..SystemDetails", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_SYSTEM_NAME = new NvarcharColumn("system_name", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_VERSION = new NvarcharColumn("version", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_INTERVAL = new NvarcharColumn("interval", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_STO_DIR = new NvarcharColumn("sto_dir", "nvarchar(1024)", this, NOT_NULL);
    public NvarcharColumn C_STO_ID = new NvarcharColumn("sto_id", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("pv_name", "nvarchar(255)", this, NOT_NULL);
    public BitColumn C_RUN_IN_NATIVE_CURRENCY = new BitColumn("run_in_native_currency", "bit", this, NOT_NULL);


}

