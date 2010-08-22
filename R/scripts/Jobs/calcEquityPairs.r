#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(Live) 

########################################################################
# Sector ETFs

securityGroups <- list(
	sectorETFs = c(110011,110012,110008,110010,110014,110009,110007,110013,110015),
	commodities = c(107676,103936),
	energy = c(108767,104533),
	financial = c(102936,105329),
	airlines = c(100917,103380),
	manufacturing = c(110979,102796),
	technology = c(101121,106276),
	telecom = c(109396,109775),
	retail = c(105588,106638),
	utility = c(104019,104508),
	healthcare = c(107325,102733),
	homebuilder = c(102064,102845)
)

#res <- NULL
#for(k in 1:length(securityGroups)){
#	res <- c(res,EquityPair$getMarketCombinations(as.numeric(securityGroups[groupNames[k]][[1]])))
#}
# EquityPair$slippageEstimate(res)

groupNames <- names(securityGroups)
transformation <- 'EquityPair.R'

for(k in 1:NROW(groupNames)){
	
	print(groupNames[k])
	
	idTab <- EquityPair$getIdCombinations(as.numeric(securityGroups[groupNames[k]][[1]]))

	builder <- EquityPair(inputSource = 'internal',outputSource = 'internal')
	
	for(num in 1:NCOL(idTab)){
		
		print(squish('Working on pair number: ',num))
				
		resList <- builder$runFactorBased(securityIdLag = idTab[1,num],securityIdLead = idTab[2,num])
		builder$updateTSDB(securityIdLag = idTab[1,num],securityIdLead = idTab[2,num],'hedge',transformation,z = -na.omit(resList$hedge))
		builder$updateTSDB(securityIdLag = idTab[1,num],securityIdLead = idTab[2,num],'tri_daily',transformation,z = na.omit(resList$triDaily))
		builder$updateTSDB(securityIdLag = idTab[1,num],securityIdLead = idTab[2,num],'tri',transformation,z = NULL,returnDates = index(na.omit(resList$triDaily)))	
	}	
}

