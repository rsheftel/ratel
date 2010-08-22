# test for Runs Group
# 
# Author: rsheftel
###############################################################################

library(QFSTO)
testdata <- squish(system.file("testdata", package="QFSTO"),'/RunGroups/')
testSystemID <- 179030

stoSetup <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	sto$strategyClass("Test")
	sto$systemQClassName("TestQClass.c")
	sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))
	sto$addParameters(name='Param1',start=1,end=3,step=1)
	sto$addParameters(name='Param2',start=2,end=8,step=2)
	return(sto)
}

test.constructor <- function(){
	checkInherits(RunGroups(stoSetup()),"RunGroups")
	checkInherits(RunGroups(systemID=testSystemID),"RunGroups")
	shouldBomb(RunGroups())
	shouldBombMatching(RunGroups(stoSetup(), systemID=123),"Can only supply either a STOSetup object or systemID, not both.")
}

test.attachedObjects <- function(){
	rg <- RunGroups(stoSetup())
	checkInherits(rg$stoObject(), "STO")
	checkInherits(rg$stoSetupObject(), "STOSetup")
}

test.filters <- function(){
	
	#Set up some single filter
	rg <- RunGroups(stoSetup())
	checkSame(data.frame(),rg$filters())
	checkSame(0,length(rg$filtersRuns()))
	rg$addFilter(parameter="Param1", operator="==", value='1')
	shouldBomb(rg$addFilter(parameter="Param1", operator="=="))
	shouldBomb(rg$addFilter(parameter="Param1", operator="==", value=1))
	shouldBombMatching(rg$addFilter(parameter="BadParam", operator="==", value='1'),'Parameter not valid.')
	expected <- data.frame(parameter='Param1',operator='==',value='1')
	checkSame(expected, rg$filters())
	checkSame(c(1,4,7,10), rg$filtersRuns())
	
	#Clear it
	rg$clearFilters()
	checkSame(data.frame(),rg$filters())
	checkSame(0,length(rg$filtersRuns()))
	
	#Set up multiple filter lines
	rg$addFilter(parameter="Param1", operator="==", value='1')
	rg$addFilter(parameter="Param2", operator="==", value='6')
	expected <- data.frame(parameter=c('Param1','Param2'),operator=c('==','=='),value=c('1','6'))
	checkSame(expected, rg$filters())
	checkSame(c(7), rg$filtersRuns())
}

test.runGroups <- function(){
	rg <- RunGroups(stoSetup())
	rg$addFilter(parameter="Param1", operator="==", value='1')
	rg$addGroup('Group1')
	rg$addGroup('Group2','Portfolio4')
	rg$addGroup('Group3',runs=c(1,10))
	
	checkSame(rg$groupNames(), c('Group1','Group2','Group3'))
	checkSame(rg$group('Group1'), list(runs=c(1,4,7,10), portfolios=c("Portfolio3",'Portfolio4')))
	checkSame(rg$group('Group2'), list(runs=c(1,4,7,10), portfolios='Portfolio4'))
	checkSame(rg$group('Group3'), list(runs=c(1,10), portfolios=c("Portfolio3",'Portfolio4')))
	shouldBomb(rg$group('BadGroup'))
	
	rg$removeGroup('Group2')
	checkSame(rg$groupNames(), c('Group1','Group3'))
	shouldBomb(rg$group('Group2'))
	
	checkSame(c('Portfolio3','Portfolio4'), rg$groupPortfolios('Group1'))
	rg$groupPortfolios('Group1','Portfolio5')
	checkSame(c('Portfolio5'), rg$groupPortfolios('Group1'))
	rg$groupPortfolios('Group1','Portfolio6', replace=FALSE)
	checkSame(c('Portfolio6','Portfolio5'), rg$groupPortfolios('Group1'))
}

test.fileMethods <- function(){
	rg <- RunGroups(stoSetup())
	
	shouldBombMatching(rg$loadFromFile(),'No RunGroups.dput file.')
	
	rg$addFilter(parameter="Param1", operator="==", value='1')
	rg$addGroup('Group1')
		
	rg$saveToFile()
	rg$removeGroup('Group1')
	checkSame(0, length(rg$groupNames()))
	
	rg$loadFromFile()
	checkSame('Group1', rg$groupNames())
	checkSame(rg$group('Group1'), list(runs=c(1,4,7,10), portfolios=c("Portfolio3",'Portfolio4')))
	
	rg$loadFromFile(systemID=testSystemID)
	checkSame(rg$groupNames(), c('GroupA','GroupB'))
	checkSame(rg$group('GroupA'), list(runs=c(1,2,3,46,47,48), portfolios=c('BondFutures','EnergyFutures','EquityFutures')))
	checkSame(rg$group('GroupB'), list(runs=c(39,42,45,84,87,90), portfolios=c("EnergyFutures",'EquityFutures')))
}
