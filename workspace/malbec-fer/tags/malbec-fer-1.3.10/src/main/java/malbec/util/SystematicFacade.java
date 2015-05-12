package malbec.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import malbec.bloomberg.types.BBYellowKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bloomberg.BloombergSecurity;

public class SystematicFacade {

    static final Logger log = LoggerFactory.getLogger(SystematicFacade.class);

    private static final Object lock = new Object();

    private static final BigDecimal DEFAULT_TICKSIZE = new BigDecimal("0.015625"); // 1/64

    private static Map<String, BigDecimal> tickSizeCache = new HashMap<String, BigDecimal>();

    private static Map<String, Long> contractSizeCache = new HashMap<String, Long>();
    private static Map<String, String> equityTicker = new HashMap<String, String>();
    private static Map<String, String> equityTickerExchange = new HashMap<String, String>();

    private static Map<String, String> bloombergToTradingScreen = new HashMap<String, String>();

    public static BigDecimal lookupFuturesTickSize(String bloombergSymbol, BBYellowKey yellowKey) {

        String bloombergKey = bloombergSymbol + " " + yellowKey.toString();
        synchronized (lock) {
            BigDecimal tickSize = tickSizeCache.get(bloombergKey);
            if (tickSize != null) {
                return tickSize;
            }

            try {
                String tickSizeString = BloombergSecurity.security(bloombergKey).string("FUT_TICK_SIZE");
                tickSize = new BigDecimal(tickSizeString);
                tickSizeCache.put(bloombergKey, tickSize);
                return tickSize;
            } catch (Exception e) {
                log.warn("Unable to lookup bloomberg security tick size: " + bloombergKey);
            }
        }
        return DEFAULT_TICKSIZE;
    }

    public static long lookupContractSize(String bloombergSymbol, BBYellowKey productCode) {
        // Equities all have a 1 for the contract size
        if (productCode == BBYellowKey.Equity) {
            return 1;
        }

        synchronized (lock) {
            String bloombergKey = bloombergSymbol + " " + productCode.toString();

            Long contractSize = contractSizeCache.get(bloombergKey);
            if (contractSize != null) {
                return contractSize;
            }

            try {
                long cs = (long) BloombergSecurity.security(bloombergKey).numeric("FUT_CONT_SIZE");
                contractSizeCache.put(bloombergKey, cs);
                return cs;
            } catch (Exception e) {
                log.warn("Unable to lookup bloomberg security contract size: " + bloombergKey);
            }
        }
        return 1;
    }

    public static String lookupExchangeTicker(String cusip) {
        synchronized (lock) {
            String ticker = equityTicker.get(cusip);

            if (ticker != null) {
                return ticker;
            }
            try {
                StringBuilder queryStr = new StringBuilder(128);
                queryStr.append("/cusip/").append(cusip);

                ticker = BloombergSecurity.security(queryStr.toString()).string("TICKER");
                equityTicker.put(cusip, ticker);

                return ticker;
            } catch (Exception e) {
                log.error("Unable to lookup ticker for: " + cusip);
            }
        }
        return "";
    }

    private static void initializeExchangeMapping() {
        // XNGS - nasdaq
        bloombergToTradingScreen.put("ARCX", "ARCA"); // NYSE ARCA
        bloombergToTradingScreen.put("XNGS", "INET"); // Instinet NASDAQ
        // bloombergToTradingScreen.put("XNGS", "NITE"); // Knight Trading - Multi-Market ECN
        // bloombergToTradingScreen.put("XNGS", "BATS"); // major NASDAQ ECN
        bloombergToTradingScreen.put("XNYS", "NYSE");
    }

    public static String lookupTickerPrimaryExchange(String ticker) {
        if (bloombergToTradingScreen.size() == 0) {
            initializeExchangeMapping();
        }

        synchronized (lock) {
            String tsExchange = equityTickerExchange.get(ticker);

            if (tsExchange != null) {
                return tsExchange;
            }
            try {
                StringBuilder queryStr = new StringBuilder(128);
                queryStr.append(ticker).append(" US Equity");

                String mic = BloombergSecurity.security(queryStr.toString()).string("ID_MIC_PRIM_EXCH");
                String otherExchange = BloombergSecurity.security(queryStr.toString())
                    .string("EQY_PRIM_EXCH");
                log.info("Ticker exchange is: " + ticker + ", " + mic + ", " + otherExchange);

                tsExchange = bloombergToTradingScreen.get(mic);

                if (tsExchange != null) {
                    equityTickerExchange.put(ticker, tsExchange);
                    return tsExchange;
                } else {
                    log.error("Unable to map ticker to exchange:" + ticker);
                }

                return "NYSE";
            } catch (Exception e) {
                log.error("Unable to lookup exchange for: " + ticker);
            }
        }
        return "";
    }

    public static void clear() {
        synchronized (lock) {
            tickSizeCache.clear();
            contractSizeCache.clear();
            equityTicker.clear();
            equityTickerExchange.clear();
        }
    }
}
