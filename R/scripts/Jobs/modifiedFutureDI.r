#
# Creates the continuous back-adjusted DI futures
# 
# Author: RSheftel
###############################################################################

library(QFFutures)

#Get date from command line, or use logic if not available
dataDateTime <- dateTimeFromArguments(commandArgs(), hour=15)

#Setup Parameters
rollDaysPrior <- 5
expiries <- 1
contracts <- 'di'

#Create the modified futures
for (contract in contracts){
	print(squish('Contract: ',contract))
	ed <- ShortRateFuture(contract=contract, rollDaysPriorToExpiry=rollDaysPrior)
	ed$setContractType('continuous')
	ed$setDateRange(startDate='1995-01-01',endDate=format(strptime(dataDateTime,'%Y-%m-%d')))
	
	print('single contracts...')
	ed$calculateSingleContracts(numberContracts=expiries)
	ed$uploadSingleContracts(tsdbSource='internal',uploadMethod='file')
}
