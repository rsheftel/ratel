constructor("FuturesOptionCalculator", function() {
	this <- extend(RObject(), "FuturesOptionCalculator")
	this
})

method("calcStrike", "FuturesOptionCalculator", function(this, underlyingPrice, strikeStep, numStep, ...) 
{
	needs(underlyingPrice = "numeric", strikeStep = "numeric", numStep = "numeric")
	requireAllMatchFirst(length, list(underlyingPrice, strikeStep))
	
	atm <- floor(underlyingPrice / strikeStep) * strikeStep	
	lapply(-numStep:numStep, function(n) atm + n * strikeStep)
}) 


method("calcOTMPrice", "FuturesOptionCalculator", function(this, underlyingPrice, strike, ...) 
{
	needs(underlyingPrice = "numeric", strike = "numeric")
	requireAllMatchFirst(length, list(underlyingPrice, strike))
	
	otmPrice <- underlyingPrice - strike;
})


method("calcOTMYield", "FuturesOptionCalculator", function(
	this, underlyingPrice, underlyingDV01, underlyingConvexity, otmPrice, ...) 
{
	needs(underlyingPrice = "numeric", 
		  underlyingDV01 = "numeric",
		  underlyingConvexity = "numeric",
		  otmPrice = "numeric")
	requireAllMatchFirst(length, list(underlyingPrice, underlyingDV01, underlyingConvexity, otmPrice))
	
	otmYield <- -1 / underlyingConvexity * (underlyingDV01 - 
		sqrt(underlyingDV01 * underlyingDV01 + 2 * underlyingConvexity * otmPrice))	
})


method("calcDV01", "FuturesOptionCalculator", function(
	this, underlyingDV01, underlyingConvexity, otmYield, ...) 
{
	needs(underlyingDV01 = "numeric",
		  underlyingConvexity = "numeric",
		  otmYield = "numeric")
	requireAllMatchFirst(length, list(underlyingDV01, underlyingConvexity, otmYield))
	
	dv01 <- underlyingDV01 - underlyingConvexity * otmYield
})


method("calcImpliedVol", "FuturesOptionCalculator", function(
	this, valueDate, underlyingPrice, disc, expiry, optionPrice, optionType, strike, 
	dateCountConvention, ...) 
{
	needs(valueDate = "character|POSIXt", underlyingPrice = "numeric", 
		disc = "numeric", expiry = "character|list(POSIXt)", optionPrice = "numeric", 
		optionType = "character", strike = "numeric", dateCountConvention = "character")
	
	requireAllMatchFirst(length, list(valueDate, underlyingPrice, disc, expiry, 
		optionPrice, optionType, strike))
		
	failIf(any(!(optionType == "call" | optionType == "put")), 
		"option type must be 'call' or 'put'.  Was ", optionType)
		
	failIf(dateCountConvention != "calendar" && dateCountConvention != "business", 
		"date count convention must be 'calendar' or 'business'.  Was ", dateCountConvention)
	
	modifiedExpiry = getModifiedExpiry(expiry, valueDate, dateCountConvention)
	
	sapply(1:length(modifiedExpiry), function(n) {
	    fincad("aaBL_iv", 
	        price_u = underlyingPrice[[n]],
	        ex = strike[[n]],
	        d_exp = modifiedExpiry[[n]],
	        d_v = valueDate[[n]],
	        price = optionPrice[[n]],
	        rate_ann = disc[[n]],
	        option_type = ifElse(optionType[[n]] == 'call', 1, 2),
	        acc = 2)
	}) 
})


method("calcBpImpliedVol", "FuturesOptionCalculator", function(
	this, impliedVol, underlyingPrice, optionPrice, underlyingDV01, dv01, strike, ...) 
{
	needs(underlyingPrice = "numeric", impliedVol = "numeric", underlyingPrice = "numeric",
		optionPrice = "numeric", underlyingDV01 = "numeric", dv01 = "numeric", strike = "numeric")
	
	requireAllMatchFirst(length, list(impliedVol, underlyingPrice, optionPrice, underlyingDV01, 
		dv01, strike))
		
	impliedVol * sqrt(strike * underlyingPrice) / sqrt(underlyingDV01 * dv01) * 100
})


method("calcMergedVol", "FuturesOptionCalculator", function(
	this, underlyingPrice, strike, callVol, putVol, ...) 
{
	needs(underlyingPrice = "numeric", strike = "numeric", putVol = "numeric", callVol = "numeric")	
	requireAllMatchFirst(length, list(strike, callVol, putVol))
		
	unlist(lapply(1:length(strike), function(n) ifElse(strike[[n]] < underlyingPrice, putVol[[n]], callVol[[n]])))	
})


method("calcOTMPriceStrike", "FuturesOptionCalculator", function(
	this, underlyingPrice, strikeStep, numStep, ...) 
{
	needs(underlyingPrice = "numeric", strikeStep = "numeric", numStep = "numeric")	
		
	minus <- lapply(numStep:0, function(n) underlyingPrice - n * strikeStep)
	plus <- lapply(1:numStep, function(n) underlyingPrice + n * strikeStep)
	unlist(c(minus, plus))
})


method("calcOTMStrikeVolLinear", "FuturesOptionCalculator", function(
	this, strike, vol, OTMPriceStrike, ...) 
{
	needs(strike = "numeric", vol = "numeric", OTMPriceStrike = "numeric")	
	requireAllMatchFirst(length, list(strike, vol))	
	
	f <- approxfun(strike, vol, rule=2)
	f(OTMPriceStrike)	
})


method("calcDelta", "FuturesOptionCalculator", function(
	this, valueDate, underlyingPrice, disc, expiry, optionType, strike,  
	dateCountConvention, haveValue = 'vol', vol = NULL, price = NULL, ...) 
{
	needs(valueDate = "character|POSIXt", underlyingPrice = "numeric", 
		disc = "numeric", expiry = "character|list(POSIXt)", optionType = "character", 
		strike = "numeric", vol = "numeric?", dateCountConvention = "character", price = "numeric?")
	
	requireAllMatchFirst(length, list(valueDate, underlyingPrice, disc, expiry, strike, 
		optionType))
		
	failIf(any(!(optionType == "call" | optionType == "put")), 
		"option type must be 'call' or 'put'.  Was ", optionType)
	
	failIf(dateCountConvention != "calendar" && dateCountConvention != "business", 
		"date count convention must be 'calendar' or 'business'.  Was ", dateCountConvention)
	
	assert(haveValue %in% c('vol', 'price'), 'haveValue must be either vol or price in FuturesOptionCalculator$calcDelta')
		
	modifiedExpiry = getModifiedExpiry(expiry, valueDate, dateCountConvention)

	if(haveValue == 'vol')	{
		output <- sapply(1:length(modifiedExpiry), function(n) {
	    	fincad("aaBL", 
	        	price_u = underlyingPrice[[n]],
	        	ex = strike[[n]],
	        	d_exp = modifiedExpiry[[n]],
	        	d_v = valueDate[[n]],
	        	vlt = vol[[n]],
	        	rate_ann = disc[[n]],
	        	option_type = ifElse(optionType[[n]] == 'call', 1, 2),
	        	stat = 2, 
	        	acc = 2)
		})
	}
	if(haveValue == 'price'){
		vol <- sapply(1:length(modifiedExpiry), function(n) {
			fincad("aaBL_iv", 
				price_u = underlyingPrice[[n]],
				ex = strike[[n]],
				d_exp = modifiedExpiry[[n]],
				d_v = valueDate[[n]],
				price = price[[n]],
				rate_ann = disc[[n]],
				option_type = ifElse(optionType[[n]] == 'call', 1, 2),
				acc = 2)
		})
		output <- FuturesOptionCalculator$calcDelta(valueDate = valueDate,
													underlyingPrice = underlyingPrice,
													disc = disc,
													expiry = expiry,
													vol = vol,
													optionType = optionType,
													strike = strike,
													dateCountConvention = dateCountConvention) 
		}
	output
})


method("calcDeltaWeight", "FuturesOptionCalculator", function(this, delta, ...) 
{
	needs(delta = "numeric")
	
	unlist(lapply(1:length(delta), 
		function(n) ifElse(delta[[n]] < -0.1 && delta[[n]] > -0.9, 1, 0)))	 	
})


method("calcDeltaVolQuadRegressionCoef", "FuturesOptionCalculator", function(
	this, vol, delta, ...) 
{
	needs(vol = "numeric", delta = "numeric")	
	requireAllMatchFirst(length, list(delta, vol))	
	
	weight <- this$calcDeltaWeight(delta)
	weightedDelta <- delta * weight	
	model <- lm(vol ~ weight + weightedDelta + I(weightedDelta^2) -1)	
	as.vector(coef(model))
})


method("calcVolQuadractic", "FuturesOptionCalculator", function(
	this, delta, coef, ...) 
{
	needs(delta = "numeric")	
	
	coef[[1]] + coef[[2]] * delta + coef[[3]] * delta * delta
})


method("calcPerpetualExpiryWeight", "FuturesOptionCalculator", function(
	this, valueDate, perpetualDistance, expiry, dateCountConvention, ...) 
{
	needs(valueDate = "character|POSIXt", perpetualDistance = "numeric", 
		expiry = "character|list(POSIXt)", dateCountConvention = "character")
		
	failIf(dateCountConvention != "calendar" && dateCountConvention != "business", 
		"date count convention must be 'calendar' or 'business'.  Was ", dateCountConvention)
	
	distance <- lapply(1:length(expiry), 
		function(n) {
			if (dateCountConvention == 'business')
			{
				getNumBusinessDays("financialcalendar", "nyb", valueDate, expiry[[n]])
			}
			else
				as.numeric(as.Date(expiry[[n]]) - as.Date(valueDate))
		}
	)
		
	weight2 <- (perpetualDistance - distance[[1]]) / (distance[[2]] - distance[[1]])
	weight1 <- 1 - weight2
	c(weight1, weight2)	
})


method("calcPerpetualExpiryVol", "FuturesOptionCalculator", function(
	this, weight1, weight2, vol1, vol2, ...) 
{
	needs(weight1 = "numeric", weight2 = "numeric", vol1 = "numeric", vol2 = "numeric")	
	requireAllMatchFirst(length, list(vol1, vol2))	
	sqrt(weight1 * vol1 * vol1 + weight2 * vol2 * vol2)
})


method("calcActualOptTRI", "FuturesOptionCalculator", function(
	this, valueDate, underlyingPrice, disc, expiry, optionPrice, optionType, strike, dateCountConvention, ...) 
{
	needs(underlyingPrice = "numeric", 
		disc = "numeric", expiry = "character|list(POSIXt)", optionPrice = "numeric", 
		optionType = "character", strike = "numeric", dateCountConvention = "character")
	
	requireAllMatchFirst(length, list(valueDate, underlyingPrice, disc, expiry, optionPrice, 
		optionType, strike))
		
	failIf(any(!(optionType == "call" | optionType == "put")), 
		"option type must be 'call' or 'put'.  Was ", optionType)
		
	failIf(dateCountConvention != "calendar" && dateCountConvention != "business", 
		"date count convention must be 'calendar' or 'business'.  Was ", dateCountConvention)
		
	vol = this$calcImpliedVol(
		valueDate = valueDate,
		underlyingPrice = underlyingPrice, 
		disc = disc, 
		expiry = expiry, 
		optionPrice = optionPrice,
		optionType = optionType,
		strike = strike,
		dateCountConvention = dateCountConvention)

	delta = this$calcDelta(
		valueDate = valueDate,
		underlyingPrice = underlyingPrice, 
		disc = disc, 
		expiry = expiry, 
		vol = vol,
		optionType = optionType,
		strike = strike,
		dateCountConvention = dateCountConvention)		
		
	print(vol)
	print(delta)
	
	underlyingPriceChange = underlyingPrice[-1] - underlyingPrice[-length(underlyingPrice)]
	optionPriceChange = optionPrice[-1] - optionPrice[-length(optionPrice)]
	TRI = optionPriceChange - delta[-length(underlyingPrice)] * underlyingPriceChange
	TRI
})


getBlbgFutOptTicker <- function(root, expiry, strike, optionType)
{
	needs(root = "character", expiry = "character|POSIXt", strike = "numeric", 
		optionType = "character")	
		  
	failIf(any(!(optionType == "call" | optionType == "put")), 
		"option type must be 'call' or 'put'.  Was ", optionType)	
		
	expiryDate <- as.POSIXlt(expiry)
	monthCode <- c('F','G','H','J','K','M','N','Q','U','V','X','Z') 
	
	paste(root, monthCode[[expiryDate$mon + 1]], expiryDate$year %% 10,
		ifElse(optionType == 'call', 'C', 'P'), ' ', formatC(strike, dig = 5, format = "f"),
		' Comdty', sep = "") 
}


getNumBusinessDays <- function(source, financialCenter, startDate, endDate)
{
	needs(startDate = "character|POSIXt", endDate = "character|POSIXt", 
		  source = "character", financialCenter = "character")
		  	
	failIf(as.numeric(difftime(endDate, startDate)) < 0, 
		"Start date must be earlier than or equal to end date. Start date was ", startDate, 
		". End date was ", endDate)
	
	day <- seq(as.Date(startDate), as.Date(endDate), by = "day")
	holidayLoader <- HolidayDataLoader()
	holiday <- holidayLoader$getHolidays(source = source, financialCenter = financialCenter, 
		startDate = startDate, endDate = endDate)
	
	dayOfWeek <- weekdays(day)
	week_day <- dayOfWeek[!dayOfWeek %in% c("Saturday","Sunday")]
	length(week_day) - length(holiday)
}

getModifiedExpiry <- function(expiry, valueDate, dateCountConvention)
{
	needs(expiry = "character|POSIXt", valueDate = "character|POSIXt", 
		dateCountConvention = "character")
		
	requireAllMatchFirst(length, list(expiry, valueDate))
		  
	failIf(dateCountConvention != "calendar" && dateCountConvention != "business", 
		"date count convention must be 'calendar' or 'business'.  Was ", dateCountConvention)
				  
	modifiedExpiry = expiry
	if (dateCountConvention == 'business')
	{
		modifiedExpiry = lapply(1:length(expiry), 
			function(n) {
				as.character(as.Date(valueDate[[n]]) + 
					getNumBusinessDays("financialcalendar", "nyb", valueDate[[n]], expiry[[n]]))
			}
		)
	}
	
	modifiedExpiry
}
