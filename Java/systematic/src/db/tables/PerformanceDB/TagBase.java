package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class TagBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TagBase T_TAG = new TagBase("Tagbase");

    public TagBase(String alias) { super("PerformanceDB..Tag", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(100)", this, NOT_NULL);
    public NvarcharColumn C_CLOSINGTIME = new NvarcharColumn("closingTime", "nvarchar(10)", this, NOT_NULL);


}

