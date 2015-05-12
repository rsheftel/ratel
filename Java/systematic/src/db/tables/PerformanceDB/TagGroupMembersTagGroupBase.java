package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class TagGroupMembersTagGroupBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TagGroupMembersTagGroupBase T_TAG_GROUP_MEMBERS_TAG_GROUP = new TagGroupMembersTagGroupBase("Tag_Group_Members_Tag_Groupbase");

    public TagGroupMembersTagGroupBase(String alias) { super("PerformanceDB..Tag_Group_Members_Tag_Group", alias); }

    public IntColumn C_PARENT_TAG_GROUP_ID = new IntColumn("parent_tag_group_id", "int", this, NOT_NULL);
    public IntColumn C_CHILD_TAG_GROUP_ID = new IntColumn("child_tag_group_id", "int", this, NOT_NULL);


}

