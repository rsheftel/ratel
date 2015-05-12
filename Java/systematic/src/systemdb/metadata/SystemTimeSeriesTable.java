package systemdb.metadata;

import static db.tables.SystemDB.ActiveMQBase.*;
import static db.tables.SystemDB.ExchangeBase.*;
import static db.tables.SystemDB.MarketBase.*;
import static systemdb.data.AsciiTable.*;
import static systemdb.metadata.SystemTSDBTable.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import systemdb.data.*;
import db.*;
import db.clause.*;
import db.tables.SystemDB.*;


public class SystemTimeSeriesTable extends TimeSeriesDataBase {
    private static final List<String> DAILY_SOURCES = list(TSDB, ASC11, HistoricalProvider.BLOOMBERG);
    private static final List<String> INTRADAY_SOURCES = list("Bloomberg", "ASCII");
    private static final long serialVersionUID = 1L;
    public static final String ACTIVE_MQ = "ActiveMQ";

    public static final SystemTimeSeriesTable SYSTEM_TS = new SystemTimeSeriesTable();
    public static String topicPrefix_FOR_TESTING;
    
    public SystemTimeSeriesTable() {
        super("system_tsd");
    }
    
    public void insert(String name, String histDaily, String live) {
        insert(
            C_NAME.with(name), 
            C_HISTDAILY.with(histDaily), 
            C_LIVE.with(live), 
            C_EXCHANGE.with("TEST"),
            C_CURRENCY.with("USD")
        );
    }
    
    public void insert(String name, String histDaily, String live, String liveFeedName) {
        insert(name, histDaily, live);
        bombUnless(live.equals("ActiveMQ"), "only ActiveMQ supported!");
        T_ACTIVEMQ.insert(
            T_ACTIVEMQ.C_NAME.with(name), 
            T_ACTIVEMQ.C_TOPIC.with(liveFeedName), 
            T_ACTIVEMQ.C_VALUEFIELD.with("value"), 
            T_ACTIVEMQ.C_DATEFIELD.with("timestamp")
        );
    }

    public HistoricalDailyData dailyHistorical(String name) {
        String source = C_HISTDAILY.value(nameMatches(name));
        bombUnless(DAILY_SOURCES.contains(source), source + " is not a valid source, use " + DAILY_SOURCES + "\non symbol " + name);
        boolean isTsdb = source.equals(TSDB);
        HistoricalProvider provider = isTsdb ? SYSTEM_SERIES_DATA : source.equals(HistoricalProvider.BLOOMBERG) ? BloombergTable.BLOOMBERG : AsciiTable.SYSTEM_ASCII;
        return provider.dataSource(name);
    }

    public List<String> subsectors(String exchange) {
        return C_SUBSECTOR.distinct(C_EXCHANGE.is(exchange));
    }

    public List<Symbol> symbolsByExchangeSubsector(String exchange, String subsector) {
        List<Symbol> result = empty();
        Clause matches = C_EXCHANGE.is(exchange).and(C_SUBSECTOR.is(subsector));
        double defaultSize = T_EXCHANGE.C_DEFAULTBIGPOINTVALUE.value(T_EXCHANGE.C_EXCHANGE.is(exchange));
        List<String> names = C_NAME.values(matches);
        if (names.isEmpty()) return result;
        SelectMultiple markets = T_MARKET.select(T_MARKET.C_NAME.in(names).and(T_MARKET.C_BIGPOINTVALUE.isNotNull()));
        
        Map<String, Double> marketSizes = markets.asMap(T_MARKET.C_NAME, T_MARKET.C_BIGPOINTVALUE);
        for (String name : names) {
            double size = marketSizes.containsKey(name) ? marketSizes.get(name) : defaultSize;
            result.add(new Symbol(name, size));
        }
        return result;
    }

    private Clause nameMatches(String name) {
        return C_NAME.is(name);
    }

    public LiveDataSource live(String symbol) {
        String live = C_LIVE.value(nameMatches(symbol));
        if(live.equals("ActiveMQ")) 
            return mqLive(symbol);
        else if(live.equals("Bloomberg"))
            return BloombergTable.BLOOMBERG.intraday(symbol);
        else
            throw bomb("unknown live data source: " + live);
    }

    private JmsLiveDescription mqLive(String symbol) {
        Row mqRow = T_ACTIVEMQ.row(T_ACTIVEMQ.C_NAME.is(C_NAME).and(nameMatches(symbol)));
        String topic = mqRow.value(T_ACTIVEMQ.C_TOPIC);
        if(hasContent(topicPrefix_FOR_TESTING)) topic = topicPrefix_FOR_TESTING + "." + topic;
        return new JmsLiveDescription(
            topic, 
            "ActiveMQ", 
            mqRow.value(T_ACTIVEMQ.C_DATEFIELD), 
            mqRow.value(T_ACTIVEMQ.C_VALUEFIELD)
        );
    }

    public String type(String name) {
        return C_TYPE.value(nameMatches(name));
    }

    public void insertBloomberg(String symbol, String bloombergTicker) {
        insertBloomberg(symbol, bloombergTicker, null, null, null, null, null, null);
    }

    public IntradaySource intraday(String symbol) {
        String source = C_HISTINTRADAY.value(nameMatches(symbol));
        bombUnless(INTRADAY_SOURCES.contains(source), source + " is not a valid source for " + symbol + ". use: \n" + INTRADAY_SOURCES);
        if (source.equals("Bloomberg"))
            return BloombergTable.BLOOMBERG.intraday(symbol);
        return SYSTEM_ASCII.dataSource(symbol);
    }

    public void insertBloomberg(String symbol, String bloombergTicker, 
        String open, String high, String low, String last, String size, String time
    ) {
        insertBloomberg(symbol, bloombergTicker, open, high, low, last, null, null, size, time);
    }
    
    public String exchange(String market) {
        return C_EXCHANGE.value(C_NAME.is(market));
    }

    public void insertBloomberg(String symbol, String bloombergTicker, String open, String high, String low,
        String bid, String ask, String size, String time) {
        insertBloomberg(symbol, bloombergTicker, open, high, low, "QuantysMid", bid, ask, size, time);
    }

    private void insertBloomberg(String symbol, String bloombergTicker, String open, String high, String low,
        String last, String bid, String ask, String size, String time) {
        insert(C_NAME.with(symbol), C_HISTINTRADAY.with("Bloomberg"), C_LIVE.with("Bloomberg"), C_HISTDAILY.with("Bloomberg"));
        BloombergTable.BLOOMBERG.insert(symbol, bloombergTicker, open, high, low, last, bid, ask, size, time);
    }

    public void insertAsciiIntraday(String symbol, String filename) {
        insert(C_NAME.with(symbol), C_HISTINTRADAY.with("ASCII"));
        SYSTEM_ASCII.insert(symbol, filename, false, 1);
    }

    public void setUseAdjustment(String symbol, boolean adjust) {
        C_USETSDBADJUSTMENT.updateOne(nameMatches(symbol), adjust);
    }

    public boolean isUseAdjustment(String name) {
        return C_USETSDBADJUSTMENT.value(nameMatches(name));
    }

    public boolean has(String name) {
        return rowExists(nameMatches(name));
    }

    public String currency(String name) {
        return C_CURRENCY.value(nameMatches(name));
    }
    
}
