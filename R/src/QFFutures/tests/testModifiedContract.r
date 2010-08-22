library(QFFutures)
testdataPath <- squish(system.file("testdata", package="QFFutures"),'/ModifiedContract/')

testConstructor <- function() {    
    mc <- ModifiedContract(market = 'RE.TEST.TY.1C')
	checkInherits(mc, "ModifiedContract")
	mc$specificName('ty')
	checkSame(mc$specificName(),'ty')
}

test.specificYearMonths <- function(){
	yearMonthZoo <- ModifiedContract('RE.TEST.TY.1C')$specificYearMonths()
	checkSame(yearMonthZoo, read.zoo(squish(testdataPath,'specificYearMonths.csv'),FUN=as.POSIXct,header=FALSE,sep=","))
	
	yearMonthZoo <- ModifiedContract('DI.1C')$specificYearMonths()
	checkSame(199512, first(yearMonthZoo))
	checkSame(200903, yearMonthZoo[as.POSIXct('2008-12-08')][[1]])
}

test.specificAttribute <- function(){
	pvbp <- ModifiedContract('RE.TEST.TY.1C', specificName = 'ty')$specificAttribute(quote_convention='price_value_basis_point',
																						source='model_jpmorgan_parallelYieldShift',
																						container=squish(testdataPath,'underlyingPVBP.csv'))
	checkSameLooking(pvbp, read.zoo(squish(testdataPath,'specificPVBP.csv'),FUN=as.POSIXct,header=FALSE,sep=","))
	shouldBomb(ModifiedContract('RE.TEST.TY.1C')$specificAttribute(quote_convention='price_value_basis_point', source='internal'))
}

