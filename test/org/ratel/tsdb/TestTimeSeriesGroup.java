package org.ratel.tsdb;

import org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.util.Dates.*;
import org.ratel.db.*;

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
