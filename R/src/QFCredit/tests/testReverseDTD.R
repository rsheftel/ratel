library(QFCredit)

tickerList <- c('arm','axl-inc','cooper','abc','liz')

revDTD <- ReverseDTD(startDate = '2009-07-15',endDate = '2009-07-17', tickerList = tickerList)

testReverseDTDConstructorFailures <- function(){
	shouldBombMatching(ReverseDTD(startDate = 1, endDate = '2009-07-17', tickerList = tickerList, tenor = '5y', ccy = 'usd', tier = 'snrfor'), 'startDate is not character')	
	shouldBombMatching(ReverseDTD(startDate = '2009-07-17', endDate = 1, tickerList = tickerList, tenor = '5y', ccy = 'usd', tier = 'snrfor'), 'endDate is not character')
	shouldBombMatching(ReverseDTD(startDate = '2009-07-17', endDate = '2009-07-17', tickerList = 1, tenor = '5y', ccy = 'usd', tier = 'snrfor'), 'tickerList is not character')
	shouldBombMatching(ReverseDTD(startDate = '2009-07-17', endDate = '2009-07-17', tickerList = tickerList, tenor = 1, ccy = 'usd', tier = 'snrfor'), 'tenor is not character')
	shouldBombMatching(ReverseDTD(startDate = '2009-07-17', endDate = '2009-07-17', tickerList = tickerList, tenor = '5y', ccy = 1, tier = 'snrfor'), 'ccy is not character')
	shouldBombMatching(ReverseDTD(startDate = '2009-07-17', endDate = '2009-07-17', tickerList = tickerList, tenor = '5y', ccy = 'usd', tier = 1), 'tier is not character')
	shouldBombMatching(ReverseDTD(startDate = 1, endDate = '2009-07-17', tickerList = tickerList, tenor = '5y', ccy = 'usd', tier = 'snrfor'), 'startDate is not character')
}

testReverseDTDConstructor <- function(){
	checkEquals(as.POSIXct('2009-07-15'), index(revDTD$.fvs)[[1]])
	checkEquals(c(104.9665, NA, 552.4481, 116.3093, 811.7735), round(as.numeric(revDTD$.fvs[2,]), 4))
	checkEquals(c(1.5849494, NA, -0.3243852, 1.4611489, -0.9209816), round(as.numeric(revDTD$.dtds[2,]), 7))
	checkEquals(c(1846.04316, NA, 634.94801, 46.83703, 1085.52585), round(as.numeric(revDTD$.oas[2,]), 5))
	checkEquals(c(3.79, 1.41, 12.31, 18.51, 2.93), round(as.numeric(revDTD$.stocks[2,]), 2))
	checkEquals(c(73.960, 55.476, 58.952, 301.600, 95.081), round(as.numeric(revDTD$.shares[2,]), 3))
	checkEquals(c(0.01804699, NA, 0.39404887, 0.12738867, 0.48939312), round(as.numeric(revDTD$.sigmaA[2,]), 8))
	checkEquals(c(4413.8381, NA, 1937.2751, 15012.5099, 928.4985), round(as.numeric(revDTD$.assets[2,]), 4))	
}

testReverseDTDMultipleDayRichCheaps <- function(){
	target <- c(-2.952745830779669, NA, 7.100018047118730, 11.159092035439523, 1.293367313935602)
	checkEquals(target, as.numeric(format(revDTD$multipleDayRichCheaps()[2,], dig = 22)))	
}

testReverseDTDRsqs <- function(){
	target <- c(0.07092485438, 0.13163568629, 0.12062210851)
	checkEquals(target, as.numeric(format(revDTD$rsqs(), dig = 10)))
}



