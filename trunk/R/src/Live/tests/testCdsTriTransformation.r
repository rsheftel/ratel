library(Live)

testConstructor <- function(){
	this <- CdsTriTransformation("cah")
	checkSame(this$.cds,SingleNameCDS('cah'))	
}

testInitialize <- function(){
	this <- CdsTriTransformation("cah")
	this$setDate(as.POSIXct("2009/05/14"))
	this$initialize()
	checkSame(this$.strike,100)
	checkSame(this$.closeTri,99.0585661111111)
	checkSame(as.numeric(this$.calculator$.irsDataSave[,'5y']),2.42542999995042)
	checkSame(as.numeric(this$.calculator$.cdsDataSave[,'5y']),0.00533973564147427)
	checkSame(as.numeric(this$.calculator$.recoveryDataSave),0.4)
}

testUpdate <- function() {	
	inputs <- list(
			Spread = "CDS:||:CAH.5Y:||:LastSpread:||:30:||:TRUE",
			Swap2y = "MARKETDATA:||:irs_usd_rate_2y:||:LastPrice:||:3.948:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_3y:||:LastPrice:||:3.976:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_4y:||:LastPrice:||:4.087:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_5y:||:LastPrice:||:4.204:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_6y:||:LastPrice:||:4.314:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_7y:||:LastPrice:||:4.409:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_8y:||:LastPrice:||:4.492:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_9y:||:LastPrice:||:4.562:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_10y:||:LastPrice:||:4.626:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_20y:||:LastPrice:||:4.899:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_30y:||:LastPrice:||:4.93:||:TRUE"
	)
	
	this <- CdsTriTransformation("cah")
	this$setDate(as.POSIXct('2009-05-14'))
	outputs <- this$update(inputs)	
	checkSame(the(strsplit(outputs[[1]], ':||:', fixed=TRUE))[4],"98.077645555556")	   
}