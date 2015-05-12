## Test file for the DTDRichCheapCalculator object

library(QFCredit)

testDTDRichCheapCalculator <- function()
{
    # tests bad inputs
    
    testDTDrc <- DTDRichCheapCalculator()
    date1 <- "2002-10-15"
    date2 <- "2002-10-16"
    dtdfile <- read.table(system.file("testdata", "testfileDTDRichCheapCalculatorDTDinfo.csv", package = "QFCredit"), sep=",", header = T, row.names = 1)
    oasfile <- read.table(system.file("testdata", "testfileDTDRichCheapCalculatorOASinfo.csv", package = "QFCredit"), sep=",", header = T, row.names = 1)
    
    dtdfile <- zoo(dtdfile, order.by = as.POSIXct(row.names(dtdfile)))
    oasfile <- zoo(oasfile, order.by = as.POSIXct(row.names(oasfile)))
    
    irs <- IRS()
    irs$setDefault()
    irsDataLoad <- IRSDataLoader()
    rates <- irsDataLoad$getCurves("internal", irs, date1,date2)
    
    shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",NULL, oasfile, rates, "5y",FALSE))
    shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, NULL, rates, "5y",FALSE))
    shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, NULL, "5y",FALSE))
    shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, NULL,FALSE))
    shouldBomb(testDTDrc$calcRichCheap(TRUE,dtdfile, oasfile, rates, NULL,FALSE))
    shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, NULL,"SD"))
     
    # tests good inputs
    
    testDTDrc <- DTDRichCheapCalculator()   
    
    testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, "5y", "value", FALSE)
    
    checkEquals(as.numeric(testDTDrc$.rateTable[1,1]), 2.187)
    checkEquals(testDTDrc$.tenor, "5y")
    checkEquals(testDTDrc$.time, 5)
    checkEquals(testDTDrc$.transformationName,"dtd_1.0")
    assert(any(class(testDTDrc$.tsdb) == "TimeSeriesDB"))
    
    checkEquals(as.numeric(round(testDTDrc$.coefficients[1,1],4)), 0.7703)
    checkEquals(as.numeric(round(testDTDrc$.fairValues[1,1],4)), 0.0193)
    checkEquals(as.numeric(round(testDTDrc$.richCheaps[1,1],6)), -0.001117)
    checkEquals(as.numeric(round(testDTDrc$.rsqs[1],2)), 0.75)
    checkEquals(as.numeric(round(testDTDrc$.deltas[1,1],4)),-0.0246)
    
    testDTDrc <- DTDRichCheapCalculator()   
    
    testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, "5y", "price", FALSE)
    
    checkEquals(as.numeric(testDTDrc$.rateTable[1,1]), 2.187)
    checkEquals(testDTDrc$.tenor, "5y")
    checkEquals(testDTDrc$.time, 5)
    checkEquals(testDTDrc$.transformationName,"dtd_1.0")
    checkEquals(as.numeric(round(testDTDrc$.coefficients[1,1],4)), 0.7703)
    checkEquals(as.numeric(round(testDTDrc$.fairValues[1,1],4)), 0.0193)
    checkEquals(as.numeric(round(testDTDrc$.richCheaps[1,1],6)), -0.001117)
    checkEquals(as.numeric(round(testDTDrc$.rsqs[1],2)), 0.75)
    checkEquals(as.numeric(round(testDTDrc$.deltas[1,1],4)), -0.0092)
    
    rm(testDTDrc, date1, date2, dtdfile, oasfile, irs, irsDataLoad, rates)
}


testDTDRichCheapCalculatorNew <- function()
{
	# tests bad inputs
	
	testDTDrc <- DTDRichCheapCalculator()
	date1 <- "2009-04-02"
	date2 <- "2009-04-03"
	dtdfile <- read.table(system.file("testdata", "testfileDTDRichCheapCalculatorDTDinfoNew.csv", package = "QFCredit"), sep=",", header = T, row.names = 1)
	oasfile <- read.table(system.file("testdata", "testfileDTDRichCheapCalculatorOASinfoNew.csv", package = "QFCredit"), sep=",", header = T, row.names = 1)
	
	dtdfile <- zoo(dtdfile, order.by = as.POSIXct(row.names(dtdfile)))
	oasfile <- zoo(oasfile, order.by = as.POSIXct(row.names(oasfile)))
	
	irs <- IRS()
	irs$setDefault()
	irsDataLoad <- IRSDataLoader()
	rates <- irsDataLoad$getCurves("internal", irs, date1,date2)
	
	shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",NULL, oasfile, rates, "5y",FALSE))
	shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, NULL, rates, "5y",FALSE))
	shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, NULL, "5y",FALSE))
	shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, NULL,FALSE))
	shouldBomb(testDTDrc$calcRichCheap(TRUE,dtdfile, oasfile, rates, NULL,FALSE))
	shouldBomb(testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, NULL,"SD"))
	
	# tests good inputs
	
	testDTDrc <- DTDRichCheapCalculator()   
	
	testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, "5y", "value", FALSE)
	
	checkEquals(as.numeric(testDTDrc$.rateTable[1,1]), 1.3041)
	checkEquals(testDTDrc$.tenor, "5y")
	checkEquals(testDTDrc$.time, 5)
	checkEquals(testDTDrc$.transformationName,"dtd_1.0")
	assert(any(class(testDTDrc$.tsdb) == "TimeSeriesDB"))
	
	checkEquals(as.numeric(round(testDTDrc$.coefficients[1,],4)), c(0.3203,0.1453,0.1229,2.2011,0.3264))
	checkEquals(as.numeric(round(testDTDrc$.fairValues[1,2],4)), 0.003)
	checkEquals(as.numeric(round(testDTDrc$.richCheaps[1,1],6)),  -0.131114)
	checkEquals(as.numeric(round(testDTDrc$.richCheaps[1,2],6)),  0.011199)
	checkEquals(as.numeric(round(testDTDrc$.rsqs[1],2)), 0.89)
	checkEquals(as.numeric(round(testDTDrc$.deltas[1,1],4)), -0.1453)
	checkEquals(as.numeric(round(testDTDrc$.deltas[1,2],4)), -0.007)
	
	testDTDrc <- DTDRichCheapCalculator()   
	
	testDTDrc$calcRichCheap("dtd_1.0",dtdfile, oasfile, rates, "5y", "price", FALSE)
	
	checkEquals(as.numeric(testDTDrc$.rateTable[1,1]), 1.3041)
	checkEquals(testDTDrc$.tenor, "5y")
	checkEquals(testDTDrc$.time, 5)
	checkEquals(testDTDrc$.transformationName,"dtd_1.0")
	checkEquals(as.numeric(round(testDTDrc$.coefficients[1,],4)), c(0.3203,0.1453,0.1229,2.2011,0.3264))
	checkEquals(as.numeric(round(testDTDrc$.fairValues[1,2],4)), 0.003)
	checkEquals(as.numeric(round(testDTDrc$.richCheaps[1,1],6)), -0.131114)
	checkEquals(as.numeric(round(testDTDrc$.richCheaps[1,2],6)), 0.011199)
	checkEquals(as.numeric(round(testDTDrc$.rsqs[1],2)), 0.89)
	checkEquals(as.numeric(round(testDTDrc$.deltas[1,1],4)),  -0.0035)
	checkEquals(as.numeric(round(testDTDrc$.deltas[1,2],4)),   -2e-04)
	
	rm(testDTDrc, date1, date2, dtdfile, oasfile, irs, irsDataLoad, rates)
}

testGetLoessFVExcel <- function()
{
    testDTDrc <- DTDRichCheapCalculator()
    shouldBomb(testDTDrc$getLoessFVExcel("junk", 0.82, 1.30, 0.40, 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, "junk", 1.30, 0.40, 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, -1, 1.30, 0.40, 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, "junk", 0.40, 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, -1, 0.40, 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.30, "junk", 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.30, -1, 0.048255, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.30, 0.40, "junk", 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.30, 0.40, -1, 5))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.30, 0.40, 0.048255, "junk"))
    shouldBomb(testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.30, 0.40, 0.048255, -1))
    
    checkEquals(0.117197411580811, testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.3, 0.4, 0, 0.048255, 5)[1])
	checkEquals(0.2387239000434684, testDTDrc$getLoessFVExcel(1.35657961148642, 0.82, 1.3, 0.4, 0.5, 0.048255, 5)[1])
}

testGetOASZero <- function()
{	
	startDate <- '2007-06-01'
	dtddata <- c(1.0566661,1.2368139,1.2073535,1.0480607,1.0480608,0.9047637,1.2843930,2.1473335,2.4500586,NA,3.6277265,1.7672294,1.6765577)
	oasdata <- c(0.027548006,0.022503810,0.020710600,0.049025048,0.025301428,0.038877040,0.014997514,0.002068067,0.037178677,NA,0.002600864,0.004382515,0.018500642)
	loessfun <- loess(oasdata~dtddata, span = 0.9, na.action = na.exclude)
	checkSame(DTDRichCheapCalculator$getOASZero(oasdata,dtddata,nNames = 10),0.0620318101322363)
	checkSame(DTDRichCheapCalculator$getOASZero(oasdata,dtddata,nNames = 5),0.0972532458592727)
}

