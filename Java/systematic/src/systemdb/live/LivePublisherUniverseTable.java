package systemdb.live;

import static systemdb.metadata.BloombergTable.*;
import static systemdb.metadata.MarketSessionTable.*;
import static util.Objects.*;

import java.util.*;

import jms.*;

import systemdb.data.*;
import systemdb.metadata.*;
import db.*;
import db.tables.SystemDB.*;

class LivePublisherUniverseTable extends LivePublisherUniverseBase {
    public class LivePublisherEntry extends Row implements TickListener {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final Symbol symbol;
        private boolean marketHoursOnly;
        private JmsLiveDescription topic;
        private MarketSession session;

        public LivePublisherEntry(Row row) {
            super(row);
            name = value(C_NAME);
            marketHoursOnly = value(C_ONLYPUBLISHDURINGMARKETHOURS);
            symbol = new Symbol(name);
            topic = symbol.jmsLive();
            maybeCacheSession();
        }

        private void maybeCacheSession() {
            if(marketHoursOnly)
                session = SESSION.forName(name, "DAY");
        }
        
        public void onTick(Tick tick) {
            if(marketHoursOnly && !session.isOpen()) return;
            topic.publish(tick);
        }

        public void setOnlyPublishDuringMarketSession(boolean b) {
            marketHoursOnly = b;
            maybeCacheSession();
            put(C_ONLYPUBLISHDURINGMARKETHOURS.with(b));
            C_ONLYPUBLISHDURINGMARKETHOURS.updateOne(allMatch(C_NAME), b);
        }

        public void startRepublisher() {
            BLOOMBERG.intraday(name).tickListener(this).start();
        }
        
        public QTopic topic() {
            return topic.topic();
        }
    }
    
    private static final long serialVersionUID = 1L;
    public static LivePublisherUniverseTable UNIVERSE = new LivePublisherUniverseTable();
    public LivePublisherUniverseTable() {
        super("lpub");
    }
    public void insert(String name, boolean onlyPublishInMarketHours) {
        insert(C_NAME.with(name), C_ONLYPUBLISHDURINGMARKETHOURS.with(onlyPublishInMarketHours));
    }
    public LivePublisherEntry entry(String name) {
        return new LivePublisherEntry(row(C_NAME.is(name)));
    }
    public List<LivePublisherEntry> entries() {
        List<LivePublisherEntry> result = empty();
        for(Row row : rows()) result.add(new LivePublisherEntry(row));
        return result;
    }
}