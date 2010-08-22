library(QFPairsTrading)
testdataPath <- squish(system.file("testdata", package="QFPairsTrading"),'/ModifiedFuturesPair/')
tempDir <- squish(dataDirectory(),'temp_TSDB/')

readZooFile <- function(filename){
	return(read.zoo(filename,format='%Y-%m-%d',sep=",",header=TRUE))
}

testConstructor <- function() {    
    mc <- ModifiedFuturesPair(market.base = 'RE.TEST.TY.1C', market.hedge = 'RE.TEST.TU.1C')
	checkInherits(mc, "ModifiedFuturesPair")
}

test.missingDates <- function(){
	fp <- ModifiedFuturesPair(market.base = 'AA.1C', market.hedge = 'BB.1C')
	fp$setUnderlyingTRIs(container=squish(testdataPath,'AABB_rawData.csv'))
	fp$setHedgeRatio(specificNames=list(base='aa',hedge='bb'), hedgeRatio.name = 'price_value_basis_point',
											hedgeRatio.source='test', 
											container=squish(testdataPath,'AABB_rawData.csv'))
	fp$generateTRI()
	
	expected <- zoo(c(100,98.81947,87.77605,98.03786,97.96266),as.POSIXct(c('2000-01-02','2000-01-04','2000-01-06','2000-01-08','2000-01-10')))
	checkSameLooking(round(fp$getTRI(),5), expected)
	
	expected <- zoo(c(100,101.46876,93.76005,103.61511,105.82907),as.POSIXct(c('2000-01-02','2000-01-04','2000-01-06','2000-01-08','2000-01-10')))
	fp$generateTRI(hedgeRatio.lag = 1)
	checkSameLooking(round(fp$getTRI(),5), expected)
}

test.frozenTRIs <- function(){
	fp <- ModifiedFuturesPair(market.base = 'RE.TEST.TY.1C', market.hedge = 'RE.TEST.TU.1C')
	fp$setUnderlyingTRIs(container='systemdb')
	fp$setHedgeRatio(specificNames=list(base='ty',hedge='tu'), hedgeRatio.name = 'pvbp', hedgeRatio.source='test',
												container=squish(testdataPath,'RE.TEST.pvbp.csv'))
	
	fp$generateTRI(hedgeRatio.offset = 0)
	fp$uploadTRI(tsdbSource='internal',uploadPath=tempDir,uploadMethod='file')
	
	filename <- 'RE.TEST.TY.1C_RE.TEST.TU.1C_pvbp_test.csv'
	benchFile <- squish(testdataPath,filename)
	uploadFile <- squish(tempDir,filename)

	checkSame(round(readZooFile(benchFile),6),round(readZooFile(uploadFile),6))
	file.remove(uploadFile)
}

test.dataDirect <- function(){
	fp <- ModifiedFuturesPair(market.base = 'RE.TEST.TY.1C', market.hedge = 'RE.TEST.TU.1C')
	fp$setHedgeRatioByName('base','tu_pvbp','test',container=squish(testdataPath,'RE.TEST.pvbp.csv'))
	fp$setHedgeRatioByName('hedge','ty_pvbp','test',container=squish(testdataPath,'RE.TEST.pvbp.csv'))
	checkSame(0.06408, first(fp$.hedgeRatio$hedge))
	checkSame(0.02034, last(fp$.hedgeRatio$base))
}