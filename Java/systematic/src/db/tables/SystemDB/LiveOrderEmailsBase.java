package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class LiveOrderEmailsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final LiveOrderEmailsBase T_LIVEORDEREMAILS = new LiveOrderEmailsBase("LiveOrderEmailsbase");

    public LiveOrderEmailsBase(String alias) { super("SystemDB..LiveOrderEmails", alias); }

    public NvarcharColumn C_SYSTEM = new NvarcharColumn("system", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_PV = new NvarcharColumn("pv", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_MARKET = new NvarcharColumn("market", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_EMAIL = new NvarcharColumn("email", "nvarchar(255)", this, NOT_NULL);


}

