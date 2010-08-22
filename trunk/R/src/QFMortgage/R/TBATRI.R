# TBATRI.R
# 
#
# Author: rsheftel

#  Add the methods for:
#		AddNewData
#		AddLiveData

###############################################################################

constructor("TBATRI", function(program = NULL, couponVector = NULL)
{
	this <- extend(RObject(), "TBATRI", .program = program, .couponVector = couponVector)
	if (inStaticConstructor(this)) return(this)
	constructorNeeds (this, program = "character", couponVector = "numeric")
	
	#Set up initial variable
	this$.couponStringVector = format(this$.couponVector,nsmall=1)
	this$.settleVector = c('1n','2n')
	this$.partialPoints = c('2y','5y','10y','30y')
	this$.partialFuturesMap <- c('tu','fv','ty','us')
	this$setRequiredZoos()
	return(this)
})

method("setRequiredZoos", "TBATRI", function(this, ...){
	
	tbaPartialDurs <- paste("this$.TBAzoos$partials[['",this$.partialPoints,"']][[coupon]]",sep="")
	swapTRIs <- paste("this$.swapZoos$TRI[['",this$.partialPoints,"']]",sep="")
	swapDv01s <- paste("this$.swapZoos$dv01[['",this$.partialPoints,"']]",sep="") 	
	treasuryTRI <- "this$.cashTreasury$TRI[['10y']]"
	tbaDv01s <- "this$.TBAzoos$dv01.1n[[coupon]]"
	treasuryDv01s <- "this$.cashTreasury$dv01[['10y']]"
	futuresTRIs <- paste("this$.futuresTreasury$TRI[['",this$.partialFuturesMap,"']]",sep="")
	futuresDv01s <- paste("this$.futuresTreasury$dv01[['",this$.partialFuturesMap,"']]",sep="")
			
	this$.requiredZoos$vNoHedge <- list()
	this$.requiredZoos$vSwapPartials <- c(tbaPartialDurs, swapTRIs, swapDv01s)
	this$.requiredZoos$vTreasury10y <- c(tbaDv01s, treasuryTRI, treasuryDv01s)
	this$.requiredZoos$vFuturesTU_FV_TY_US <- c(tbaPartialDurs, futuresTRIs, futuresDv01s)
	this$.requiredZoos$vFuturesTU_TY <- c(tbaPartialDurs, futuresTRIs[c(1,3)], futuresDv01s[c(1,3)])
	this$.requiredZoos$vFuturesTY <- c(tbaPartialDurs, futuresTRIs[[3]], futuresDv01s[[3]])
})

method("setTsdbSources", "TBATRI", function(this, tbaPriceSource="internal", tbaDurationSource="internal", swapTRISource="internal", 
												cashTreasurySource='internal', futuresDuration='internal',...)
{
	#Set up the tsdb data sources for the various elements
	needs(tbaPriceSource="character?", tbaDurationSource="character?", swapTRISource="character?")
			
	this$.sources$tbaPrice 		<- tbaPriceSource
	this$.sources$tbaDuration	<- tbaDurationSource
	this$.sources$swapTRI		<- swapTRISource
	this$.sources$cashTreasury	<- cashTreasurySource
	this$.sources$futuresDuration <- futuresDuration
})

method("setDateRange", "TBATRI", function(this, dataDate, startDate=NULL, daysBack=7,...)
{
	#Set the date range to use from TSDB to get the "prior" date. This is required to make sure you get the previous date
		
	needs(dataDate = "POSIXct|character", startDate="POSIXct|character?", daysBack="numeric?")
	
	this$.dataDate <- as.POSIXct(dataDate)
	if (!is.null(startDate))
		if (as.POSIXct(startDate) <= this$.dataDate)
			this$.lookbackDate <- as.POSIXct(startDate)
		else
			fail('startDate must be equal to or before endDate')
	else
		this$.lookbackDate <- seq(this$.dataDate,length=2,by=paste('-',daysBack,' DSTday',sep=""))[2]
})

method("setTBADataFromTsdb", "TBATRI", function(this, ...)
{
#Get all the raw TBA Data from TSDB
		
	tsdb <- TimeSeriesDB()	
	
	attributeList <- list(
				instrument="mbs_tba", 
				program=this$.program, 
				settle="", 
				quote_convention="",
				quote_type="close",
				coupon=this$.couponStringVector
				)
	
	#1n & 2n Prices
	attributeList$settle <- "1n"
	attributeList$quote_convention <- "price"
	this$.TBAzoos$price.1n <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaPrice, start=this$.lookbackDate, end=this$.dataDate, arrange.by="coupon")
	rownames(this$.TBAzoos$price.1n) <- as.numeric(rownames(this$.TBAzoos$price.1n))
	
	attributeList$settle <- "2n"
	attributeList$quote_convention <- "price"
	this$.TBAzoos$price.2n <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaPrice, start=this$.lookbackDate, end=this$.dataDate, arrange.by="coupon")
	rownames(this$.TBAzoos$price.2n) <- as.numeric(rownames(this$.TBAzoos$price.2n))
	
	#1n & 2n Settle Dates
	attributeList$settle <- "1n"
	attributeList$quote_convention <- "settle_date"
	this$.TBAzoos$settle.1n <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaPrice, start=this$.lookbackDate, end=this$.dataDate, arrange.by="coupon")
	rownames(this$.TBAzoos$settle.1n) <- as.numeric(rownames(this$.TBAzoos$settle.1n))
		
	attributeList$settle <- "2n"
	attributeList$quote_convention <- "settle_date"
	this$.TBAzoos$settle.2n <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaPrice, start=this$.lookbackDate, end=this$.dataDate, arrange.by="coupon")
	rownames(this$.TBAzoos$settle.2n) <- as.numeric(rownames(this$.TBAzoos$settle.2n))
	
	#1n dv01
	attributeList$settle <- "1n"
	attributeList$quote_convention <- "dv01"
	this$.TBAzoos$dv01.1n <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaDuration, start=this$.lookbackDate, end=this$.dataDate, arrange.by="coupon")
	rownames(this$.TBAzoos$dv01.1n) <- as.numeric(rownames(this$.TBAzoos$dv01.1n))
	
	#1n partial durations
	attributeList$settle <- "1n"
	attributeList$quote_convention <- "partial_duration"
	partialArray <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaDuration, start=this$.lookbackDate, end=this$.dataDate, arrange.by=c("coupon","tenor"))
	
	for (partialPoint in colnames(partialArray)){
		for (coupon in rownames(partialArray)){
			this$.TBAzoos$partials[[partialPoint]][[as.character(as.numeric(coupon))]] <- partialArray[[coupon,partialPoint]]
		}
	}
})

method("setSwapDataFromTsdb", "TBATRI", function(this, ...){
#Get the swap TRIs and dv01s from the TSDB
	
	tsdb <- TimeSeriesDB()
	
	attributeList <- list(
		ccy="usd",
		instrument="irs",  
		transformation="", 
		quote_convention="rate",
		quote_type="close",
		tenor=this$.partialPoints
		)
	
	
	#Load swap TRIs
	attributeList$transformation = "tri"
	this$.swapZoos$TRI  <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$swapTRI, start=this$.lookbackDate, end=this$.dataDate, arrange.by="tenor")
	
	attributeList$transformation = "dv01"
	this$.swapZoos$dv01  <- tsdb$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$swapTRI, start=this$.lookbackDate, end=this$.dataDate, arrange.by="tenor")
})

method("setCashTreasuryDataFromTsdb", "TBATRI", function(this, ...){

	attributeList <- list(
		ccy='usd',
		sector='government',
		issuer='us_treasury',
		instrument='bond',
		quote_type='close',
		quote_side='mid'
		)
		
	attributeList$quote_convention = "tri"
	attributeList$modified = "1c"
	this$.cashTreasury$TRI  <- TimeSeriesDB$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$cashTreasury, start=this$.lookbackDate, end=this$.dataDate, arrange.by="maturity")
	
	attributeList$quote_convention = "dv01"
	attributeList$modified = "otr"
	this$.cashTreasury$dv01  <- TimeSeriesDB$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$cashTreasury, start=this$.lookbackDate, end=this$.dataDate, arrange.by="maturity")	 		
})

method("setTreasuryFuturesData", "TBATRI", function(this, container.TRI='systemdb', container.dv01='tsdb', ...){
needs(container.TRI='character', container.dv01='character?')

	for (future in this$.partialFuturesMap){
		this$.futuresTreasury$TRI[[future]] <- TSDataLoader$getDataByName(container.TRI, squish(toupper(future),'.1C'), field='close', start=this$.lookbackDate, end=this$.dataDate)
	} 
	
	attributeList <- list(
		contract=this$.partialFuturesMap,
		instrument='futures',
		expiry='1c',
		quote_type='close',
		quote_side='mid',
		quote_convention='price_value_basis_point'
	)
	
	this$.futuresTreasury$dv01 <- TSDataLoader$getDataByAttributeList(container=container.dv01, attributeList, this$.sources$futuresDuration, arrangeBy="contract")
	#Add the time to sync up
	for (future in this$.partialFuturesMap){
		this$.futuresTreasury$TRI[[future]] <- setZooTimes(this$.futuresTreasury$TRI[[future]], hour=15)
		this$.futuresTreasury$dv01[[future]] <- setZooTimes(this$.futuresTreasury$dv01[[future]], hour=15)
	} 	
})


method("calculateTBAContinuousPriceChange", "TBATRI", function(this,...)
{
	#Calculate the 1c returns from the loaded prices
	#Add roll logic
	#Add ability to specify the N in price.Nc	
	
	coupons <- names(this$.TBAzoos$price.1n)
	fields <- c('price.1n','price.2n','settle.1n','settle.2n')
	
	for (coupon in coupons){
		for (field in fields){
			if (coupon %in% names(this$.TBAzoos[[field]])){
				assign(field,this$.TBAzoos[[field]][[coupon]])
			}
		}
		dates <- index(price.1n)
		if (length(dates) >= 2){
			for (dateCount in 2:length(dates)){
				date.today <- dates[dateCount]
				date.prior <- dates[dateCount-1]
				if (settle.1n[date.today][[1]]==settle.1n[date.prior][[1]]){
					price.change <- price.1n[date.today][[1]] - price.1n[date.prior][[1]]
				}
				else if (settle.1n[date.today][[1]]==settle.2n[date.prior][[1]]){
					price.change <- price.1n[date.today][[1]] - price.2n[date.prior][[1]]
				}
				else{
					price.change <- NA
				}
				if (!is.na(price.change)){
					if (dateCount == 2)	#for the first date
						price.change.zoo <- zoo(price.change, date.today)
					else #for all other dates
						price.change.zoo <- c(price.change.zoo, zoo(price.change,date.today))
				}
			}
			this$.TBA$price.1c$change[[coupon]] <- price.change.zoo
		}
	}
})

method("calculateTBAContinuousPriceLevel", "TBATRI", function(this,...)
{
	#From the continuous price changes, calculate the price levels
		
	coupons <- names(this$.TBA$price.1c$change)
	
	for (coupon in coupons){
		startPrice <- 100
		startDate <- first(index(this$.TBAzoos$price.1n[[coupon]]))
		this$.TBA$price.1c$level[[coupon]] <- getCumTriFromDailyTri(this$.TBA$price.1c$change[[coupon]], baseTri = startPrice,refDate = startDate)
	}
})

###############################################################################
#	Helper methods
###############################################################################

method("datesInZoo", "TBATRI", function(this, zooCopy, testDates, ...)
{
	needs (zooCopy="zoo?", testDates="POSIXct")
	
	#Return TRUE if all the dates exist in a single Zoo
	
	return(!is.null(zooCopy) && all(testDates %in% index(zooCopy)))
	
})

method("generateTRI", "TBATRI", function(this, hedgeBasket='vSwapPartials', ...)
{	
	coupons <- names(this$.TBA$price.1c$level)
	
	for (coupon in coupons){
		requiredZoos <- this$.TBA$price.1c$level[[coupon]]
		for (zooName in this$.requiredZoos[[hedgeBasket]]){
			requiredZoos <- merge(requiredZoos, eval(parse(text=zooName)), all=FALSE)
		}
		dates <- index(na.omit(requiredZoos))
		if (length(dates)>=2){
			print(squish('---Coupon: ',coupon))
			tri.daily.vector <- as.vector(rep(NA,length(dates)-1))
			for (dateCount in 2:length(dates)){
				date.today <- dates[dateCount]
				date.prior <- dates[dateCount-1]
				print(squish('Calculating Date: ',format(date.today,"%Y-%m-%d")))
				tri.daily.vector[[dateCount-1]] <- do.call(squish('calculateTRI',hedgeBasket), list(this, coupon, date.today, date.prior))
			}
			if (any(!is.na(tri.daily.vector)))
				this$.TRI[[hedgeBasket]][[coupon]] <- zoo(tri.daily.vector[!is.na(tri.daily.vector)],dates[2:length(dates)][!is.na(tri.daily.vector)])
		}
	}
})

method("tbaPriceChange", "TBATRI", function(this, coupon, date.today, date.prior,...){
	#just straight TRI of TBA
	needs(coupon="character", date.today="POSIXct", date.prior="POSIXct")
	dates.both <- c(date.today,date.prior)
	
	problem <- function(err) { print(squish(err, " for coupon ", coupon, "\n")); NA}
	
	#Check that every data point required exists for the required dates	
	if (!this$datesInZoo(this$.TBA$price.1c$level[[coupon]], dates.both)) return(problem("no price"))
	
	#Calculate the TRI
	tba.price.chg <- as.numeric(this$.TBA$price.1c$level[[coupon]][date.today]) - as.numeric(this$.TBA$price.1c$level[[coupon]][date.prior])
	return(tba.price.chg)		
})

###############################################################################
#	Methods for TRI generation v. hedges specific
###############################################################################

method("calculateTRIvSwapPartials", "TBATRI", function(this, coupon, date.today, date.prior,...){
	needs(coupon="character", date.today="POSIXct", date.prior="POSIXct")

	dates.both <- c(date.today,date.prior)
	
	problem <- function(err) { print(squish(err, " for coupon ", coupon, "\n")); NA}
	
	#Check that every data point required exists for the required dates	
	for (partialPoint in this$.partialPoints){
		if (!this$datesInZoo(this$.swapZoos$TRI[[partialPoint]], dates.both)) return(problem(squish("no swap partials for point ", partialPoint)))
		if (!this$datesInZoo(this$.TBAzoos$partials[[partialPoint]][[coupon]], date.prior)) return(problem(squish("no partial dur for yesterday ", partialPoint)))
		if (!this$datesInZoo(this$.swapZoos$dv01[[partialPoint]], date.prior)) return(problem(squish("no swap dv01 for yesterday ", partialPoint)))
	}
	
	#Calculate the TRI
	tba.price.chg <- this$tbaPriceChange(coupon,date.today,date.prior)
	
	swap.TRI.all <- 0
	for (partialPoint in this$.partialPoints){
		swap.TRI <- as.numeric(this$.swapZoos$TRI[[partialPoint]][date.today]) - as.numeric(this$.swapZoos$TRI[[partialPoint]][date.prior])
		swap.dv01 <- as.numeric(this$.swapZoos$dv01[[partialPoint]][date.prior])
		tba.partial.dv01 <- as.numeric(this$.TBAzoos$partials[[partialPoint]][[coupon]][date.prior])
		
		swap.TRI.all <- swap.TRI.all + (swap.TRI/swap.dv01 * tba.partial.dv01)
	}
	
	tba.TRI <- tba.price.chg - swap.TRI.all
	return(tba.TRI)
})

method("calculateTRIvNoHedge", "TBATRI", function(this, coupon, date.today, date.prior,...){
	return(this$tbaPriceChange(coupon,date.today,date.prior))		
})

method("calculateTRIvTreasury10y", "TBATRI", function(this, coupon, date.today, date.prior,...){
	needs(coupon="character", date.today="POSIXct", date.prior="POSIXct")
	
	dates.both <- c(date.today,date.prior)
	maturity <- '10y'
	
	problem <- function(err) { print(squish(err, " for coupon ", coupon, "\n")); NA}
	
	#Check that every data point required exists for the required dates	
	if (!this$datesInZoo(this$.cashTreasury$TRI[[maturity]], dates.both)) return(problem("no cash treasury TRI"))
	if (!this$datesInZoo(this$.TBAzoos$dv01.1n[[coupon]], date.prior)) return(problem("no TBA dv01 for yesterday "))
	if (!this$datesInZoo(this$.cashTreasury$dv01[[maturity]], date.prior)) return(problem("no cash treasury dv01 for yesterday"))
	
	#Calculate the TRI
	tba.price.chg <- this$tbaPriceChange(coupon,date.today,date.prior)
	
	treas.TRI <- 0
	treas.TRI <- as.numeric(this$.cashTreasury$TRI[[maturity]][date.today]) - as.numeric(this$.cashTreasury$TRI[[maturity]][date.prior])
	treas.dv01 <- as.numeric(this$.cashTreasury$dv01[[maturity]][date.prior])
	tba.dv01 <- as.numeric(this$.TBAzoos$dv01.1n[[coupon]][date.prior])
	
	tba.TRI <- tba.price.chg - (treas.TRI * tba.dv01/treas.dv01)
	return(tba.TRI)
})

#################### Treasury Futures TRI Methods #############################################

method("calculateTRIvFuturesTU_FV_TY_US", "TBATRI", function(this, coupon, date.today, date.prior, ...){
	weights <- diag(1,4,4)
	return(this$calculateTRIvTreasuryFutures(coupon,date.today,date.prior,weights))
})

method("calculateTRIvFuturesTU_TY", "TBATRI", function(this, coupon, date.today, date.prior, ...){
	weights <- matrix(0,4,4)
	weights[1,1] <- 1
	weights[1,2] <- 0.5
	weights[3,2] <- 0.5
	weights[3,3:4] <- 1
	return(this$calculateTRIvTreasuryFutures(coupon,date.today,date.prior,weights))
})

method("calculateTRIvFuturesTY", "TBATRI", function(this, coupon, date.today, date.prior, ...){
	weights <- matrix(0,4,4)
	weights[3,] <- 1
	return(this$calculateTRIvTreasuryFutures(coupon,date.today,date.prior,weights))
})

method("calculateTRIvTreasuryFutures", "TBATRI", function(this, coupon, date.today, date.prior, partialWeights, ...){
	needs(coupon="character", date.today="POSIXct", date.prior="POSIXct")
	dates.both <- c(date.today,date.prior)
	
	problem <- function(err) { print(squish(err, " for coupon ", coupon, "\n")); NA}
	rownames(partialWeights) <- this$.partialFuturesMap
	requiredFutures <- rownames(partialWeights)[rowSums(partialWeights)>0]
	
	#Check that every data point required exists for the required dates
	for (partialPoint in this$.partialPoints){
		futurePoint <- this$.partialFuturesMap[[match(partialPoint,this$.partialPoints)]]  #Maps the partial point to futures ticker
		if (sum(partialWeights[futurePoint,]) > 0){
			if (!this$datesInZoo(this$.futuresTreasury$TRI[[futurePoint]], dates.both)) return(problem(squish("no futures TRI for point ", futurePoint)))
			if (!this$datesInZoo(this$.futuresTreasury$dv01[[futurePoint]], date.prior)) return(problem(squish("no futures dv01 for yesterday ", futurePoint)))
		}
		if (!this$datesInZoo(this$.TBAzoos$partials[[partialPoint]][[coupon]], date.prior)) return(problem(squish("no partial dur for yesterday ", partialPoint)))
	}
	
	tbaPartialDv01s <- as.numeric(sapply(this$.partialPoints, function(pp) this$.TBAzoos$partials[[pp]][[as.character(coupon)]][date.prior][[1]]))
	futuresDv01s <- as.numeric(sapply(this$.partialFuturesMap, function(pp) ifelse((pp %in% requiredFutures), this$.futuresTreasury$dv01[[pp]][date.prior][[1]], 0)))
	futuresTRIs <- as.numeric(sapply(this$.partialFuturesMap, function(pp) ifelse((pp %in% requiredFutures), this$.futuresTreasury$TRI[[pp]][date.today][[1]], 0))
							- sapply(this$.partialFuturesMap, function(pp) ifelse((pp %in% requiredFutures), this$.futuresTreasury$TRI[[pp]][date.prior][[1]], 0)))
	
	tba.price.chg <- this$tbaPriceChange(coupon,date.today,date.prior)
	
	ratioMatrix <- (1/futuresDv01s) %*% t(tbaPartialDv01s)
	expectedMatrix <- ratioMatrix * futuresTRIs
	futuresExpected <- sum(rowSums(expectedMatrix * partialWeights, na.rm=TRUE))
	return(as.vector(tba.price.chg) - futuresExpected)
})

###############################################################################
#	Methods for TRI writing to tsdb
###############################################################################

method("uploadTRItoTsdb", "TBATRI", function(this, hedgeBasket='vSwapPartials', source='internal', uploadPath=NULL, uploadMethod='file',...){
	#Save the results to tsdb
	coupons <- names(this$.TRI[[hedgeBasket]])
	if (is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()
	
	#write out the tri_daily
	
	for (coupon in coupons){
		tsdbName <- paste(this$.program, format(as.numeric(coupon),nsmall=1), '1c', 'tri_daily',hedgeBasket,sep="_")
		uploadZooToTsdb(this$.TRI[[hedgeBasket]][[coupon]], tsdbNames=tsdbName, tsdbSources=source, uploadMethod=uploadMethod,uploadFilename=tsdbName, uploadPath=uploadPath)
	}
	
	#calculate and write out the tri
	
	for (coupon in coupons){
		triDaily <- this$.TRI[[hedgeBasket]][[coupon]]
		tsdbName <- paste(this$.program, format(as.numeric(coupon),nsmall=1), '1c', 'tri',hedgeBasket,sep="_")
		if (TimeSeriesDB()$timeSeriesExists(tsdbName) && (TimeSeriesDB$numObservations(tsdbName,source)!=0)){
			existingEndDate <-  businessDaysAgo(-1,this$.lookbackDate,'nyb')
			existingZoo <- TimeSeriesDB()$retrieveOneTimeSeriesByName(tsdbName, source, end=existingEndDate)
			startPrice <- last(existingZoo)
			startDate <- last(index(existingZoo))
		}
		else{
			startPrice <- 100
			startIndex <- match(first(index(triDaily)), index(this$.TBA$price.1c$level[[coupon]]))-1
			startDate <- index(this$.TBA$price.1c$level[[coupon]])[startIndex]
		}
		
		triZoo <- getCumTriFromDailyTri(triDaily[index(triDaily)>startDate], baseTri = startPrice,refDate = startDate)
		uploadZooToTsdb(triZoo, tsdbNames=tsdbName, tsdbSources=source, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

