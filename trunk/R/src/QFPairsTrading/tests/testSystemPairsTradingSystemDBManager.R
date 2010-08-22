## Test file for the SystemPairsTradingSystemDBManager object
library(QFPairsTrading)

pairNames <- c("ESIG","ESTY")
versionString <- "1.0"
versionNumeric <- 10
transformationName <- "rolling_regression"
window <- 20
systemDescription <- "pairs trading"
systemName <- "LiqInj"
systemDBUploadPath <- squish(dataDirectory(), "/SystemDB_upload/pairs trading/")
this <- SystemPairsTradingSystemDBManager(
		pairNames = pairNames,
		transformationName = transformationName,
		versionString = versionString,
		systemDescription = systemDescription,
		systemName = systemName,
		window = window
)

test.SystemPairsTradingSystemDBManager.constructor <- function(){
    checkSame(this$.pairNames,pairNames)
    checkSame(this$.versionString,versionString)
	checkSame(this$.versionNumeric,versionNumeric)
    checkSame(this$.transformationName,transformationName)
	checkSame(this$.systemDBUploadPath,systemDBUploadPath)
    checkSame(this$.systemName,systemName)
	checkSame(this$.window,window)
}