package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class TagGroupBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TagGroupBase T_TAG_GROUP = new TagGroupBase("Tag_Groupbase");

    public TagGroupBase(String alias) { super("PerformanceDB..Tag_Group", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(100)", this, NOT_NULL);


}

