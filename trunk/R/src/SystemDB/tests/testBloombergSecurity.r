library(SystemDB)

testBloombergSecurity <- function() {
    tya = BloombergSecurity("TYA Comdty")
    checkSame("100000.0", tya$stringValue("FUT_CONT_SIZE"));
    checkSame(100000, tya$numberValue("FUT_CONT_SIZE"));
    checkSame("IBM", BloombergSecurity("/cusip/459200101")$stringValue("TICKER"));
}


testMultiDayDownload <- function() { 
    usdeur <- BloombergSecurity("USDEUR Curncy")
    results <- usdeur$observations("LAST_PRICE", Range("2009/01/01", "2009/02/01"))
    checkSame(0.7187, the(results[as.POSIXct("2009/01/02")]))
    checkLength(results, 22)
}

testFullMultiDayDownload <- function() { 
    usdeur <- BloombergSecurity("USDEUR Curncy")
    results <- usdeur$observations("LAST_PRICE")
    checkSame(0.7187, the(results[as.POSIXct("2009/01/02")]))
    checkTrue(length(results)>= 2724)
}

testIntradayBars <- function() {
	usdeur <- BloombergSecurity("USDEUR Curncy")
	result <- usdeur$bars("5minute", Range(Sys.time() - 7 * 86400, Sys.time()))
	checkTrue(nrow(result) > 1000)
	checkTrue(nrow(result) < 3000)
}
