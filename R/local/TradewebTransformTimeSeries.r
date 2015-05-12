# Export transformed time series on Tradeweb data
###############################################################################


library(AtgData)
prodDB()

#Basic attributes

outputDir <- "//cnyc12p20006a/rdg$/ATG/Reporting/transformed/"
filenameBase <- 'tradeweb_daily_timeSeries_'

#US Treasury data
attributes <- list()
attributes$base <- list(venue		= "tradeweb",
						series_type	= "venue_performance",
						instrument	= "treasury",
						issuer		= "us_treasury")

source <- 'tradeweb'					
sectors <- 'nominal'


####################### Loop through it all  ##############################

for (sector in sectors){
	#Make aggregated nominal data from raw data
	attributes$nominal <- attributes$base
	attributes$nominal$sector <- sector
	
	# Load the raw measures and calculate the derived measures
	units <- c('volume','trade_count')
	measures.raw <- c('inquiry_amount','market_share','hit_rate','no_quote_rate') 
	
	#use the inquiry_amount to get the sub-sector names
	attributes[[sector]]$units <- units[[1]]
	attributes[[sector]]$measure <- measures.raw[[1]]
	temp <- TSDB$observationsByAttributes(attributes[[sector]],source=source)
	tickers <- rownames(temp)
	
	maturities <- c()
	for (ticker in tickers){
		x <- TSDB$attributes(ticker)
		maturities <- c(maturities, x['maturity_range'][[1]])
	}
	sort(maturities)
		
	data <- list()
	for (unit in units){
		for (measure in measures.raw){
			for (maturity in maturities){
				attributes[[sector]]$units <- unit
				attributes[[sector]]$measure <- measure
				attributes[[sector]]$maturity_range <- maturity
				print(squish("Loading for sector: ",sector,", unit: ",unit,", measure: ",measure,", maturity: ",maturity))
				data.matrix <- TSDB$observationsByAttributes(attributes[[sector]],source=source)
				data[[sector]][[unit]][[measure]][[maturity]] <- TSReader$tsdbMatrixToZoo(data.matrix)
			}
		}
	}
	
	measures.calculated <- c('executed_amount','total_market_size','no_quote_amount')
	for (unit in units){
		for (maturity in maturities){
			baseName <- paste(c(attributes$base$venue, attributes$base$instrument, sector, maturity, unit),collapse="_")
			
			x <- 'executed_amount'
			data[[sector]][[unit]][[x]][[maturity]] <- data[[sector]][[unit]]$inquiry_amount[[maturity]] * data[[sector]][[unit]]$hit_rate[[maturity]]
			colnames(data[[sector]][[unit]][[x]][[maturity]]) <- paste(c(baseName,x),collapse="_")
			
			x <- 'total_market_size'
			data[[sector]][[unit]][[x]][[maturity]] <- data[[sector]][[unit]]$executed_amount[[maturity]] / data[[sector]][[unit]]$market_share[[maturity]]
			colnames(data[[sector]][[unit]][[x]][[maturity]]) <- paste(c(baseName,x),collapse="_")
			data[[sector]][[unit]][[x]][[maturity]][data[[sector]][[unit]][[x]][[maturity]]==Inf] <- NA
			
			x <- 'no_quote_amount'
			data[[sector]][[unit]][[x]][[maturity]] <- data[[sector]][[unit]]$inquiry_amount[[maturity]] * data[[sector]][[unit]]$no_quote_rate[[maturity]]
			colnames(data[[sector]][[unit]][[x]][[maturity]]) <- paste(c(baseName,x),collapse="_")
		}
	}
	
	# Create the roll ups
	rollUpSums <- c(measures.calculated, 'inquiry_amount')
	for (unit in units){
		for (measure in rollUpSums){
			mergedZoo <- do.call(merge, data[[sector]][[unit]][[measure]])
			data[[sector]][[unit]][[measure]]$ALL <- getZooDataFrame(rollapply(mergedZoo,width=1,FUN=sum,by.column=FALSE))
			colnames(data[[sector]][[unit]][[measure]]$ALL) <- 
				paste(c(attributes$base$venue, attributes$base$instrument, sector, 'ALL', unit, measure), collapse="_")
		}
	}
	
	###-----------------------------
	#In final version, make each calc a method, then a method to calc a list that is passed in
	###-----------------------------

	rollUpCalculated <- c('market_share', 'hit_rate', 'no_quote_rate')
	for (unit in units){
		measure <- 'market_share'
		data[[sector]][[unit]][[measure]]$ALL <- data[[sector]][[unit]]$executed_amount$ALL / data[[sector]][[unit]]$total_market_size$ALL 
		colnames(data[[sector]][[unit]][[measure]]$ALL) <- 
			paste(c(attributes$base$venue, attributes$base$instrument, sector, 'ALL', unit, measure), collapse="_")
		
		
		measure <- 'hit_rate'
		data[[sector]][[unit]][[measure]]$ALL <- data[[sector]][[unit]]$executed_amount$ALL / data[[sector]][[unit]]$inquiry_amount$ALL 
		colnames(data[[sector]][[unit]][[measure]]$ALL) <- 
			paste(c(attributes$base$venue, attributes$base$instrument, sector, 'ALL', unit, measure), collapse="_")
		
		
		measure <- 'no_quote_rate'
		data[[sector]][[unit]][[measure]]$ALL <- data[[sector]][[unit]]$no_quote_amount$ALL / data[[sector]][[unit]]$inquiry_amount$ALL 
		colnames(data[[sector]][[unit]][[measure]]$ALL) <- 
			paste(c(attributes$base$venue, attributes$base$instrument, sector, 'ALL', unit, measure), collapse="_")
	}
	
	if (!all(sort(c(measures.raw, measures.calculated)) == sort(c(rollUpSums, rollUpCalculated)))) {print('ERROR! Not all values rolled up')}
	
	#Calculate average stats
	averageStats <- c('average_executed_trade_size','average_inquiry_trade_size','average_total_market_trade_size')
	for (maturity in c(maturities,'ALL')){
		measure <- 'average_executed_trade_size'
		data[[sector]]$volume[[measure]][[maturity]] <- data[[sector]]$volume$executed_amount[[maturity]] / data[[sector]]$trade_count$executed_amount[[maturity]]
		colnames(data[[sector]]$volume[[measure]][[maturity]]) <- 
			paste(c(attributes$base$venue, attributes$base$instrument, sector, maturity, 'volume', measure), collapse="_")
		
		measure <- 'average_inquiry_trade_size'
		data[[sector]]$volume[[measure]][[maturity]] <- data[[sector]]$volume$inquiry_amount[[maturity]] / data[[sector]]$trade_count$inquiry_amount[[maturity]]
		colnames(data[[sector]]$volume[[measure]][[maturity]]) <- 
			paste(c(attributes$base$venue, attributes$base$instrument, sector, maturity, 'volume', measure), collapse="_")
		
		measure <- 'average_total_market_trade_size'
		data[[sector]]$volume[[measure]][[maturity]] <- data[[sector]]$volume$total_market_size[[maturity]] / data[[sector]]$trade_count$total_market_size[[maturity]]
		colnames(data[[sector]]$volume[[measure]][[maturity]]) <- 
			paste(c(attributes$base$venue, attributes$base$instrument, sector, maturity, 'volume', measure), collapse="_")
	}
	
	#generate flat output file of time series
	leaves <- list()
	for (unit in units){
		for (measure in c(measures.raw, measures.calculated, averageStats)){
			for (maturity in c(maturities, 'ALL')){
				indexName <- paste(c(attributes$base$venue, attributes$base$instrument, sector, maturity, unit, measure),collapse="_")
				leaves[[indexName]] <- data[[sector]][[unit]][[measure]][[maturity]]
			}
		}
	}
	# Write out the time series zoos
	outputZoos <- do.call(merge,leaves)
	filename <- squish(outputDir, filenameBase, sector,'.csv') 
	print (squish('Writing output file: ',filename))
	write.csv(as.data.frame(outputZoos),filename)
}	

