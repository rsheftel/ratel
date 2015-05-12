getSystemDBCreditMarketName <- function(cdsTickerID,tenor,toUpper = TRUE){
	
	needs(cdsTickerID = "character")
	needs(toUpper = "logical")
	
	# Index (to do: have a mapping table)
	
	if(cdsTickerID == "cdx-na-ig")return(paste("CNAIG",characterToNumericTenor(tenor),sep =""))
	if(cdsTickerID == "cdx-na-ig-hvol")return(paste("CNAIGHV",characterToNumericTenor(tenor),sep =""))
	if(cdsTickerID == "itraxx-eur")return(paste("IEU",characterToNumericTenor(tenor),sep =""))
	
	# Single names
	
	tick <- substr(sub("-","",strsplit(cdsTickerID,"_")[[1]][1]),1,4)    
	if(cdsTickerID == "attinc-ml_snrfor_usd_mr")tick <- "attim"
	if(cdsTickerID == "cmcsa-cablellc_snrfor_usd_mr")tick <- "cmcsc"
	
	docSen <- NULL
	if(strsplit(cdsTickerID,"_")[[1]][2] == "snrfor"){
		if(strsplit(cdsTickerID,"_")[[1]][4] =="mr")docSen <- "M"
		if(strsplit(cdsTickerID,"_")[[1]][4] =="xr")docSen <- "X"
	}
	if(strsplit(cdsTickerID,"_")[[1]][2] == "sublt2"){
		if(strsplit(cdsTickerID,"_")[[1]][4] =="mr")docSen <- "U"
		if(strsplit(cdsTickerID,"_")[[1]][4] =="xr")docSen <- "V"
	}
	
	needs(docSen = "character")
	
	result <- paste(                              
			tick,                             
			characterToNumericTenor(tenor),                                                                          
			docSen,  
			sep = "")
	
	if(toUpper)result <- toupper(result)
	return(result)
}

getMarketFromTicker <- function(ticker,lowerCase = FALSE){	
	data <- data.frame(read.csv(system.file("credit_data","ticker_market_map.csv", package = "QFCredit"), sep = ",", header = TRUE),stringsAsFactors = FALSE)	
	tickers <- as.character(data[,1])		
	marketsLower <- as.character(data[,2])	
	marketsUpper <- as.character(data[,3])
	m <- match(ticker,tickers)
	if(lowerCase) marketsLower[m]
	else marketsUpper[m]
}

getRefBase <- function(currentData,refDate)
{
	needs(refDate = "character|POSIXt?")
	if(!is.null(currentData) && !is.null(refDate)){
		assert(class(currentData)=="zoo",paste(currentData,"should be a zoo object"))
		refDate <- as.POSIXct(refDate)
		priorDates <- subset(currentData,index(currentData)<refDate)
		if(NROW(priorDates)>0){
			result <- getZooDataFrame(priorDates[NROW(priorDates)])
			return(list(refDate = index(result), refIndex = as.numeric(result)))
		}
	}
	return(list(refDate = refDate, refIndex = 100))
}