# Script that takes the portfolio curves generated from the hypo process and
# uploads to PerformanceDB
# Author: RSheftel
###############################################################################

############### Constants ###########################

curveGroup.source 	<- 'AllSystemsQ'
curveGroup.tags   	<- 'PnlTags_All'
hypo.source 		<- 'Hypo_Daily'

#####################################################

library(SystemDB)
library(QFPortfolio)

endDate <- dateTimeFromArguments(commandArgs())
curveDir <- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(endDate,'%Y%m%d'),'/curves')

# Set up the curve groups
actual <- CurveGroup(curveGroup.source)
hypo <- CurveGroup(curveGroup.tags)
hypoCurves <- hypo$childCurves(dir=curveDir)
tags <- hypo$childNames()

priorDate <- function(xZoo, theDate){
	dates <- index(xZoo)
	datesPrior <- (dates < theDate)
	datesPrior <- datesPrior[datesPrior==TRUE]
	return(dates[length(datesPrior)])
}

badData <- NULL
badTag  <- NULL
allTags <- PerformanceDB$tags()
for (tag in tags){
	equity <- hypoCurves[[tag]]$equity()
	if (!is.null(equity)){
		equity.end <- as.numeric(equity[endDate])
		startDate <- priorDate(equity,endDate)
		equity.start <- as.numeric(equity[startDate])
		if ((length(equity.end)>0) && (length(equity.start)>0)){
			pnl <- equity.end - equity.start
			print(squish("Inserting Pnl for tag : ",tag,', startDate : ',format(startDate,'%Y-%m-%d'),', endDate : ',format(endDate,'%Y-%m-%d')))
			if (tag %in% allTags){
				uploadFile <- PerformanceDB$updatePnL(	startDate=startDate, endDate=endDate,
														source=hypo.source, tag=tag,
														pnl=pnl, commitToDB=TRUE)
				failIf((uploadFile != TRUE),"Error inserting data into PerformanceDB")
			}else{
				print('Tag not found in PerformanceDB.')
				badTag <- squish(badTag,'\n',tag)
			}
		}else{
			badData <- squish(badData,'\n',tag)
		}
	}
}

#email if data is missing from the raw curves
if (!is.null(badData)){
	email <- Mail$problem(subject='Hypo Pnl Upload to PerformanceDB missing data.',content=badData)
	email$sendTo('team')
}

#email if attempted to upload a bad tag
if (!is.null(badTag)){
	email <- Mail$problem(subject='Hypo Pnl Upload to PerformanceDB bad Tags.',content=badTag)
	email$sendTo('team')
}

#email a warnging is there are missing tags from either group.
msivs.actual <- actual$msivs()
msivs.hypo 	 <- hypo$msivs()

inActualNotHypo <- msivs.actual[!(msivs.actual %in% msivs.hypo)]
inHypoNotActual <- msivs.hypo[!(msivs.hypo %in% msivs.actual)] 

#For the inHypoNotActual, filter out markets that are ended in the MarketHistory table
removeMsivs <- NULL
for (msiv in inActualNotHypo){
	endDates <- SystemDB$marketHistory(SystemDB$marketFromMsivs(msiv))$EndDate
	if (!is.null(endDates))
		if (!any(is.na(endDates)))
			removeMsivs <- c(removeMsivs, msiv)
}
inActualNotHypo <- inActualNotHypo[!(inActualNotHypo %in% removeMsivs)]

#Send emails
	body <- NULL
	if(length(inActualNotHypo) > 0){
		body <- squish(body,'\n\nThe following msivs are in ACTUAL, but not in HYPO:\n\n')
		body <- squish(body, paste(inActualNotHypo, collapse="\n"))
	}
	if(length(inHypoNotActual) > 0)	{
		body <- squish(body,'\n\nThe following msivs are in HYPO, but not in ACTUAL:\n\n')
		body <- squish(body, paste(inHypoNotActual, collapse="\n"))
	}
	if(!is.null(body)){
		email <- Mail$notification(subject='Hypo Pnl Upload to PerformanceDB missing curves.',content=body)
		email$sendTo('team')
	}
