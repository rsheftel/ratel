package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class WatcherExclusionsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final WatcherExclusionsBase T_WATCHEREXCLUSIONS = new WatcherExclusionsBase("WatcherExclusionsbase");

    public WatcherExclusionsBase(String alias) { super("SystemDB..WatcherExclusions", alias); }

    public VarcharColumn C_TAG = new VarcharColumn("tag", "varchar(50)", this, NOT_NULL);
    public VarcharColumn C_WHO = new VarcharColumn("who", "varchar(50)", this, NOT_NULL);
    public DatetimeColumn C_DATEEXCLUDED = new DatetimeColumn("dateExcluded", "datetime", this, NOT_NULL);


}

