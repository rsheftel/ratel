package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class BloombergBase extends Table {

    private static final long serialVersionUID = 1L;    public static final BloombergBase T_BLOOMBERG = new BloombergBase("Bloombergbase");

    public BloombergBase(String alias) { super("SystemDB..Bloomberg", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_SECURITY = new NvarcharColumn("Security", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_OPENFIELD = new NvarcharColumn("OpenField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_HIGHFIELD = new NvarcharColumn("HighField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_LOWFIELD = new NvarcharColumn("LowField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_LASTFIELD = new NvarcharColumn("LastField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_BIDFIELD = new NvarcharColumn("BidField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_ASKFIELD = new NvarcharColumn("AskField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_SIZEFIELD = new NvarcharColumn("SizeField", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_TIMEFIELD = new NvarcharColumn("TimeField", "nvarchar(50)", this, NULL);
    public BitColumn C_ISFILTERED = new BitColumn("IsFiltered", "bit", this, NOT_NULL);
    public NvarcharColumn C_VERIFIEDBY = new NvarcharColumn("VerifiedBy", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_VERIFIEDDATE = new DatetimeColumn("VerifiedDate", "datetime", this, NULL);
    public NvarcharColumn C_CALCULATE_METHOD = new NvarcharColumn("Calculate_method", "nvarchar(50)", this, NULL);


}

