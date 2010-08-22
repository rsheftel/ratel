library(Live)
library(fincad)
library(QFFX)

currencyPair <- c("usd/jpy")
trans <- FXCarryTriTransformation(currencyPair=currencyPair,putCall="call")      
                        
trans$setDate(as.POSIXct("2009/02/09"))

test.FXCarryTriInputsOutputs <- function() {
       
    checkInherits(trans, "FXCarryTriTransformation")
    checkSame(list(
            SeriesDefinition("MARKETDATA","FX.USDJPY.Rate.Spot", "LastPrice")
        ),
        noNames(trans$inputs())
    )
    checkSame(
		c(
			SeriesDefinition$from("FXCARRY", "USDJPY.Tri.USDJPY6MTRI.C1","LastPrice"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Tri.USDJPY6MTRI.C1","OpenPrice"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Tri.USDJPY6MTRI.C1","HighPrice"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Tri.USDJPY6MTRI.C1","LowPrice"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Tri.USDJPY6MTRI.C1","LastVolume"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Tri.USDJPY6MTRI.C1","Timestamp"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Signal.USDJPY6MPOUT.C","LastPayoutRatio"),
			SeriesDefinition$from("FXCARRY", "USDJPY.Signal.USDJPY6MPOUT.C","PayoutRatioTimestamp")
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
    checkSame(as.numeric(trans$.data$.rateData[trans$.yesterday,"spot"]),92.14)
	checkSame(round(as.numeric(trans$.data$.volData[trans$.yesterday,"6m"]),6),0.165386)
	checkSame(round(trans$.payoutRatio,6),0.095105) 
	checkSame(round(as.numeric(trans$.optionData$tri),5),125.63365)   
	checkSame(as.numeric(trans$.optionData$strike),88.58)
	checkSame(as.numeric(trans$.optionData$lastRollDate),1232946000)
	checkSame(as.numeric(trans$.optionData$expiryDate),1248408000)
	checkSame(as.numeric(trans$.optionData$settleDate),1248753600)
	
}

test.Update <- function() {

    trans <- FXCarryTriTransformation(currencyPair = currencyPair, putCall="call")
    trans$setDate(as.POSIXct("2009/02/09"))

    # Initialized
    inputs <- list(
		rate = "MARKETDATA:||:FX.USDJPY.Rate.Spot:||:LastPrice:||:92.14:||:TRUE"   
	)
    
	# We start up...first tick is last night's close
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[7]],"FXCARRY:||:USDJPY.Signal.USDJPY6MPOUT.C:||:LastPayoutRatio:||:0.095104720032")
	checkSame(outputs[[5]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LowPrice:||:125.633650072393")
	checkSame(outputs[[3]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:HighPrice:||:125.633650072393")
	checkSame(outputs[[2]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:OpenPrice:||:125.633650072393")
	checkSame(outputs[[1]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LastPrice:||:125.633650072393")
	
	# Another tick, unchanged
	inputs <- list(
		rate = "MARKETDATA:||:FX.USDJPY.Rate.Spot:||:LastPrice:||:93.14:||:TRUE" 
	)
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[7]],"FXCARRY:||:USDJPY.Signal.USDJPY6MPOUT.C:||:LastPayoutRatio:||:0.095104720032")
	checkSame(outputs[[5]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LowPrice:||:125.633650072393")
	checkSame(outputs[[3]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:HighPrice:||:126.236922679452")
	checkSame(outputs[[2]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:OpenPrice:||:125.633650072393")
	checkSame(outputs[[1]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LastPrice:||:126.236922679452")
	

	# New Rate Tick
	inputs <- list(
		rate = "MARKETDATA:||:FX.USDJPY.Rate.Spot:||:LastPrice:||:92.14:||:TRUE"   
	)
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[7]],"FXCARRY:||:USDJPY.Signal.USDJPY6MPOUT.C:||:LastPayoutRatio:||:0.095104720032")
	checkSame(outputs[[5]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LowPrice:||:125.596154240094")
	checkSame(outputs[[3]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:HighPrice:||:125.633650072393")
	checkSame(outputs[[2]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:OpenPrice:||:125.633650072393")
	checkSame(outputs[[1]],"FXCARRY:||:USDJPY.Tri.USDJPY6MTRI.C1:||:LastPrice:||:125.596154240094")
}

test.ConversionRate <- function() {
	currencyPair <- c("eur/jpy")
	trans <- FXCarryTriTransformation(currencyPair=currencyPair,putCall="call")      
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
	checkSame(round(as.numeric(trans$.optionData$tri),5),115.43042)
	checkSame(round(as.numeric(trans$.conversionRateClose),5),1.2942)
	checkSame(outputs[[7]],"FXCARRY:||:EURJPY.Signal.EURJPY6MPOUT.C:||:LastPayoutRatio:||:0.077417488746")
	checkSame(outputs[[5]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:LowPrice:||:149.390045248709")
	checkSame(outputs[[3]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:HighPrice:||:149.390045248709")
	checkSame(outputs[[2]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:OpenPrice:||:149.390045248709")
	checkSame(outputs[[1]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:LastPrice:||:149.390045248709")
	
	#Now actual tick
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[7]],"FXCARRY:||:EURJPY.Signal.EURJPY6MPOUT.C:||:LastPayoutRatio:||:0.077417488746")
	checkSame(outputs[[5]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:LastVolume:||:1.000000000000")
	checkSame(outputs[[4]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:LowPrice:||:149.390045248709")
	checkSame(outputs[[3]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:HighPrice:||:150.397441027308")
	checkSame(outputs[[2]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:OpenPrice:||:149.390045248709")
	checkSame(outputs[[1]],"FXCARRY:||:EURJPY.Tri.EURJPY6MTRI.C1:||:LastPrice:||:150.397441027308")
	
	
}