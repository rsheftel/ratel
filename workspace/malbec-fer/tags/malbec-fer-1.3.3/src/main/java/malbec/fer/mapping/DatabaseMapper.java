package malbec.fer.mapping;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import malbec.bloomberg.types.BBYellowKey;
import malbec.util.DateTimeUtil;
import malbec.util.ExecutorConfig;
import malbec.util.SystematicFacade;
import malbec.util.TaskService;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle all logic for mapping based on Database tables.
 */
public class DatabaseMapper implements IDatabaseMapper {

    /**
     * Ensure we only do this once.
     */
    static {
        TaskService.getInstance().createAndAddSingleThreadScheduled("DBMapper");
    }

    final private Logger log = LoggerFactory.getLogger(getClass());

    private StrategyAccountMapper sam = new StrategyAccountMapper();

    private FuturesSymbolMapper fsm = new FuturesSymbolMapper();

    private PlatformSecurity ps = new PlatformSecurity();

    private RouteAccountMapper ram = new RouteAccountMapper();

    private MarketTickersMapper mtm = new MarketTickersMapper();

    private ShortSellItemMapper ssim = new ShortSellItemMapper();

    public DatabaseMapper() {
        this(false);
    }

    public DatabaseMapper(boolean initialize) {
        if (initialize) {
            sam.initialize();
            fsm.initialize();
            // ps.initialize();
            ram.initialize();
            mtm.initialize();
            ssim.initialize(new LocalDate());
        }

        ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance().getExecutor(
            "DBMapper");

        // Schedule the reload to run every 15 minutes on the hour
        ExecutorConfig dms = DateTimeUtil.scheduleEvery(15, "7:00", TimeUnit.MINUTES, TimeUnit.SECONDS)
            .asLargestTimeUnit();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("DBMapper reload started");
                reloadAll();
                log.info("DBMapper reload finished");
            }

        }, dms.getInitialDelay(), dms.getPeriod(), dms.getTimeUnit());

        log.info("Scheduled DBMapper reload timer " + dms);

        // schedule the short sell reload to be every day at 9:15 AM
        ExecutorConfig sss = DateTimeUtil.scheduleEvery(24, "9:15", TimeUnit.HOURS, TimeUnit.MINUTES)
            .inTimeUnit(TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("DBMapper short sell reload started");
                reloadShortSellMapper();
                log.info("DBMapper short sell reload finished");
            }

        }, sss.getInitialDelay(), sss.getPeriod(), sss.getTimeUnit());
        log.info("Scheduled DBMapper short sell reload timer " + sss);

        // schedule the Futures TickSize reload to be every day at 4:00 AM
        ExecutorConfig tss = DateTimeUtil.scheduleEvery(24, "4:00", TimeUnit.HOURS, TimeUnit.MINUTES)
            .inTimeUnit(TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("Systematic cache clear started");
                SystematicFacade.clear();
                log.info("Systematic cache clear finished");
            }

        }, tss.getInitialDelay(), tss.getPeriod(), tss.getTimeUnit());
        log.info("Systematic cache clear timer " + tss);

    }

    /**
     * Reload all database mapped conversions
     */
    private synchronized void reloadAll() {
        sam.reload();
        fsm.reload();
        // ps.reload();
        ram.reload();
        mtm.reload();
    }

    private synchronized void reloadShortSellMapper() {
        ssim.reload();
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

    public String mapBloombergRootToPlatformRoot(String platform, String symbolRoot, String defaultValue) {
        return fsm.lookupPlatformRoot(platform, symbolRoot, defaultValue);
    }

    public String reverseMapFuturesSymbol(String platform, String symbolRoot) {
        return fsm.lookupBloombergRoot(platform, symbolRoot, false);
    }

    public void addFuturesSymbolMapping(String platform, String bloombergRoot, String platformReceivingRoot, String platformSendingRoot, BigDecimal priceMultiplier) {
        fsm.addBloombergMapping(platform, bloombergRoot, platformReceivingRoot, platformSendingRoot, priceMultiplier);
    }

    public boolean canClientTradeOnPlatform(String clientName, String platform) {
        return ps.canSendOrder(clientName, platform);
    }

    public void addPlatformToClient(String clientName, String platform) {
        ps.addPlatformToClient(clientName, platform);
    }

    public BigDecimal lookupFuturesInboundPriceMultiplier(String platform, String futuresRootSymbol) {
        return fsm.lookupToBloombergPriceMultiplier(platform, futuresRootSymbol);
    }

    public String mapPlatformRootToBloombergSymbol(String platform, String symbolRoot, String maturityMonth) {
        return fsm.mapPlatformRootToBloombergSymbol(platform, symbolRoot, maturityMonth);
    }

    public String lookupAccountForRoute(String platform, String route) {
        return ram.lookupAccount(platform, route);
    }

    @Override
    public String mapBloombergRootToPlatformSendingRoot(String platform, String bloombergRoot) {
        return fsm.lookupPlatformSendingRoot(platform, bloombergRoot, false);
    }

    public BigDecimal lookupFuturesOutboundPriceMultiplier(String platform, String bloombergRoot) {
        return fsm.lookupToPlatformPriceMultiplier(platform, bloombergRoot);
    }

    @Override
    public String mapMarketToBloomberg(String marketTicker) {
        return mtm.lookupBloomberg(marketTicker);
    }

    public void addMarketMapping(String marketSymbol, String bloombergSymbol, String yellowKey, String tsdbSymbol, String bbRoot) {
        mtm.addMarketMapping(marketSymbol, bloombergSymbol, yellowKey, tsdbSymbol, bbRoot);
    }

    public BigDecimal addShortShares(String primeBroker, String ticker, int additionalShares) {
        return addShortShares(primeBroker, ticker, BigDecimal.valueOf(additionalShares));
    }

    public BigDecimal addShortShares(String primeBroker, String ticker, BigDecimal additionalShares) {
        return ssim.add(primeBroker, ticker, additionalShares);
    }

    public BigDecimal sharesToShort(String primeBroker, String ticker) {
        return ssim.sharesToShort(primeBroker, ticker);
    }

    @Override
    public BigDecimal subtractShortShares(String primeBroker, String ticker, BigDecimal shares) {
        return ssim.subtract(primeBroker, ticker, shares);
    }

    @Override
    public BBYellowKey lookupYellowKey(String bloombergRoot) {
        return mtm.lookupYellowKey(bloombergRoot);
    }

    @Override
    public String lookupBloombergRootForPlatformRoot(String platform, String platformRoot) {
        return fsm.lookupBloombergRoot(platform, platformRoot, true);
    }

    public void addRouteAccount(String platform, String route, String account) {
        ram.addMapping(platform, route, account);
    }
}
