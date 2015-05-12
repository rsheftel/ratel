package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ASCIIBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ASCIIBase T_ASCII = new ASCIIBase("ASCIIbase");

    public ASCIIBase(String alias) { super("SystemDB..ASCII", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_FILENAME = new NvarcharColumn("Filename", "nvarchar(100)", this, NULL);
    public NvarcharColumn C_COLUMNDATE = new NvarcharColumn("columnDate", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_COLUMNOPEN = new NvarcharColumn("columnOpen", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_COLUMNHIGH = new NvarcharColumn("columnHigh", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_COLUMNLOW = new NvarcharColumn("columnLow", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_COLUMNCLOSE = new NvarcharColumn("columnClose", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_COLUMNVOLUME = new NvarcharColumn("columnVolume", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_COLUMNOPENINTEREST = new NvarcharColumn("columnOpenInterest", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_VERIFYBY = new NvarcharColumn("VerifyBy", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_VERIFYDATE = new DatetimeColumn("VerifyDate", "datetime", this, NULL);
    public NvarcharColumn C_COMMENTS = new NvarcharColumn("Comments", "nvarchar(50)", this, NULL);
    public FloatColumn C_PRICEMULTIPLIER = new FloatColumn("PriceMultiplier", "float(53)", this, NOT_NULL);


}

