#
#	Test of PortfolioRun class  (rsheftel)
#
#################################################################

library(QFPortfolio)

div <- Diversity(groupName="TestPortfolio3")

test.constructor <- function(){    
	checkInherits(div, "Diversity")
	shouldBomb(Diversity())
	checkSame(c('TestSubGroup2','TEST.SP.1C_TestSystem2_daily_1.0:TestPV3'), div$childNames())
	checkSame(as.character(Diversity('TestPortfolio3')),'Diversity: TestPortfolio3')
	checkSame(as.data.frame(Diversity('TestPortfolio3')), data.frame(Diversity='Diversity: TestPortfolio3'))	
}

test.loadMarkets <- function(){
	div$loadMarkets()
	sp <- Symbol('TEST.SP.1C')$series()[,'close']
	checkSame(sp, div$.fullMarketZoo[['TEST.SP.1C']])
	
	#With the new logic this part is called in the score function, but test kept, has less meaning.
	div$.marketZoo <- div$.fullMarketZoo
	div$.populateChildGroupMarkets()
	checkSame(getZooDataFrame(sp,"TEST.SP.1C"), div$childMarketsZoo('TEST.SP.1C_TestSystem2_daily_1.0:TestPV3'))
}

test.totalCorrelationLevelScore <- function(){
	div <- Diversity(groupName="TestPortfolio3")
	expected <- c(1.34196, 1)
	names(expected) <- div$childNames()
	## Note that you do not need to loadMarkets, the score will do it.
	checkSame(expected, round(div$score('TotalCorrelationLevel'),6))
	shouldBombMatching(div$score('BadMethod'), 'Not a valid method: BadMethod')
}

test.totalCorrelationReturnsScore <- function(){
	div <- Diversity(groupName="TestPortfolio3")
	expected <- c(1.893079, 1)
	names(expected) <- div$childNames()
	## Note that you do not need to loadMarkets, the score will do it.
	checkSame(expected, round(div$score('TotalCorrelationReturns'),6))
}

test.totalCorrelationLevelMissingData <- function(){
	div <- Diversity(groupName="TestPortfolio3")
	div$loadMarkets()
	expected <- c(1.12290, 1)
	#Remove data from the zoos, this is just to test
	div$.fullMarketZoo[['RE.TEST.TU.1C']][] <- NA
	names(expected) <- div$childNames()
	checkSame(expected, round(div$score('TotalCorrelationLevel'),6))
}

test.calcPrincipalComponents <- function(){
	div <- Diversity(group = 'TestPortfolio3')
	expected <- c(2,1)
	names(expected) <- div$childNames()
	checkSame(expected,div$score('PrincipalComponents', interp = FALSE))
	expected[1] <- 1
	checkSame(expected,div$score('PrincipalComponents',threshold = 0.7, interp = FALSE))
	expected[1] <- 3
	checkSame(expected,div$score('PrincipalComponents',threshold = 0.999, interp = FALSE))
	expected[1] <- 1.537094
	checkSame(expected, round(div$score('PrincipalComponents'),6))
	expected[1] <- 1
	checkSame(expected, div$score('PrincipalComponents', threshold = 0.7))
	expected[1] <- 2.523810
	checkSame(expected, round(div$score('PrincipalComponents', threshold = 0.999),6))
	expected[1] <- 1.847092
	checkSame(expected, round(div$score('PrincipalComponents', changes = TRUE),6))
	expected[1] <- 1.841898
	checkSame(expected, round(div$score('PrincipalComponents', changes = TRUE, lag = 5),6))
}

test.usingRange <- function(){
	div <- Diversity(groupName="TestPortfolio3")
	div$loadMarkets()
	
	expected <- c(1.381817, 1)
	names(expected) <- div$childNames()
	checkSame(expected, round(div$score('TotalCorrelationLevel',Range('2008-02-01','2008-04-30')),6))
}

test.fixedWeights <- function(){
	div <- Diversity(group = 'TestPortfolio3')
	weights <- c(0.25, 0.75)
	names(weights) <- c('TestSubGroup2','TEST.SP.1C_TestSystem2_daily_1.0:TestPV3')
	checkSame(div$score('Fixed',weights=weights), weights)
	
	checkSame(div$score('Fixed', weights=as.list(weights)), weights)
	
	badWeights <- c(0.25, 0.75)
	names(badWeights) <- c('TestSubGroup2', 'BadName')
	shouldBombMatching(div$score('Fixed',weights=badWeights), 'Names in weight vector not valid.')
	
	shouldBombMatching(div$score('Fixed',weights=badWeights[[1]]), 'Must provide a weight for each child.')
}

