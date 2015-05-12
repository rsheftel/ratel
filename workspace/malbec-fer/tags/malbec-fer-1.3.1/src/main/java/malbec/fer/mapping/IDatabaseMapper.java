package malbec.fer.mapping;

import java.math.BigDecimal;

public interface IDatabaseMapper {

    String lookupAccount(String platform, String strategy, String accountType);

    String lookupAccountType(String platform, String account);

    double lookupFuturesInboundPriceMultiplier(String platform, String futuresRootSymbol);

    String lookupFuturesProductCode(String platform, String futuresRootSymbol);

    String lookupAccountForRoute(String platform, String route);

    /**
     * Try to move this somewhere else
     * 
     * @param platform
     * @param symbolRoot
     * @param maturityMonth
     * @return
     */
    String mapPlatformRootToBloombergSymbol(String platform, String symbolRoot, String maturityMonth);

    String mapBloombergRootToRicRoot(String platform, String bloombergRoot);

    BigDecimal lookupFuturesOutboundPriceMultiplier(String platform, String bloombergRoot);

    String mapMarketToBloomberg(String marketTicker);

}