package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class IntervalBase extends Table {

    private static final long serialVersionUID = 1L;    public static final IntervalBase T_INTERVAL = new IntervalBase("Intervalbase");

    public IntervalBase(String alias) { super("SystemDB..Interval", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("ID", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);


}

