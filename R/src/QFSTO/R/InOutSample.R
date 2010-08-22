# InOutSample Class
# 
# Author: rsheftel
###############################################################################

constructor("InOutSample", function(systemID.in=NULL, systemID.out=NULL, source=NULL){
	this <- extend(RObject(), "InOutSample")
	constructorNeeds(this, systemID.in="numeric|integer", systemID.out="numeric|integer", source="character?")
	if (inStaticConstructor(this)) return(this)
	this$.initializeLists()
	this$.sources <- c('in','out')
	this$.systemID[['in']] <- systemID.in
	this$.systemID[['out']] <- systemID.out
	if(!is.null(source)) this$loadRunGroups(source)
	return(this)
})

#################################################################################################################
#	Report Methods
#################################################################################################################

method("generateReports", "InOutSample", function(this, groupNames=NULL, ...){
	this$portfolioReports()
	this$runGroupReports(groupNames)
})

method("runGroupReports", "InOutSample", function(this, groupNames=NULL, ... ){
	needs(groupNames="character?")
	runGroups <- this$.runGroups[[this$.runGroups$source]]
	if (is.null(groupNames)) groupNames <- runGroups$groupNames()
	
	for (group in groupNames){
		print(squish('Running reports for group : ',group))
		this$.group <- group
		this$.runs <- as.numeric(runGroups$group(group)$runs)
		for (portfolio in runGroups$group(group)$portfolios){
			print(squish('Running reports for portfolio : ',portfolio))
			this$.portfolio <- portfolio
			for (x in this$.sources) {
				this$.msiv[[x]] <- this$.sto[[x]]$msiv(portfolio)
				this$.setCurveCube(portfolio, x)
				this$.setMetricCube(portfolio, x)
			}
			this$.makeGroupDir()
			this$plotSingleMsivMultipleRuns()
			this$plotSingleMsivEachRun()			
			this$reportWFESelectedRunsTable()
		}
	}
})

method("portfolioReports", "InOutSample", function(this, ...){
	for (portfolio in this$.stoSetup$out$portfolioNames()){
		print(squish('Running reports for portfolio: ',portfolio))
		this$.portfolio <- portfolio
		for (x in this$.sources) {
			this$.msiv[[x]] <- this$.sto[[x]]$msiv(portfolio)
			this$.setMetricCube(portfolio, x)
		}
		this$.makePortfolioDir()
		this$reportWFEValidationTables()		
	}
})


method("plotSingleMsivMultipleRuns", "InOutSample", function(this, ...){
	pdf(squish(this$.groupDir,'SingleMsivMultipleRuns_',this$.group,'.pdf'))
	STOCharts$plotEquityMultipleMsivRunStitchInOutSample( this$.curveCube[['in']]$cube, this$.curveCube[['out']]$cube, 
														  this$.msiv[['in']], this$.msiv[['out']], this$.runs, 
														  title=squish('In-Out Sample: ',this$.group,', Portfolio : ',this$.portfolio))
	dev.off()
})

method("plotSingleMsivEachRun", "InOutSample", function(this, ...){
	pdf(squish(this$.groupDir,'SingleMsivEachRun_',this$.group,'.pdf'))
	for (run in this$.runs){
		STOCharts$plotEquityMsivRunStitchInOutSample(	this$.curveCube[['in']]$cube, this$.curveCube[['out']]$cube, 
														this$.msiv[['in']], this$.msiv[['out']], run, 
														title=squish('In-Out Sample: ',this$.group,' : ',this$.portfolio,' : ',run))		
	}
	dev.off()
})

method("reportWFEValidationTables", "InOutSample", function(this, ...){
	STOWFETables$createValidationTables(this$.sto[['in']], this$.sto[['out']], this$.msiv[['in']], 
										cubeIn = this$.metricCube[['in']]$cube, cubeOut = this$.metricCube[['out']]$cube,
										fileLocation=this$.portfolioDir)
})

method("reportWFESelectedRunsTable", "InOutSample", function(this, ...){
	STOWFETables$createSelectedRunsTables(this$.sto[['in']], this$.sto[['out']], this$.msiv[['in']], this$.runs, 
											cubeIn = this$.metricCube[['in']]$cube, cubeOut = this$.metricCube[['out']]$cube,
											fileLocation=this$.groupDir)
})

#################################################################################################################
#	Setup Methods
#################################################################################################################

method(".initializeLists", "InOutSample", function(this, ...){
	this$.systemID 	<- list()
	this$.runGroups <- list()	
	this$.stoSetup	<- list()
	this$.sto		<- list()
	this$.msiv		<- list()
	this$.curveCube <- list()
	this$.metricCube <- list()
})

method("loadRunGroups", "InOutSample", function(this, source='in', ...){
	needs(source="character")
	this$.runGroups[['in']] <- RunGroups(systemID=this$.systemID[['in']])	
	this$.runGroups[['out']] <- RunGroups(systemID=this$.systemID[['out']])
	this$.runGroups$source <- source
	this$.runGroups[[source]]$loadFromFile()
	this$.setupObjects()
	this$.setupDirectory()
})

method("runGroups", "InOutSample", function(this, source='in', ...){
	needs(source="character")
	return(this$.runGroups[[source]])	
})

method(".setupObjects", "InOutSample", function(this, ...){
	for (x in this$.sources) this$.stoSetup[[x]] <- this$.runGroups[[x]]$stoSetupObject()
	for (x in this$.sources) this$.sto[[x]] <- this$.runGroups[[x]]$stoObject()
})

method(".setupDirectory", "InOutSample", function(this, ...){
	this$.reportDir <- squish(	this$.stoSetup[['out']]$stoDirectory(),'/',this$.stoSetup[['out']]$stoID(),
								'/Reports/InOutSample/v',this$.stoSetup[['in']]$stoID(),'/')
})

method(".makeGroupDir", "InOutSample", function(this, ...){
	this$.groupDir <- squish(this$.reportDir,'RunGroups/',this$.group,'/',this$.portfolio,'/')
	dir.create(this$.groupDir, recursive=TRUE)
})

method(".makePortfolioDir", "InOutSample", function(this, ...){
	this$.portfolioDir <- squish(this$.reportDir,'Portfolios/',this$.portfolio,'/')
	dir.create(this$.portfolioDir, recursive=TRUE)
})

#################################################################################################################
#	Cube Methods
#################################################################################################################

method(".setMetricCube", "InOutSample", function(this, portfolio, source, ...){
	needs(portfolio="character", source="character")
	msiv <- this$.sto[[source]]$msiv(portfolio)
	markets <- c(portfolio, this$.stoSetup[[source]]$portfolios()[[portfolio]])	
	for (market in markets) cat(squish('\nGenerating metric.cube for : ',market,'\n'))
	if (!all(markets %in% this$.metricCube[[source]]$markets)){
		this$.metricCube[[source]]$cube		<- NULL
		this$.metricCube[[source]]$cube		<- this$.sto[[source]]$metrics(msiv)
		this$.metricCube[[source]]$markets	<- markets
	}
})

method(".setCurveCube", "InOutSample", function(this, portfolio, source, ...){
	needs(portfolio="character", source="character")
	
	msiv <- this$.sto[[source]]$msiv(portfolio)
	markets <- c(portfolio, this$.stoSetup[[source]]$portfolios()[[portfolio]])	
	for (market in markets) cat(squish('\nGenerating curve.cube for : ',market,' : runs : ',paste(this$.runs,collapse=','),'\n'))
	this$.downloadCurves(markets, source)
	if ((!all(markets %in% this$.curveCube[[source]]$markets)) || (!all(this$.runs %in% this$.curveCube[[source]]$runs))){
		print(gc())
		this$.curveCube[[source]]$cube 		<- NULL
		this$.curveCube[[source]]$cube 		<- this$.sto[[source]]$curves(msivs=msiv,runs = this$.runs)
		this$.curveCube[[source]]$markets 	<- markets
		this$.curveCube[[source]]$runs 		<- this$.runs
	}
})

method(".downloadCurves", "InOutSample", function(this, markets, source, ...){
	if(!isWindows()){
		print('Curves can only be downloaded on Windows, errors may occur!')
		return()
	}
	CurveCube$download(systemId=this$.stoSetup[[source]]$systemID(), runs=this$.runs, markets=markets, skipExisting=TRUE)
})
