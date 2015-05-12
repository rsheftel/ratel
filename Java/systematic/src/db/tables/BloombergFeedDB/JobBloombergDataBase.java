package db.tables.BloombergFeedDB;

import db.*;
import db.columns.*;

public class JobBloombergDataBase extends Table {

    private static final long serialVersionUID = 1L;    public static final JobBloombergDataBase T_JOBBLOOMBERGDATA = new JobBloombergDataBase("JobBloombergDatabase");

    public JobBloombergDataBase(String alias) { super("BloombergFeedDB..JobBloombergData", alias); }

    public IntColumn C_IDJOB = new IntColumn("idJob", "int", this, NOT_NULL);
    public IntColumn C_IDBBDATA = new IntColumn("idBBData", "int", this, NOT_NULL);


}

