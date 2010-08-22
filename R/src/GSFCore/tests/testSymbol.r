

library(GSFCore)

testCsiSymbol <- function() { 
	dout("hello")
    s <- Symbol("TY.1C")
    checkTrue(s$barCount() >= 5254)
    #checkSameLooking("1982-05-03", s$firstBarDate())	
    series <- s$series()
    checkShape(series, rows = s$barCount(), cols = 6, colnames=c("open", "high", "low", "close", "openInterest", "volume"))
}

testTsdbNulls <- function() { 
    s <- Symbol("DTD10.GM5M")
    series <- s$series()
    checkShape(series, rows = s$barCount(), cols = 6)
    checkTrue(is.na(last(series[, "volume"])))
    checkTrue(is.na(first(series[, "openInterest"])))    
}