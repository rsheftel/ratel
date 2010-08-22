# RunGroupsReport Class
# 
# Author: rsheftel
###############################################################################


constructor("RunGroupsReport", function(runGroups=NULL){
	this <- extend(RObject(), "RunGroupsReport", .runGroups=runGroups)
	constructorNeeds(this, runGroups="RunGroups")
	if (inStaticConstructor(this)) return(this)
	this$.initializeLists()
	this$.sto <- this$.runGroups$stoObject()
	this$.stoSetup <- this$.runGroups$stoSetupObject()
	return(this)
})

method(".initializeLists","RunGroupsReport",function(this, ...){
	this$.msivs <- list()
	this$.metricCube <- list()
	this$.curveCube <- list()
	this$.portfolio <- NULL
	this$.groupName <- NULL
	this$.runs <- NULL
})

method("generateReports" ,"RunGroupsReport", function(this, groupNames=NULL, ...){
	needs(groupNames="character?")
	if (is.null(groupNames)) groupNames <- this$.runGroups$groupNames()
	for (groupName in groupNames){
		print(squish("Running reports for group: ",groupName))
		#Across all portfolios in the group reports
		this$.setupAllPortfolios(groupName)
		this$plotAllPortfoliosMutlipleRuns()
		this$plotSumAllPortfoliosRuns()
		this$plotAllPortfoliosEachRun()
		this$plotAllPortfoliosCorrelationMsivs()
		this$plotAllPortfoliosTradePath()
		
		#Each individual portfolio in the group reports
		for (portfolio in this$.portfolios){
			this$.setupSinglePortfolio(portfolio)
			this$selectedRunTable()
			this$plotPortfolioMultipleRuns()
			this$plotMarketsAcrossRuns()
			this$plotMarketsEachRun()
			this$plotEachMarketMutlipleRuns()
			this$plotRunCorrelations()
			this$plotMarketCorrelations()
			this$plotPortfolioTradePath()
		}
	}			
})

method(".setupAllPortfolios", "RunGroupsReport", function(this, groupName, ...){
	needs(groupName="character")
	this$.groupName <- groupName
	this$.runs <- as.numeric(this$.runGroups$group(this$.groupName)$runs)
	this$.portfolios <- this$.runGroups$group(this$.groupName)$portfolios
	this$.msivs$portfolios <- lapply(this$.portfolios, function(x) this$.sto$msiv(x))
	this$.setMetricCube(this$.msivs$portfolios)
	this$.setCurveCube(this$.msivs$portfolios)
	this$.makeDir()
})

method(".setupSinglePortfolio", "RunGroupsReport", function(this, portfolio, ...){
	needs(portfolio="character")
	this$.portfolio <- portfolio
	this$.msivs$portfolio <- this$.sto$msiv(this$.portfolio)
	this$.setMetricCube(list(this$.msivs$portfolio))
	this$.msivs$markets <- lapply(this$.stoSetup$portfolios()[[this$.portfolio]],function(market) this$.sto$msiv(market))
	this$.msivs$all <- this$.msivs$markets
	this$.msivs$all[[length(this$.msivs$all)+1]] <- this$.msivs$portfolio
	this$.setCurveCube(this$.msivs$all)
})

#################################################################################################################
#	Single Portfolio Plot Methods
#################################################################################################################

method("selectedRunTable", "RunGroupsReport", function(this, ...){
	filename <- squish(this$.groupDir,this$.portfolio,'_SelectedRuns')
	STOSelectedRunsTable$createFiles(this$.sto,this$.msivs$portfolio,runNums=this$.runs, filePath=filename,cube=this$.metricCube$cube)
})

method("plotPortfolioMultipleRuns", "RunGroupsReport", function(this, ...){
	this$pdfPlot( 	STOUtils$msivsRunsToZoo(this$.curveCube$cube, this$.msivs$portfolio, this$.runs),
					squish(this$.portfolio,' : ',this$.groupName),
					squish(this$.portfolio,'_portfolioMsivXmultipleRuns.pdf'))
})

method("plotMarketsAcrossRuns", "RunGroupsReport", function(this, ...){
	equityZoo <- STOUtils$mergeMsivsAcrossRuns(this$.curveCube$cube, this$.msivs$markets, this$.runs)
	colnames(equityZoo) <- c(sapply(this$.msivs$markets, function(msiv) msiv$market()),'Average')
	this$pdfPlot(equityZoo, squish(this$.portfolio,' : ',this$.groupName), squish(this$.portfolio,'_marketMsivsXacrossRuns.pdf'))
})

method("plotMarketsEachRun", "RunGroupsReport", function(this, ...){
	pdf(file=squish(this$.groupDir,this$.portfolio,'_marketMsivsXeachRun.pdf'))
	for (run in this$.runs){
		equityZoo <- STOUtils$mergeMultipleMsivsSingleRun(this$.curveCube$cube, this$.msivs$markets, run)
		simpleZooPlot(equityZoo,'Dates','Equity',squish(this$.portfolio,' : ',this$.groupName,' : ',run))
	}
	dev.off()
})

method("plotEachMarketMutlipleRuns", "RunGroupsReport", function(this, ...){
	pdf(file=squish(this$.groupDir,this$.portfolio,'_eachMarketMsivXmultipleRuns.pdf'))
	for (msiv in this$.msivs$markets){
		equityZoo <- STOUtils$mergeSingleMsivMultipleRuns(this$.curveCube$cube, msiv, this$.runs)
		simpleZooPlot(equityZoo,'Dates','Equity',squish(this$.portfolio,' : ',this$.groupName,' : ',msiv$market()))
	}
	dev.off()
})

method("plotYearPanels", "RunGroupsReport", function(this, ...){
		### THIS DOES NOT SAVE TO PDF CORRECTLY! ###
	equityZoo <- STOUtils$msivsRunsToZoo(this$.curveCube$cube, this$.msivs$portfolio, this$.runs)[,'Average']
	pdf(file=squish(this$.groupDir,this$.portfolio,'_yearPanels.pdf'))
	plotZooPanelByCalendar(equityZoo)
	dev.off()
})

method("plotRunCorrelations", "RunGroupsReport", function(this, ...){
	if(length(this$.runs)<=1){
		print("Cannot do correlation on only one run.")
		return()
	}
	if(length(this$.runs) > 10){
		print("Cannot do correlation on more than 10 runs.")
		return()
	}
	pdf(file=squish(this$.groupDir,this$.portfolio,'_correlation_Runs.pdf'))
	STOCharts$plotCorrelationPanel(this$.curveCube$cube, this$.msivs$portfolio, this$.runs)
	dev.off()
})

method("plotMarketCorrelations", "RunGroupsReport", function(this, ...){
	if(length(this$.msivs$markets)<=1){
		print("Cannot do correlation on only one market.")
		return()
	}
	pdf(file=squish(this$.groupDir,this$.portfolio,'_correlation_markets.pdf'))
	STOCharts$plotCorrelationPanel(this$.curveCube$cube, this$.msivs$markets, this$.runs)
	dev.off()
})

method("plotPortfolioTradePath", "RunGroupsReport", function(this, ...){
	cat('\n\nBuilding TradeCube for TradePath...\n')
	builder <- TradeCubeBuilder(sto=this$.sto, reverseDateLogic='50/50')
	tradeCube <- builder$runMultipleRunsForMsivList(msivs=this$.portfolioMarketMsivs(this$.portfolio), runs=this$.runs)
	tradeCube$pdfReport(squish(this$.groupDir,this$.portfolio,'_TradePath.pdf'))
	if(!all(tradeCube$.entrySizeFrame<0)){
		tradeCubeLong <- tradeCube$filter(entrySize > 0)
		tradeCubeLong$pdfReport(squish(this$.groupDir,this$.portfolio,'_TradePath_Long.pdf'))
	}
	if(!all(tradeCube$.entrySizeFrame>0)){
		tradeCubeShort <- tradeCube$filter(entrySize < 0)
		tradeCubeShort$pdfReport(squish(this$.groupDir,this$.portfolio,'_TradePath_Short.pdf'))
	}
})

#################################################################################################################
#	All Portfolios Plot Methods
#################################################################################################################

method("pdfPlot", "RunGroupsReport", function(this, equityZoo, title, filename, ...){
	needs(equityZoo="zoo", title="character", filename="character")
	pdf(squish(this$.groupDir,filename))
	simpleZooPlot(equityZoo,'Dates','Equity',title)
	try(dev.off(), silent=TRUE)	
})

method("plotAllPortfoliosMutlipleRuns", "RunGroupsReport", function(this, ...){
	this$pdfPlot(	STOUtils$mergeMsivsAcrossRuns(this$.curveCube$cube, this$.msivs$portfolios, this$.runs),
					squish('Each portfolio : All Runs : ',this$.groupName),
					'ALLPORTFOLIOS_portfolioMsivsXeachRun.pdf')
})

method("plotSumAllPortfoliosRuns", "RunGroupsReport", function(this, ...){
	equityZoo <- list()
	for (run in this$.runs){
		msivs <- this$.msivs$portfolios
		allPortfolios <- ParameterizedMsivs(msivs=msivs, weights = rep(1/length(msivs), length(msivs)), runs = rep(run,length(msivs)))
		equityZoo[[as.character(run)]] <- allPortfolios$curve(this$.curveCube$cube)$equity()
	}
	this$pdfPlot(do.call(merge, equityZoo),squish('All portfolios : Each Run : ',this$.groupName), 'ALLPORTFOLIOS_runs.pdf')
})

method("plotAllPortfoliosEachRun", "RunGroupsReport", function(this, ...){
	pdf(squish(this$.groupDir,'ALLPORTFOLIOS_eachRun.pdf'))
	for (run in this$.runs){
		equityZoo <- STOUtils$mergeMultipleMsivsSingleRun(this$.curveCube$cube, this$.msivs$portfolios, run)
		simpleZooPlot(equityZoo,'Dates','Equity',squish('All portfolios : ',this$.groupName,' : ',run))
	}
	try(dev.off(),silent=TRUE)
})

method("plotAllPortfoliosCorrelationMsivs", "RunGroupsReport", function(this, ...){
	if(length(this$.msivs$portfolios)<=1){
		print("Cannot do correlation on only one portfolio.")
		return()
	}
	pdf(squish(this$.groupDir,'ALLPORTFOLIOS_Correlation_MSIVs.pdf'))
	STOCharts$plotCorrelationPanel(this$.curveCube$cube, this$.msivs$portfolios, this$.runs)
	try(dev.off(), silent=TRUE)		
})

method("plotAllPortfoliosTradePath", "RunGroupsReport", function(this, ...){
	cat('\n\nBuilding TradeCube for TradePath...\n')
	builder <- TradeCubeBuilder(sto=this$.sto, reverseDateLogic='50/50')
	msivs <- this$.portfolioMarketMsivs(this$.portfolios)
	this$.setCurveCube(msivs)
	tradeCube <- builder$runMultipleRunsForMsivList(msivs=msivs, runs=this$.runs)
	tradeCube$pdfReport(squish(this$.groupDir,'ALLPORTFOLIOS_TradePath.pdf'))
	if(!all(tradeCube$.entrySizeFrame<0)){
		tradeCubeLong <- tradeCube$filter(entrySize > 0)
		tradeCubeLong$pdfReport(squish(this$.groupDir,'ALLPORTFOLIOS_TradePath_Long.pdf'))
	}
	if(!all(tradeCube$.entrySizeFrame>0)){
		tradeCubeShort <- tradeCube$filter(entrySize < 0)
		tradeCubeShort$pdfReport(squish(this$.groupDir,'ALLPORTFOLIOS_TradePath_Short.pdf'))
	}
})

#################################################################################################################
#	Cube Methods
#################################################################################################################

method(".setMetricCube", "RunGroupsReport", function(this, msivs, ...){
	needs(msivs="MSIV|list(MSIV)")
	markets <- sapply(msivs, function(x) x$market())
	for (market in markets) cat(squish('\nGenerating metric.cube for : ',market,'\n'))
	if (!all(markets %in% this$.metricCube$markets)){
		this$.metricCube$cube <- NULL
		this$.metricCube$cube <- this$.sto$metrics(msivs)
		this$.metricCube$markets <- markets
	}
})

method(".setCurveCube", "RunGroupsReport", function(this, msivs, ...){
	needs(msivs="MSIV|list(MSIV)")
	markets <- sapply(msivs, function(x) x$market())
	for (market in markets) cat(squish('\nGenerating curve.cube for : ',market,' : runs : ',paste(this$.runs,collapse=','),'\n'))
	this$.downloadCurves(markets)
	if ((!all(markets %in% this$.curveCube$markets)) || (!all(this$.runs %in% this$.curveCube$runs))){
		print(gc())
		this$.curveCube$cube <- NULL
		this$.curveCube$cube <- this$.sto$curves(msivs=msivs,runs = this$.runs)
		this$.curveCube$markets <- markets
		this$.curveCube$runs <- this$.runs
	}
})

method(".downloadCurves", "RunGroupsReport", function(this, markets, ...){
	if(!isWindows()){
		print('Curves can only be downloaded on Windows, errors may occur!')
		return()
	}
	CurveCube$download(	systemId=this$.runGroups$stoSetupObject()$systemID(), runs=this$.runs, markets=markets, skipExisting=TRUE)
})

#################################################################################################################
#	Helper Methods
#################################################################################################################

method(".makeDir", "RunGroupsReport", function(this, ...){
	this$.groupDir <- squish(this$.stoSetup$stoDirectory(),'/',this$.stoSetup$stoID(), "/Reports/RunGroups/",this$.groupName,'/')
	dir.create(this$.groupDir, recursive=TRUE)
})

method(".portfolioMarketMsivs", "RunGroupsReport", function(this, portfolios, ...){
	needs(portfolios="character")
	markets <- unlist(sapply(portfolios, function(x) this$.stoSetup$portfolios()[[x]])) 
	return(lapply(markets, function(x) this$.sto$msiv(x)))
})
