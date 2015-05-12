package malbec.fer.mapping;

import java.math.BigDecimal;

import malbec.bloomberg.types.BBYellowKey;

public interface IDatabaseMapper {

    String lookupAccount(String platform, String strategy, String accountType);

    String lookupAccountType(String platform, String account);

    BigDecimal lookupFuturesInboundPriceMultiplier(String platform, String futuresRootSymbol);

    String lookupAccountForRoute(String platform, String route, String accountType);

    String mapPlatformRootToBloombergSymbol(String platform, String symbolRoot, String maturityMonth);

    String mapBloombergRootToPlatformSendingRoot(String platform, String bloombergRoot);

    BigDecimal lookupFuturesOutboundPriceMultiplier(String platform, String bloombergRoot);

    String mapMarketToBloomberg(String marketTicker);

    BigDecimal subtractShortShares(String primeBroker, String ticker, BigDecimal shares);
    
    BigDecimal addShortShares(String primeBroker, String ticker, BigDecimal additionalShares);

    BBYellowKey lookupYellowKey(String symbolRoot);
    
    String lookupBloombergRootForPlatformRoot(String platform, String platformRoot);
    
    String lookupStrategy(String platform, String account);
    
    String lookupBloombergCountryCode(String fixExchange);
}