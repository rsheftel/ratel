package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class SectorsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SectorsBase T_SECTORS = new SectorsBase("Sectorsbase");

    public SectorsBase(String alias) { super("SystemDB..Sectors", alias); }

    public NvarcharColumn C_SECTOR = new NvarcharColumn("Sector", "nvarchar(50)", this, NOT_NULL);


}

