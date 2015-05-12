package malbec.fer.mapping;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import malbec.util.NamedThreadFactory;
import malbec.util.TaskService;

/**
 * Handle all logic for mapping based on Database tables.
 */
public class DatabaseMapper {

    /**
     * Ensure we only do this once.
     */
    static {
        TaskService.getInstance().addExecutor("DBMapper",
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("DBMapper-Reload")));
    }

    final private Logger log = LoggerFactory.getLogger(getClass());

    private StrategyAccountMapper sam = new StrategyAccountMapper();

    private FuturesSymbolMapper fsm = new FuturesSymbolMapper();

    private PlatformSecurity ps = new PlatformSecurity();
    
    private RouteAccountMapper ram = new RouteAccountMapper();

    public DatabaseMapper() {
        this(false);
    }

    public DatabaseMapper(boolean initialize) {
        if (initialize) {
            sam.initialize();
            fsm.initialize();
//            ps.initialize();
            ram.initialize();
        }

        // Schedule the reload to run every 15 minutes
        DateTime now = new DateTime();
        int minutes = now.getMinuteOfHour();
        int normalizedMinutes = minutes % 15;
        int minutesToWait = 15 - normalizedMinutes;

        ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance().getExecutor(
                "DBMapper");

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("DBMapper reload started");
                reloadAll();
                log.info("DBMapper reload finished");
            }

        }, minutesToWait, 15, TimeUnit.MINUTES);

        log.info("Scheduled DBMapper reload timer to start in " + minutesToWait + " minutes");
    }

    /**
     * Reload all database mapped conversions
     */
    private synchronized void reloadAll() {
        sam.reload();
        fsm.reload();
//        ps.reload();
        ram.reload();
    }

    public String lookupAccount(String platform, String strategy, String accountType) {
        return sam.lookupAccount(platform, strategy, accountType);
    }

    public String lookupAccountType(String platform, String account) {
        return sam.lookupAccountType(platform, account);
    }
    
    public void addAccountMapping(String platform, String strategy, String accountType, String account) {
        sam.addMapping(platform, strategy, accountType, account);
    }

    public String mapFuturesSymbol(String platform, String symbolRoot, String defaultValue) {
        return fsm.mapBloombergSymbolToPlatform(platform, symbolRoot, defaultValue);
    }

    public String reverseMapFuturesSymbol(String platform, String symbolRoot) {
        return fsm.mapPlatformToBloomberg(platform, symbolRoot, false);
    }

    public void addFuturesSymbolMapping(String platform, String bloombergSymbol, String platformSymbol) {
        fsm.addMapping(platform, bloombergSymbol, platformSymbol);
    }

    public int mapFuturesMonth(char monthCode) {
        return fsm.codeToMonth(monthCode);
    }

    public boolean canClientTradeOnPlatform(String clientName, String platform) {
        return ps.canSendOrder(clientName, platform);
    }

    public void addPlatformToClient(String clientName, String platform) {
        ps.addPlatformToClient(clientName, platform);
    }

    public double lookupFuturesPriceMultipler(String platform, String futuresRootSymbol) {
        return fsm.lookupFuturesPriceMultipler(platform, futuresRootSymbol);
    }

    public String lookupFuturesProductCode(String platform, String futuresRootSymbol) {
        return fsm.lookupFuturesProductCode(platform, futuresRootSymbol);
    }
    
    public String extractMaturityMonthFromSymbol(String futuresSymbol) {
        return fsm.extractMaturityMonthFromSymbol(futuresSymbol);
    }

    public String mapToBloombergSymbol(String platform, String symbolRoot, String maturityMonth) {
        return fsm.mapToBloombergSymbol(platform, symbolRoot, maturityMonth);
    }

    public String lookupAccountForRoute(String platform, String route) {
        return ram.lookupAccount(platform, route);
    }

}
