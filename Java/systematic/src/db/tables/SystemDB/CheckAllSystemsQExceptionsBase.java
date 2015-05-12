package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class CheckAllSystemsQExceptionsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CheckAllSystemsQExceptionsBase T_CHECKALLSYSTEMSQEXCEPTIONS = new CheckAllSystemsQExceptionsBase("CheckAllSystemsQExceptionsbase");

    public CheckAllSystemsQExceptionsBase(String alias) { super("SystemDB..CheckAllSystemsQExceptions", alias); }

    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public NvarcharColumn C_PV_NAME = new NvarcharColumn("PV_Name", "nvarchar(50)", this, NOT_NULL);
    public BitColumn C_ALLOWMISSINGFROMALLSYSTEMSQ = new BitColumn("AllowMissingFromAllSystemsQ", "bit", this, NOT_NULL);
    public BitColumn C_ALLOWMISSINGFROMMSIVLIVEHISTORY = new BitColumn("AllowMissingFromMsivLiveHistory", "bit", this, NOT_NULL);


}

