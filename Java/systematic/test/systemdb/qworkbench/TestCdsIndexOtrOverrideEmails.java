package systemdb.qworkbench;

import static com.malbec.tsdb.markit.CdsIndexOtrOverride.*;
import static db.clause.Clause.*;
import static util.Dates.*;

import java.util.*;

import cds.*;
import db.*;

public class TestCdsIndexOtrOverrideEmails extends DbTestCase {


    @Override protected void setUp() throws Exception {
        super.setUp();
        freezeNow("1995/05/01");
        OVERRIDE.deleteAll(TRUE);
        OVERRIDE.insert("CDXNAIG", midnight(), null, 7, 1);
        OVERRIDE.insert("OTHER", midnight(), daysAhead(1, midnight()), 8, 3);
    }
    
    public void testFindsCorrectRows() throws Exception {
        Date asOf = midnight();
        assertEmpty(OVERRIDE.overriddenNear(daysAgo(7, asOf)));
        assertSize(2, OVERRIDE.overriddenNear(daysAgo(6, asOf))); // both are active at the end of the lookahead period.
        assertSize(2, OVERRIDE.overriddenNear(asOf));
        assertSize(2, OVERRIDE.overriddenNear(daysAhead(1, asOf)));
        assertSize(1, OVERRIDE.overriddenNear(daysAhead(2, asOf))); // OTHER rolled off
        OVERRIDE.C_END_DATE.updateOne(OVERRIDE.C_MARKIT_NAME.is("CDXNAIG"), asOf);
        assertEmpty(OVERRIDE.overriddenNear(daysAhead(2, asOf)));
    }
    
    public void testMainDoesNotEmailForNoOverrides() throws Exception {
        emailer.allowMessages();
        new CdsIndexOtrOverrideEmails().doEmails(midnight(), "jeff");
        emailer.requireSent(1);
        emailer.clear();
        emailer.disallowMessages();
        new CdsIndexOtrOverrideEmails().doEmails(daysAgo(14, midnight()), "jeff");
    }
}
