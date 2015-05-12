package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class TagGroupMembersTagBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TagGroupMembersTagBase T_TAG_GROUP_MEMBERS_TAG = new TagGroupMembersTagBase("Tag_Group_Members_Tagbase");

    public TagGroupMembersTagBase(String alias) { super("PerformanceDB..Tag_Group_Members_Tag", alias); }

    public IntColumn C_TAG_GROUP_ID = new IntColumn("tag_group_id", "int", this, NOT_NULL);
    public IntColumn C_TAG_ID = new IntColumn("tag_id", "int", this, NOT_NULL);


}

