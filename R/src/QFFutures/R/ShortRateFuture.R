# ShortRateFuture Class
# 
# Author: RSheftel
###############################################################################

#Make faster by replacing roll apply with the rowSums?

constructor("ShortRateFuture", function(contract = NULL, rollDaysPriorToExpiry=NULL){
	this <- extend(RObject(), "ShortRateFuture", .contract=contract, .rollDays=rollDaysPriorToExpiry)
	constructorNeeds(this, contract="character", rollDaysPriorToExpiry="numeric|integer")
	if (inStaticConstructor(this)) return(this)
	this$setContractType()
	this$setDateRange()
	this$setSources()
	this$setRollObject()
 	this$makeBundleList()
	this$makePackList()
	this$.yellowKey = "Comdty"
	return(this)
})

method("setContractType", "ShortRateFuture", function(this, contractType='continuous', ...){
	needs(contractType='character')
	switch(contractType,
		continuous = this$.contractSuffix <- 'c',
		fail(squish('Not valid contract type: ',contractType))
	)
})

method("setDateRange", "ShortRateFuture", function(this, startDate=NULL, endDate=NULL, ...){
	needs(startDate='character|POSIXct?', endDate='character|POSIXct?')
	this$.dates$start <- startDate
	this$.dates$end <- endDate
})

method("setSources", "ShortRateFuture", function(this, contractSource='internal',...){
	needs(contractSource='character?')
	this$.sources$contract <- contractSource		
})

method("setRollObject", "ShortRateFuture", function(this, ...){
	this$.roll <- Roll(function(x) daysToExpiry(x,this$.rollDays))
})

method("daysToEndOfStub", "ShortRateFuture", function(this, ...){
	dateRange <- as.POSIXct(format(seq(as.Date(this$.dates$start),as.Date(this$.dates$end),by='day')))
		
	numCycles <- ceiling(as.numeric(difftime(last(dateRange),first(dateRange))/365.25))
	contract <- Contract(this$.contract, this$.yellowKey, numCycles=numCycles+1)
	
	rollDates <- contract$rollDatesAsOf(this$.roll,first(dateRange))
	contractSpecs <- contract$rawList(first(dateRange))
	
	contractSpecs[,"futureYearMonth"] <- contractSpecs[,"futureYear"]*100 + contractSpecs[,"futureMonth"]
	
	#Set up the frontExpiry dates
	rollDateLookup <- as.Date(c(as.POSIXct('1900-01-01'),rollDates))
	rollDateValues <- as.Date(c(rollDates,as.POSIXct('2900-12-31')))
	frontExpiry <- approx(rollDateLookup,rollDateValues,as.Date(dateRange),method='constant',rule=2,f=0)$y	
	frontExpiry <- as.POSIXct(format(as.Date(frontExpiry)))
	frontExpiry[frontExpiry > last(rollDates)] <- NA

	nextContractYearMonth <- (as.POSIXlt(frontExpiry)$year + 1900)*100 + (as.POSIXlt(frontExpiry)$mon+1)
	nextContractStart <- contractSpecs[ match(nextContractYearMonth,contractSpecs[,"futureYearMonth"]),"expiryDate"]
	
	return(zoo(round(as.numeric(difftime(nextContractStart,dateRange,unit="days")),0),dateRange))
})

#################################################################################
#   Single Contract Methods
#################################################################################

method("calculateSingleContracts", "ShortRateFuture", function(this, numberContracts=20, container='tsdb', ...){
	needs(numberContracts='numeric', container='character')
	
	forwardMonths <- 1:numberContracts
	this$.contractObj <- Contract(this$.contract, this$.yellowKey, numCycles=(ceiling(numberContracts/4)+1))
	
	this$.singleContracts <- vector('list',length(forwardMonths))
	names(this$.singleContracts) <- forwardMonths	
	this$.contractObj$loadRawData(startDate = this$.dates$start, endDate = this$.dates$end, dataSource = this$.sources$contract, container=container)
	for (expiry in forwardMonths){
		print(squish('Making single this$.contract for month: ',expiry))
		this$.singleContracts[[expiry]] <- this$.contractObj$continuousSimple(expiry, rollObj=this$.roll)
	}	
})

method("getSingleContract", "ShortRateFuture", function(this, contractNumber, ...){
	return(this$.singleContracts[[contractNumber]])	
})

method("getContractObject", "ShortRateFuture", function(this, ...){
	return(this$.contract)
})

method("uploadSingleContracts", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file',...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?')
	
	for (expiry in names(this$.singleContracts)){
		tsdbName <- squish(this$.contract,'.',expiry,this$.contractSuffix,'_price_mid')
		#PURGE!
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.singleContracts[[expiry]], tsdbNames=tsdbName, tsdbSources=tsdbSource, 
						uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}	
})

#################################################################################
#   Pack Methods
#################################################################################

method("makePackList", "ShortRateFuture", function(this, ...){
	packs <- c('white','red','green','blue','gold')
	this$.packList <- vector('list',length(packs))
	names(this$.packList) <- packs
	for (count in seq_along(packs)){
		this$.packList[[packs[count]]]$underlyingContracts <- ((count-1)*4+1):(count*4)
	}
	#Set up empty pack list
	this$.packs <- list()				
})

method("calculatePacks", "ShortRateFuture", function(this, packs=c('white','red','green','blue','gold'),...){
	needs(packs='character')
	singleContractZoos <- do.call(merge,this$.singleContracts)
	for (pack in packs){
		underlyingMonths <- this$.packList[[pack]]$underlyingContracts
		underlyingZoos <- singleContractZoos[,underlyingMonths]
		this$.packs[[pack]] <- na.omit(rollapply(underlyingZoos,1,mean,by.column=FALSE))
	}
})

method("getPack", "ShortRateFuture", function(this, pack, ...){
	needs(pack='character')
	return(this$.packs[[pack]])
})

method("uploadPacks", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file',...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?')
	
	for (pack in names(this$.packs)){
		tsdbName <- squish(this$.contract,'_',pack,'_pack_1',this$.contractSuffix,'_price_mid')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.packs[[pack]], tsdbNames=tsdbName, tsdbSources=tsdbSource,
						uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

method("makePackCombination", "ShortRateFuture", function(this, weights, name, ...){
	needs(weights='numeric', name='character')
	if(is.null(this$.packCombos)) this$.packCombos <- list()
	packZoos <- do.call(merge,this$.packs)
	failIf(length(weights)!= ncol(packZoos),"Weights vector must be same dimension as pack contracts")
	
	weightedZoos <- rollapply(packZoos, 1, function(x) x*weights, by.column=FALSE)
	goodZoos <- weightedZoos[,weights!=0]
	this$.packCombos[[name]] <- na.omit(zoo(rowSums(goodZoos),index(packZoos)))
				
})

method("makePackFly", "ShortRateFuture", function(this, nearWingPack=NULL, middlePack=NULL, farWingPack=NULL, ...){
	needs(nearWingPack='character', middlePack='character', farWingPack='character')
	weights <- rep(0,length(this$.packs))
	weights[match(nearWingPack,names(this$.packs))] <- -1
	weights[match(middlePack,names(this$.packs))] <- 2
	weights[match(farWingPack,names(this$.packs))] <- -1
	name <- squish(nearWingPack,'_',middlePack,'_',farWingPack,"_pack_fly_1",this$.contractSuffix)
	this$makePackCombination(weights, name)		
})

method('makePackSpread', "ShortRateFuture", function(this, basePack=NULL, hedgePack=NULL,...){
	needs(basePack='character', hedgePack='character')
	if(is.null(this$.packSpreads)) this$.packSpreads <- list()
	baseZoo <- this$.packs[[basePack]]
	hedgeZoo <- this$.packs[[hedgePack]]
	this$.packSpreads[[squish(basePack,'_',hedgePack)]] <- na.omit(baseZoo - hedgeZoo)	
})

method("uploadPackSpreads", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file', triBase=100, ...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?',triBase='numeric|integer')

	#Makes an _tri time series as well by adding a triBase to keep from negative numbers.	
	for (packSpread in names(this$.packSpreads)){
		tsdbName <- squish(this$.contract,'_',packSpread,'_pack_spread_1',this$.contractSuffix,'_tri')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.packSpreads[[packSpread]] + triBase, tsdbNames=tsdbName,
						tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

method("uploadPackCombinations", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file', triBase=100, ...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?',triBase='numeric|integer')
	
	#Makes an _tri time series as well by adding a triBase to keep from negative numbers.	
	for (packCombo in names(this$.packCombos)){
		tsdbName <- squish(this$.contract,'_',packCombo,'_tri')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.packCombos[[packCombo]] + triBase, tsdbNames=tsdbName,
						tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

#################################################################################
#   Bundle Methods
#################################################################################

method("makeBundleList", "ShortRateFuture", function(this, ...){
	bundles <- paste(1:10,'y',sep="")
	this$.bundleList <- vector('list',length(bundles))
	names(this$.bundleList) <- bundles
	for (count in seq_along(bundles)){
		this$.bundleList[[bundles[count]]]$underlyingContracts <- 1:(count*4)
	}
	#Set up empty bundle list
	this$.bundles <- list()				
})

method("calculateBundles", "ShortRateFuture", function(this, bundles=c('1y','2y','3y','4y','5y'),...){
	needs(bundles='character')
		
	singleContractZoos <- do.call(merge,this$.singleContracts)
	for (bundle in bundles){
		underlyingMonths <- this$.bundleList[[bundle]]$underlyingContracts
		underlyingZoos <- singleContractZoos[,underlyingMonths]
		this$.bundles[[bundle]] <- na.omit(rollapply(underlyingZoos,1,mean,by.column=FALSE))
	}
})

method("calculateBundlePvbps", "ShortRateFuture", function(this, bundles=c('1y','2y','3y','4y','5y'), ...){
	needs(bundles='character')
	
	stubYears <- this$daysToEndOfStub()/365.25
	for (bundle in bundles){
		bundleYears <- length(this$.bundleList[[bundle]]$underlyingContracts)/4
		bundleDates <- index(this$.bundles[[bundle]])
		bundleStubYears <- zooForDates(stubYears, as.POSIXct(format(as.Date(bundleDates))))
		bundleStubYears <- setZooTimes(bundleStubYears, as.POSIXlt(first(bundleDates))$hour)
		this$.bundlePvbps[[bundle]] <- bundleStubYears/bundleYears + 1
	}
})

method("getBundle", "ShortRateFuture", function(this, bundle, ...){
	needs(bundle='character')
	return(this$.bundles[[bundle]])
})

method("uploadBundles", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file',...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?')
	
	for (bundle in names(this$.bundles)){
		tsdbName <- squish(this$.contract,'_',bundle,'_bundle_1',this$.contractSuffix,'_price_mid')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.bundles[[bundle]], tsdbNames=tsdbName, tsdbSources=tsdbSource,
						uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

method("uploadBundlePvbps", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file',...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?')
	
	for (bundle in names(this$.bundlePvbps)){
		tsdbName <- squish(this$.contract,'_',bundle,'_bundle_1',this$.contractSuffix,'_pvbp')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.bundlePvbps[[bundle]], tsdbNames=tsdbName, tsdbSources=tsdbSource,
			uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

method('makeBundleSpread', "ShortRateFuture", function(this, baseBundle=NULL, hedgeBundle=NULL,...){
	needs(baseBundle='character', hedgeBundle='character')
	if(is.null(this$.bundleSpreads)) this$.bundleSpreads <- list()	
	baseZoo <- this$.bundles[[baseBundle]]
	hedgeZoo <- this$.bundles[[hedgeBundle]]
	this$.bundleSpreads[[squish(baseBundle,'_',hedgeBundle)]] <- na.omit(baseZoo - hedgeZoo)	
})

method("uploadBundleSpreads", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file', triBase=100, ...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?',triBase='numeric|integer')
	
	#Makes an _tri time series as well by adding a triBase to keep from negative numbers.	
	for (bundleSpread in names(this$.bundleSpreads)){
		tsdbName <- squish(this$.contract,'_',bundleSpread,'_bundle_spread_1',this$.contractSuffix,'_tri')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.bundleSpreads[[bundleSpread]] + triBase, tsdbNames=tsdbName, 
						tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

#################################################################################
#   Single Contract Combination Methods
#################################################################################

method("makeSingleCombination", "ShortRateFuture", function(this, weights, name, ...){
	needs(weights='numeric', name='character')
	if(is.null(this$.singleCombos)) this$.singleCombos <- list()
	singleContractZoos <- do.call(merge,this$.singleContracts)
	failIf(length(weights)!= ncol(singleContractZoos),"Weights vector must be same dimension as single contracts")
	
	weightedZoos <- rollapply(singleContractZoos, 1, function(x) x*weights, by.column=FALSE)
	goodZoos <- weightedZoos[,weights!=0]
	this$.singleCombos[[name]] <- na.omit(zoo(rowSums(goodZoos),index(singleContractZoos)))
})

method('makeSingleSpread', "ShortRateFuture", function(this, longForwardMonth=NULL, shortForwardMonth=NULL, ...){
	needs(longForwardMonth='numeric', shortForwardMonth='numeric')
	weights <- rep(0,length(this$.singleContracts))
	weights[longForwardMonth] <- 1
	weights[shortForwardMonth] <- -1
	this$makeSingleCombination(weights, squish(longForwardMonth,this$.contractSuffix,'_',shortForwardMonth,this$.contractSuffix,'_spread'))
})

method('makeSingleButterfly', "ShortRateFuture", function(this, nearWingMonth=NULL, middleMonth=NULL, farWingMonth=NULL, ...){
	needs(nearWingMonth='numeric', middleMonth='numeric', farWingMonth='numeric')
	weights <- rep(0,length(this$.singleContracts))
	weights[nearWingMonth] <- -1
	weights[middleMonth] <- 2
	weights[farWingMonth] <- -1
	name <- squish(nearWingMonth,this$.contractSuffix,'_',middleMonth,this$.contractSuffix,'_',farWingMonth,this$.contractSuffix,'_fly')
	this$makeSingleCombination(weights, name)
})

method("uploadSingleCombinations", "ShortRateFuture", function(this, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file', triBase=100, ...){
	needs(tsdbSource='character?', uploadPath='character?', uploadMethod='character?',triBase='numeric|integer')
	
	#Makes an _tri time series as well by adding a triBase to keep from negative numbers.	
	for (singleCombo in names(this$.singleCombos)){
		tsdbName <- squish(this$.contract,'_',singleCombo,'_tri')
		if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		uploadZooToTsdb(this$.singleCombos[[singleCombo]] + triBase, tsdbNames=tsdbName,
						tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

method('markets', 'ShortRateFuture', function(this,...){
	conn <- SQLConnection()
	conn$init()	
	as.character(conn$select(
		"select Name from SystemDB..Time_series_data where Name NOT Like '%.GFUT.%'and Name NOT Like '%.TS.%' and Type = 'Future' and Expiry = 'Continuous' and SubSector = 'Short Rate Future'"
	)[[1]])
})

method('adjustedList', 'ShortRateFuture', function(this,numCycles,date,...){
	List(Contract(this$.contract, this$.yellowKey, numCycles=numCycles))$adjusted(date,this$.roll)
})

method('frontDataForMarket', 'ShortRateFuture', function(this,marketName,adjustedList,field = 'ticker',...){
	# Special cases
	if(marketName %in% c("ED.1C.3C.5C.FLY","ED.1C.5C.SPREAD","ED.WHITE.RED.GREEN.PACK.FLY"))
		return(adjustedList[1,field])	
	if(marketName %in% c("ED.RED.GREEN.BLUE.PACK.FLY","ED.RED.GREEN.PACK.SPREAD"))
		return(adjustedList[5,field])
	if(marketName == "ED.4C.5C.SPREAD")
		return(adjustedList[4,field])
	# General cases
	splitStr <- strsplit(marketName,'\\.')[[1]]
	suffix <- splitStr[NROW(splitStr)]
	color <- splitStr[2]
	nth <- as.numeric(leftStr(suffix,nchar(suffix)-1))	
	colors <- c("WHITE","RED","GREEN","BLUE","GOLD","PURPLE","ORANGE","PINK","SILVER","COPPER")
	indices <- c(1,5,9,13,17,21,25,29,33,37)
	if(color %in% colors){
		failIf(nth != 1,'Case not handled! Where market name has color and nth != 1')
		for(i in 1:NROW(colors))
			if(color == colors[i]) return(adjustedList[indices[i],field])	
	}else{
		adjustedList[nth,field]		
	}
})