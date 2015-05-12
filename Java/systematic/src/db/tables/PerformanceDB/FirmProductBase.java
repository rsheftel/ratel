package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class FirmProductBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FirmProductBase T_FIRMPRODUCT = new FirmProductBase("FirmProductbase");

    public FirmProductBase(String alias) { super("PerformanceDB..FirmProduct", alias); }

    public IntColumn C_ID = new IntColumn("Id", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(200)", this, NOT_NULL);
    public IntColumn C_FIRMID = new IntColumn("FirmId", "int", this, NOT_NULL);
    public IntColumn C_PRODUCTID = new IntColumn("ProductId", "int", this, NOT_NULL);


}

