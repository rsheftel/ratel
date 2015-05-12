package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class LiveRTransformationBase extends Table {

    private static final long serialVersionUID = 1L;    public static final LiveRTransformationBase T_LIVERTRANSFORMATION = new LiveRTransformationBase("LiveRTransformationbase");

    public LiveRTransformationBase(String alias) { super("SystemDB..LiveRTransformation", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_GROUP_NAME = new NvarcharColumn("group_name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_CLASS_NAME = new NvarcharColumn("class_name", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_ARGS = new NvarcharColumn("args", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_MESSAGING_LAYER = new NvarcharColumn("messaging_layer", "nvarchar(50)", this, NOT_NULL);


}

