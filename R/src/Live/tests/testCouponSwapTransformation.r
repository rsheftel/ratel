library(Live)


test.CouponSwap <- function(){
	trans <- CouponSwapTransformation("fncl",c(5.0,5.5,6.0),c(5.0,5.5), dv01.source='internal',modelVersion='1.0')
	checkInherits(trans, "CouponSwapTransformation")
	
	#Note that the inputs are driven from the coupons.live, but the outputs from the coupon.start/coupon.end
	checkSame(list(
			SeriesDefinition("MARKETDATA","irs_usd_rate_2y", "LastPrice"),
			SeriesDefinition("MARKETDATA","irs_usd_rate_10y", "LastPrice"), 
			SeriesDefinition("TBA","fncl_5.0_1n_price.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.5_1n_price.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.0_2n_price.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.5_2n_price.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.0_3n_price.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.5_3n_price.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.0_1n_settle_date.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.5_1n_settle_date.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.0_2n_settle_date.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.5_2n_settle_date.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.0_3n_settle_date.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.5_3n_settle_date.internal", "LastValue"),
			SeriesDefinition("TBA","fncl_5.0_1n_price.internal", "timeStamp"),
			SeriesDefinition("TBA","fncl_5.5_1n_price.internal", "timeStamp"),
			SeriesDefinition("TBA","fncl_5.0_2n_price.internal", "timeStamp"),
			SeriesDefinition("TBA","fncl_5.5_2n_price.internal", "timeStamp"),
			SeriesDefinition("TBA","fncl_5.0_3n_price.internal", "timeStamp"),
			SeriesDefinition("TBA","fncl_5.5_3n_price.internal", "timeStamp")
		),
		noNames(trans$inputs())
	)
	checkSame(list(
			SeriesDefinition("CouponSwap", "fncl_5.5_5.0", "45d_price_actual"),
			SeriesDefinition("CouponSwap", "fncl_6.0_5.5", "45d_price_actual"),
			SeriesDefinition("CouponSwap", "fncl_5.5_5.0", "45d_price_model"),
			SeriesDefinition("CouponSwap", "fncl_6.0_5.5", "45d_price_model"),
			SeriesDefinition("CouponSwap", "fncl_5.5_5.0", "1n_weighted_roll"),
			SeriesDefinition("CouponSwap", "fncl_6.0_5.5", "1n_weighted_roll"),
			SeriesDefinition("CouponSwap", "fncl_5.5_5.0", "Timestamp"),
			SeriesDefinition("CouponSwap", "fncl_6.0_5.5", "Timestamp")
		),
		noNames(trans$outputs())
	)
}

inputs <- list(
			"MARKETDATA:||:irs_usd_rate_2y:||:LastPrice:||:2.79625:||:TRUE",
			"MARKETDATA:||:irs_usd_rate_10y:||:LastPrice:||:4.3425:||:TRUE", 
			"TBA:||:fncl_5.0_1n_price.internal:||:LastValue:||:95.211:||:TRUE",
			"TBA:||:fncl_5.5_1n_price.internal:||:LastValue:||:98.011:||:TRUE",
			"TBA:||:fncl_6.0_1n_price.internal:||:LastValue:||:99.931:||:TRUE",
			"TBA:||:fncl_6.5_1n_price.internal:||:LastValue:||:101.154:||:TRUE",
			"TBA:||:fncl_5.0_2n_price.internal:||:LastValue:||:95.083:||:TRUE",
			"TBA:||:fncl_5.5_2n_price.internal:||:LastValue:||:97.855:||:TRUE",
			"TBA:||:fncl_6.0_2n_price.internal:||:LastValue:||:99.771:||:TRUE",
			"TBA:||:fncl_6.5_2n_price.internal:||:LastValue:||:100.967:||:TRUE",
			"TBA:||:fncl_5.0_3n_price.internal:||:LastValue:||:94.934:||:TRUE",
			"TBA:||:fncl_5.5_3n_price.internal:||:LastValue:||:97.683:||:TRUE",
			"TBA:||:fncl_6.0_3n_price.internal:||:LastValue:||:99.607:||:TRUE",
			"TBA:||:fncl_6.5_3n_price.internal:||:LastValue:||:100.764:||:TRUE",
			"TBA:||:fncl_5.0_1n_settle_date.internal:||:LastValue:||:20081113:||:TRUE",
			"TBA:||:fncl_5.5_1n_settle_date.internal:||:LastValue:||:20081113:||:TRUE",
			"TBA:||:fncl_6.0_1n_settle_date.internal:||:LastValue:||:20081113:||:TRUE",
			"TBA:||:fncl_6.5_1n_settle_date.internal:||:LastValue:||:20081113:||:TRUE",
			"TBA:||:fncl_5.0_2n_settle_date.internal:||:LastValue:||:20081211:||:TRUE",
			"TBA:||:fncl_5.5_2n_settle_date.internal:||:LastValue:||:20081211:||:TRUE",
			"TBA:||:fncl_6.0_2n_settle_date.internal:||:LastValue:||:20081211:||:TRUE",
			"TBA:||:fncl_6.5_2n_settle_date.internal:||:LastValue:||:20081211:||:TRUE",
			"TBA:||:fncl_5.0_3n_settle_date.internal:||:LastValue:||:20090113:||:TRUE",
			"TBA:||:fncl_5.5_3n_settle_date.internal:||:LastValue:||:20090113:||:TRUE",
			"TBA:||:fncl_6.0_3n_settle_date.internal:||:LastValue:||:20090113:||:TRUE",
			"TBA:||:fncl_6.5_3n_settle_date.internal:||:LastValue:||:20090113:||:TRUE",
			"TBA:||:fncl_5.0_1n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_5.5_1n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_6.0_1n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_6.5_1n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_5.0_2n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_5.5_2n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_6.0_2n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_6.5_2n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_5.0_3n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_5.5_3n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",
			"TBA:||:fncl_6.0_3n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE",						
			"TBA:||:fncl_6.5_3n_price.internal:||:timeStamp:||:2008/10/27 10:00:00:||:TRUE"
		)
		
stripTimestamps <- function(outputs) { 
	outputs[ -grep(":Timestamp:", outputs) ]
}
				
		
test.updateLogic.Live <- function(){
	trans <- CouponSwapTransformation("fncl",seq(5.0,6.5,0.5),c(5,5.5,6,6.5),dv01.source='internal',modelVersion='1.0')
	trans$setDates(as.POSIXct("2008-10-27"))
	trans$.testMode <- TRUE
	#update is called twice, the first returns the open of day, the second is the first live tick
	outs <- trans$update(inputs, quiet=FALSE)
	outs <- trans$update(inputs, quiet=FALSE)
		
	expected <- list( 		
		"CouponSwap:||:fncl_5.5_5.0:||:45d_price_actual:||:2.772000000000",
		"CouponSwap:||:fncl_6.0_5.5:||:45d_price_actual:||:1.916000000000",
		"CouponSwap:||:fncl_6.5_6.0:||:45d_price_actual:||:1.196000000000",
		"CouponSwap:||:fncl_5.5_5.0:||:45d_price_model:||:3.004658664257",
		"CouponSwap:||:fncl_6.0_5.5:||:45d_price_model:||:2.407517260363",
		"CouponSwap:||:fncl_6.5_6.0:||:45d_price_model:||:1.848749372465",
		"CouponSwap:||:fncl_5.5_5.0:||:1n_weighted_roll:||:0.058582816652",
		"CouponSwap:||:fncl_6.0_5.5:||:1n_weighted_roll:||:0.039856851906",
		"CouponSwap:||:fncl_6.5_6.0:||:1n_weighted_roll:||:0.068465810351"
	)
	checkSame(stripTimestamps(outs), expected)
}

test.updateLogic.FirstTick <- function(){
	trans <- CouponSwapTransformation("fncl",seq(5.0,6.5,0.5),c(5,5.5,6,6.5),dv01.source='internal',modelVersion='1.0')
	trans$setDates(as.POSIXct("2008-10-27"))
	trans$.testMode <- TRUE
	#The first tick returns the open of day data
	outs <- trans$update(inputs, quiet=FALSE)
	
	expected <- list( 		
		"CouponSwap:||:fncl_5.5_5.0:||:45d_price_actual:||:2.315848214286",
		"CouponSwap:||:fncl_6.0_5.5:||:45d_price_actual:||:1.558175223214",
		"CouponSwap:||:fncl_6.5_6.0:||:45d_price_actual:||:0.928710937500",
		"CouponSwap:||:fncl_5.5_5.0:||:45d_price_model:||:2.601824836926",
		"CouponSwap:||:fncl_6.0_5.5:||:45d_price_model:||:2.036510812924",
		"CouponSwap:||:fncl_6.5_6.0:||:45d_price_model:||:1.471706905841",
		"CouponSwap:||:fncl_5.5_5.0:||:1n_weighted_roll:||:0.064849285872",
		"CouponSwap:||:fncl_6.0_5.5:||:1n_weighted_roll:||:0.035599496290",
		"CouponSwap:||:fncl_6.5_6.0:||:1n_weighted_roll:||:0.070874752078"
	)
	checkSame(stripTimestamps(outs), expected)	
}

test.skipUpdate <- function(){
	trans <- CouponSwapTransformation("fncl",seq(5.0,6.5,0.5),c(5,5.5,6,6.5),dv01.source='internal',modelVersion='1.0')
	trans$setDates(as.POSIXct("2008-10-27"))
	trans$.testMode <- TRUE
	trans$inputs()
	#The first tick returns the open of day data
	trans$.lastUpdateTime <- as.POSIXct('2008-10-27 10:01:00')
	outs <- trans$update(inputs, quiet=FALSE)
	checkLength(outs,12)
	trans$.lastUpdateTime <- as.POSIXct('2008-10-27 10:01:00')
	outs <- trans$update(inputs, quiet=FALSE)
	checkLength(outs,0)
	trans$.lastUpdateTime <- as.POSIXct('2008-10-27 09:59:00')
	outs <- trans$update(inputs, quiet=FALSE)
	
	expected <- list( 		
		"CouponSwap:||:fncl_5.5_5.0:||:45d_price_actual:||:2.772000000000",
		"CouponSwap:||:fncl_6.0_5.5:||:45d_price_actual:||:1.916000000000",
		"CouponSwap:||:fncl_6.5_6.0:||:45d_price_actual:||:1.196000000000",
		"CouponSwap:||:fncl_5.5_5.0:||:45d_price_model:||:3.004658664257",
		"CouponSwap:||:fncl_6.0_5.5:||:45d_price_model:||:2.407517260363",
		"CouponSwap:||:fncl_6.5_6.0:||:45d_price_model:||:1.848749372465",
		"CouponSwap:||:fncl_5.5_5.0:||:1n_weighted_roll:||:0.058582816652",
		"CouponSwap:||:fncl_6.0_5.5:||:1n_weighted_roll:||:0.039856851906",
		"CouponSwap:||:fncl_6.5_6.0:||:1n_weighted_roll:||:0.068465810351"
	)
	checkSame(stripTimestamps(outs), expected)
}

test.initialize <- function(){
	trans <- CouponSwapTransformation("fncl",seq(4,8.5,0.5),seq(4,6.5,0.5),dv01.source='internal',modelVersion='1.5')
	checkSame(trans$initialize(),'SUCCESS')
}
