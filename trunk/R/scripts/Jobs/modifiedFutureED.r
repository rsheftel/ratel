#
# Creates the continuous back-adjusted ED futures
# 
# Author: RSheftel
###############################################################################

library(QFFutures)

#Get date from command line, or use logic if not available
arguments <- commandArgs()
arguments <- arguments[-(1:match("--args", arguments))]

if(NROW(arguments)==0){
	dataDate <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb", switchTime="15:00:00")$lastBusinessDate
}else
	dataDate <- arguments

dataDate <- as.POSIXlt(dataDate)
dataDate$hour <- 15
dataDateTime <- as.POSIXct(dataDate)

#Setup Parameters
rollDaysPrior <- 5
expiries <- 1:20
contracts <- 'ed'

#Create the modified futures
for (contract in contracts){
	print(squish('Contract: ',contract))
	ed <- ShortRateFuture(contract=contract, rollDaysPriorToExpiry=rollDaysPrior)
	ed$setContractType('continuous')
	ed$setDateRange(startDate='1994-01-01',endDate=format(strptime(dataDateTime,'%Y-%m-%d')))
	
	print('single contracts...')
	ed$calculateSingleContracts(numberContracts=20)
	ed$uploadSingleContracts(tsdbSource='internal',uploadMethod='direct')
	
	print('packs...')
	ed$calculatePacks(packs=c('white','red','green','blue','gold'))
	ed$uploadPacks(tsdbSource='internal',uploadMethod='direct')
	
	print('bundles...')
	ed$calculateBundles(bundles=c('1y','2y','3y','4y','5y'))
	ed$uploadBundles(tsdbSource='internal',uploadMethod='direct')
	
	print('single combinations...')
	ed$makeSingleSpread(1,5)
	ed$makeSingleSpread(4,5)
	ed$makeSingleButterfly(1,3,5)
	ed$uploadSingleCombinations(tsdbSource='internal',uploadMethod='direct')
	
	print('pack spreads...')
	ed$makePackSpread(basePack='red',hedgePack='green')
	ed$uploadPackSpreads(tsdbSource='internal',uploadMethod='direct')
	
	print('pack flys...')
	ed$makePackFly(nearWing='red',middle='green',farWing='blue')
	ed$makePackFly(nearWing='white',middle='red',farWing='green')
	ed$uploadPackCombinations(tsdbSource='internal',uploadMethod='direct')
	
	print('bundle pvbp...')
	ed$calculateBundlePvbps(bundles=c('1y','2y','3y','4y','5y'))
	ed$uploadBundlePvbps(tsdbSource='internal',uploadMethod='direct')
}
