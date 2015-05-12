package malbec.fer.mapping;

public interface IDatabaseMapper {

    String lookupAccount(String platform, String strategy, String accountType);

    String lookupAccountType(String platform, String account);

    double lookupFuturesPriceMultipler(String platform, String futuresRootSymbol);

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
    String mapToBloombergSymbol(String platform, String symbolRoot, String maturityMonth);

    /**
     * Try to move this somewhere else
     * 
     * @param futuresSymbol
     * @return
     */
    String extractMaturityMonthFromSymbol(String futuresSymbol);

}