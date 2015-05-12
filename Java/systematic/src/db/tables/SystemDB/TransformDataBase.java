package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class TransformDataBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TransformDataBase T_TRANSFORM_DATA = new TransformDataBase("Transform_database");

    public TransformDataBase(String alias) { super("SystemDB..Transform_data", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_TRANSFORMATION_NAME = new NvarcharColumn("Transformation_Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(2000)", this, NULL);
    public BitColumn C_HISTDAILY = new BitColumn("HistDaily", "bit", this, NULL);
    public BitColumn C_HISTTICK = new BitColumn("HistTick", "bit", this, NULL);
    public BitColumn C_TODAYTICK = new BitColumn("TodayTick", "bit", this, NULL);
    public BitColumn C_LIVE = new BitColumn("Live", "bit", this, NULL);


}

