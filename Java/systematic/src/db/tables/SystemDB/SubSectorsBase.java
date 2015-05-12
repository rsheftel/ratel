package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class SubSectorsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SubSectorsBase T_SUBSECTORS = new SubSectorsBase("SubSectorsbase");

    public SubSectorsBase(String alias) { super("SystemDB..SubSectors", alias); }

    public NvarcharColumn C_SUBSECTOR = new NvarcharColumn("SubSector", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_SECTOR = new NvarcharColumn("Sector", "nvarchar(50)", this, NULL);


}

