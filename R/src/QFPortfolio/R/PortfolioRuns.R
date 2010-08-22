# PortfolioRuns Class
# 
# Author: rsheftel
###############################################################################


constructor("PortfolioRuns", function(name=NULL, groupName=NULL, curvesDirectory=NULL, curvesExtension="bin", rawRange=NULL){
	this <- extend(RObject(), "PortfolioRuns", .name=name, .groupName=groupName, .curvesDirectory=curvesDirectory, .curvesExtension=curvesExtension)
	constructorNeeds(this, name="character", groupName="character", curvesDirectory="character", curvesExtension="character", rawRange="Range?")
	if (inStaticConstructor(this)) return(this)
	this$.run <- NULL
	this$.runs <- list()
	cat('Loading curves...')
	this$.curves <- CurveGroup(this$.groupName)$childCurves(dir=this$.curvesDirectory, extension=this$.curvesExtension, range=rawRange)
	cat('done\n')
	return(this)
})

#################################################################################################################
#	Getter only
#################################################################################################################

method("curvesDirectory", "PortfolioRuns", function(this, ...){
	return(this$.curvesDirectory)	
})

method("name", "PortfolioRuns", function(this, ...){
	return(this$.name)	
})

method("groupName", "PortfolioRuns", function(this, ...){
	return(this$.groupName)	
})

method("currentRun", "PortfolioRuns", function(this, ...){
	return(this$.run)		
})

method("childWeightedCurves", "PortfolioRuns", function(this, ...){
	return(this$.curves)		
})

#################################################################################################################
#	Run management
#################################################################################################################

method("addRun", "PortfolioRuns", function(this, runName, loadCurves = FALSE, ...){
	needs(runName="character")
	this$.run <- PortfolioRun(runName, this$.groupName, this$.curvesDirectory, this$.curvesExtension)
	if(loadCurves) this$.run$loadCurves(this$childWeightedCurves())
	priorNames <- names(this$.runs)
	this$.runs <- appendSlowly(this$.runs, this$.run)
	names(this$.runs) <- c(priorNames, runName)
})

method("useRun", "PortfolioRuns", function(this, runName, ...){
	needs(runName="character")
	this$.run <- this$.runs[[runName]]
})

method("getRun", "PortfolioRuns", function(this, runName, ...){
	needs(runName="character")
	return(this$.runs[[runName]])
})

method("runNames", "PortfolioRuns", function(this, ...){
	return(names(this$.runs))	
})