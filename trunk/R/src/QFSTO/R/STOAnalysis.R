# STO Analysis Class
# 
# Author: rsheftel
###############################################################################


constructor("STOAnalysis", function(systemID=NULL){
	this <- extend(RObject(), "STOAnalysis", .systemID=systemID)
	constructorNeeds(this, systemID="numeric|integer")
	if (inStaticConstructor(this)) return(this)
	this$.metricCubePortfolio <- NULL
	return(this)
})

method("runAll", "STOAnalysis", function(this,  metricList, ...){
	needs(metricList = 'list(Metric)')
	this$metricList(metricList)
	this$loadStoDetails()
	this$loadStoObject()
	this$runSTOSetupReport()
	this$runAllPortfolioReports()	
})

#################################################################################################################
#	Getter only
#################################################################################################################

method("stoDetails","STOAnalysis", function(this, ...){
	return(this$.stoDetails)	
})

method("stoObject", "STOAnalysis", function(this, ...){
	return(this$.sto)	
})

method("metricList", "STOAnalysis", function(this, metrics=NULL, ...){
	needs(metrics="list(Metric)?")
	if (is.null(metrics)) return(this$.metricList)
	this$.metricList <- metrics
})

method("allMetrics","STOAnalysis", function(this, ...){
	if(is.null(this$.metricCube)) return(NULL)
	return(this$.metricCube$availableMetrics())
})

#################################################################################################################
#	Loader Methods
#################################################################################################################

method("loadStoDetails","STOAnalysis", function(this, ...){
	this$.stoDetails <- STOSetup(systemID=this$.systemID)
	cat(squish('\nLoading system details from SystemDB for system.id : ',this$.systemID,'\n'))
	this$.stoDetails$loadFromSystemDB(verbose=TRUE)
	this$.setupDirs()
	this$.setParameters()
})

method(".setupDirs", "STOAnalysis", function(this, ...){
	this$.dirs <- list()
	this$.dirs$main <- squish(this$.stoDetails$stoDirectory(),'/',this$.stoDetails$stoID(),"/Reports/")
})

method(".setParameters", "STOAnalysis", function(this, ...){
	this$.parameters <- as.character(this$.stoDetails$parameters()$name)
	this$.fixedParameters <- as.character(this$.stoDetails$parameters()$name[this$.stoDetails$parameters()$count==1])
	this$.variableParameters <- this$.parameters[-match(this$.fixedParameters,this$.parameters)]
})

method(".makeDir", "STOAnalysis", function(this, subDir=NULL, dirTag=NULL, ...){
	needs(subDir="character?", dirTag="character?")
	directory <- squish(this$.dirs$main,subDir)
	try(dir.create(directory, recursive=TRUE),silent=TRUE)
	if (!is.null(dirTag)) this$.dirs[[dirTag]] <- directory
})

method("loadStoObject", "STOAnalysis", function(this, ...){
	cat("\nLoading the sto object...\n")
	this$.sto <- this$.stoDetails$stoObject()	
	if(is.null(this$.sto)) this$.sto <- STO(this$.stoDetails$stoDirectory(), this$.stoDetails$stoID(), calculateMetrics=FALSE)
})

method(".makeMetricCube", "STOAnalysis", function(this, portfolio=NULL, ...){
	needs(portfolio="character")
	this$.msiv <- this$.sto$msiv(portfolio)
	if (is.null(this$.metricCubePortfolio) || (this$.metricCubePortfolio != portfolio)){
		this$.metricCube <- NULL
		print("Garbage...")
		print(gc())
		cat(squish('\nGenerating metric.cube for : ',as.character(this$.msiv),'\n'))		
		this$.metricCube <- this$.sto$metrics(this$.msiv)
		this$.metricCubePortfolio <- portfolio
	}
})

#################################################################################################################
#	General reports
#################################################################################################################

method("runSTOSetupReport", "STOAnalysis", function(this, ...){
	cat(squish("\nGenerating STOSetup report in ",this$.dirs$main,'\n'))
	this$.makeDir()
	report <- STOSetupReport(this$.stoDetails, squish(this$.dirs$main,'SystemDetails.html'))
	report$reportAll()
	report$closeConnection()
})

#################################################################################################################
#	Portfolio level reports
#################################################################################################################

method("runAllPortfolioReports", "STOAnalysis", function(this,portfolios = c('ALL',this$.stoDetails$portfolioNames()),...){
	for (portfolio in portfolios){
		print(squish('Running reports for portfolio : ',portfolio))
		this$.makeMetricCube(portfolio)
		if(is.null(this$metricList())) this$metricList(this$allMetrics())
		this$.runSTOValidationTable(portfolio)
		this$.runWiskograms(portfolio)
		this$.runSurfaceSliceReports(portfolio)
		this$.runReshapeMetrics(portfolio)
		this$.runSortedRunsByKeyMetrics(portfolio)
		this$.runSmashFile(portfolio)
	}
})

method(".runSTOValidationTable", "STOAnalysis", function(this, portfolio, ...){
	needs(portfolio="character")
	this$.makeDir('Validation/','validation')
	cat(squish('\nGenerating STOValidation reports in : ',this$.dirs$validation,'\n'))
	STOValidationTable(this$.sto)$createFiles(	msiv=this$.msiv, filePath=squish(this$.dirs$validation,portfolio), 
												cube=this$.metricCube, metricList=this$allMetrics())
})

method(".runReshapeMetrics", "STOAnalysis", function(this, portfolio, ...){
	needs(portfolio="character")
	this$.makeDir('ReshapedMetrics/','reshape')
		
	mps <- STOUtils$getMetricParameterSurface(this$.sto, this$.metricCube, this$.msiv, this$metricList())
	print(squish('Reshaped Metrics - ',as.character(this$.msiv)))
	molten <- melt(mps, id=c('run',this$.parameters))		
	casted <- list()
	report <- HWriterUtils(filename=squish(this$.dirs$reshape,squish(portfolio,'_ReshapeMetrics')),pdf=FALSE)
	hwrite(squish("Reshaped Metrics - ",as.character(this$.msiv)), report$connection(), heading=1, center=FALSE, style='font-family: sans-serif')
	for (param in this$.variableParameters){
		hwrite(param, report$connection(), heading=2, center=FALSE, style='font-family: sans-serif')
		for (functor in c('mean','median')){
			cat(squish('\nCalculating : ',param,' : ',functor))
			casted[[param]][[functor]] <- cast(molten, squish(param, ' ~ variable'), fun.aggregate=match.fun(functor), margins='grand_row')
			hwrite(functor, report$connection(), heading=3, center=FALSE, style='font-family: sans-serif')
			hwrite(HWriterUtils$dataTable(casted[[param]][[functor]], rowNames=FALSE, colNames=TRUE),report$connection())
		}
	}
	report$closeConnection()
})

method(".runSortedRunsByKeyMetrics", "STOAnalysis", function(this, portfolio, numRows=1000, ...){
	needs(portfolio="character", numRows="numeric|integer")
	this$.makeDir('SortedRuns/','sortedRuns')
	mps <- STOUtils$getMetricParameterSurface(this$.sto, this$.metricCube, this$.msiv, this$allMetrics())
	print(squish('Sorted Runs by Key Metrics - ',as.character(this$.msiv)))
	
	for (metric in this$metricList()){
		metric.char <- as.character(metric)
		cat(squish('Metric : ',metric.char,'\n'))
		report <- HWriterUtils(filename=squish(this$.dirs$sortedRuns,squish(portfolio,'_',metric.char)),pdf=FALSE)
		hwrite(squish("Sorted Runs - ",as.character(this$.msiv),' : ',metric.char), report$connection(), heading=1, center=FALSE, style='font-family: sans-serif')			
		sorted <- mps[order(mps[[metric.char]],decreasing=TRUE,na.last=TRUE),][1:numRows,]
		hwrite(HWriterUtils$dataTable(sorted, rowNames=FALSE, colNames=TRUE),report$connection())
	}
	report$closeConnection()
})

method(".runSmashFile", "STOAnalysis", function(this, portfolio, ...){
	needs(portfolio="character")
	this$.makeDir('Smash/','smash')
	mps <- STOUtils$getMetricParameterSurface(this$.sto, this$.metricCube, this$.msiv, this$allMetrics())
	write.csv(mps,squish(this$.dirs$smash,portfolio,'_smashed.csv'), row.names=FALSE)
})

#################################################################################################################
#	Surface slice methods
#################################################################################################################
method(".runSurfaceSliceReports", "STOAnalysis", function(this, portfolio, ...){
	needs(portfolio="character")
	this$.makeDir('SurfaceSlices/','surfaceSlices')
	paramCombos <- combn(this$.variableParameters,2)
	for (x in 1:NCOL(paramCombos)){
		for (metric in this$metricList()){
			dir <- squish(this$.dirs$surfaceSlices,portfolio)
			dir.create(squish(dir,'/images'), recursive=TRUE)
			axes <- c(paramCombos[1,x],paramCombos[2,x])
			filename <- squish(paste(axes,collapse="_"),'_',as.character(metric))
			print(squish('Generating Surface Slice Report : ',portfolio,' : ',filename))
			report <- SurfaceSliceReport(htmlDirectory=dir, filename=filename, sto=this$.sto, imagesDirectory=squish(dir,'/images'),
										 msiv=this$.msiv, metric=metric, axes=c(paramCombos[1,x],paramCombos[2,x]), metricCube=this$.metricCube)
			report$addTitle()
			report$addContourPlot()
			report$addMultiLinePlot()
			report$addDataGrid()
			report$closeConnection()
			report <- NULL
		}
	}
})

#################################################################################################################
#	Wiskogram methods
#################################################################################################################

method(".runWiskograms", "STOAnalysis", function(this, portfolio, ...){
	needs(portfolio="character")
	print(squish("Generating ALL flip reports for portfolio : ",portfolio))
	this$.runAllWiskograms(portfolio)
	for (parameter in this$.variableParameters){
		print(squish("Generating flip reports for portfolio : ",portfolio,', parameter : ',parameter))
		this$.runSingleWiskogram(portfolio, parameter)
	}
})

method(".runAllWiskograms", "STOAnalysis", function(this, portfolio, ...){
	needs(portfolio="character")
	this$.wiskogram(portfolio, list(RunFilter$with('ALL', this$.sto$parameters()$runs())), 'ALL')
})

method(".runSingleWiskogram", "STOAnalysis", function(this, portfolio, parameter, ...){
	needs(portfolio="character", parameter="character")
	this$.wiskogram(portfolio, this$.sto$parameters()$filtersAll(parameter), parameter)
})

method(".wiskogram", "STOAnalysis", function(this, portfolio, filters, fileSuffix, ...){
	needs(portfolio="character", filters="RunFilter|list(RunFilter)", fileSuffix="character")
	generator <- MetricPanelReport(this$.sto, this$metricList(), this$.msiv, this$.metricCube)
	generator$runReport(squish(portfolio,'_',fileSuffix),filters,openInBrowser=FALSE)
	generator <- NULL
})
