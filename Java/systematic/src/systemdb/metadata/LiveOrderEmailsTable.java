package systemdb.metadata;

import static db.clause.Clause.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class LiveOrderEmailsTable extends LiveOrderEmailsBase {
    private static final long serialVersionUID = 1L;
    public static final LiveOrderEmailsTable ORDER_EMAILS = new LiveOrderEmailsTable();

    public LiveOrderEmailsTable() {
        super("order_emails");
    }

    public void insert(String system, String pv, String market, String email) {
        insert(C_SYSTEM.with(system), C_PV.with(pv), C_MARKET.with(market), C_EMAIL.with(email));
    }

    public List<String> emails(String system, String pv, String market) {
        Clause systemMatches = parenGroup(C_SYSTEM.is("ALL").or(C_SYSTEM.is(system)));
        Clause pvMatches = parenGroup(C_PV.is("ALL").or(C_PV.is(pv)));
        Clause marketMatches = parenGroup(C_MARKET.is("ALL").or(C_MARKET.is(market)));
        Clause matches = systemMatches.and(pvMatches).and(marketMatches);
        SelectOne<String> select = SelectOne.select(C_EMAIL, matches);
        select.orderBy(C_EMAIL.ascending());
        return select.values();
    }
    

}
