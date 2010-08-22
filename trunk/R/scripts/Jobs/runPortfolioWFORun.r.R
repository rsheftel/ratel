# First written on 5/13/09
#
# Takes 2 args: the close date, and the runNumber (originally run with 6 runs)
#
# Runs are then combined with coalateWFORuns.r
# 
# Author: dhorowitz
###############################################################################

library(STO)
library(QFPortfolio)
library(QFReports)

args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
	dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else
{
	dateList <- getRefLastNextBusinessDates(lastBusinessDate = args[1], holidaySource = "financialcalendar", financialCenter = "nyb")
}

runNumber <- as.numeric(args[2])

end <- as.POSIXct(dateList$lastBusinessDate)
dataDateTime <- end
start1 <- as.POSIXct('2005-12-31')
start2 <- as.POSIXct('2006-12-31')

# Set up Standard Environment Variables
standardPortfolioVars <- function() {
	ae.group 		<<- "AllSystemsQ"
	ae.objective	<<- PsiNu
	ae.outputDir	<<- squish(dataDirectory(),"STProcess/RightEdge/Portfolio/",format(dataDateTime,"%Y%m%d"),"/")
	ae.reportDir	<<- squish(ae.outputDir,"reports/")
	ae.curvesDir 	<<- squish(ae.outputDir,"curves/")
	ae.aum			<<- 50000000
	ae.runDate		<<- dataDateTime
}

#Set up the report dir
print("Setting up report dir.")
standardPortfolioVars()

print(squish('RunNumber = ', runNumber))
#Initialize object
print("Initializing PortfolioRun...")
prun <- PortfolioRun(name = "SingleRunEntirePeriod",
	groupName = ae.group,
	curvesDirectory = ae.curvesDir,
	curvesExtension="bin")
#Set dates
print(squish("Date range: ",format(as.POSIXct('1996-01-01'),"%Y-%m-%d")," to ",format(ae.runDate,"%Y-%m-%d")))
prun$dateRange(start=as.POSIXct('1996-01-01'), end=ae.runDate)	

#Load curves
print("Loading Curves...")
prun$loadCurves()					
systems <- prun$curveNames()
print(squish("Curves Loaded: ",paste(systems,sep=",")))
systems.count <- length(systems)

#Set objective
prun$objective(ae.objective)
print(squish("Objective function: ", as.character(prun$objective())))		

#Set penalty
print("Set penalty...")
prun$penalty(penalty=10000000000)

#Min/max weights
print("Set min/max weights")
mins <- rep(0,systems.count)
names(mins) <- systems
maxs <- rep(5,systems.count)			#Review this.
names(maxs) <- systems
prun$minMaxWeights(mins=as.list(mins), maxs=as.list(maxs))

#Set constraints
print("Setting constraints.")
VaRBudget <- -ae.aum*0.01
DrawDownConstraint <- -ae.aum*0.03

diverse <- Diversity(groupName=ae.group)
diversityScore <- diverse$score("PrincipalComponents")
prun$diversityScore(as.list(diversityScore))
summedVarConstraint <- SummedVarConstraint(systems,summedAlpha=0.1,varBudget = VaRBudget)
for(system in systems) {
	percentScore <- diversityScore[system] / sum(diversityScore)
	summedVarConstraint$set(system, -percentScore, percentScore)	
}
prun$addConstraint(summedVarConstraint)
prun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=DrawDownConstraint,max=0)

#Set seeds
print("Setting seeds.")
seeds <- rep(1,systems.count)
names(seeds) <- systems
print(squish("Seeds : ",paste(seeds,sep=",")))
prun$seeds(as.list(seeds))

#Optimize
print("Optimizing.")
prun$penalty(2000 * ae.aum)
prun$optimType("nlminb") 
prun$params(params=list(eval.max=100000,iter.max=100000,trace=5,rel.tol=0.1)) 
prun$setupOptimizer()
monthList <- c(36,36,36,48,48,48)
periodList <- c(12,3,1,12,3,1)
startList <- c(start1,start1,start1,start2,start2,start2)

wfo <- WalkForwardOptimizer(prun$optimizer(), RollingWindow(Period$months(monthList[runNumber])), Period$months(periodList[runNumber]))
wfo$optimize(startDate = startList[runNumber], endDate = end, seed = prun$seeds(), saveWeights = TRUE, fastRestarts = 1, fastTol = 0.01, slowRestarts = 0)
weightsLong <- wfo$savedWeights()

outputCurve <- WeightedCurves(prun$optimizer()$curves(), weightsLong)
pnl <- outputCurve$curve()$as.zoo()[,1]
pnl <- pnl[!is.na(pnl)]
outputZoo <- cumsum(pnl)

weightsFixed <- Weights(weightsLong$.weights[1,])
fixedCurve <- WeightedCurves(prun$optimizer()$curves(), weightsFixed)
pnlFixed <- fixedCurve$curve()$as.zoo()[,1]
pnlFixed <- pnlFixed[!is.na(pnlFixed)]
fixedZoo <- cumsum(pnlFixed)
outputZoo <- merge(outputZoo, fixedZoo)
colnames(outputZoo) <- c('WFO','FixedWeights')

write.csv(outputZoo, squish(ae.reportDir, 'run',runNumber,'_', as.character(startList[runNumber]),'.csv'), row.names = index(outputZoo))
weightZoo <- weightsLong$as.zoo()
colnames(weightZoo) <- systems
write.csv(weightZoo, squish(ae.reportDir, 'weights',runNumber,'_', as.character(startList[runNumber]),'.csv'), row.names = index(weightZoo))


rm(list = ls())
