package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class VersionBase extends Table {

    private static final long serialVersionUID = 1L;    public static final VersionBase T_VERSION = new VersionBase("Versionbase");

    public VersionBase(String alias) { super("SystemDB..Version", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public IntIdentityColumn C_ID = new IntIdentityColumn("ID", "int identity", this, NOT_NULL);


}

