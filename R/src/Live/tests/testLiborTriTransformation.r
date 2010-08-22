library(Live)
library(fincad)

trans <- LiborTriTransformation()      
                        
trans$setDate(as.POSIXct("2009/02/09"))

list(onRate = SeriesDefinition("MARKETDATA","libor_usd_rate_on", "LastPrice"))

test.LiborTriInputsOutputs <- function() {
       
    checkInherits(trans, "LiborTriTransformation")
    
    checkSame(list(
            SeriesDefinition("MARKETDATA","libor_usd_rate_on", "LastPrice")
        ),
        noNames(trans$inputs())
    )
    checkSame(
		c(
			SeriesDefinition$from("LIBORTRI", "ON","LastTRI"),
			SeriesDefinition$from("LIBORTRI", "ON","LastDailyTRI"),
			SeriesDefinition$from("LIBORTRI", "ON","Timestamp")
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
    checkSame(trans$.data,0.31)
    checkSame(trans$.closeTri,165.9248)    
}

test.Update <- function() {

    trans <- LiborTriTransformation()
        
    trans$setDate(as.POSIXct("2009/02/09"))

    # Initialized
    
	inputs <- list(
		onRate = "MARKETDATA:||:libor_usd_rate_on:||:LastPrice:||:0.31:||:FALSE"        
	)
    
    outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[2]],"LIBORTRI:||:ON:||:LastDailyTRI:||:0.000861111111")
	checkSame(outputs[[1]],"LIBORTRI:||:ON:||:LastTRI:||:165.925662472340")
	
	# No update
	
	inputs <- list(
		onRate = "MARKETDATA:||:libor_usd_rate_on:||:LastPrice:||:0.8:||:FALSE"        
	)
	
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs,list())

	# Additional Test
	
	trans <- LiborTriTransformation()
	
	trans$setDate(as.POSIXct("2009/02/10"))
	
	inputs <- list(
		onRate = "MARKETDATA:||:libor_usd_rate_on:||:LastPrice:||:0.305:||:FALSE"        
	)
	
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs[[2]],"LIBORTRI:||:ON:||:LastDailyTRI:||:0.000854166667")
	checkSame(outputs[[1]],"LIBORTRI:||:ON:||:LastTRI:||:165.926516639007")
	
	inputs <- list(
		onRate = "MARKETDATA:||:libor_usd_rate_on:||:LastPrice:||:0.9:||:FALSE"        
	)
	
	outputs <- trans$update(inputs, quiet=FALSE)
	checkSame(outputs,list())
}