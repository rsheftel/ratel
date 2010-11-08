package org.ratel.tsdb;

import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.util.Objects.*;
import org.ratel.db.*;

public class TestDeletePoint extends DbTestCase {

    private static final SeriesSource BOGUS_CLOSE = new SeriesSource("aapl close:bogus");

    public void testCanDeletePoint() throws Exception {
        int count = BOGUS_CLOSE.count();
        assertEquals(0, SeriesControl.run(array("-delete", "T", "-id", "" + series("aapl close").id(), "-source", "bogus", "-date", "1998/06/19", "-command", "delete")));
        assertEquals(count - 1, BOGUS_CLOSE.count());
    }
    
    public void testCanNotDeletePoint() throws Exception {
        int count = BOGUS_CLOSE.count();
        assertEquals(1, SeriesControl.run(array("-delete", "T", "-id", "" + series("aapl close").id(), "-source", "bogus", "-date", "1998/06/19 01:00:00", "-command", "delete")));
        assertEquals(count, BOGUS_CLOSE.count());    
    }
    
}
