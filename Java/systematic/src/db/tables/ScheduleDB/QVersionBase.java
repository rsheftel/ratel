package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class QVersionBase extends Table {

    private static final long serialVersionUID = 1L;    public static final QVersionBase T_Q_VERSION = new QVersionBase("q_versionbase");

    public QVersionBase(String alias) { super("ScheduleDB..q_version", alias); }

    public NvarcharColumn C_Q_VERSION = new NvarcharColumn("q_version", "nvarchar(50)", this, NOT_NULL);


}

