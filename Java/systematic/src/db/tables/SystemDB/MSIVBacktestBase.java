package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MSIVBacktestBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MSIVBacktestBase T_MSIVBACKTEST = new MSIVBacktestBase("MSIVBacktestbase");

    public MSIVBacktestBase(String alias) { super("SystemDB..MSIVBacktest", alias); }

    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_STODIR = new NvarcharColumn("STOdir", "nvarchar(1024)", this, NULL);
    public NvarcharColumn C_STOID = new NvarcharColumn("STOid", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_RUNDATE = new DatetimeColumn("RunDate", "datetime", this, NULL);
    public DatetimeColumn C_STARTDATE = new DatetimeColumn("StartDate", "datetime", this, NULL);
    public DatetimeColumn C_ENDDATE = new DatetimeColumn("EndDate", "datetime", this, NULL);
    public BitColumn C_VALIDATIONACCEPT = new BitColumn("ValidationAccept", "bit", this, NULL);


}

