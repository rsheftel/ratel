package systemdb.data;

import static systemdb.data.Interval.*;
import static util.Dates.*;
import static util.Range.*;

import java.util.*;

import amazon.*;

public class TestSymbolAmazon extends S3CacheableTestCase {
    
    public void testAmazonIntradayBars() throws Exception {
        Symbol symbol = new Symbol("RE.TEST.TY.1C");
        Date start = hoursAhead(10, businessDaysAgo(1, midnight(), "nyb"));
        Date end = hoursAhead(1, start);
        List<Bar> bar = symbol.bars(range(start, end), MINUTE);
        assertSize(60, bar);
    }
}
