package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class GroupMemberGroupsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final GroupMemberGroupsBase T_GROUPMEMBERGROUPS = new GroupMemberGroupsBase("GroupMemberGroupsbase");

    public GroupMemberGroupsBase(String alias) { super("SystemDB..GroupMemberGroups", alias); }

    public NvarcharColumn C_GROUPNAME = new NvarcharColumn("GroupName", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_MEMBERGROUPNAME = new NvarcharColumn("MemberGroupName", "nvarchar(200)", this, NOT_NULL);
    public FloatColumn C_WEIGHT = new FloatColumn("Weight", "float(53)", this, NOT_NULL);


}

