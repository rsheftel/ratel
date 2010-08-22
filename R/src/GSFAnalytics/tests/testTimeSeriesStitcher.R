# test the TimeSeriesStitcher object

library("GSFAnalytics")
tsdb <- TimeSeriesDB()

testTimeSeriesStitcher <- function()
{
    out(RNGkind())
    # get some sample data
    testTRI <- getMergedTimeSeries(tsdb,"cdx-na-ig_tri_daily_5y_otr","internal",
        start = "2005-03-21", end = "2005-04-30")
        
    testLevels <- getMergedTimeSeries(tsdb,"cdx-na-ig-hvol_market_spread_5y_otr","internal",
        start = "2005-03-21", end = "2005-04-30",filter = "15:00:00")* 10000
    
    multiTRI <- merge(testTRI,testTRI)
    
    multiLevels <- merge(testLevels, testLevels)
    
    # test bad inputs
    
    tss <- TimeSeriesStitcher()
    
    
    shouldBomb(tss$loadZooObj("junk"))
    shouldBomb(tss$chooseWindows("junk"))
    shouldBomb(tss$chooseWindows(-1))
    shouldBomb(tss$restitchTRISeries("junk",10,TRUE))
    shouldBomb(tss$restitchTRISeries(testTRI,-1,TRUE))
    shouldBomb(tss$restitchTRISeries(testTRI,"junk",TRUE))
    shouldBomb(tss$restitchTRISeries(testTRI,10,"junk"))
    shouldBomb(tss$restitchLevelsSeries("junk",10,TRUE))
    shouldBomb(tss$restitchLevelsSeries(testLevels,-1,TRUE))
    shouldBomb(tss$restitchLevelsSeries(testLevels,"junk",TRUE))
    shouldBomb(tss$restitchLevelsSeries(testLevels,10,"junk"))
    
    
    shouldBomb(tss$restitchMultipleSeries("junk", 10, TRUE, "levels"))
    shouldBomb(tss$restitchMultipleSeries(multiTRI, "junk", TRUE, "levels"))
    shouldBomb(tss$restitchMultipleSeries(multiTRI, 10, "junk", "levels"))
    shouldBomb(tss$restitchMultipleSeries(multiTRI, 10, TRUE, NULL))
    
    shouldBomb(tss$restitchTRIandSpreads("junk", 10, TRUE, "5y", "financialcalendar", "nyb", "internal"))
    shouldBomb(tss$restitchTRIandSpreads(testLevels, "junk", TRUE, "5y", "financialcalendar", "nyb", "internal"))
    shouldBomb(tss$restitchTRIandSpreads(testLevels, 10, "junk", "5y", "financialcalendar", "nyb", "internal"))
    shouldBomb(tss$restitchTRIandSpreads(testLevels, 10, TRUE, "junk", "financialcalendar", "nyb",  "internal"))
    shouldBomb(tss$restitchTRIandSpreads(testLevels, 10, TRUE, "5y", 10000, "nyb", TRUE, "internal"))
    shouldBomb(tss$restitchTRIandSpreads(testLevels, 10, TRUE, "5y", "financialcalendar", 10000,  "internal"))
    shouldBomb(tss$restitchTRIandSpreads(testLevels, 10, TRUE, "5y", "financialcalendar", "nyb",  10000))
    
        
    # test good inputs
    
    set.seed(12345, 'default')
    
    tss <- TimeSeriesStitcher()
    tss$loadZooObj(testTRI)
    checkEquals(last(testTRI), last(tss$.zooObj))
    checkEquals(last(testTRI), last(tss$.tsObj))
    checkEquals(29, tss$.lenObj)
    
    tss$chooseWindows(10)
    checkEquals(10, tss$.winLength)
    checkEquals(3, tss$.numWindows)
    
    set.seed(12345,'default')
    checkEquals(99.8246, round(tss$restitchTRISeries(testTRI, 10, FALSE, FALSE)[2],4))
    
    
    tss <- TimeSeriesStitcher()
    set.seed(12345,'default')
    checkEquals(91.3095, round(tss$restitchLevelsSeries(testLevels, 10, FALSE, FALSE)[2],4))
    
    set.seed(12345,'default')
    testOutput <- tss$restitchMultipleSeries(multiTRI, 10, TRUE, "tri")
    checkEquals(99.8246, round(as.numeric(testOutput[2,1]),4))
    checkEquals(99.8246, round(as.numeric(testOutput[2,2]),4))
    
    set.seed(12345,'default')
    #testOutput <- tss$restitchTRIandSpreads(multiLevels,10,FALSE, "5y", "financialcalendar", "nyb", "internal")
    #checkEquals(91.3095, as.numeric(round(testOutput[[1]][2,1],4)))
    #checkEquals(91.3095, as.numeric(round(testOutput[[1]][2,2],4)))
    #checkEquals(99.82575, as.numeric(round(testOutput[[2]][2,1],5)))
    #checkEquals(99.82575, as.numeric(round(testOutput[[2]][2,2],5)))
}