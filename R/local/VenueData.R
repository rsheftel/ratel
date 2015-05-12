# VenueData Class
# 
# Author: RSheftel
###############################################################################


then a new call called VenueDataReport that takes in a VenueData object and does repoting, and graphing with ggplot2, and reshape2
use as a template the htmlUtils and the portfolio performance.




constructor("VenueData", function(venue=NULL, instrument=NULL, issuer=NULL, sectors=NULL, source=NULL) {
	extend(RObject(), "VenueData")
	if (inStaticConstructor(this)) return(this)
	constructorNeeds(this, venue="character", instrument="character", issuer="character", sectors="character")
	this$.initialize(venue, instrument, issuer, sectors, source)
})

method(".initialize", "VenuData", function(this, venue=NULL, instrument=NULL, issuer=NULL, sectors=NULL, source=NULL, ...){
	needs(venue="character", instrument='character', issuer='character', sectors='character')
	
	this$.attributes <- list()
	this$.attributes$base <- list(venue		= venue,
								series_type	= "venue_performance",
								instrument	= instrument,
								issuer		= issuer)
	
	this$.source <- iff(source=NULL, venue, source)	
	this$.sectors <- sectors
	this$.data <- list()
	this$.units <- NULL
	this$.maturities <- NULL
	this$.measures <- list(raw=NULL, calculated=NULL, average=NULL)
})

method("sectors", "VenueData", function(this, ...){
	return(this$.sectors)
})

method("units", "VenueData", function(this, units=NULL, ...){
	if(is.null(units) return(this$.units)
	failIf(((!is.null(units) && !is.null(this$.units)), 'Units already set, object is immutable.')
	this$.units <-units
})

method("setMeasures", "VenueData", function(this, raw=NULL, calculated=NULL, ...){
	if(!is.null(raw)) 		this$.measures$raw <- unique(c(this$.measures$raw, raw))
	if(!is.null(calculated)	this$.measures$calcuated <- unique(c(this$.measures$calculated, calculated))
	this$.measures$all <- unque(c(this$.measures$raw, this$.measures$calculated))
})

method("measures", "VenueData", function(this, ...){
	return(this$.measures)
})

method("maturities", "VenueData", function(this, maturities, ...){
	if(is.null(maturities) return(this$.maturities)
	failIf(((!is.null(maturities) && !is.null(this$.maturities)), 'Maturities already set, object is immutable.')
	this$.maturities <-maturities
})

method("data", "VenueData", function(this, sectors, unit, measure, maturity, ...){
	return(this$.data[[sector]][[unit]][[measure]][[maturity]])
})

method("smash", "VenueData", function(this, sectors=NULL, units=NULL, measures=NULL, maturities=NULL, ...){
	sectors <- iff(is.null(sectors), this$sectors(), sectors)
	units <- iff(is.null(units), this$units(), units)
	measures <- iff(is.null(measures), this$measures()$all, measures)
	maturities <- iff(is.null(maturities), c(this$maturities(),'ALL'), maturities)
	
	leaves <- list()
	for (sector in sectors){
		for (unit in units){
			for (measure in measures){
				for (maturity in maturities){
					indexName <- paste(c(this$.attributes$base$venue, this$.attributes$base$instrument, sector, maturity, unit, measure),collapse="_")
					leaves[[indexName]] <- this$.data[[sector]][[unit]][[measure]][[maturity]]
				}
			}
		}
	}
	return(do.call(merge,leaves))
})

#### Main load and calculate methods ####

method("loadVenueData", "VenueData", function(this, ...){
	if (this$.venue=='tradeweb'){
		this$units(c('volume','trade_count'))
		this$loadRawData(c('inquiry_amount','market_share','hit_rate','no_quote_rate'))
		
		calculateForRaw <- c('executed_amount','total_market_size','no_quote_amount')
		this$calculateTransformedData(calculateForRaw, this$units(), this$maturities())
		
		rollUpSums <- c(calculateForRaw, 'inquiry_amount')
		this$rollUpSums(rollUpSums)
		
		rollUpCalculated <- c('market_share', 'hit_rate', 'no_quote_rate')
		this$calculateTransformedData(rollUpCalculated, this$units(), 'ALL')
		
		failIf (!all(sort(this$measures()$all)) == sort(c(rollUpSums, rollUpCalculated))), 'ERROR! Not all values rolled up')

		averageStats <- c('average_executed_trade_size','average_inquiry_trade_size','average_total_market_trade_size')
		this$calculateTransformedData(averageStats, 'volume', c(this$maturities(), 'ALL'))
		
	}else{
		fail('Not a recognized venue.')
	}
})

method("loadRawData", "VenueData", function(this, measures, ...){
	needs(measures="chracter")
	failIf(!is.null(this$.data), 'Data already loaded, object is immutable.')
	this$setMeasures(raw=measures)
	for (sector in this$.sectors){
		#Make aggregated nominal data from raw data
		this$.attributes[[sector]] <- this$.attributes$base
		this$.attributes[[sector]]$sector <- sector
		
		#use the first unit/measure to get the sub-sector names
		this$.attributes[[sector]]$units <- this$units()[[1]]
		this$.attributes[[sector]]$measure <- this$measures()$raw[[1]]
		temp <- TSDB$observationsByAttributes(this$.attributes[[sector]],source=this$.source)
		tickers <- rownames(temp)
		
		maturities <- c()
		for (ticker in tickers){
			x <- TSDB$attributes(ticker)
			maturities <- c(maturities, x['maturity_range'][[1]])
		}
		maturities <- sort(maturities)
		this$maturities(maturities)
		
		for (unit in this$units()){
			for (measure in this$measures()$raw){
				for (maturity in maturities){
					this$.attributes[[sector]]$units <- unit
					this$.attributes[[sector]]$measure <- measure
					this$.attributes[[sector]]$maturity_range <- maturity
					print(squish("Loading for sector: ",sector,", unit: ",unit,", measure: ",measure,", maturity: ",maturity))
					data.matrix <- TSDB$observationsByAttributes(this$.attributes[[sector]],source=this$.source)
					this$.data[[sector]][[unit]][[measure]][[maturity]] <- TSReader$tsdbMatrixToZoo(data.matrix)
				}
			}
		}
	}
})

method("rollUpSums", "VenueData", function(this, measures, ...){
	for (sector in this$setors()){
		for (unit in this$units()){
			for (measure in measures){
				mergedZoo <- do.call(merge, this$.data[[sector]][[unit]][[measure]])
				this$.data[[sector]][[unit]][[measure]]$ALL <- getZooDataFrame(rollapply(mergedZoo,width=1,FUN=sum,by.column=FALSE))
				this$.setMeasureZooName(sector, unit, measure, 'ALL')
			}
		}
	}
})

#####----- Data Transformations -----#####

method("calculateTransformedData", "VenueData", function(this, calculations, units, maturities, ...){
	needs(calculations="character", units="character", maturities="character")
	failIf(!all(units %in% this$units()), 'Not valid units!')
	failIf(!all(maturities %in% c(this$maturities(),'ALL'), 'Not valid maturities!')
	
	this$setMeasures(calculated=calculations)
	for (calculation in calculations){
		for (sector in this$sectors()){
			for (unit in units){
				for (maturity in maturities){
					do.call(squish("calc.",calculation),list(this, sector, unit, maturity))
				}
			}
		}
	}
})

method(".setMeasureZooName","VenueData", function(this, sector, unit, measure, maturity, ...){
	needs(sector="character", unit="character", measure="character", maturity="character")
	zooColumnName <- paste(c(this$.attributes$base$venue, this$.attributes$base$instrument, sector, maturity, unit, measure),collapse="_")
	colnames(data[[sector]][[unit]][[measure]][[maturity]]) <- zooColumnName

})

method("calc.executed_amount", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	measure <- 'executed_amount'
	this$.data[[sector]][[unit]][[measure]][[maturity]] <- 
				this$.data[[sector]][[unit]]$inquiry_amount[[maturity]] * this$.data[[sector]][[unit]]$hit_rate[[maturity]]
	this$.setMeasureZooName(sector, unit, measure, maturity)
})

method("calc.total_market_size", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	measure <- 'total_market_size'
	this$.data[[sector]][[unit]][[measure]][[maturity]] <- 
				this$.data[[sector]][[unit]]$executed_amount[[maturity]] / this$.data[[sector]][[unit]]$market_share[[maturity]]
	this$.data[[sector]][[unit]][[measure]][[maturity]][this$.data[[sector]][[unit]][[measure]][[maturity]]==Inf] <- NA
	this$.setMeasureZooName(sector, unit, measure, maturity)
})

method("calc.no_quote_amount", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	measure <- 'no_quote_amount'
	this$.data[[sector]][[unit]][[measure]][[maturity]] <- 
				this$.data[[sector]][[unit]]$inquiry_amount[[maturity]] * this$.data[[sector]][[unit]]$no_quote_rate[[maturity]]
	this$.setMeasureZooName(sector, unit, measure, maturity)
})
	
method("calc.market_share", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	measure <- 'market_share'
	this$.data[[sector]][[unit]][[measure]][[maturity]] <- 
				this$.data[[sector]][[unit]]$executed_amount[[maturity]] / this$.data[[sector]][[unit]]$total_market_size[[maturity]] 
	this$.setMeasureZooName(sector, unit, measure, maturity)
})

method("calc.hit_rate", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	measure <- 'hit_rate'
	this$.data[[sector]][[unit]][[measure]][[maturity]] <- 
				this$.data[[sector]][[unit]]$executed_amount[[maturity]] / this$.data[[sector]][[unit]]$inquiry_amount[[maturity]] 
	this$.setMeasureZooName(sector, unit, measure, maturity)		
})

method("calc.no_quote_rate", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	measure <- 'no_quote_rate'
	this$.data[[sector]][[unit]][[measure]][[maturity]] <- 
				this$.data[[sector]][[unit]]$no_quote_amount[[maturity]] / this$.data[[sector]][[unit]]$inquiry_amount[[maturity]] 
	this$.setMeasureZooName(sector, unit, measure, maturity)
})	

#####----- Average Stats -----#####
	
method("calc.average_executed_trade_size", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	failIf((unit != 'volume'), 'Average calculations require unit = "volume"')
	measure <- 'average_executed_trade_size'
	this$.data[[sector]]$volume[[measure]][[maturity]] <- 
				this$.data[[sector]]$volume$executed_amount[[maturity]] / this$.data[[sector]]$trade_count$executed_amount[[maturity]]
	this$.setMeasureZooName(sector, unit, measure, maturity)
})

method("calc.average_inquiry_trade_size", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	failIf((unit != 'volume'), 'Average calculations require unit = "volume"')
	measure <- 'average_inquiry_trade_size'
	this$.data[[sector]]$volume[[measure]][[maturity]] <- 
				this$.data[[sector]]$volume$inquiry_amount[[maturity]] / this$.data[[sector]]$trade_count$inquiry_amount[[maturity]]
	this$.setMeasureZooName(sector, unit, measure, maturity)
})

method("calc.average_total_market_trade_size", "VenueData", function(this, sector, unit, maturity, ...){
	needs(sector="character", unit="character", maturity="character")
	failIf((unit != 'volume'), 'Average calculations require unit = "volume"')
	measure <- 'average_total_market_trade_size'
	this$.data[[sector]]$volume[[measure]][[maturity]] <- 
			this$.data[[sector]]$volume$total_market_size[[maturity]] / this$.data[[sector]]$trade_count$total_market_size[[maturity]]
	this$.setMeasureZooName(sector, unit, measure, maturity)
})
