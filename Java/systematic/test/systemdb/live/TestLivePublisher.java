package systemdb.live;

import static db.clause.Clause.*;
import static systemdb.live.LivePublisherUniverseTable.*;
import static systemdb.metadata.BloombergTable.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import jms.*;
import systemdb.live.LivePublisherUniverseTable.*;
import systemdb.metadata.*;
import systemdb.metadata.BloombergTable.BloombergIntraday.*;
import amazon.monitor.*;

public class TestLivePublisher extends JMSTestCase {
    private static final String TEST = "RE.TEST.TY.1C";
    private LivePublisherEntry entry;
    
    @Override public void setUp() throws Exception {
        super.setUp();
        UNIVERSE.deleteAll(TRUE);
        UNIVERSE.insert(TEST, false);
        entry = UNIVERSE.entry(TEST);
        fieldsListener = new RecentFieldsKeeper();
        topic = new QTopic("TEST.RE.TEST.TY.1C");
        topic.register(fieldsListener);
    }
    Map<String, String> values = map("OPEN_TDY", "1.0", "HIGH_TDY", "2.0", "LOW_TDY", "3.0", "LAST_PRICE", "4.0", "SIZE_LAST_TRADE", "5", "TIME", "12:34:56.000+00:00");
    private RecentFieldsKeeper fieldsListener;
    private QTopic topic;
    
    public void testMarketTickerLookup() throws Exception {
        String name = "RE.TEST.TU.1C";
        BLOOMBERG.insert(name, BloombergTable.LOOKUP_MARKET_TICKER_SENTINEL);
        MarketTickersTable.TICKERS.insert(name);
        assertEquals("ASDF Garbage", BLOOMBERG.intraday(name).tickListener(null).security());
    }
        
    public void testBloombergStuff() throws Exception {
        BLOOMBERG.setFiltered(TEST, true);
        processMessage();
        hasMessage(4.0);
        values.put("LAST_PRICE", "-4.0");
        topic.setReadonly(true);
        processMessage();
        topic.setReadonly(false);
        BLOOMBERG.setFiltered(TEST, false);
        processMessage();
        hasMessage(-4.0);

        BLOOMBERG.useMids(TEST, "BID_TDY", "ASK_TDY");
        values.put("BID_TDY", "10.0");
        values.put("ASK_TDY", "20.0");
        processMessage();
        hasMessage(15.0);
        values.put("ASK_TDY", "-2.0");
        processMessage();
        hasMessage(4.0);
        
        BLOOMBERG.setFiltered(TEST, true);
        topic.setReadonly(true);
        processMessage();
    }
    
    public void testMarketSessionFiltering() throws Exception {
        // market session from 9am to 3pm
        entry.setOnlyPublishDuringMarketSession(true);
        
        freezeNow("2009/05/13 08:59:59");
        topic.setReadonly(true);
        processMessage();
        
        freezeNow("2009/05/13 09:00:00");
        topic.setReadonly(false);
        processMessage();
        hasMessage(4.0);

        freezeNow("2009/05/13 14:59:59");
        topic.setReadonly(false);
        processMessage();
        hasMessage(4.0);
        
        freezeNow("2009/05/13 15:00:00");
        topic.setReadonly(true);
        processMessage();

        entry.setOnlyPublishDuringMarketSession(false);
        topic.setReadonly(false);
        
        freezeNow("2009/05/13 08:59:59");
        processMessage();
        hasMessage(4.0);

        freezeNow("2009/05/13 15:00:00");
        topic.setReadonly(false);
        processMessage();
        hasMessage(4.0);
    }

    private void hasMessage(double expected) {
        fieldsListener.waitForMessage(1000);
        assertEquals(expected, fieldsListener.latest.numeric("LastPrice"));
    }

    private void processMessage() {
        BloombergTickListener listener = BLOOMBERG.intraday(TEST).tickListener(entry);
        listener.setValues(values);
        listener.subOnMessage();
    }
}
