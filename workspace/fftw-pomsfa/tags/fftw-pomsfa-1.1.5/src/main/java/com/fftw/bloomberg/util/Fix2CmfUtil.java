package com.fftw.bloomberg.util;

import java.math.BigDecimal;
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
import org.joda.time.LocalDate;
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

import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.datetime.DateTimeUtil;

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

    private static final Map<PairKey<TradingPlatform, String>, SymbolMappingEntry> futuresMapping = new HashMap<PairKey<TradingPlatform, String>, SymbolMappingEntry>();

    private static final List<StrategyAccountEntry> strategyAccount = new ArrayList<StrategyAccountEntry>();

    private static DateTime loadTime;

    private static final String STATEGY_ACCOUNT_SELECT = "select * from TRADING_STRATEGY";

    private static final String FUTURES_SYMBOL_SELECT = "select PLATFORM_ID, PLATFORM_SYMBOL, BLOOMBERG_SYMBOL, PRODUCT_CODE, MULTIPLIER "
        + "from FUTURES_SYMBOL_MAPPING f, PRODUCT_CODE pc "
        + "where f.BLOOMBERG_SECTOR=pc.TEXT_CODE";

    private static SimpleJdbcTemplate jdbcTemplate;

    /**
     * Reload the internal maps so that we can pickup database changes.
     */
    public synchronized static void reload ()
    {
        // clear all the internal data
        fixExchange2BCC.clear();
        futuresMapping.clear();
        strategyAccount.clear();
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
        initializeFutures();
        initializeStrategyAccounts();

        if (loadTime == null)
        {
            // initialize the refresh timer task - try to ensure it
            // is always XX:15, XX:30, XX:45 and XX:00
            DateTime now = new DateTime();
            int minutes = now.getMinuteOfHour();
            int normalizedMinutes = minutes % 15;
            int minutesToWait = 15 - normalizedMinutes;
            long delay = minutesToWait * 1000 * 60;

            refreshTimer.schedule(new RefreshTimer(), delay, 1000 * 60 * 15);
            log.info("Scheduled refresh timer to start in " + minutesToWait + " minutes");
        }

        loadTime = new DateTime();
    }

    private static void initializeStrategyAccounts ()
    {
        List mappings = jdbcTemplate.query(STATEGY_ACCOUNT_SELECT, new StrategyAccountRowMapper());
        strategyAccount.addAll(mappings);
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
        // Other assigned numberic values
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
    }

    /**
     * Initialize TradeStation Futures mapping
     */
    private static void initializeFutures ()
    {
        List<PairKey<PairKey<TradingPlatform, String>, SymbolMappingEntry>> mappings = jdbcTemplate
            .query(FUTURES_SYMBOL_SELECT, new FutureSymbolRowMapper());

        for (PairKey<PairKey<TradingPlatform, String>, SymbolMappingEntry> tableEntry : mappings)
        {
            PairKey<TradingPlatform, String> key = tableEntry.firstPart;
            SymbolMappingEntry value = tableEntry.secondPart;
            futuresMapping.put(key, value);
        }
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

    public synchronized static int mapFuturesProductCode (TradingPlatform platform, Symbol fixSymbol)
    {
        PairKey<TradingPlatform, String> key = new PairKey<TradingPlatform, String>(platform,
            fixSymbol.getValue());

        SymbolMappingEntry values = futuresMapping.get(key);
        return values == null ? 1 /* commodity */: values.productCode;
    }

    public synchronized static BigDecimal mapFuturesPriceMultiplier (TradingPlatform platform,
        Symbol fixSymbol)
    {
        PairKey<TradingPlatform, String> key = new PairKey<TradingPlatform, String>(platform,
            fixSymbol.getValue());

        SymbolMappingEntry values = futuresMapping.get(key);
        return values == null ? BigDecimal.ONE : values.priceMultiplier;
    }

    /**
     * Map TradeStation FIX Drop Copy Futures symbols to
     * 
     * @param fixSymbol
     * @return
     */
    private static String mapFuturesSymbol (TradingPlatform platform, Symbol fixSymbol)
    {
        PairKey<TradingPlatform, String> key = new PairKey<TradingPlatform, String>(platform,
            fixSymbol.getValue());

        SymbolMappingEntry values = futuresMapping.get(key);

        String rtValue = values == null ? fixSymbol.getValue() : values.symbol;

        return ensureLength(rtValue, 2);
    }

    private static String ensureLength (String str, int length)
    {

        if (str.length() < length)
        {
            StringBuilder sb = new StringBuilder(length * 2);
            sb.append(str);
            for (int i = str.length(); i < length; i++)
            {
                sb.append(" ");
            }

            return sb.toString();
        }
        return str;
    }

    /**
     * Build the Bloomberg <tt>futures</tt> symbol base on:
     * 
     * <ol>
     * <li>root symbol</li>
     * <li>month</li>
     * <li>year without the century</li>
     * </ol>
     * 
     * @param rootSymbol
     * @param monthYear
     * @return
     */
    public synchronized static String futureSymbol (TradingPlatform platform, Symbol rootSymbol,
        MaturityMonthYear monthYear)
    {
        StringBuilder sb = new StringBuilder(24);
        sb.append(mapFuturesSymbol(platform, rootSymbol));

        // Create a local date so we can extract the year and month
        // This may not be the fastest method, but it is the safest
        LocalDate ld = DateTimeUtil.getLocalDate(monthYear.getValue() + "01");

        int year = ld.getYearOfCentury();
        int month = ld.getMonthOfYear();

        switch (month)
        {
            case 1: // January
                sb.append("F");
                break;
            case 2: // February
                sb.append("G");
                break;
            case 3: // March
                sb.append("H");
                break;
            case 4: // April
                sb.append("J");
                break;
            case 5: // May
                sb.append("K");
                break;
            case 6: // June
                sb.append("M");
                break;
            case 7: // July
                sb.append("N");
                break;
            case 8: // August
                sb.append("Q");
                break;
            case 9: // September
                sb.append("U");
                break;
            case 10: // October
                sb.append("V");
                break;
            case 11: // November
                sb.append("X");
                break;
            case 12: // December
                sb.append("Z");
                break;
            default:
                throw new IllegalArgumentException("Invalid trade trade month for futures trade: "
                    + monthYear.getValue());
        }

        sb.append(year);

        return sb.toString();
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
                    if (entry.platform.equals(platform) && entry.tagValue.equals(tagValue))
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
                    if (entry.tagValue.equals(tagValue))
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

    public synchronized static String getLoadTimeAsString ()
    {
        return loadTime.toString();
    }

    public void setDataSource (DataSource dataSource)
    {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    private static class SymbolMappingEntry
    {
        private String symbol;

        private Integer productCode;

        private BigDecimal priceMultiplier;

        public SymbolMappingEntry (String symbol, Integer productCode, BigDecimal multiplier)
        {
            this.symbol = symbol;
            this.productCode = productCode;
            this.priceMultiplier = multiplier;
        }

        public String getSymbol ()
        {
            return symbol;
        }

        public Integer getProductCode ()
        {
            return productCode;
        }

        public BigDecimal getPriceMultiplier ()
        {
            return priceMultiplier;
        }
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
            return "tag=" + fixTag + ", tagValue=" + tagValue + ", account=" + account
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

    private static class FutureSymbolRowMapper implements
        ParameterizedRowMapper<PairKey<PairKey<TradingPlatform, String>, SymbolMappingEntry>>
    {
        public PairKey<PairKey<TradingPlatform, String>, SymbolMappingEntry> mapRow (ResultSet rs,
            int rowNum) throws SQLException
        {
            TradingPlatform platform = TradingPlatform.valueFor(rs.getString("PLATFORM_ID")
                .toUpperCase());
            String symbol = rs.getString("PLATFORM_SYMBOL").toUpperCase();

            String bbSymbol = rs.getString("BLOOMBERG_SYMBOL").toUpperCase();
            int productCode = rs.getInt("PRODUCT_CODE");
            BigDecimal multiplier = rs.getBigDecimal("MULTIPLIER");

            PairKey<TradingPlatform, String> key = new PairKey<TradingPlatform, String>(platform,
                symbol);
            SymbolMappingEntry value = new SymbolMappingEntry(bbSymbol, productCode, multiplier);
            PairKey<PairKey<TradingPlatform, String>, SymbolMappingEntry> tableEntry = new PairKey<PairKey<TradingPlatform, String>, SymbolMappingEntry>(
                key, value);

            return tableEntry;
        }
    }

    private static class RefreshTimer extends TimerTask
    {
        @Override
        public void run ()
        {
            log.info("Refreshing mapping data");
            Fix2CmfUtil.reload();
            log.info("Refreshed mapping data");
        }

    }
}
