package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class MSIVLiveHistoryBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MSIVLiveHistoryBase T_MSIVLIVEHISTORY = new MSIVLiveHistoryBase("MSIVLiveHistorybase");

    public MSIVLiveHistoryBase(String alias) { super("SystemDB..MSIVLiveHistory", alias); }

    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("PV_Name", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_START_TRADING = new DatetimeColumn("Start_trading", "datetime", this, NOT_NULL);
    public DatetimeColumn C_END_TRADING = new DatetimeColumn("End_trading", "datetime", this, NULL);


}

