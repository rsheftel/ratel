library(Live)
library(fincad)
library(QFFX)

currencyPair <- c("usd/jpy")
trans <- FXTriTransformation(currencyPair=currencyPair)      
                        
trans$setDate(as.POSIXct("2009/02/09"))

list(rate = SeriesDefinition("MARKETDATA","FX.USDJPY.Rate.Spot", "LastPrice"))

test.FXTriInputsOutputs <- function() {
       
    checkInherits(trans, "FXTriTransformation")
    checkSame(list(
            SeriesDefinition("MARKETDATA","FX.USDJPY.Rate.Spot", "LastPrice")
        ),
        noNames(trans$inputs())
    )
    checkSame(
		c(
			SeriesDefinition$from("FX", "USDJPY","LastPrice"),
			SeriesDefinition$from("FX", "USDJPY","OpenPrice"),
			SeriesDefinition$from("FX", "USDJPY","HighPrice"),
			SeriesDefinition$from("FX", "USDJPY","LowPrice"),
			SeriesDefinition$from("FX", "USDJPY","Timestamp"),
			SeriesDefinition$from("FX", "USDJPY","LastVolume")
		),
        trans$outputs()
    )
}

test.Dates <- function() {
    checkSame(trans$.date, as.POSIXct("2009/02/09"))
	checkSame(trans$.yesterday, as.POSIXct("2009/02/06"))
}

test.initialize <- function(){
    checkSame("SUCCESS",trans$initialize())
    checkSame(trans$.data,92.14)
    checkSame(round(trans$.closeTri,5),213.89347)    
}

test.Update <- function() {

    trans <- FXTriTransformation(currencyPair = currencyPair)
    trans$setDate(as.POSIXct("2009/02/09"))

    # Initialized
    inputs <- list(
		rate = "MARKETDATA:||:FX.USDJPY.Rate.Spot:||:LastPrice:||:93.14:||:TRUE"       
	)
    
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[5]],"FX:||:USDJPY:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FX:||:USDJPY:||:LowPrice:||:213.893474922464")
	checkSame(outputs[[3]],"FX:||:USDJPY:||:HighPrice:||:214.967127488494")
	checkSame(outputs[[2]],"FX:||:USDJPY:||:OpenPrice:||:213.893474922464")
	checkSame(outputs[[1]],"FX:||:USDJPY:||:LastPrice:||:214.967127488494")
	
	# Another tick
	inputs <- list(
		rate = "MARKETDATA:||:FX.USDJPY.Rate.Spot:||:LastPrice:||:93.14:||:FALSE"        
	)
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[5]],"FX:||:USDJPY:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FX:||:USDJPY:||:LowPrice:||:213.893474922464")
	checkSame(outputs[[3]],"FX:||:USDJPY:||:HighPrice:||:214.967127488494")
	checkSame(outputs[[2]],"FX:||:USDJPY:||:OpenPrice:||:213.893474922464")
	checkSame(outputs[[1]],"FX:||:USDJPY:||:LastPrice:||:214.967127488494")

	# New Tick
	
	inputs <- list(
		rate = "MARKETDATA:||:FX.USDJPY.Rate.Spot:||:LastPrice:||:92.14:||:FALSE" 
	 )
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[5]],"FX:||:USDJPY:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FX:||:USDJPY:||:LowPrice:||:213.893474922464")
	checkSame(outputs[[3]],"FX:||:USDJPY:||:HighPrice:||:213.893474922464")
	checkSame(outputs[[2]],"FX:||:USDJPY:||:OpenPrice:||:213.893474922464")
	checkSame(outputs[[1]],"FX:||:USDJPY:||:LastPrice:||:213.893474922464")
}

test.ConversionRate <- function() {
	currencyPair <- c("eur/jpy")
	trans <- FXTriTransformation(currencyPair=currencyPair,putCall="call")      
	checkEquals(trans$.conversionCurrency,FXCurr$setByCross("eur/usd"))
	trans$setDate(as.POSIXct("2009/02/09"))
	
	checkSame(list(
			SeriesDefinition("MARKETDATA","FX.EURJPY.Rate.Spot", "LastPrice"),
			SeriesDefinition("MARKETDATA","FX.EURUSD.Rate.Spot", "LastPrice")
		),
		noNames(trans$inputs())
	)
	inputs <- list(
		rate = "MARKETDATA:||:FX.EURJPY.Rate.Spot:||:LastPrice:||:120.0:||:TRUE", 
		conversionRate = "MARKETDATA:||:FX.EURUSD.Rate.Spot:||:LastPrice:||:1.30:||:TRUE"
	)
	# We start up
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(round(as.numeric(trans$.conversionRateClose),5),1.2942)
	checkSame(trans$getCloseTri(),768.49172808595)

	checkSame(outputs[[5]],"FX:||:EURJPY:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FX:||:EURJPY:||:LowPrice:||:994.581994488836")
	checkSame(outputs[[3]],"FX:||:EURJPY:||:HighPrice:||:999.854359511735")
	checkSame(outputs[[2]],"FX:||:EURJPY:||:OpenPrice:||:994.581994488836")
	checkSame(outputs[[1]],"FX:||:EURJPY:||:LastPrice:||:999.854359511735")
} 