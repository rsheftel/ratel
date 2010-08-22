library(Live)
library("STO")

source(system.file("testHelper.r", package = "STO"))

testWFOConstructorWithNoStartEndDates <- function() {
	wfo <- createWFO()
	sto <- wfo$.sto
	checkSame(sto,wfo$.sto)
	checkSame("WFO",wfo$.name)
	checkSame(sto$msivs(),wfo$.msivs)
	checkSame(Range(as.POSIXct("2002-09-27"),as.POSIXct("2007-10-05")),wfo$.range)
	# Set up checks
	checkTrue(file.exists(wfo$.dir))
	checkTrue(file.exists((wfo$.curvesPath)))
	checkTrue(file.exists((wfo$.optimalRunsPath)))
	destroyWFO()
	# Bad inputs
	shouldBomb(WFO(name = TRUE,sto = sto,msivs = sto$msivs()[1:2],startDate = startDate,endDate = endDate))
	shouldBomb(WFO(name = name,sto = "notASto",msivs = sto$msivs()[1:2],startDate = startDate,endDate = endDate))
	shouldBomb(WFO(name = name,sto = sto,msivs = sto$msivs()[[1]],startDate = startDate,endDate = endDate))
	shouldBomb(WFO(name = name,sto = sto,msivs = sto$msivs()[1:2],startDate = TRUE,endDate = endDate))
	shouldBomb(WFO(name = name,sto = sto,msivs = sto$msivs()[1:2],startDate = startDate,endDate = TRUE))
	shouldBomb(WFO(name = name,sto = sto,msivs = sto$msivs()[1:2],startDate = endDate,endDate = startDate))
}

testWFOConstructorWithStartEndDates <- function() {
	wfo <- createWFO("2003-01-01","2004-01-01")
	checkSame(Range(as.POSIXct("2003-01-01"),as.POSIXct("2004-01-01")),wfo$.range)
	destroyWFO()
}

testSchedule <- function() {
	expRollCommonChecks <- function(){
		checkShape(schedule,5,3,colnames = c("step","startDate","endDate"))
		checkSame(schedule[1,"endDate"],as.POSIXct("2003-09-27"))
		checkSame(schedule[1,"startDate"],as.POSIXct("2002-09-27"))
		checkSame(schedule[5,"endDate"],as.POSIXct("2007-09-26"))
	}
	# Expanding
	wfo <- createWFO()
	shouldBomb(wfo$schedule())
	schedule <- wfo$createSchedule("2003-09-27",365)
	expRollCommonChecks()
	checkSame(schedule[5,"startDate"],schedule[1,"startDate"])
	checkSame(wfo$nbSteps(),5)
	checkSame(wfo$steps(),1:5)
	# Rolling
	schedule <- wfo$createSchedule("2003-09-27",365,365)
	expRollCommonChecks()
	checkSame(schedule[5,"startDate"],as.POSIXct("2006-09-26"))
	# Bad inputs
	shouldBomb(wfo$createSchedule("2000-09-27",365,365))
	shouldBomb(wfo$createSchedule("2003-09-27",365,900))
	shouldBomb(wfo$createSchedule("2010-09-27",365,365))
	shouldBomb(wfo$createSchedule(TRUE,365,365))
	shouldBomb(wfo$createSchedule("2010-09-27",TRUE,365))
	shouldBomb(wfo$createSchedule("2010-09-27",365,TRUE))
	destroyWFO()
}

testMSIVsCharacters <- function() {
	# Two MSIVs
	wfo <- createWFO()
	checkSame(wfo$msivsCharacter(),c("CVE_10_Daily_CET10.AEP5M","CVE_10_Daily_CET10.AES5X"))
	destroyWFO()
	# One MSIV
	wfo <- createWFOOneMSIV()
	checkSame(wfo$msivsCharacter(),"CVE_10_Daily_CET10.AEP5M")
	destroyWFO()
}

testAddPortfolios <- function() {
	wfo <- createWFO()
	schedule <- wfo$createSchedule("2003-09-27",365)
	pw <- wfo$calcPortfolioWeights(p.EqualWeighting)
	shouldBomb(wfo$portfolioNames())
	portfolioNames <- wfo$addPortfolios()
	checkSame(portfolioNames,data.frame(step = 1:5,portfolioNames = "WFOPort1",stringsAsFactors = FALSE))
	checkSame(wfo$portfolioNames(),portfolioNames)
	destroyWFO()
}

testSummaryAndCalcParameterizedMsivs <- function() {
	wfo <- createWFO()
	wfo$createSchedule("2003-09-27",365)
	wfo$calcPortfolioWeights(p.EqualWeighting)
	wfo$addPortfolios()
	wfo$calcOptimalRuns("LiqInjETF",or.LiqInjETF,steps = 1:5,nBest = 2, q = 0.5, optiMetric = NetProfit)
	shouldBomb(wfo$summary("DoesNotExist"))
	checkShape(wfo$summary("LiqInjETF"),5,7,colnames = c("step","startDate","endDate","portfolioNames","optimalRuns","CVE_10_Daily_CET10.AEP5M","CVE_10_Daily_CET10.AES5X"))
	shouldBomb(wfo$calcParameterizedMsivs("DoesNotExist",TRUE))
	wfo$calcParameterizedMsivs("LiqInjETF",TRUE)
	checkTrue(wfo$.optimizedPnlCurvesLookup$has("LiqInjETF"))
	checkTrue(wfo$.parameterizedMsivsLookup$has("LiqInjETF"))
	destroyWFO()
}

testOptimizedAndStitchedEquityCurves <- function(){
	wfo <- createWFO()
	wfo$createSchedule("2005-09-27",365)
	wfo$calcPortfolioWeights(p.EqualWeighting)
	wfo$addPortfolios()
	or.testOptimalRuns <- function(wfo,range,msiv){
		if(range$.end == wfo$ranges()[[1]]$.end)return(1)
		if(range$.end == wfo$ranges()[[2]]$.end)return(2)
		if(range$.end == wfo$ranges()[[3]]$.end)return(3)
	}
	wfo$calcOptimalRuns("testOptimalRuns",or.testOptimalRuns,steps = 1:3)
	shouldBomb(wfo$oneOptimizedEquityCurve(6,"testOptimalRuns"))
	shouldBomb(wfo$oneOptimizedEquityCurve("junk","testOptimalRuns"))
	shouldBomb(wfo$oneOptimizedEquityCurve(1,"doesNotExist"))
	z1Cut <- wfo$oneOptimizedEquityCurve(1,"testOptimalRuns",TRUE)
	z1Long <- wfo$oneOptimizedEquityCurve(1,"testOptimalRuns",FALSE)
	assert(class(z1Cut) == "zoo" & class(z1Long) == "zoo" & NROW(z1Cut) < NROW(z1Long))
	z2Cut <- wfo$oneOptimizedEquityCurve(2,"testOptimalRuns",TRUE)
	z2Long <- wfo$oneOptimizedEquityCurve(2,"testOptimalRuns",FALSE)
	z3Cut <- wfo$oneOptimizedEquityCurve(3,"testOptimalRuns",TRUE)
	z3Long <- wfo$oneOptimizedEquityCurve(3,"testOptimalRuns",FALSE)
	shouldBomb(wfo$stitchEquityCurve("junk",ec.untilNextOptimization))
	shouldBomb(wfo$stitchEquityCurve("testOptimalRuns",TRUE))
	zStitched <- wfo$stitchEquityCurve("testOptimalRuns",ec.untilNextOptimization)
	merged <- data.frame(merge(z1Cut,z2Cut,z3Cut,zStitched,z1Long,z2Long,z3Long))
	target <- data.frame(read.csv(system.file("testdata/wfoSTO","testOptimizedAndStitchedEquityCurves.csv", package = "STO"), sep = ",", header = TRUE))
	rownames(target) <- rownames(merged)
	checkSame(target,merged)
	shouldBomb(wfo$equityCurve(TRUE,"testOptimalRuns",ec.untilNextOptimization))
	shouldBomb(wfo$equityCurve("myCurve","junk",ec.untilNextOptimization))
	shouldBomb(wfo$equityCurve("myCurve","testOptimalRuns",TRUE))
	curve <- wfo$equityCurve("myCurve","testOptimalRuns",ec.untilNextOptimization)
	checkSame(curve,zStitched)
	destroyWFO()
}