package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class FirmBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FirmBase T_FIRM = new FirmBase("Firmbase");

    public FirmBase(String alias) { super("PerformanceDB..Firm", alias); }

    public IntColumn C_ID = new IntColumn("Id", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(200)", this, NOT_NULL);


}

