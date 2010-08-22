library("STO")

source(system.file("testHelper.r", package = "STO"))

.setUp <- .tearDown <- function() {
    unlink(recursive=TRUE, squish(dataDir("SimpleCurvesRaggedDates"), "/ABC_1_daily_combined"))
    unlink(recursive=TRUE, squish(dataDir("SimpleCurvesRaggedDates"), "/ABC_1_daily_bigger"))
    unlink(recursive=TRUE, squish(dataDir("SimpleCurves"), "/ABC_1_daily_combined"))
    unlink(recursive=TRUE, squish(dataDir("Curves"), "/NDayBreak_1_daily_PORTFOLIO"))
}

testBasicCurveCubeConstruction <- function() { 
    curve.cube <- realDataCube()
    curve.cube$requireMSIVs(msivs)
}

testCurveCubeLoadsPositionEquityCurves <- function() {
    curve.cube <- realDataCube()
    loader <- CurveFileLoader(CurveCube$filename(dataDir(), msivUS, 8))
    test.curve <- PositionEquityCurve(loader)
    curve.cube$requireCurve(siv$m("@US")[[1]], run = 8, test.curve)
}

testCurveMismatchedRunsBombs <- function() {
    badCube <- CurveCube(dataDir("SimpleCurvesMismatchedRuns"))
    shouldBombMatching(badCube$curve(simpleMsivs[[2]], 4)$pnl(), "file does not exist.*ABC_1_daily_mkt2.*4")
}

testEachMarketMustHaveConsistentDatesInAllRuns <- function() {
    DEACTIVATED("not implemented yet")
    shouldBombMatching(
        CurveCube(dataDir("SimpleCurvesInconsistentDates"))$curve(simpleMsivs[[1]], 2)$pnl(),
        ":2005-11-10 17:00:00, 2005-11-15 17:00:00"
    )
}

testBadDirectoryFails <- function() {
    shouldBomb(CurveCube("foo"))
    shouldBomb(CurveCube(squish(dataDir(), "/NDayBreak_1_daily_@US")))
}

testDatesIsExposed <- function() { 
    curves <- realDataCube()
    curve8 <- curves$curve(msivUS, 8)
    checkSame(curve8$dates(), curves$dates())
}

testCreatesCorrectCommandsForCurveFilesDownload <- function() { 
    if(!isWindows()) return()
    checkSame(
        'QRun Q.Trading.Results.CurveFiles -systemId 39 -runs "39" -skipExisting FALSE',
        CurveCube$.downloadCommand(systemId =39, runs=39)
    )
    checkSame(
        'QRun Q.Trading.Results.CurveFiles -systemId 39 -runs "39,40" -skipExisting TRUE -markets "m3,m4,m5"',
        CurveCube$.downloadCommand(systemId =39, runs=c(39,40), skipExisting=TRUE, markets=c('m3', 'm4', 'm5'))
    )
    

}

