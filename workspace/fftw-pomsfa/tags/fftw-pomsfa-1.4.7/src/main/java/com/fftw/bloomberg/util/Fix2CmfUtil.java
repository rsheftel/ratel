package com.fftw.bloomberg.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;

import com.fftw.bloomberg.types.AccountType;
import com.fftw.bloomberg.types.TradingPlatform;

/**
 * This class handles most of the logic for converting FIX tags to Bloomberg
 * CMFP values.
 * 
 * 
 */
public class Fix2CmfUtil
{
    private static final Logger log = LoggerFactory.getLogger(Fix2CmfUtil.class);

    private static final Timer refreshTimer = new Timer("MappingRefreshTimer", true);

    /**
     * FIX exchange to Bloomberg Country Code
     */
    private static final Map<String, String> fixExchange2BCC = new HashMap<String, String>();

    private static List<StrategyAccountEntry> strategyAccount = new ArrayList<StrategyAccountEntry>();

    private static Map<String, String> tickerToBbuid = new HashMap<String, String>();

    private static Map<String, String> fixBic2BbBroker = new HashMap<String, String>();

    private static Map<PairKey<TradingPlatform, AccountType>, String> primeBroker = new HashMap<PairKey<TradingPlatform, AccountType>, String>();

    private static DateTime loadTime;

    private static final String STRATEGY_ACCOUNT_SELECT = "select TS.PLATFORM_ID, FixTAG, TagValue, "
        + "TS.BloombergAccount, TS.BloombergStrategy from TRADING_STRATEGY TS";

    private static final String USER_MAPPED_STRATEGY = "select UMS.PLATFORM_ID, UMS.FixTAG, UMS.TagValue, "
        + "TS.BloombergAccount, TS.BloombergStrategy from TRADING_STRATEGY TS, USER_MAPPED_STRATEGY UMS "
        + "where TS.PLATFORM_ID = UMS.PLATFORM_ID and UPPER(TS.TagValue) = UPPER(UMS.BloombergStrategy)"
        + " and TS.FixTAG = UMS.FixTAG";

    // private static final String INSERT_USER_MAPPED_STRATEGY = "insert into
    // USER_MAPPED_STRATEGY"
    // + " (PLATFORM_ID, FixTAG, TagValue, BloombergStrategy) " + "values ( ?,
    // ?, ?, ? ) ";

    private static final String PRIME_BROKER_SELECT = "select * from PrimeBroker";

    private static final String BANK_ID_CODE_SELECT = "select * from BankIdentifierCode";

    private static final String TICKER_TO_BBUID_SELECT = "select * from TickerToUniqueId";

    private static SimpleJdbcTemplate jdbcTemplate;

    /**
     * Reload the internal maps so that we can pickup database changes.
     */
    public synchronized static void reload ()
    {
        // clear all the internal data
//        fixExchange2BCC.clear();
//        futuresMapping.clear();
//        strategyAccount.clear();
//        tickerToBbuid.clear();
//        fixBic2BbBroker.clear();
//        primeBroker.clear();

        initializeMaps();
    }

    /**
     * Load all the internal mapping logic.
     * 
     * Should be called by the container (Spring/Mule)
     */
    public static void initializeMaps ()
    {
        initializeExchanges();
        strategyAccount = initializeStrategyAccounts();
        tickerToBbuid = initializeTickerToBbuid();
        fixBic2BbBroker = initializeFixBic2BbBroker();
        primeBroker = initializePrimeBroker();

        if (loadTime == null)
        {
            // TODO convert to using the Ferret scheduler calculator
            // initialize the refresh timer task - try to ensure it
            // is always XX:15, XX:30, XX:45 and XX:00
            DateTime now = new DateTime();
            int minutes = now.getMinuteOfHour();
            int normalizedMinutes = minutes % 15;
            int minutesToWait = 15 - normalizedMinutes;
            long delay = minutesToWait * 1000L * 60L;

            refreshTimer.schedule(new RefreshTimer(), delay, 1000 * 60 * 15);
            log.info("Scheduled refresh timer to start in " + minutesToWait + " minutes");
        }

        loadTime = new DateTime();
    }

    private static Map<String, String> initializeFixBic2BbBroker ()
    {
        List<String[]> mappings = jdbcTemplate.query(BANK_ID_CODE_SELECT, new Bic2BbcRowMapper());

        Map<String, String> tmpMap = new HashMap<String, String>();
        
        for (String[] data : mappings)
        {
            String bic = data[0];
            String bbc = data[1];
            tmpMap.put(bic, bbc);
        }
        
        return tmpMap;
    }

    private static List<StrategyAccountEntry> initializeStrategyAccounts ()
    {
        // Build our 'union'
        StringBuilder sb = new StringBuilder(STRATEGY_ACCOUNT_SELECT.length()
            + USER_MAPPED_STRATEGY.length() + 20);

        sb.append(STRATEGY_ACCOUNT_SELECT).append(" union ").append(USER_MAPPED_STRATEGY);
        List<StrategyAccountEntry> mappings = jdbcTemplate.query(sb.toString(),
            new StrategyAccountRowMapper());
        
        List<StrategyAccountEntry> tmpList = new ArrayList<StrategyAccountEntry>();
        
        tmpList.addAll(mappings);
        
        return tmpList;
    }

    public static void addStrategyAccountMapping(TradingPlatform platform, int fixTag,
        String tagValue, String account, String strategy) {
        
        StrategyAccountEntry sae = new StrategyAccountEntry(platform, fixTag, tagValue, account, strategy);
        strategyAccount.add(sae);
    }

    /**
     * This maps the FIX 4.2 tag SecurityExchage (207) to Bloomberg
     * CompositeExchange codes
     */
    private static void initializeExchanges ()
    {
        fixExchange2BCC.put("A", "US"); // 
        fixExchange2BCC.put("B", "US");
        fixExchange2BCC.put("W", "US");
        fixExchange2BCC.put("MW", "US");
        fixExchange2BCC.put("C", "US");
        fixExchange2BCC.put("O", "US");
        fixExchange2BCC.put("N", "US");
        fixExchange2BCC.put("P", "US");
        fixExchange2BCC.put("PH", "US");
        fixExchange2BCC.put("PNK", "US");
        // Other assigned numeric values
        fixExchange2BCC.put("1", "US"); // American Stock Exchange Options
        fixExchange2BCC.put("42", "US"); // ARCAEDGE Bulletin Board
        fixExchange2BCC.put("39", "US"); // Archipelago Exchange
        fixExchange2BCC.put("54", "US"); // BATS
        fixExchange2BCC.put("56", "US"); // Direct Edge X
        // Goldman Sachs internal exchanges
        // 209 - US -- Goldman SIGMA
        // Futures exchanges - not needed when sending to Bloomberg
        fixExchange2BCC.put("2", "US"); // CME in TradeStation
        fixExchange2BCC.put("CBT", "US"); // CBOT in TradeStation
        // Trading Screen does not send the correct value for THM exchange
        fixExchange2BCC.put("THM", "US"); // Third Market in TradingScreen
        fixExchange2BCC.put("TH", "US"); // Third Market real value
    }

    private static Map<PairKey<TradingPlatform, AccountType>, String> initializePrimeBroker ()
    {
        List<PairKey<PairKey<TradingPlatform, AccountType>, String>> mappings = jdbcTemplate.query(
            PRIME_BROKER_SELECT, new PrimeBrokerRowMapper());

        Map<PairKey<TradingPlatform, AccountType>, String> tmpMap = new HashMap<PairKey<TradingPlatform, AccountType>, String>();
        
        for (PairKey<PairKey<TradingPlatform, AccountType>, String> tableEntry : mappings)
        {
            PairKey<TradingPlatform, AccountType> key = tableEntry.firstPart;
            String value = tableEntry.secondPart;
            tmpMap.put(key, value);
        }
        
        return tmpMap;
    }

    private static  Map<String, String> initializeTickerToBbuid ()
    {
        List<String[]> mappings = jdbcTemplate.query(TICKER_TO_BBUID_SELECT,
            new TickerToBbuidRowMapper());

        Map<String, String> tmpMap = new HashMap<String, String>();
        
        for (String[] data : mappings)
        {
            String ticker = data[0];
            String bbuid = data[1];
            tmpMap.put(ticker, bbuid);
        }
        
        return tmpMap;
    }

    /**
     * Map FIX tag 207 to the Bloomberg 'Composite Exchange Code'.
     * 
     * @param fixExchange
     * @return
     */
    private static String fixExchangeToBCC (SecurityExchange fixExchange)
    {
        String bloombergExchange = fixExchange2BCC.get(fixExchange.getValue().toUpperCase());

        if (bloombergExchange == null)
        {
            log.error("No matching Bloomberg exchange for FIX value " + fixExchange.getValue());
        }
        return bloombergExchange;
    }

    public synchronized static String mapPrimeBroker (TradingPlatform platform,
        AccountType accountType)
    {
        PairKey<TradingPlatform, AccountType> key = new PairKey<TradingPlatform, AccountType>(
            platform, accountType);

        return primeBroker.get(key);
    }

    public synchronized static String mapPrimeBroker (TradingPlatform platform,
        AccountType accountType, String defaultValue)
    {
        String primeBroker = mapPrimeBroker(platform, accountType);

        if (primeBroker == null)
        {
            return defaultValue;
        }
        else
        {
            return primeBroker;
        }
    }

    /**
     * Lookup the Bloomberg unique ID for the specified ticker.
     * 
     * @param ticker
     * @return the BBUID or null if not found
     */
    public synchronized static String tickerToBloombergUniqueId (String ticker)
    {
        return tickerToBbuid.get(ticker);
    }

    /**
     * Build the Bloomberg <tt>options</tt> symbol based on the:
     * <ol>
     * <li>root symbol</li>
     * <li>exchange</li>
     * <li>month of trade</li>
     * <li>put or call</li>
     * <li>strike price</li>
     * </ol>
     * 
     * @param rootSymbol
     * @param exchange
     * @param monthYear
     * @param putOrCall
     * @param strikePrice
     * @return
     */
    public synchronized static String optionsSymbol (Symbol rootSymbol, SecurityExchange exchange,
        MaturityMonthYear monthYear, PutOrCall putOrCall, StrikePrice strikePrice)
    {
        String bcec = Fix2CmfUtil.fixExchangeToBCC(exchange);
        if (bcec == null)
        {
            log.warn("Unknown exchange '" + exchange.getValue() + "' Defaulting to US");
            bcec = "US";
        }

        String month = monthYear.getValue().substring(4, 6);
        String putCall = putOrCall.getValue() == 1 ? "C" : "P";

        String strikePriceStr = String.format("%.2f", strikePrice.getValue());
        StringBuilder sb = new StringBuilder(50);
        sb.append(rootSymbol.getValue()).append(" ");
        sb.append(bcec).append(" ");
        sb.append(month).append(" ");
        sb.append(putCall);
        sb.append(strikePriceStr);

        return sb.toString();
    }

    public synchronized static String equitySymbol (Symbol symbol, SecurityExchange exchange)
    {
        String bcec = Fix2CmfUtil.fixExchangeToBCC(exchange);
        if (bcec == null)
        {
            log.warn("Unknown exchange '" + exchange.getValue() + "' Defaulting to US");
            bcec = "US";
        }

        return symbol.getValue() + " " + bcec;
    }

    public synchronized static String bloombergAccount (TradingPlatform platform, Message message)
    {
        try
        {
            for (StrategyAccountEntry entry : strategyAccount)
            {
                if (entry.platform.equals(platform) && message.isSetField(entry.fixTag))
                {
                    String tagValue = message.getString(entry.fixTag).toUpperCase();
                    if (entry.tagValue.equalsIgnoreCase(tagValue))
                    {
                        return entry.account;
                    }
                }
            }
        }
        catch (FieldNotFound e)
        {
            // This will not happen since we check if it is set first
        }
        return null;
    }

    public synchronized static String bloombergStrategy (TradingPlatform platform, Message message)
    {
        try
        {
            for (StrategyAccountEntry entry : strategyAccount)
            {
                if (entry.platform.equals(platform) && message.isSetField(entry.fixTag))
                {
                    String tagValue = message.getString(entry.fixTag).toUpperCase();
                    if (entry.tagValue.equalsIgnoreCase(tagValue))
                    {
                        return entry.strategy;
                    }
                }
            }
        }
        catch (FieldNotFound e)
        {
            // This will not happen since we check if it is set first
        }
        return null;
    }

    /**
     * Check the message and the mapping table to determine what the missing
     * mapping value is.
     * 
     * @param platform
     * @param message
     * @return
     */
    public synchronized static Map<Integer, String> determineMissingMappingValues (
        TradingPlatform platform, Message message)
    {
        // find all the fix tag values we are checking
        Map<Integer, String> fixTagValue = new HashMap<Integer, String>();

        try
        {
            for (StrategyAccountEntry entry : strategyAccount)
            {
                if (entry.platform.equals(platform))
                {
                    if (message.isSetField(entry.fixTag))
                    {
                        String tagValue = message.getString(entry.fixTag).toUpperCase();
                        fixTagValue.put(entry.fixTag, tagValue);
                    }
                    else
                    {
                        fixTagValue.put(entry.fixTag, "");
                    }
                }
            }
        }
        catch (FieldNotFound e)
        {
            // This will not happen since we check if it is set first
        }

        // Do not add to the table automatically
        // for (int tag : fixTagValue.keySet())
        // {
        // try
        // {
        // Object[] parameters = new Object[4];
        //
        // parameters[0] = platform.getText();
        // parameters[1] = tag;
        // parameters[2] = fixTagValue.get(tag);
        // parameters[3] = "TEST";
        //
        // jdbcTemplate.update(INSERT_USER_MAPPED_STRATEGY, parameters);
        // }
        // catch (DataAccessException e)
        // {
        // // ignore, assuming duplicates
        // }
        // }

        return fixTagValue;
    }

    public synchronized static String mapExecutingBrokerToBloomberg (String bic, String defaultCode)
    {

        String executingBroker = fixBic2BbBroker.get(bic);
        if (executingBroker == null)
        {
            return defaultCode;
        }
        else
        {
            return executingBroker;
        }

    }

    public synchronized static String getLoadTimeAsString ()
    {
        return loadTime.toString();
    }

    public void setDataSource (DataSource dataSource)
    {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    private static class StrategyAccountEntry
    {
        private TradingPlatform platform;

        private int fixTag;

        private String tagValue;

        private String account;

        private String strategy;

        StrategyAccountEntry (TradingPlatform platform, int tag, String tagValue, String account,
            String strategy)
        {
            this.platform = platform;
            fixTag = tag;
            this.tagValue = tagValue;
            this.account = account;
            this.strategy = strategy;
        }

        public TradingPlatform getTradingPlatform ()
        {
            return platform;
        }

        public int hashCode ()
        {
            return fixTag * 17 + tagValue.hashCode();
        }

        @Override
        public boolean equals (Object obj)
        {
            if (obj instanceof StrategyAccountEntry)
            {
                return equalsEntry((StrategyAccountEntry)obj);
            }
            return false;
        }

        private boolean equalsEntry (StrategyAccountEntry obj)
        {
            return fixTag == obj.fixTag && tagValue.equals(obj.tagValue)
                && account.equals(obj.tagValue) && strategy.equals(obj.strategy);
        }

        public String toString ()
        {
            return "platform=" + platform + ", tag=" + fixTag + ", tagValue=" + tagValue + ", account=" + account
                + ", strategy=" + strategy;
        }
    }

    private static class StrategyAccountRowMapper implements
        ParameterizedRowMapper<StrategyAccountEntry>
    {
        public StrategyAccountEntry mapRow (ResultSet rs, int rowNum) throws SQLException
        {
            TradingPlatform platform = TradingPlatform.valueFor(rs.getString("PLATFORM_ID")
                .toUpperCase());
            int tag = rs.getInt("FixTag");
            String tagValue = rs.getString("TagValue");
            String account = rs.getString("BloombergAccount");
            String strategy = rs.getString("BloombergStrategy");

            return new StrategyAccountEntry(platform, tag, tagValue, account, strategy);
        }
    }

    private static class TickerToBbuidRowMapper implements ParameterizedRowMapper<String[]>
    {
        public String[] mapRow (ResultSet rs, int rowNum) throws SQLException
        {
            String ticker = rs.getString("BloombergTicker").toUpperCase();
            String bbuid = rs.getString("BloombergUniqueIdentifier").toUpperCase();

            return new String[]
            {
                ticker, bbuid
            };
        }
    }

    private static class Bic2BbcRowMapper implements ParameterizedRowMapper<String[]>
    {
        public String[] mapRow (ResultSet rs, int rowNum) throws SQLException
        {
            String bic = rs.getString("BIC").toUpperCase().trim();
            String bbc = rs.getString("BloombergCode").toUpperCase().trim();

            return new String[]
            {
                bic, bbc
            };
        }
    }

    private static class PairKey<A, B>
    {
        private A firstPart;

        private B secondPart;

        public PairKey (A first, B second)
        {
            this.firstPart = first;
            this.secondPart = second;
        }

        public A getFirstPart ()
        {
            return firstPart;
        }

        public B getSecondPart ()
        {
            return secondPart;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals (Object obj)
        {
            if (obj instanceof PairKey)
            {
                PairKey<A, B> pair = (PairKey<A, B>)obj;
                return firstPart.equals(pair.firstPart) && secondPart.equals(pair.secondPart);
            }
            return false;
        }

        @Override
        public int hashCode ()
        {
            return firstPart.hashCode() * 17 + secondPart.hashCode();
        }

        @Override
        public String toString ()
        {
            return firstPart + ", " + secondPart;
        }
    }

    private static class PrimeBrokerRowMapper implements
        ParameterizedRowMapper<PairKey<PairKey<TradingPlatform, AccountType>, String>>
    {
        public PairKey<PairKey<TradingPlatform, AccountType>, String> mapRow (ResultSet rs,
            int rowNum) throws SQLException
        {
            TradingPlatform platform = TradingPlatform.valueFor(rs.getString("PLATFORM_ID")
                .toUpperCase());
            AccountType accountType = AccountType.valueFor(rs.getString("AccountType")
                .toUpperCase());

            String execBroker = rs.getString("PrimeBroker").toUpperCase();

            PairKey<TradingPlatform, AccountType> key = new PairKey<TradingPlatform, AccountType>(
                platform, accountType);

            PairKey<PairKey<TradingPlatform, AccountType>, String> tableEntry = new PairKey<PairKey<TradingPlatform, AccountType>, String>(
                key, execBroker);

            return tableEntry;
        }
    }

    private static class RefreshTimer extends TimerTask
    {
        @Override
        public void run ()
        {
            try
            {
                log.info("Refreshing mapping data");
                Fix2CmfUtil.reload();
                log.info("Refreshed mapping data");
            }
            catch (Exception e)
            {
                log.error("Issue when refreshing mapping tables", e);
            }
        }
    }

    /**
     * Used only for testing
     */
    @Deprecated
    public static void clearStrategyMapping ()
    {
        strategyAccount.clear();
    }
}