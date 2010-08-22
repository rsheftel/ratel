## Test file for the RollingRegressionBuilder object

library(QFPairsTrading)

window <- 5
tsdb <- TimeSeriesDB()
dataZoo <- getMergedTimeSeries(
    tsdb,
    c("gm_snrfor_usd_mr_spread_3y","gm_snrfor_usd_mr_spread_5y","gm_snrfor_usd_mr_spread_10y"),
    "internal",
    startDate = "2008-01-01",endDate = "2008-01-10"
)*1000

stringFormula <- "dataZoo[,1]~dataZoo[,-1]"

test.RollingRegressionBuilder.constructor <- function(){
    this <- RollingRegressionBuilder(window)
    checkEquals(this$.window,window)
    shouldBomb(RollingRegressionBuilder(TRUE))
}

test.RollingRegressionBuilder.runLinearModel <- function(){
    this <- RollingRegressionBuilder(window)

    # base case
    outputObj <- this$runLinearModel(dataZoo,stringFormula)
    needs(outputObj = "RegressionOutputs")
    checkEquals(outputObj$.r2,zoo(as.matrix(c(0.999399293025988,0.999169299774747,0.972314175123514)),as.POSIXct(c("2008-01-08","2008-01-09","2008-01-10"))))
    
    # case where the model specification implies fewer dataZoo observations
    stringFormulaDiff <- "diff(dataZoo[,1])~diff(dataZoo[,-1])"
    this$runLinearModel(dataZoo,stringFormulaDiff)
    
    # one date
    dataZoo <- dataZoo[1:5,]
    outputObj <- this$runLinearModel(dataZoo,stringFormula)
    checkEquals(outputObj$.r2,zoo(as.matrix(c(0.999399293025988)),as.POSIXct(c("2008-01-08"))))
    
    shouldBomb(this$runLinearModel(dataZoo,"does not exist"))
    dataZoo <- dataZoo[1:4,]
    shouldBomb(this$runLinearModel(dataZoo,stringFormula))
}

test.RollingRegressionBuilder.runLinearModelWithWeights <- function(){
	this <- RollingRegressionBuilder(window,weighting = 'halfLife')
		
	outputObj <- this$runLinearModel(dataZoo,stringFormula)	
	checkEquals(round(outputObj$.r2,7),zoo(as.matrix(c(0.9995874,0.9997439,0.9967999)),as.POSIXct(c("2008-01-08","2008-01-09","2008-01-10"))))
}

test.RollingRegressionBuilder.getWeights <- function(){
	this <- RollingRegressionBuilder(5,weighting = 'halfLife')
	w <- this$getWeights(5)
	checkSame(c(0.5000000,0.5743492,0.6597540,0.7578583,0.8705506),round(w,7))
	w <- this$getWeights(2)
	checkSame(c(0.7578583,0.8705506),round(w,7))
	this <- RollingRegressionBuilder(3,weighting = 'halfLife')
	w <- this$getWeights(5)
	checkSame(c(0.3149803,0.3968503,0.5000000,0.6299605,0.7937005),round(w,7))
}