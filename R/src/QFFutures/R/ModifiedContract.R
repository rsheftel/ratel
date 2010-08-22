# Methods that act on a modified futures contract
# 
# Author: RSheftel
###############################################################################

constructor("ModifiedContract", function(market = NULL, specificName = NULL, yellowKey='Comdty'){
	this <- extend(RObject(), "ModifiedContract", .market=market, .yellowKey=yellowKey)
	constructorNeeds(this, market="character", specificName="character?")
	if (inStaticConstructor(this)) return(this)
	if (is.null(specificName)) specificName <- tolower(first(unlist(strsplit(market,'\\.'))))
	this$specificName(specificName)
	return(this)
})

method("specificName","ModifiedContract", function(this, specificName=NULL, ...){
	needs(specificName='character?')
	if (is.null(specificName)) return(this$.specificName)
	this$.specificName <- specificName
})

method("specificYearMonthsFromASCII", "ModifiedContract", function(this,...){
	asciiFilename <- SystemDB()$asciiFilename(this$.market)
	modifiedZoo <- read.zoo(asciiFilename,sep=",", format="%Y%m%d", header=TRUE)
	index(modifiedZoo) <- as.POSIXct(as.character(index(modifiedZoo)))
	return(modifiedZoo[,'Numeric.Delivery.Month'])		
})

method("specificYearMonthsFromTSDB", "ModifiedContract", function(this, ...){
	roll <- eval(parse(text=SystemDB$rebalanceFunction(this$.market)))
	dates <- index(Symbol(this$.market)$series())
	return(Contract(this$.specificName,this$.yellowKey)$frontYearMonths(dates,roll))
})

method("specificYearMonths", "ModifiedContract", function(this,...){
	histData <- SystemDB$histDaily(this$.market)
	if (histData=='ASCII') return(this$specificYearMonthsFromASCII())
	return(this$specificYearMonthsFromTSDB())
})

method("specificAttribute", "ModifiedContract", function(this, quote_convention, source, container="tsdb", ...){
	needs(quote_convention='character', source='character', container='character')
	failIf(is.null(this$.specificName),"Specific Name must be specified")
	
	targetYearMonths <- this$specificYearMonths()
	attributeList <- list(	contract=this$.specificName,
							quote_convention=quote_convention,
							quote_type='close',
							expiry='actual'
							)		
	specificAttributeZoo <- TSDataLoader$getDataByAttributeList(container=container, attributeList, source, arrangeBy='ticker')
	specificAttributeZoo <- strip.times.zoo(do.call(merge, specificAttributeZoo))
	specificAttributeZoo <- zooForDates(specificAttributeZoo, index(targetYearMonths), remove.na=FALSE)
		
	specificTickerYearMonths <- as.numeric(rightStr(colnames(specificAttributeZoo), 6))
	monthYearGrid <- matrix(specificTickerYearMonths, nrow(specificAttributeZoo), ncol(specificAttributeZoo), byrow=TRUE)
	matchGrid <- monthYearGrid==targetYearMonths
	matchGrid[matchGrid==FALSE] <- NA
	matchAttributeZoo <- specificAttributeZoo * zoo(matchGrid,index(specificAttributeZoo))
	resultZoo <- na.omit(matchAttributeZoo[,1])
	for (matchCol in 2:ncol(matchAttributeZoo)){
		resultZoo <- rbind(resultZoo,na.omit(matchAttributeZoo[,matchCol]))
	}
	return(resultZoo)
})