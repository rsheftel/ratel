# test for VenueData Class
# 
# Author: RSheftel
###############################################################################

library(AtgBusinessIntelligence)

test.constructor <- function(){
	shouldBomb(VenueData())
	checkInherits(VenueData(venue='tradeweb',instrument='ustreasury',issuer='treasury', sectors='nominals'),"VenueData")
}

test.sectors <- function(){
	obj <- VenueData(venue='tradeweb', instrument='treasury', issuer='us_treasury', sectors='nominals')
	checkSame(obj$sectors(), 'nominals')
	obj <- VenueData(venue='tradeweb', instrument='treasury', issuer='us_treasury', sectors=c('nominals','bills'))
	checkSame(obj$sectors(), c('nominals','bills'))
}

test.maturities <- function(){
	obj <- VenueData(venue="testVenue", instrument="testInstrument", issuer="testIssuer", sectors=c("sector1","sector2"))
	obj$maturities(c('maturity1','maturity2'))
	checkSame(obj$maturities(), c('maturity1','maturity2'))
}

test.units <- function(){
	obj <- VenueData(venue='tradeweb', instrument='treasury', issuer='us_treasury', sectors='nominals')
	obj$units(c('unit1','unit2'))
	checkSame(obj$units(), c('unit1','unit2'))
	shouldBombMatching(obj$units(c('newUnit')), 'Units already set, object is immutable!')
}

test.measures <- function(){
	obj <- VenueData(venue='tradeweb', instrument='treasury', issuer='us_treasury', sectors='nominals')
	obj$setMeasure(raw=c('raw1,','raw2'), calculated=
}


test.smash <- function(){

	use the fake object, then smash and see results
}

#### ---- Test Venue specifics ---- ####

test.tradeweb <- function(){
	test the entire tradeweb suite here, from load to raw to calculate transformed
	add the sub tests later
}

#### ---- Data Transformations ----####

test.calculateTransformedData <- function(){
	obj <- fakeObject('measure1','measure2')
	shouldBombMatching(obj$calculateTransformedData('executed_amount','badUnit','maturity1'),'Not valid unit!')
	shouldBombMatching(obj$calculateTransformedData('executed_amount','volume','badMaturity'),'Not a valid maturity!')
}

test.rollUpSums <- function()

test.executed_amount <- function(){
	obj <- fakeObject('inquiry_amount','hit_rate')
	obj$calculateTransformedData('executed_anmount','volume','maturity1')
	checkSame(obj$data('sector1','volume','executed_amount','maturity1'), zoo(1,2,3)*zoo(0.1,NA,10))

	checkSame(obj$measures()$raw, c('inquiry_amount','hit_rate'))
	checkSame(obj$measures()$calculated, c('executed_amount'))
	checkSame(obj$measures()$all, c('inquiry_amount','hit_rate','executed_amount'))
}

test.total_market_size <- function(){
	obj <- fakeObject('executed_amount','market_share')
	obj$calculateTransformedData('total_market_size','volume',c('maturity1','maturity2'))
	checkSame(obj$data('sector2','volume','total_market_size','maturity2'), zoo(6,5,11)/zoo(3,0.5,8))
}

test.no_quote_amount <- function(){
	obj <- fakeObject('inquiry_amount','no_quote_rate')
	obj$calculateTransformedData('no_quote_amount',c('volume','trade_count'),c('maturity1','maturity2'))
	checkSame(obj$data('sector1','trade_count', 'no_quote_amount','maturity1'), zoo(8,5,1)*zoo(4,40,NA))
}

test.market_share <- function(){
	obj <- fakeObject('executed_amount','total_market_size')
	obj$calculateTransformedData('market_share',c('volume','trade_count'),c('maturity1','maturity2'))
	checkSame(obj$data('sector2','volume', 'market_share','maturity1'), zoo(9,8,7)/zoo(1,0.1,2))
}

test.hit_rate <- function(){
	obj <- fakeObject('inquiry_amount','no_quote_rate')
	obj$calculateTransformedData('hit_rate','trade_count',c('maturity1','maturity2'))
	checkSame(obj$data('sector1','trade_count', 'hit_rate','maturity2'), zoo(0.5,0.25,0.1)/zoo(50,500,0))
}

test.no_quote_rate <- function(){
	obj <- fakeObject('no_quote_amount','inquiry_amount')
	obj$calculateTransformedData('no_quote_rate','trade_count','maturity2')
	checkSame(obj$data('sector2','trade_count', 'no_quote_rate','maturity2'), zoo(7,8,9)/zoo(15,0.5,0.1))
}



fakeObject <- function(measure1, measure2){
	obj <- VenueData(venue="testVenue", instrument="testInstrument", issuer="testIssuer", sectors=c("sector1","sector2"))
	
	obj$maturities(c('maturity1','maturity2'))
	obj$units(c('volume','trade_count'))
	obj$setMeasures(raw=c(measure1,measure2))
	
	obj$.data$sector1$volume[[measure1]]$maturity1 <- zoo(1,2,3)
	obj$.data$sector1$volume[[measure1]]$maturity2 <- zoo(6,5,4)
	obj$.data$sector1$volume[[measure2]]$maturity1 <- zoo(0.1,NA,10)
	obj$.data$sector1$volume[[measure2]]$maturity2 <- zoo(NA,0.01,100)
	obj$.data$sector2$volume[[measure1]]$maturity1 <- zoo(9,8,7)
	obj$.data$sector2$volume[[measure1]]$maturity2 <- zoo(6,5,11)
	obj$.data$sector2$volume[[measure2]]$maturity1 <- zoo(1,0.1,2)
	obj$.data$sector2$volume[[measure2]]$maturity2 <- zoo(3,0.5,8)


	obj$.data$sector1$trade_count[[measure1]]$maturity1 <- zoo(8,5,1)
	obj$.data$sector1$trade_count[[measure1]]$maturity2 <- zoo(0.5,0.25,0.1)
	obj$.data$sector1$trade_count[[measure2]]$maturity1 <- zoo(4,40,NA)
	obj$.data$sector1$trade_count[[measure2]]$maturity2 <- zoo(50,500,0)
	obj$.data$sector2$trade_count[[measure1]]$maturity1 <- zoo(10,100,1000)
	obj$.data$sector2$trade_count[[measure1]]$maturity2 <- zoo(7,8,9)
	obj$.data$sector2$trade_count[[measure2]]$maturity1 <- zoo(NA,11,12)
	obj$.data$sector2$trade_count[[measure2]]$maturity2 <- zoo(15,0.5,0.1)

	return(obj)
}