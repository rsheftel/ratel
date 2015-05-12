package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class EmailAliasesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final EmailAliasesBase T_EMAIL_ALIASES = new EmailAliasesBase("email_aliasesbase");

    public EmailAliasesBase(String alias) { super("ScheduleDB..email_aliases", alias); }

    public NvarcharColumn C_ALIAS = new NvarcharColumn("alias", "nvarchar(255)", this, NOT_NULL);
    public VarcharColumn C_RECIPIENTS = new VarcharColumn("recipients", "varchar(1024)", this, NOT_NULL);


}

