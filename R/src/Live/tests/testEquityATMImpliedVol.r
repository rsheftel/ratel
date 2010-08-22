library(Live)

testConstructor <- function(){
	this <- EquityATMImpliedVol("cah",91,5)
	checkSame(this$.ticker,'cah')
	checkSame(this$.strikeIncrement,5)
	checkSame(this$.daysToExpiration,91)
	this$setDate(as.POSIXct("2009/05/14"))
	checkSame(this$.date,as.POSIXct('2009-05-14'))
	checkSame(this$.yesterday,as.POSIXct('2009-05-13'))
	checkSame(this$.targetExpirationDate,as.POSIXct('2009-08-13'))
}

testInitialize <- function(){
	this <- EquityATMImpliedVol("cah",91,5)
	this$setDate(as.POSIXct("2009/05/14"))
	this$initialize()
	checkSame(class(this$.closeStock),'numeric')
	checkSame(class(this$.strikes),'numeric')
	checkSame(class(this$.yearMonths),'character') 
	checkSame(class(this$.expirations),'character')
	checkSame(class(this$.optionTickers),'matrix')
}

tetsGetOptionTicker <- function(){
	this <- EquityATMImpliedVol("cah",91,5)
	checkSame(this$getOptionTicker('05/09',30,'c'),'CAH 05/09 C30 Equity')
}

testGetYearMonthMaturities <- function(){
	this <- EquityATMImpliedVol("cah",91,5)
	this$setDate(as.POSIXct("2009/05/14"))
	res <- this$yearMonths()
	checkSame(c("2009-05-01","2009-06-01","2009-07-01","2009-08-01","2009-09-01","2009-10-01","2009-11-01","2009-12-01","2010-01-01","2010-02-01","2010-03-01","2010-04-01","2010-05-01"),res)	
}

testGetStrikes <- function(){
	this <- EquityATMImpliedVol("cah",91,5)	
	res <- this$getStrikes(31,5)
	checkSame(c(25,30,35),res)
	this <- EquityATMImpliedVol("cah",91,5)
	res <- this$getStrikes(31,5)
	checkSame(c(25,30,35),res)
	res <- this$getStrikes(31.4,1)
	checkSame(c(30,31,32),res)	
	res <- this$getStrikes(31.6,1)
	checkSame(c(31,32,33),res)
}

testUpdate <- function() {	
	inputs <- list(
		Stock = "EQUITY:||:CAH:||:LastPrice:||:30.1:||:TRUE"
	)
	this <- EquityATMImpliedVol("cah",91,5)
	this$setDate(as.POSIXct(Sys.time()))
	outputs <- this$update(inputs)
	expectedOutputs <- list(
		"EQUITY:||:CAH:||:Last91dImpliedVol:||:whatever",
		"EQUITY:||:CAH:||:TimestampVol:||:whatever"
	)
	checkSame(the(strsplit(expectedOutputs[[1]], ':||:', fixed=TRUE))[-4],the(strsplit(outputs[[1]], ':||:', fixed=TRUE))[-4])
	checkSame(the(strsplit(expectedOutputs[[2]], ':||:', fixed=TRUE))[-4],the(strsplit(outputs[[2]], ':||:', fixed=TRUE))[-4])    
}