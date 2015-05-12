package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class PATCHLOGBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PATCHLOGBase T_PATCH_LOG = new PATCHLOGBase("PATCH_LOGbase");

    public PATCHLOGBase(String alias) { super("IvyDB..PATCH_LOG", alias); }

    public SmalldatetimeColumn C_PATCHDATE = new SmalldatetimeColumn("PatchDate", "smalldatetime", this, NULL);
    public VarcharColumn C_MESSAGE = new VarcharColumn("Message", "varchar(255)", this, NULL);


}

