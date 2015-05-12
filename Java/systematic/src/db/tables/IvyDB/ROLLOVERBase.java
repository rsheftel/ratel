package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class ROLLOVERBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ROLLOVERBase T_ROLLOVER = new ROLLOVERBase("ROLLOVERbase");

    public ROLLOVERBase(String alias) { super("IvyDB..ROLLOVER", alias); }

    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("DATE", "smalldatetime", this, NULL);


}

