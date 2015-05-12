package util;

import static util.Dates.*;

public class TestYearMonth extends Asserts {
    
    public void testYearMonth() throws Exception {
        YearMonth sep = new YearMonth("200809");
        assertEquals(2008, sep.year());
        assertEquals(9, sep.month());
        assertEquals("2008/09/01", ymdHuman(sep.first()));
        assertEquals("2008/09/30", ymdHuman(sep.end()));
        assertEquals(sep, new YearMonth("2008/09"));
        assertNotYearMonth("2008/9");
        assertNotYearMonth("200/09");
        assertNotYearMonth("200013");
        
    }
    private void assertNotYearMonth(String malformed) {
        try {
            new YearMonth(malformed);
            fail();
        } catch (RuntimeException success) {
            assertMatches("could not parse " + malformed + " as a YearMonth", success);
        }
    }
    
}
