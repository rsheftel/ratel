# PortfolioRunsReport Class
# 
# Author: rsheftel
###############################################################################

constructor("PortfolioRunsReport", function(portfolioRuns=NULL, filename=NULL, childReportDirectory=NULL){
	this <- extend(RObject(), "PortfolioRunsReport", .pruns = portfolioRuns, .filename=filename, .childReportDirectory=childReportDirectory)
	constructorNeeds(this, portfolioRuns="PortfolioRuns", filename="character?", childReportDirectory="character")
	if (inStaticConstructor(this)) return(this)
	this$.makeFilenames()
	this$.openConnections()
	return(this)
})
#############################################################################################################
#			Report Methods - Main
#############################################################################################################

method ("reportAll", "PortfolioRunsReport", function(this, aum=NULL, ...){
	needs(aum="numeric?")
	this$addTitle()
	this$addSettings()
	this$addGraphLink()
	this$addChildReports(aum)
	this$addChildReportLinks()
	this$addOptimizationSummary()
	this$addWeights()
	this$addMetrics()
	this$plotPortfolioEquity()
})

#############################################################################################################
#			Report Methods - Settings
#############################################################################################################

method("addWeights", "PortfolioRunsReport", function(this, ...){
	hwrite("Weights", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	data <- data.frame()
	for (runName in this$.pruns$runNames()){
		prun <- this$.pruns$getRun(runName)
		weights <- prun$optimalWeights()
		names(weights) <- prun$curveNames()
		runData <- data.frame(t(weights))
		data <- rbind(data,runData)
	}
	rownames(data) <- this$.pruns$runNames()
	hwrite (HWriterUtils$dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn, br=TRUE)		
})

#############################################################################################################
#			Report Methods - Settings
#############################################################################################################

method("addTitle", "PortfolioRunsReport", function(this, ...){
	hwrite(squish("Portfolio Runs - ",this$.pruns$name()), this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

method("addSettings", "PortfolioRunsReport", function(this, ...){
	data <- list(GroupName=this$.pruns$groupName(), CurvesDirectory=this$.pruns$curvesDirectory())
	hwrite("Settings", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(HWriterUtils$listToColumn(data), rowNames=TRUE, colnames=FALSE), this$.conn, br=FALSE)
})

#############################################################################################################
#			Report Methods - Summary
#############################################################################################################

method("addOptimizationSummary", "PortfolioRunsReport", function(this, ...){
	hwrite("Optimization Summary", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	data <- data.frame()
	for (runName in this$.pruns$runNames()){
		prun <- this$.pruns$getRun(runName)
		runData <- data.frame(	StartDate	=prun$startDate(),
								EndDate		=prun$endDate(),
								Objective	=ifelse(is.null(prun$objective()), NA, as.character(prun$objective())),
								OptimType	=prun$optimType())
		oldNames <- rownames(data)
		data <- rbind(data,runData)
		rownames(data) <- c(oldNames, prun$name())
	}
	hwrite (HWriterUtils$dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn, br=TRUE)
})

method("addMetrics", "PortfolioRunsReport", function(this, ...){
	hwrite('Metrics', this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	singleValue <- function(z){
		if(length(unique(z))==1) return(unique(z))
		return(NA)
	}
	prun <- this$.pruns$getRun(first(this$.pruns$runNames()))
	for (curves in prun$.lists$curves){
		for (slip in prun$.lists$slips){
			hwrite(squish(curves,':',slip), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
			orig.list <- list()
			opt.list  <- list()
			for (runName in this$.pruns$runNames()){
				prun <- this$.pruns$getRun(runName)
				metrics <- prun$metric(source='original', curves=curves, slip=slip, curveName='Portfolio')
				orig.list[[runName]] <- zoo(metrics, names(metrics))
				metrics  <- prun$metric(source='optimal', curves=curves, slip=slip, curveName='Portfolio')
				opt.list[[runName]] <- zoo(metrics, names(metrics))
			}
			orig.zoo <- do.call(merge,orig.list)
			opt.zoo <- do.call(merge,opt.list)
			orig.zoo <- rollapply(orig.zoo,1,singleValue,by.column=FALSE)
			data <- as.data.frame(merge(orig.zoo, opt.zoo))
			colnames(data) <- c('original',this$.pruns$runNames())
			hwrite (HWriterUtils$dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn, br=TRUE)
		}		
	}
})

#############################################################################################################
#			Report Methods - Child Reports
#############################################################################################################

method("addChildReports", "PortfolioRunsReport", function(this, aum=NULL,...){
	needs(aum="numeric?")
	print("Generating Child Reports...")
	for (childName in this$.pruns$runNames()){
		print(childName)
		prun <- this$.pruns$getRun(childName)
		single.filename <- squish(this$.childReportDirectory,childName)
		this$.filenames$childReports[[childName]] <- single.filename
		single.report <- PortfolioRunReport(prun, single.filename)
		single.report$reportOptimization()
		if (!is.null(aum)) single.report$reportReturns(aum)
		single.report$graphOptimization()
		if (!is.null(aum)) single.report$graphReturns(aum)
		single.report$closeConnection()
	}
})

method("addChildReportLinks", "PortfolioRunsReport", function(this, ...){
	hwrite("Child Run Reports", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	for (childName in this$.pruns$runNames()){
		hwrite(childName, this$.conn, link=squish('file:///',makeWindowsFilename(this$.filenames$childReports[[childName]]),'.html'), br=TRUE)		
	}
})

#############################################################################################################
#			Graph Methods - Equity
#############################################################################################################
method("addGraphLink", "PortfolioRunsReport", function(this, ...){
	hwrite("Graphs", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite("Graphs for Runs", this$.conn, link=squish('file:///',makeWindowsFilename(this$.filenames$pdf)), br=TRUE)	
})

method("plotPortfolioEquity", "PortfolioRunsReport", function(this, ...){
	portfolioCurves <- list()
	for (childName in this$.pruns$runNames()){
		prun <- this$.pruns$getRun(childName)
		equity <- prun$portfolioCurveEquity("optimal", "raw", "slipped")
		names(equity) <- prun$name()
		portfolioCurves[[childName]] <- equity
	}
	portfolioZoo <- do.call(merge,portfolioCurves)
	if (!is.null(names(index(portfolioZoo)))) names(index(portfolioZoo)) <- c()
	
	chart.TimeSeries(portfolioZoo[,], ylab="Equity", main=squish("Portfolio Equity (optimal:raw:slipped)"),
		lwd=2, legend.loc='topleft', colorset=paired)
})

#############################################################################################################
#			Support Functions
#############################################################################################################

method(".makeFilenames", "PortfolioRunsReport", function(this, ...){
	this$.filenames <- list()
	if(is.null(this$.filename)) this$.filename <- squish(tempDirectory(),'PortfolioRunsReport',format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0))
	this$.filenames$html <- this$.filename
	if(rightStr(this$.filenames$html,5)!='.html') this$.filenames$html <- squish(this$.filename,'.html')
	this$.filenames$pdf <- squish(leftStr(this$.filenames$html,nchar(this$.filenames$html)-5),'.pdf')
	this$.filenames$childReports <- c()
})

method(".openConnections", "PortfolioRunsReport", function(this, ...){
	this$.conn <- openPage(filename=this$.filenames$html, title=this$.filename)
	file.remove(this$.filenames$pdf)
	pdf(this$.filenames$pdf)
})

method("closeConnection", "PortfolioRunsReport", function(this, ...){
	closePage(this$.conn)
	dev.off()
	this$.conn <- NULL	
})

method("openReport", "PortfolioRunsReport", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$.filenames$html)
})

method("filenames", "PortfolioRunsReport",function(this, ...){
	return(this$.filenames)		
})
