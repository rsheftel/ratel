## Test file for the CreditUtilities class
library(QFCredit)

test.getSystemDBCreditMarketName <- function()
{
	shouldBomb(getSystemDBCreditMarketName())
	shouldBomb(getSystemDBCreditMarketName(cdsTickerID = T,tenor = "5y"))
	shouldBomb(getSystemDBCreditMarketName(cdsTickerID = "gm_snrfor_usd_mr",tenor = "5"))
	shouldBomb(getSystemDBCreditMarketName(cdsTickerID = "gm_snrf_usd_mr",tenor = "5y"))
	shouldBomb(getSystemDBCreditMarketName(cdsTickerID = "gm_snrfor_usd_mr",tenor = "5y",toUpper = "1"))
	
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "gm_snrfor_usd_mr",tenor = "5y"),
			"GM5M"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "attinc-ml_snrfor_usd_mr",tenor = "5y"),
			"ATTIM5M"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "cmcsa-cablellc_snrfor_usd_mr",tenor = "5y"),
			"CMCSC5M"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "gm_snrfor_usd_xr",tenor = "5y"),
			"GM5X"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "gm_snrfor_usd_xr",tenor = "10y"),
			"GM10X"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "gm_sublt2_usd_xr",tenor = "10y"),
			"GM10V"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "gm_sublt2_usd_mr",tenor = "10y"),
			"GM10U"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "gm_sublt2_usd_mr",tenor = "10y",toUpper = FALSE),
			"gm10U"
	)
	
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "cdx-na-ig-hvol",tenor = "5y"),
			"CNAIGHV5"
	)
	checkEquals(
			getSystemDBCreditMarketName(cdsTickerID = "cdx-na-ig-hvol",tenor = "10y"),
			"CNAIGHV10"
	)
	
}

test.getMarketFromTicker <- function()
{
	checkSame(getMarketFromTicker('cah',FALSE),'CAH5M')
	checkSame(getMarketFromTicker('cah',TRUE),'cah5M')
}

testGetRefBase <- function()
{	
	shouldBomb(getRefBase())
	shouldBomb(getRefBase(currentData = data.frame(1)))
	shouldBomb(getRefBase(currentData = zoo(1,"2005-01-01"),refDate = 5))
	
	tri <- data.frame(c(100,99.551904,99.1235))
	rownames(tri) <- c("2007-05-01","2007-05-02","2007-05-03")
	colnames(tri) <- "tri"
	tri <- zoo(tri,as.POSIXct(rownames(tri)))
	
	checkEquals(list(refDate = as.POSIXct("2007-05-03"),refIndex = 99.1235),getRefBase(tri,"2008-01-01"))        
	checkEquals(list(refDate = NULL,refIndex = 100),getRefBase(tri,NULL))
	checkEquals(list(refDate = as.POSIXct("2005-01-01"),refIndex = 100),getRefBase(tri,"2005-01-01"))    
	checkEquals(list(refDate = as.POSIXct("2007-05-02"),refIndex = 99.551904),getRefBase(tri,"2007-05-03"))
}