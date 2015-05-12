package mail;

import static db.clause.Clause.*;
import db.clause.*;
import db.tables.ScheduleDB.*;

public class EmailAliasesTable extends EmailAliasesBase {
    private static final long serialVersionUID = 1L;
    public static final EmailAliasesTable EMAILS = new EmailAliasesTable();
    
    public EmailAliasesTable() {
        super("aliases");
    }
    
    public void insert(String alias, String recipients) {
        insert(C_ALIAS.with(alias), C_RECIPIENTS.with(recipients));
    }

    public void clear() {
        deleteAll(TRUE);
    }

    public String munge(String name) {
        if (hasAlias(name).isEmpty()) return name;
        return C_RECIPIENTS.value(hasAlias(name));
    }

    private Clause hasAlias(String name) {
        return C_ALIAS.is(name);
    }

}
