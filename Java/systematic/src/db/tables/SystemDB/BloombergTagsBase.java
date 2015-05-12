package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class BloombergTagsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final BloombergTagsBase T_BLOOMBERGTAGS = new BloombergTagsBase("BloombergTagsbase");

    public BloombergTagsBase(String alias) { super("SystemDB..BloombergTags", alias); }

    public IntColumn C_SYSTEMID = new IntColumn("systemId", "int", this, NOT_NULL);
    public NvarcharColumn C_SYSTEM = new NvarcharColumn("System", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_INTERVAL = new NvarcharColumn("Interval", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_VERSION = new NvarcharColumn("Version", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("PV_Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_TAG = new NvarcharColumn("Tag", "nvarchar(50)", this, NOT_NULL);
    public BitColumn C_AUTOEXECUTETRADES = new BitColumn("AutoExecuteTrades", "bit", this, NOT_NULL);


}

