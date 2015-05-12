package systemdb.metadata;

import util.*;
import db.*;
import db.clause.*;
import db.tables.SystemDB.*;
import static systemdb.metadata.ExchangeSessionTable.*;

public class MarketSessionTable extends MarketSessionsBase {

    private static final long serialVersionUID = 1L;
    public static final MarketSessionTable SESSION = new MarketSessionTable();
    
    public MarketSessionTable() {
        super("session");
    }
    
    public void insert(String market, String session, String open, String close, int closeOffset, String timeZone) {
        insert(
            C_MARKET.with(market),
            C_NAME.with(session),
            C_OPENTIME.with(open),
            C_CLOSETIME.with(close),
            C_PROCESSCLOSEORDERSOFFSETSECONDS.with(closeOffset),
            C_TIMEZONE.with(timeZone)
        );
    }
    
    public void insert(String market, String session, String open, String close, int closeOffset) {
        insert(market, session, open, close, closeOffset, "America/New_York");
    }
    
    public void update(String market, String session, String close, int closeOffset) {
        updateOne(new Row(
            C_CLOSETIME.with(close),
            C_PROCESSCLOSEORDERSOFFSETSECONDS.with(closeOffset)
        ), keyMatches(market, session));
    }
    
    public MarketSession forName(String marketName, String sessionName) {
        MarketSession exchange = EXCHANGE_SESSION.defaultSession(marketName, sessionName);
        if (hasMarketSession(marketName, sessionName)) {
            Row market = row(marketName, sessionName);
            return new MarketSession(
                market.value(C_OPENTIME), 
                market.value(C_CLOSETIME), 
                market.isEmpty(C_PROCESSCLOSEORDERSOFFSETSECONDS)
                    ? exchange.closeOffsetSeconds()
                    : market.value(C_PROCESSCLOSEORDERSOFFSETSECONDS),
                new QTimeZone(market.value(C_TIMEZONE))
            );
        }
        return exchange;
    }

    private boolean hasMarketSession(String market, String name) {
        return keyMatches(market, name).exists();
    }
    
    private Row row(String market, String name) {
        return row(keyMatches(market, name));
    }
    
    private Clause keyMatches(String market, String name) {
        return C_MARKET.is(market).and(C_NAME.is(name));
    }
    
    public void delete(String market, String name) {
        deleteOne(keyMatches(market, name));
    }

    public void removeCloseOffset(String string, String day) {
        C_PROCESSCLOSEORDERSOFFSETSECONDS.updateOne(keyMatches(string, day), null);
    }


    
}
