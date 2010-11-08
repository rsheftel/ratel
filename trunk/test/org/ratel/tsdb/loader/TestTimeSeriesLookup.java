package org.ratel.tsdb.loader;

import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.AttributeValues.*;
import static org.ratel.tsdb.TimeSeries.*;
import org.ratel.tsdb.*;
import org.ratel.db.*;

public class TestTimeSeriesLookup extends DbTestCase {

    private static final AttributeValue HOLIDAY = INSTRUMENT.value("holiday");

    public void testEmailsDuplicates() throws Exception {
        emailer.allowMessages();
        new TimeSeriesLookup(HOLIDAY.tsamFilter(), INSTRUMENT);
        emailer.requireSent(1);
    }
    
    public void testIdBorksOnDuplicate() throws Exception {
        emailer.allowMessages();
        TimeSeriesLookup lookup = new TimeSeriesLookup(HOLIDAY.tsamFilter(), INSTRUMENT);
        try {
            lookup.id(values(HOLIDAY));
            fail();
        } catch (RuntimeException ex) {
            assertMatches("duplicate", ex);
        }
    }
    
    public void testLookupWorks() throws Exception {
        TimeSeriesLookup lookup = new TimeSeriesLookup(HOLIDAY.tsamFilter(), INSTRUMENT, FINANCIAL_CENTER);
        Integer id = lookup.id(values(HOLIDAY, FINANCIAL_CENTER.value("nyb")));
        TimeSeries ts = TimeSeries.series(id);
        assertEquals("holidays_nyb", ts.name());

        AttributeValue jeffCenter = FINANCIAL_CENTER.value("jeffscenter");
        jeffCenter.createIfNeeded();
        AttributeValues jeffHolidayValues = values(HOLIDAY, jeffCenter);
        Integer jeffTsId = lookup.id(jeffHolidayValues);
        assertNull(jeffTsId);
        lookup.create("jeff", jeffHolidayValues);
        jeffTsId = lookup.id(jeffHolidayValues);
        assertNotNull(jeffTsId);
        assertEquals("jeff", series(jeffTsId).name());
    }

    
    
}
