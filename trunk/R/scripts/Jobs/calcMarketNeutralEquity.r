#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(Live)

########################################################################

sectorETFs <- c(110011,110012,110008,110010,110014,110009,110007,110013,110015)
sampleEquities <- c(
	107676,103936,108767,104533,107484,102936,105329,105169,100917,103380,110979,102796,101121,
	106276,109396,109775,111205,111860,105588,106638,104019,104508,107325,102733,102064,102845,104939
)
	
#pairs <- EquityPair$getMarketCombinations(sampleEquities,TRUE)
#EquityPair$slippageEstimate(pairs)

securityIDs <- c(sampleEquities,sectorETFs)
spyId <- 109820

builder <- MarketNeutralEquity(weighting = 'halfLife',outputSource = 'internal')

for(id in securityIDs){
	print(id)
	transformation <- 'MarketNeutralEquity.R'
	resList <- builder$runRollingRegression(securityId = id,window = 125)
	
	pair <- EquityPair(inputSource = 'internal',outputSource = 'internal')
	pair$updateTSDB(securityIdLag = id,securityIdLead = spyId,'beta',transformation,z = na.omit(resList$beta))
	pair$updateTSDB(securityIdLag = id,securityIdLead = spyId,'hedge',transformation,z = -na.omit(resList$hedge))
	pair$updateTSDB(securityIdLag = id,securityIdLead = spyId,'tri_daily',transformation,z = na.omit(resList$triDaily))
	pair$updateTSDB(securityIdLag = id,securityIdLead = spyId,'tri',transformation,z = NULL,returnDates = index(na.omit(resList$triDaily)))			
}