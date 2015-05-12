package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ExpiryBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ExpiryBase T_EXPIRY = new ExpiryBase("Expirybase");

    public ExpiryBase(String alias) { super("SystemDB..Expiry", alias); }

    public NvarcharColumn C_EXPIRY = new NvarcharColumn("Expiry", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(250)", this, NULL);


}

