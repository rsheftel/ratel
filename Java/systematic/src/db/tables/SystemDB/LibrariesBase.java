package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class LibrariesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final LibrariesBase T_LIBRARIES = new LibrariesBase("Librariesbase");

    public LibrariesBase(String alias) { super("SystemDB..Libraries", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PLATFORM = new NvarcharColumn("Platform", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_SOURCE = new NvarcharColumn("Source", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_EXECUTABLE = new NvarcharColumn("Executable", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_DOCUMENTATION = new NvarcharColumn("Documentation", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_OWNER = new NvarcharColumn("Owner", "nvarchar(50)", this, NULL);


}

