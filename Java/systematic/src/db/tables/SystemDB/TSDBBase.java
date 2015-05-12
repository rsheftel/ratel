package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class TSDBBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TSDBBase T_TSDB = new TSDBBase("TSDBbase");

    public TSDBBase(String alias) { super("SystemDB..TSDB", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DATA_SOURCE = new NvarcharColumn("Data_source", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_NAME_OPEN = new NvarcharColumn("Name_open", "nvarchar(70)", this, NULL);
    public NvarcharColumn C_NAME_HIGH = new NvarcharColumn("Name_high", "nvarchar(70)", this, NULL);
    public NvarcharColumn C_NAME_LOW = new NvarcharColumn("Name_low", "nvarchar(70)", this, NULL);
    public NvarcharColumn C_NAME_CLOSE = new NvarcharColumn("Name_close", "nvarchar(70)", this, NOT_NULL);
    public NvarcharColumn C_NAME_VOLUME = new NvarcharColumn("Name_volume", "nvarchar(70)", this, NULL);
    public NvarcharColumn C_NAME_OPEN_INTEREST = new NvarcharColumn("Name_open_interest", "nvarchar(70)", this, NULL);
    public NvarcharColumn C_CALCULATE_METHOD = new NvarcharColumn("Calculate_method", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("StartDate", "datetime", this, NULL);
    public NvarcharColumn C_VERIFYBY = new NvarcharColumn("VerifyBy", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_VERIFYDATE = new DatetimeColumn("VerifyDate", "datetime", this, NULL);
    public BitColumn C_TEMPLATE = new BitColumn("Template", "bit", this, NULL);
    public NvarcharColumn C_COMMENTS = new NvarcharColumn("Comments", "nvarchar(50)", this, NULL);


}

