package tsdb;

import tsdb.TimeSeriesGroupTable.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesGroupTable.*;
import static util.Dates.*;
import db.*;

public class TestTimeSeriesGroup extends DbTestCase {
    public void testTimeSeriesGroup() throws Exception {
        TimeSeriesGroup group = GROUPS.insert("test group", series("irs_usd_rate_2y_mid"), series("irs_usd_rate_10y_mid"));
        TimeSeriesGroup empty= GROUPS.insert("test empty group");
        assertFalse(group.isEmpty(now()));
        assertTrue(empty.isEmpty(now()));
        assertTrue(group.anyPopulatedToday(date("2008/03/31"), INTERNAL));
        assertFalse(group.anyPopulatedToday(date("2098/03/31"), INTERNAL));
        TimeSeriesGroup retrieved = GROUPS.get("test group");
        retrieved.delete();
        try {
            GROUPS.get("test group");
            fail();
        } catch (Exception success) {
            assertMatches("not found", success);
        }
    }
    
}
