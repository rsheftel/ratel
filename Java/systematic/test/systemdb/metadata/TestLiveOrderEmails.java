package systemdb.metadata;

import db.*;
import static db.clause.Clause.*;
import static systemdb.metadata.LiveOrderEmailsTable.*;
import static util.Objects.*;

public class TestLiveOrderEmails extends DbTestCase {
    public void testSomething() throws Exception {
        ORDER_EMAILS.deleteAll(TRUE);
        ORDER_EMAILS.insert("NDayBreak-1.0-daily", "BFBD30", "ALL", "us");
        assertEquals(list("us"), ORDER_EMAILS.emails("NDayBreak-1.0-daily", "BFBD30", "ALL"));
        assertEquals(list(), ORDER_EMAILS.emails("NDayBreak-1.0-daily", "BFBD31", "ALL"));
        assertEquals(list(), ORDER_EMAILS.emails("NDayBreak-1.0-dail", "BFBD30", "ALL"));
        assertEquals(list("us"), ORDER_EMAILS.emails("NDayBreak-1.0-daily", "BFBD30", "AMARKET"));
        ORDER_EMAILS.insert("NDayBreak-1.0-daily", "ALL", "SPECIAL", "team");
        assertEquals(list("team", "us"), ORDER_EMAILS.emails("NDayBreak-1.0-daily", "BFBD30", "SPECIAL"));
    }
}
