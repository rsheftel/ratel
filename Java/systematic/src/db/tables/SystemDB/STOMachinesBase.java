package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class STOMachinesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final STOMachinesBase T_STOMACHINES = new STOMachinesBase("STOMachinesbase");

    public STOMachinesBase(String alias) { super("SystemDB..STOMachines", alias); }

    public NvarcharColumn C_HOSTNAME = new NvarcharColumn("hostname", "nvarchar(50)", this, NOT_NULL);


}

