package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class PlatformsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PlatformsBase T_PLATFORMS = new PlatformsBase("Platformsbase");

    public PlatformsBase(String alias) { super("SystemDB..Platforms", alias); }

    public NvarcharColumn C_PLATFORM = new NvarcharColumn("Platform", "nvarchar(50)", this, NOT_NULL);


}

