library(QFPortfolio)

jtest <- JTestGroups$by()

.setUp <- function() {
    jtest$setUp()
}

.tearDown <- function() {
    jtest$tearDown()
    jtest$releaseLock()
}

curveDir <- system.file("testdata/GroupCurves", package="QFPortfolio")

testGroupFiles <- function() {
    curve <- CurveGroup("mega")$curve(curveDir, extension="csv")
    checkSame(curve$metric(NetProfit), 6168)
}

# mega is 4 * SP FAST + 1 * SP SLOW + 6 * US FAST + 28 * US SLOW 
# 2007/01/01 to 2007/04/01 by months
# SP.1C FAST pnl 1/1 + 2/1 = 12 * 4
# SP.1C SLOW pnl 1/1 + 2/1 = 150 * 1
# US.1C FAST pnl 2-4 = 0  1-4 = 1000 * 6
# US.1C SLOW pnl 2-4 = 0  1-4 = 1 * 28

testGroupFilesWithMarketCut <- function() {
    jMarket <- JSymbol$by_String("TEST.SP.1C")
    jMarket$addPeriod_by_String_String("2007/01/25", "2007/02/25")
    jMarket$addPeriod_by_String_String("2007/01/01", "2007/01/15")
    jMarket$addPeriod_by_String_String("2007/03/10", "2007/03/25")
    jMarket <- JSymbol$by_String("TEST.US.1C")
    jMarket$addPeriod_by_String_String("2007/02/01", "")
    curve <- CurveGroup("mega")$curve(curveDir, extension="csv", cut=TRUE)
    checkSame(curve$metric(NetProfit), 198)
}

testGroupFilesWithMarketCutWithoutPeriods <- function() {
    jMarket <- JSymbol$by_String("TEST.SP.1C")
    jMarket$addPeriod_by_String_String("2007/01/01", "2007/01/15")
    jMarket$addPeriod_by_String_String("2007/01/25", "2007/02/25")
    jMarket$addPeriod_by_String_String("2007/03/10", "2007/03/25")
    curve <- CurveGroup("mega")$curve(curveDir, extension="csv", cut=TRUE)
    checkSame(curve$metric(NetProfit), 6226)
}

testEndPeriodLandsOnDataPoint <- function() {
    sp <- JSymbol$by_String("TEST.SP.1C")
    sp$addPeriod_by_String_String("2007/01/01", "2007/02/01")
    us <- JSymbol$by_String("TEST.US.1C")
    us$addPeriod_by_String_String("2007/02/01", "")
    curve <- CurveGroup("mega")$curve(curveDir, extension="csv", cut=TRUE)
    checkSame(curve$metric(NetProfit), 198)
    sp$addPeriod_by_String_String("2007/02/02", "2007/02/28")
    curve <- CurveGroup("mega")$curve(curveDir, extension="csv", cut=TRUE)
    checkSame(curve$metric(NetProfit), 198)
}
