package systemdb.metadata;

import util.*;
import db.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class ExchangeSessionTable extends ExchangeSessionsBase {

    private static final long serialVersionUID = 1L;
    public static final ExchangeSessionTable EXCHANGE_SESSION = new ExchangeSessionTable();
    
    public ExchangeSessionTable() {
        super("exchangeSessions");
    }
    
    public void insert(String exchange, String session, String open, String close, int closeOffset) {
        insert(
            C_EXCHANGE.with(exchange),
            C_NAME.with(session),
            C_OPENTIME.with(open),
            C_CLOSETIME.with(close),
            C_PROCESSCLOSEORDERSOFFSETSECONDS.with(closeOffset)
        );
    }
    
    public void update(String exchange, String session, String close, int closeOffset) {
        updateOne(new Row(
            C_CLOSETIME.with(close),
            C_PROCESSCLOSEORDERSOFFSETSECONDS.with(closeOffset)
        ), keyMatches(exchange, session));
    }
    
    private Clause keyMatches(String exchange, String name) {
        return C_EXCHANGE.is(exchange).and(C_NAME.is(name));
    }
    
    public void delete(String exchange, String name) {
        deleteOne(keyMatches(exchange, name));
    }

    public void deleteIfExists(String exchange, String name) {
        if (rowExists(keyMatches(exchange, name)))
            delete(exchange, name);
    }

    public MarketSession defaultSession(String market, String name) {
        String exchange = new Market(market).exchange();
        Row session = row(keyMatches(exchange, name));
        return new MarketSession(
            session.value(C_OPENTIME), 
            session.value(C_CLOSETIME), 
            session.value(C_PROCESSCLOSEORDERSOFFSETSECONDS),
            new QTimeZone(session.value(C_TIMEZONE))
        );
    }

}
