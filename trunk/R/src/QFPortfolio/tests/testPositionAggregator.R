# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


library(QFPortfolio)

testdata <- squish(system.file("testdata", package="QFPortfolio"),'/AggregateCurves/')
testDir <- squish(testdata,'curves')
ac <- AggregateCurves('TestExposure', verbose = FALSE)
ac$loadCurves(testDir, interval=Interval$DAILY)

test.constructor <- function(){
	checkInherits(PositionAggregator(ac), "PositionAggregator")
	checkSame(PositionAggregator(ac)$aggregateCurves()$atomicCurves('market')[[1]][[1]]$position(),
		 PositionAggregator(curveDirectory = testDir, groupName = 'TestExposure')$aggregateCurves()$atomicCurves('market')[[1]][[1]]$position())
	shouldBombMatching(PositionAggregator('ac'), 'aggregateCurves is not AggregateCurves is character')
	shouldBombMatching(PositionAggregator(curveDirectory = testDir), 'must supply a groupName')
	ac2 <- AggregateCurves('TestExposure')
	shouldBombMatching(PositionAggregator(ac2), 'no aggregateCurves loaded in PositionAggregator')
}

test.Grouped.Positions.And.Risk <- function(){
	pa <- PositionAggregator(ac)
	positions <- pa$groupedPositions(verbose = TRUE)
	checkSame(21, as.numeric(positions[[3]][as.POSIXct('1999-04-08'),]))
	
	risk <- pa$groupedPositionRisk(positions, 60, FALSE, TRUE)
	checkSame(727.608662582029, as.numeric(risk[[3]][as.POSIXct('1999-04-08'),]))	
	
	shouldBombMatching(pa$riskBySector(absoluteValue = FALSE, reportVariance = FALSE), 'if absoluteValue is FALSE, then cannot report sqrts')
	result <- pa$riskBySector(absoluteValue = TRUE, reportVariance = TRUE, groupedPositionList = positions, groupedRisk = risk)
	target1 <- zoo(c(5919.473097625244), order.by = as.POSIXct('1999-04-08'))
	target2 <- zoo(c(727.608662582029), order.by = as.POSIXct('1999-04-08'))
	target <- merge(target1,target2) 
	colnames(target) <- c('Interest Rate', 'Volatility')
	checkSameLooking(target, result[as.POSIXct('1999-04-08'),])
	
	result <- pa$riskBySector(absoluteValue = FALSE, reportVariance = TRUE, groupedPositionList = positions, groupedRisk = risk)
	target1 <- zoo(c(-5919.473097625244), order.by = as.POSIXct('1999-04-08'))
	target2 <- zoo(c(727.608662582029), order.by = as.POSIXct('1999-04-08'))
	target <- merge(target1,target2) 
	colnames(target) <- c('Interest Rate', 'Volatility')
	checkSameLooking(target, result[as.POSIXct('1999-04-08'),])
	
	result <- pa$riskBySector(absoluteValue = TRUE, reportVariance = FALSE, groupedPositionList = positions, groupedRisk = risk)
	target1 <- zoo(c(76.93811212672978), order.by = as.POSIXct('1999-04-08'))
	target2 <- zoo(c(26.97422218678472), order.by = as.POSIXct('1999-04-08'))
	target <- merge(target1,target2) 
	colnames(target) <- c('Interest Rate', 'Volatility')
	checkSameLooking(target, result[as.POSIXct('1999-04-08'),])
}

test.processFXZoo <- function(){
	pa <- PositionAggregator(ac)
	testZoo <- read.csv(squish(testdata,'testFXZoo.csv'))
	testZoo <- zoo(testZoo[,-1], order.by = testZoo[,1])
	
	target <- c(6,-105,205,305,405,-505,-605,-705,-805)
	result <- pa$.processFXZoo(testZoo)
	checkSame(target, as.numeric(result[6,]))
}


