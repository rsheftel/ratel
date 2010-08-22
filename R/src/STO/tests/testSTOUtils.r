library(STO)
source(system.file("testHelper.r", package = "STO"))

fileMatches <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="STO"),'/STOUtils/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))	
	return(checkSame(benchData,testData))
}

test.constructor <- function(){
	sto <- STOUtils()
	checkInherits(sto, "STOUtils")
}

test.mergeMultipleMsivsSingleRun <- function(){
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	
	curve1 <- sto$curves()$curve(sto$msivs()[[1]], 1)$equity()
	curve2 <- sto$curves()$curve(sto$msivs()[[2]], 1)$equity()
	mergedCurves <- merge.zoo(curve1*0.5 + curve2*0.5)
	expected <- merge.zoo(merge.zoo(curve1,curve2),curve1*0.5 + curve2*0.5)
	expected[is.na(expected[,3]),3] <- 0
	colnames(expected) <- c('CVE_10_Daily_CET10.AEP5M','CVE_10_Daily_CET10.AES5X','Average')
	checkSame(STOUtils$mergeMultipleMsivsSingleRun(sto$curves(),sto$msivs(),1), expected)
}

test.mergeSingleMsivMultipleRuns <- function(){
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	
	curve1 <- sto$curves()$curve(sto$msivs()[[2]], 1)$equity()
	curve2 <- sto$curves()$curve(sto$msivs()[[2]], 3)$equity()
	mergedCurves <- merge.zoo(curve1*0.5 + curve2*0.5)
	expected <- merge.zoo(merge.zoo(curve1,curve2),curve1*0.5 + curve2*0.5)
	expected[is.na(expected[,3]),3] <- 0
	colnames(expected) <- c('1','3','Average')
	result <- STOUtils$mergeSingleMsivMultipleRuns(sto$curves(),sto$msivs()[[2]],c(1,3))
	result <- result[index(result) >= first(index(expected))]
	checkSame(result, expected)
}

test.msivsRunsToZoo <- function(){
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	
	checkSame(STOUtils$msivsRunsToZoo(sto$curves(),sto$msivs(),1), STOUtils$mergeMultipleMsivsSingleRun(sto$curves(),sto$msivs(),1))
	checkSame(STOUtils$msivsRunsToZoo(sto$curves(),sto$msivs()[[2]],c(1,3)), STOUtils$mergeSingleMsivMultipleRuns(sto$curves(),sto$msivs()[[2]],c(1,3)))
	shouldBomb(STOUtils$msivsRunsToZoo(sto$curves(),sto$msivs()[[2]],1))
	shouldBomb(STOUtils$msivsRunsToZoo(sto$curves(),sto$msivs(),1:3))
}

test.mergeMsivsAcrossRuns <- function(){
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	
	msiv1 <- (sto$curves()$curve(sto$msivs()[[1]], 1)$equity() + sto$curves()$curve(sto$msivs()[[1]], 2)$equity() + sto$curves()$curve(sto$msivs()[[1]], 3)$equity())/3
	msiv2 <- (sto$curves()$curve(sto$msivs()[[2]], 1)$equity() + sto$curves()$curve(sto$msivs()[[2]], 2)$equity() + sto$curves()$curve(sto$msivs()[[2]], 3)$equity())/3
	curves <- merge(msiv1,msiv2,(msiv1+msiv2)/2)
	curves[is.na(curves)] <- 0
	colnames(curves) <- c('CVE_10_Daily_CET10.AEP5M','CVE_10_Daily_CET10.AES5X','Average')
	
	checkSame(STOUtils$mergeMsivsAcrossRuns(sto$curves(),sto$msivs(),c(1,2,3)), curves)
}

test.portfolioMatrix <- function(){
	sto <- STO(stoDirectory(), "PortfolioSTO")
	expected <- cbind(c('CET10.AEP5M','CET10.AES5X'),c(0.5,0.5))
	colnames(expected) <- c('','portfolioWeights')
	checkSameLooking(STOUtils$portfolioMatrix(sto,'EvenWeights'), expected)
}

test.metricParameterSurface <- function(){
	sto <- STO(stoDirectory(), "SimpleSTOTemplate")
	result <- STOUtils$getMetricParameterSurface(sto,sto$metrics(),sto$msivs()[[3]],list(TSGrossProfit,TSClosePositionProfit,TSGrossLoss))
	benchmarkFile <- read.csv(squish(system.file("testdata", package="STO"),'/STOUtils/metricParameterSurface.csv'))
	checkSame(result, benchmarkFile)
	
	filtered <- STOUtils$filterMetricParameterSurface(result,TSGrossProfit,10,100)
	checkSame(filtered,benchmarkFile[1,])
	filtered <- STOUtils$filterMetricParameterSurface(result,TSGrossLoss,-100,0)
	checkSame(filtered,benchmarkFile[c(1,3),])
	filtered <- STOUtils$filterMetricParameterSurface(result,list(TSClosePositionProfit,TSGrossLoss),c(10,-100),c(1000,0))
	checkSame(filtered,benchmarkFile[3,])
	shouldBomb(STOUtils$filterMetricParameterSurface(result,list(TSClosePositionProfit,TSGrossLoss),c(10,-100),c(1000)))
	shouldBomb(STOUtils$filterMetricParameterSurface(result,TSClosePositionProfit,c(10,-100),c(1000,0)))
}

test.simultaneousTrading <- function(){
	
	# General case
	
	dates <- c('2009-01-01','2009-01-02','2009-01-03','2009-01-04','2009-01-05','2009-01-06')
	positionZoos <- list()
	positionZoos[[1]] <- zoo(c(0,1,0,1,3,2,0,5),as.POSIXct(c(dates,'2009-01-07','2009-01-08')))
	positionZoos[[2]] <- zoo(c(0,0,1,-2,3,0),as.POSIXct(dates))
	
	res <- STOUtils$.simultaneousTrading(positionZoos,TRUE)
	frame <- data.frame(MSIVsTraded = c(0,1,2),NumberOfBars=c(1,3,2),PercentOfTradedBars = c(NA,0.6,0.4),PercentOfTotalBars = c(1/6,0.5,2/6))
	checkSame(res,frame)
	
	res <- STOUtils$.simultaneousTrading(positionZoos,FALSE)
	frame <- data.frame(MSIVsTraded = c(0,1,2),NumberOfBars=c(2,4,2),PercentOfTradedBars = c(NA,2/3,1/3),PercentOfTotalBars = c(0.25,0.5,0.25))
	checkSame(res,frame)
	
	# No overlapping dates
	
	index(positionZoos[[1]]) <- as.POSIXct(as.character(as.Date(index(positionZoos[[1]])) + 20))
	checkSame(STOUtils$.simultaneousTrading(positionZoos,TRUE),NULL)
	
	res <- STOUtils$.simultaneousTrading(positionZoos,FALSE)
	frame <- data.frame(MSIVsTraded = c(0,1),NumberOfBars=c(6,8),PercentOfTradedBars = c(NA,1),PercentOfTotalBars = c(6/14,1-6/14))
	checkSame(res,frame)
	
	# simultaneousTradingFrame function
	
	sto <- STO(stoDirectory(), "SimpleSTOTemplate")
	res <- STOUtils$simultaneousTrading(sto$curves(),sto$msivs()[1:3],c(1,1,1))
	frame <- data.frame(MSIVsTraded = c(0,1,2,3),NumberOfBars=c(0,0,1,2),PercentOfTradedBars = c(NA,0,1/3,2/3),PercentOfTotalBars = c(0,0,1/3,2/3))
	checkSame(res,frame)
	
	shouldBomb(STOUtils$simultaneousTrading(sto$curves(),sto$msivs()[1:3],c(1,1)))
	shouldBomb(STOUtils$simultaneousTrading(sto$curves(),sto$msivs()[1:1],c(1,1,1)))
}