# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
#
#
#library(QFFutures)
#library(QFFuturesOptions)
#library(QFFixedIncome)
#library(QFMath)
#
#testFuturesOptionStraddleConstructor <- function(){
#	shouldBomb(FuturesOptionStraddle(1))
#	this <- FuturesOptionStraddle('ty')
#}
#
#testFuturesOptionStraddleUnderlying <- function(){
#	this <- FuturesOptionStraddle('ty')
#	checkSame('ty', this$underlying())
#	checkSame('Contract', class(this$underlyingContract())[1])
#
#}
#
#testFuturesOptionStraddleUnderlyingFuturesAttributes <- function(){
#	this <- FuturesOptionStraddle('ty')
#	target <- list(instrument = 'futures',
#				   future_month = 12,
#				   future_year = 2007,
#				   quote_side = 'mid',
#				   quote_convention = 'price',
#				   contract = 'ty')
#	result <- this$underlyingFuturesAttributes(12,2007)
#	checkSame(target, result)	
#}
#
#testFuturesOptionStraddleMainFunctions <- function(){
#	this <- FuturesOptionStraddle('ty')
#	start <- '2007-10-25'
#	end <- '2007-11-02'
#	
#	#first check getAvailableDates
#	targetDates <- c('2007-10-25', '2007-10-26', '2007-10-29', '2007-10-30', '2007-10-31', '2007-11-01', '2007-11-02')
#	result <- this$getAvailableDates(start,end)
#	checkEquals(as.POSIXct(targetDates), result)
#	
#	#check getRollDates
#	targetDates <- as.POSIXct(paste(targetDates, ' 15:00:00', sep = ''))
#	target2 <- zoo(data.frame(optionMonth = c(12,12,12,12,3,3,3), optionYear = c(2007,2007,2007,2007,2008,2008,2008),
#						underlyingMonth = c(12,12,12,12,3,3,3), underlyingYear = c(2007,2007,2007,2007,2008,2008,2008)),
#						order.by = targetDates)
#	row.names(target2) <- rep(1,7)
#	result <- this$getRollDates(result, FuturesOptionRollLogic$jpMorgan, 'quarterly')
#	checkSame(target2, result)
#	
#	#check getHistoricalStrikesForRollDates
#	target3 <- zoo(c(111,NA,NA,NA,109.5,NA,NA), order.by = targetDates)
#	strikes <- this$getHistoricalStrikesForRollDates(result)
#	checkSame(target3, strikes)
#	
#	#check getNextStrike
#	target4 <- 109.5
#	nextStrike <- this$getNextStrike(result, 5, this$underlyingContract())
#	checkSame(target4, nextStrike)
#	
#	#check getHistoricalPrices
#	optionData <- cbind(result, strikes)
#	callPx <- c(0.625, 0.5, 0.5, 0.453125, 1.328125, 1.78125,2.1875)
#	putPx <- c(0.765625, 0.859375, 0.828125, 0.796875, 1.1875, 0.875,0.75)
#	target5 <- cbind(optionData, callPx, putPx)
#	result <- this$getHistoricalPrices(optionData)
#	checkSame(target5, result)
#	
#	#check ExpiryDates
#	optionData <- result
#	expiryDates <- c(1195516800,1195516800,1195516800,1195516800,1203638400,1203638400,1203638400)
#	targetExpiryDates <- cbind(result, expiryDates)
#	result <- this$getExpiryDates(optionData)
#	checkSame(targetExpiryDates, result)
#	
#	#check Swap Rates
#	targetSwapRates <- c(5.00625, 4.71375, 4.70875, 4.70625, 4.89625, 4.89375, 4.865, 4.8375, 4.80625, 4.77375, 4.74063, 4.70813, 4.68375, 4.65813, 4.6375)
#	rates <- this$loadSwapRates(start,end, 'internal')
#	checkSame(targetSwapRates, as.numeric(rates[5,]))
#	
#	#check Discount Rates
#	optionData <- result
#	targetDiscountRates <- c(4.816911764705882,4.794705882352941,4.76125,4.727708333333333,4.873214285714286,4.865732758620690,4.848193103448276)
#	discountRate<- this$getDiscountRates(rates, optionData)
#	checkSame(targetDiscountRates, as.numeric(discountRate))
#	
#	#check underlyingFuturesPrices
#	optionData <- cbind(optionData, discountRate)
#	underlyingFutures <- c(110.859375, 110.640625, 110.65625, 110.65625, 109.640625, 110.421875, 110.953125)
#	target6 <- cbind(optionData, underlyingFutures)
#	result <- this$underlyingFuturesPrices(optionData)
#	checkSame(target6, result)
#	
#	#check underlyingFuturesChanges
#	underlyingFuturesChanges <- c(NA, -0.218750, 0.015625, 0, -0.640625, 0.78125, 0.53125)
#	target7 <- cbind(target6, underlyingFuturesChanges)
#	result <- this$underlyingFuturesChanges(target6)
#	checkSame(target7, result)
#	
#	#check calcDeltas
#	optionData <- result
#	deltaCall <- c(0.4693670354565796,0.4163539682136133,0.419305867437661,0.412766792744983,0.5155159483970215,0.6082773701960589, 0.6594796916485347)
#	deltaPut <- c(-0.5272167999655356,-0.5802432778808299,-0.5798127823327421,-0.5844024259085778,-0.4695100702597413,-0.3770833391289822, -0.3252361399182568)
#	targetDelta <- cbind(optionData, deltaCall, deltaPut)
#	result <- this$calcDeltas(optionData)
#	checkSame(targetDelta, result)	
#
#	#check calcReturns
#	straddles <- result[,'callPx'] + result[,'putPx']
#	optionData <- cbind(result, straddles)
#	targetReturns <- c(100, 99.9560953640137, 99.9274061344772, 99.849281134472,99.9424520569818,100.047134964687,100.20556313568267)
#	
#	result <- this$calculateReturns(optionData, 100)
#	checkSame(targetReturns, as.numeric(result[,'dailyReturn']))
#}
