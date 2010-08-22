# Class that works on pairs of modified futures options
# 
# Author: RSheftel
###############################################################################


constructor("ModifiedFuturesPair", function(market.base = NULL, market.hedge = NULL){
	this <- extend(RObject(), "ModifiedFuturesPair")
	constructorNeeds(this, market.base="character", market.hedge="character")
	if (inStaticConstructor(this)) return(this)
	this$.market$base <- market.base
	this$.market$hedge <- market.hedge
	this$.legs <- c('base','hedge')
	return(this)
})

method("specificNames", "ModifiedFuturesPair", function(this, specificName=NULL, leg=NULL, ...){
	needs(specificName='character', leg='character')
	if (is.null(specificName)) return(this$.specificName[[leg]])
	this$.specificName[[leg]] <- specificName
})

method("setUnderlyingTRIs", "ModifiedFuturesPair", function(this, container='systemdb', source='internal', field='close', ...){
	needs(container='character',source='character?', field='character?')
	for (leg in this$.legs){
		this$.tri$level[[leg]] <- TSDataLoader$getDataByName(container, this$.market[[leg]], source=source, field=field)	
	}		
})

method("setHedgeRatio", "ModifiedFuturesPair", function(this, specificNames=list(base=NULL,hedge=NULL), hedgeRatio.name=NULL, hedgeRatio.source=NULL, container='tsdb', calculatePvbp=TRUE, ...){
	needs(specificNames='list(character)?', hedgeRatio.name='character',hedgeRatio.source='character?',container='character')
	this$.hedgeRatio$name <- hedgeRatio.name
	this$.hedgeRatio$source <- hedgeRatio.source
	for (leg in this$.legs){
		mc <- ModifiedContract(this$.market[[leg]])
		mc$specificName(specificNames[[leg]])
		this$.specificName[[leg]] <- mc$specificName()
		if ((container=='tsdb') && calculatePvbp){
			this$.hedgeRatio[[leg]] <- mc$specificAttribute(hedgeRatio.name, hedgeRatio.source, container)
		}
		else{
			this$.hedgeRatio[[leg]] <- TSDataLoader$getDataByName(container, paste(this$.specificName[[leg]],hedgeRatio.name,sep="_"),hedgeRatio.source)
		}
		this$.hedgeRatio[[leg]] <- strip.times.zoo(this$.hedgeRatio[[leg]])
	}
})

method("setHedgeRatioByName", "ModifiedFuturesPair", function(this, leg, hedgeName, hedgeSource, container='tsdb'){
	needs(leg="character", hedgeName="character", hedgeSource="character", container='character')
	this$.hedgeRatio[[leg]] <- strip.times.zoo(TSDataLoader$getDataByName(container, hedgeName, hedgeSource))	
})

method("generateTRI", "ModifiedFuturesPair", function(this, hedgeRatio.lag=0, ...){
	needs(hedgeRatio.lag = 'numeric|integer')
	this$.hedgeRatio$ratio <- na.omit(this$.hedgeRatio$base / this$.hedgeRatio$hedge)
	futuresPair <- Pair(this$.tri$level$base, this$.tri$level$hedge)
	this$.tri$daily$pair <- futuresPair$calculateZooFromHedgeRatios(lag(this$.hedgeRatio$ratio,hedgeRatio.lag))
	firstDate <- futuresPair$getFirstDateFromZooFromHedgeRatios()
	this$.tri$level$pair <- getCumTriFromDailyTri(this$.tri$daily$pair, baseTri = 100,refDate = firstDate)
})

method("getTRI", "ModifiedFuturesPair", function(this, ...){
	return(this$.tri$level$pair)	
})

method("getHedgeRatio", "ModifiedFuturesPair", function(this, ...){
	return(this$.hedgeRatio$ratio)	
})

method("uploadTRI", "ModifiedFuturesPair", function(this, tsdbName=NULL, tsdbSource=NULL, uploadPath=NULL, uploadMethod='file', timeStamp=NULL, ...){	
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()
	
	if (is.null(tsdbName)) tsdbName <- paste(this$.market$base, this$.market$hedge, this$.hedgeRatio$name, this$.hedgeRatio$source, sep="_")
	
	#PURGE!
	if ((uploadMethod=='direct') && (TimeSeriesDB()$timeSeriesExists(tsdbName))) TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
	
	tsdbValues <- this$.tri$level$pair
	if (!is.null(timeStamp)) tsdbValues <- setZooTimes(tsdbValues, hour=timeStamp)  		
	uploadZooToTsdb(tsdbValues, tsdbNames=tsdbName, tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)			
})