package db.tables.TSDB;

import db.*;
import db.columns.*;

public class SysdiagramsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SysdiagramsBase T_SYSDIAGRAMS = new SysdiagramsBase("sysdiagramsbase");

    public SysdiagramsBase(String alias) { super("TSDB..sysdiagrams", alias); }

    public SysnameColumn C_NAME = new SysnameColumn("name", "sysname", this, NOT_NULL);
    public IntColumn C_PRINCIPAL_ID = new IntColumn("principal_id", "int", this, NOT_NULL);
    public IntIdentityColumn C_DIAGRAM_ID = new IntIdentityColumn("diagram_id", "int identity", this, NOT_NULL);
    public IntColumn C_VERSION = new IntColumn("version", "int", this, NULL);
    public VarbinaryColumn C_DEFINITION = new VarbinaryColumn("definition", "varbinary(2147483647)", this, NULL);


}

