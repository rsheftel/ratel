package systemdb.metadata;

import db.*;
import static systemdb.metadata.MarketSessionTable.*;
import static systemdb.metadata.ExchangeSessionTable.*;
import static util.Dates.*;

public class TestMarketSessionTable extends DbTestCase {

    private static final String DAY = "DAY";
    private static final String NIGHT = "NIGHT";

    @Override protected void setUp() throws Exception {
        super.setUp();
        EXCHANGE_SESSION.deleteIfExists("CBOT", DAY);
        EXCHANGE_SESSION.insert("CBOT", DAY, "08:30:00", "11:00:00", 5*60);
        EXCHANGE_SESSION.insert("CBOT", NIGHT, "14:30:00", "14:45:00", 5*60);
        SESSION.delete("RE.TEST.TY.1C", DAY);
        SESSION.insert("RE.TEST.TY.1C", DAY, "09:30:00", "10:30:00", 30*60);
        SESSION.insert("RE.TEST.TY.1C", NIGHT, "19:30:00", "20:30:00", 30*60, "America/Chicago");
    }
    
    public void testCanLookupSessionByName() throws Exception {
        MarketSession session = SESSION.forName("RE.TEST.TY.1C", DAY);
        assertEquals(todayAt("09:30:00"), session.open());
        assertEquals(todayAt("10:30:00"), session.close());
        assertEquals(secondsAgo(1800, todayAt("10:30:00")), session.processCloseAt());
    }
    
    public void testCanLookupSessionExchangeDefaulted() throws Exception {
        SESSION.delete("RE.TEST.TY.1C", DAY);
        MarketSession session = SESSION.forName("RE.TEST.TY.1C", DAY);
        assertEquals(todayAt("08:30:00"), session.open());
        assertEquals(todayAt("11:00:00"), session.close());
        assertEquals(secondsAgo(300, todayAt("11:00:00")), session.processCloseAt());
    }

    public void testCanLookupProcessCloseExchangeDefaultedOnExistingSession() throws Exception {
        SESSION.removeCloseOffset("RE.TEST.TY.1C", DAY);
        MarketSession session = SESSION.forName("RE.TEST.TY.1C", DAY);
        assertEquals(todayAt("09:30:00"), session.open());
        assertEquals(todayAt("10:30:00"), session.close());
        assertEquals(secondsAgo(300, todayAt("10:30:00")), session.processCloseAt());
    }
    
    public void testTimeZone() throws Exception {
        MarketSession session = SESSION.forName("RE.TEST.TY.1C", NIGHT);
        assertEquals(todayAt("20:30:00"), session.open());
        assertEquals(todayAt("21:30:00"), session.close());
        assertEquals(secondsAgo(1800, todayAt("21:30:00")), session.processCloseAt());
    }
}
