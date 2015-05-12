package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class GroupMemberMSIVPVsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final GroupMemberMSIVPVsBase T_GROUPMEMBERMSIVPVS = new GroupMemberMSIVPVsBase("GroupMemberMSIVPVsbase");

    public GroupMemberMSIVPVsBase(String alias) { super("SystemDB..GroupMemberMSIVPVs", alias); }

    public NvarcharColumn C_GROUPNAME = new NvarcharColumn("GroupName", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("PV_Name", "nvarchar(50)", this, NOT_NULL);
    public FloatColumn C_WEIGHT = new FloatColumn("Weight", "float(53)", this, NULL);


}

