library(Live)

securityIDs <- EquityDataLoader$getSecurityIDsUniverse()

for(quoteType in c("close","open","high","low")){
	print(quoteType)
	FETL <- FinancedEquityTRILoader(quoteType = quoteType)
	FETL$financeAndUploadEquityTRIs(securityIDList = securityIDs, uploadMethod = 'direct')	
}