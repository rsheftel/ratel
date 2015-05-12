package db.tables.BloombergFeedDB;

import db.*;
import db.columns.*;

public class UsersBase extends Table {

    private static final long serialVersionUID = 1L;    public static final UsersBase T_USERS = new UsersBase("Usersbase");

    public UsersBase(String alias) { super("BloombergFeedDB..Users", alias); }

    public NvarcharColumn C_USERNAME = new NvarcharColumn("username", "nvarchar(50)", this, NOT_NULL);
    public IntColumn C_UUID = new IntColumn("uuid", "int", this, NOT_NULL);
    public NvarcharColumn C_IPADDRESS = new NvarcharColumn("ipAddress", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_LASTSUCCESS = new DatetimeColumn("lastSuccess", "datetime", this, NULL);
    public NvarcharColumn C_STATUS = new NvarcharColumn("status", "nvarchar(50)", this, NOT_NULL);


}

