# Portfolio Batch Job
#
# 	http://www.quadrafund.com/cgi-bin/TWiki/bin/view/Trading/PortfolioScripts
#
# Author: rsheftel
###############################################################################

library(QFPortfolio)
library(QFReports)
dataDateTime <- dateTimeFromArguments(commandArgs())

# Optional command arguments are:  outputDir groupName scriptName

optionalArgs <- rep(NA,3)
arguments <- commandArgs()[-(1:match("--args", commandArgs()))]

argumentsLen <- length(arguments)
if (argumentsLen > 1) optionalArgs[1:(argumentsLen-1)] <- arguments[2:argumentsLen]

# Set up Standard Environment Variables
standardPortfolioVars <- function() {
	ae.group 		<<- ifelse(is.na(optionalArgs[[2]]), "AllSystemsQ", optionalArgs[[2]])
	ae.objective	<<- PsiNu
	ae.outputDir	<<- ifelse(is.na(optionalArgs[[1]]), 
								squish(dataDirectory(),"STProcess/RightEdge/Portfolio/",format(dataDateTime,"%Y%m%d"),"/"),
								addSlashToDirectory(optionalArgs[[1]]))
	ae.reportDir	<<- squish(ae.outputDir,"reports/")
	ae.curvesDir 	<<- squish(ae.outputDir,"curves/")
	ae.aum			<<- 50000000
	ae.runDate		<<- dataDateTime
	
	#Target choices for Fixed weights
	ae.sector.weights <<- c(0.08, 0.20, 0.24, 0.15, 0.15, 0.11, 0.02, 0.01, 0.02, 0.02)
	names(ae.sector.weights) <<- c(	'Q_Credit',
									'Q_Rates',
									'Q_FX',
									'Q_Equity',
									'Q_Mortgage',
									'Q_Energy',
									'Q_Metals',
									'Q_Commodity',
									'Q_Sporadic',
									'Q_Probationary')
	
	ae.metrics <<- list(PsiNu,
						CalmarRatio,
						CalmarRatioWeekly,
						CalmarRatioMonthly,
						ConditionalTwentyPercentileCalmarRatio,
						ConditionalTenPercentileCalmarRatio,
						ConditionalTwentyPercentileDrawDown,
						ConditionalTenPercentileDrawDown,
						MaxDrawDown,
						KRatio,
						AnnualizedNetProfit,
						DailyStandardDeviation,
						WeeklyStandardDeviation,
						SharpeRatioWeekly,
						SharpeRatioMonthly,
						SortinoRatio,
						OmegaRatio,
						DownSideDeviation,
						ConditionalTenPercentileDailyVaR,
						ConditionalOnePercentileDailyVaR
						)
}

print (squish('RunDate   : ',as.character(dataDateTime)))

#Set up the report dir
print("Setting up report dir.")
standardPortfolioVars()
print (squish('OutputDir : ',ae.outputDir))
failIf(!file.exists(leftStr(ae.outputDir,nchar(ae.outputDir)-1)),squish("Output dir does not exist: ",ae.outputDir))
failIf(!file.exists(leftStr(ae.curvesDir,nchar(ae.curvesDir)-1)),squish("Curves dir does not exist: ",ae.curvesDir))
dir.create(ae.reportDir)

#Run scripts
scriptDir <- squish(Sys.getenv("MAIN"),"/R/scripts/Portfolio/")
print(squish("Running scripts in directory: ",scriptDir))

if(is.na(optionalArgs[[3]])){ 
	scripts <- c("IntraSectorEntirePeriod.R", "MultiRunsByMetricAllocation.R") #,"SingleRunEntirePeriod.R","MultiRunsByDateRange.R")
}else{
	scripts <-	optionalArgs[[3]]
}

print (squish('Group : ',ae.group))
print (squish('Scripts : ',paste(scripts,collapse=",")))
								
for (script in scripts){
	standardPortfolioVars()
	print(squish("Running script: ",script))
	source(squish(scriptDir,script))
}
print("Done.")
