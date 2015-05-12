package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class GroupsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final GroupsBase T_GROUPS = new GroupsBase("Groupsbase");

    public GroupsBase(String alias) { super("SystemDB..Groups", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(200)", this, NOT_NULL);


}

