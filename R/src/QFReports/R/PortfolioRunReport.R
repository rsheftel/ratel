# PortfolioRunReport Class
###############################################################################

constructor("PortfolioRunReport", function(portfolioRun=NULL, filename=NULL){
	this <- extend(RObject(), "PortfolioRunReport", .prun = portfolioRun, .filename=filename)
	constructorNeeds(this, portfolioRun="PortfolioRun", filename="character?")
	if (inStaticConstructor(this)) return(this)
	this$.makeFilenames()
	this$.openConnections()
	return(this)
})

#############################################################################################################
#			Report Methods - Main
#############################################################################################################

method ("reportOptimization", "PortfolioRunReport", function(this, ...){
	this$addTitle()
	this$addGraphLink()
	this$addGroupSettings()
	this$addOptimizationSettings()
	this$addResults()
})

method("reportReturns", "PortfolioRunReport", function(this, aum, ...){
	needs(aum="numeric")
	this$.aum <- aum
	hwrite(squish("Returns (AUM = $",format(this$.aum,scientific=FALSE,big.mark=",",nsmall=0),")"), 
						this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	this$addCalendarReturns()
	hwrite("Drawdowns", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	this$addDrawdowns()
})

method("reportCMF", "PortfolioRunReport", function(this, ...){
	this$addCommonMarketFactors()
})

#############################################################################################################
#			Graph Methods - Main
#############################################################################################################

method("graphOptimization", "PortfolioRunReport", function(this, ...){	
	this$plotEquityCurves()
	this$plotMetricsForChildren(metrics=list(AnnualizedNetProfit,WeeklyStandardDeviation,DailyStandardDeviation,DownSideDeviation), 
								percentages=c(FALSE,TRUE,TRUE,TRUE))
})

method("graphReturns", "PortfolioRunReport", function(this, ...){
	this$plotPerformanceSummary()
})

#############################################################################################################
#			Graph Methods - Equity Curves
#############################################################################################################

method("plotEquityCurves", "PortfolioRunReport", function(this, ...){
	this$plotPortfolioCurves('raw')
	this$plotPortfolioCurves('cut')
	this$plotChildCurves(curves='raw')
	this$plotChildCurves(curves='cut')
	#this$plotCurveCorrelation()
})

method("plotPortfolioCurves", "PortfolioRunReport", function(this, curve=NULL, ...){
	needs(curve='character')
	portfolioCurves <- list()
	for (source in this$.prun$.lists$sources)
		for (slip in this$.prun$.lists$slips){
			curveName <- paste(source,curve,slip,sep=":")
			equity <- this$.prun$portfolioCurveEquity(source, curve, slip)
			names(equity) <- curveName
			portfolioCurves[[curveName]] <- equity
		}
	
	portfolioZoo <- do.call(merge,portfolioCurves)
	if (!is.null(names(index(portfolioZoo)))) names(index(portfolioZoo)) <- c()
	
	chart.TimeSeries(portfolioZoo[,], ylab="Equity", main=squish("Portfolio Equity - ",curve),
							lwd=2, legend.loc='topleft',
							event.color='lightgrey',event.lines=this$.startEndRange())
})

method("plotChildCurves","PortfolioRunReport", function(this,source='optimal',curves='raw',slip='slipped', includePortfolio=FALSE, ...){
	if (includePortfolio){
		curves.zoo <- this$.prun$allCurvesEquity(source, curves, slip)
	}
	else{
		curves.zoo <- this$.prun$childCurvesEquity(source,curves,slip)
	}
	names(index(curves.zoo)) <- c()
	chart.TimeSeries(curves.zoo[,], ylab="Child Curve Equity", main=squish("Equity - ",curves),
						lwd=2, legend.loc='topleft',
						event.color='lightgrey',event.lines=this$.startEndRange(), colorset=paired)
})

#############################################################################################################
#			Graph Methods - Calendar and Correlation
#############################################################################################################

method("plotByYear","PortfolioRunReport", function(this,source='optimal',curves='raw',slip='slipped',...){
	plotZooPanelByCalendar(this$.prun$portfolioCurveEquity(source,curves,slip))
})

method("plotCurveCorrelation","PortfolioRunReport", function(this,source='optimal',curves='raw',slip='slipped',...){
	portfolio <- this$.prun$portfolioCurveEquity(source,curves,slip)
	curves.zoo <- merge(portfolio, this$.prun$childCurvesEquity(source,curves,slip))
	curves.zoo[is.na(curves.zoo)] <- 0
	plotCorrelationPanel(data.frame(curves.zoo))
	title(paste(source,curves,slip,sep=":"))
})

method("plotPerformanceSummary", "PortfolioRunReport", function(this, ...){
	charts.PerformanceSummary(this$.monthlyReturns()*100, lwd=2, ylog=FALSE, method="StdDev", return.method="returns", 
								main="Performance - Portfolio")
	charts.PerformanceSummary(this$.monthlyChildReturns()*100, lwd=2, ylog=FALSE, method="StdDev", return.method="returns", 
								main="Performance - Children", colorset=paired, all=TRUE)
})

#############################################################################################################
#			Graph Methods - Distributions
#############################################################################################################

method('plotMetricsForChildren', 'PortfolioRunReport', function(this, metrics, percentages, ...){
	needs(metrics='Metric|list(Metric)', percentages='logical')
	failIf((length(metrics) != length(percentages)),'Length of metric and percentages must be the same.')
	availableMetrics <- names(this$.prun$metric(curveName='Portfolio'))
	for (x in 1:length(metrics)){
		metric <- as.character(metrics[[x]])
		if (metric %in% availableMetrics){ this$plotMetricForChildren(metric=metric, percentages[x])}
		else{ print (squish('Metric not available for plotMetricForChildren: ', metric))}
	}
})

method('plotMetricForChildren', 'PortfolioRunReport', function(this, metric, percentage=FALSE, curves='cut',slip='slipped',...){
	needs(metric="Metric|character", percentage="logical", curves='character', slip='character')
	metMatrix <- NULL
	metric <- as.character(metric)
	for (child in this$.prun$curveNames()){
		original <- this$.prun$metric(source='original', curves=curves, slip=slip, curveName=child, metric=metric)
		optimal <- this$.prun$metric(source='optimal', curves=curves, slip=slip, curveName=child, metric=metric)
		metMatrix <- cbind(metMatrix,(c(original,optimal)))
	}
	colnames(metMatrix) <- this$.prun$curveNames()
	rownames(metMatrix) <- c('original','optimal')
	ylab <- 'Value'
	if (percentage) {
		metMatrix <- metMatrix / rowSums(metMatrix)
		ylab <- 'Percentage'
	}
	.at <- barplot(metMatrix, beside=TRUE, main=squish(curves,' : ',slip,' : ',metric), xlab='', ylab=ylab,
					col=c('grey','green'),xaxt='n', legend.text=c(rownames(metMatrix)))
	axis(1,at = .at[1,],labels = colnames(metMatrix),padj=0,las=3,cex.axis = 0.5)
	grid()
	abline(0,0)
})

#############################################################################################################
#			Report Methods - Header and Settings
#############################################################################################################

method("addTitle", "PortfolioRunReport", function(this, ...){
	hwrite(squish("Portfolio Run - ",this$.prun$name()), this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

method("addGraphLink", "PortfolioRunReport", function(this, ...){
	hwrite("Graphs for Run", this$.conn, link=basename(this$.filenames$pdf))
})

method("addGroupSettings", "PortfolioRunReport", function(this, ...){
	data <- list(GroupName=this$.prun$groupName(), CurvesDirectory=this$.prun$curvesDirectory())
	hwrite("Group Settings", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite (this$.dataTable(this$.listToColumn(data), rowNames=TRUE, colNames=FALSE), this$.conn, br=FALSE)
	hwrite("Group Members Curves", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	hwrite(this$.dataTable(this$curvesData(), rowNames=FALSE, colNames=TRUE), this$.conn)
})

method("curvesData", "PortfolioRunReport", function(this, ...){
	curve.names <- this$.prun$curveNames()
	curves <- this$.prun$childWeightedCurves(source='original', curves='raw', slip='noslip')
	data <- c()
	for (curve in curve.names)
		data <- rbind(data, data.frame(Curve=curve, StartDate=first(index(curves[[curve]]$pnl())), EndDate=last(index(curves[[curve]]$pnl()))))
	slippages <- this$.prun$slippages()
	if(is.null(slippages)) slippages <- rep("NULL",length(curve.names))  
	data <- cbind(data, data.frame(Slippage=slippages))
	return(data)
})

#############################################################################################################
#			Report Methods - Optimization Settings
#############################################################################################################

method("addOptimizationSettings", "PortfolioRunReport", function(this, ...){
	hwrite("Optimization Settings", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif', br=TRUE)
	data <- list(StartDate=this$.prun$startDate(), EndDate=this$.prun$endDate())
	hwrite (this$.dataTable(this$.listToColumn(data), rowNames=TRUE, colNames=FALSE), this$.conn, br=TRUE)
	data <- list(Objective=as.character(this$.prun$objective()))
	hwrite (this$.dataTable(this$.listToColumn(data), rowNames=TRUE, colNames=FALSE), this$.conn, br=TRUE)
	data <- list(OptimizationType=as.character(this$.prun$optimType()))
	hwrite (this$.dataTable(this$.listToColumn(data), rowNames=TRUE, colNames=FALSE), this$.conn, br=TRUE)
	if (!is.null(this$.prun$params()))
		hwrite (this$.dataTable(this$.listToColumn(this$.prun$params()), rowNames=TRUE, colNames=FALSE), this$.conn, br=TRUE)
	this$addConstraints()
})

method("addConstraints", "PortfolioRunReport", function(this, ...){
	hwrite("Constraints", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif', br=TRUE)
	
	constraints <- c('Portfolio','AllChildren','EachChild','WeightedCurve')
	data <- list()
	for (con in this$.prun$constraintList())
		switch (con$type,
					Portfolio = data$Portfolio <- rbind(data$Portfolio, data.frame(metric=as.character(con$metric), min=con$min, max=con$max)),
					AllChildren = data$AllChildren <- rbind(data$AllChildren, data.frame(metric=as.character(con$metric), min=con$min, max=con$max)),
					EachChild = data$EachChild <- rbind(data$EachChild, data.frame(metric=as.character(con$metric), min=paste(con$min,collapse=','), max=paste(con$max,collapse=','))),
					WeightedCurve = data$WeightedCurve <- rbind(data$WeightedCurve, data.frame(constraint=con$report))
				)
			
	for (con in constraints){
		if(!is.empty(data[[con]])){
			hwrite(con, this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
			hwrite(this$.dataTable(data[[con]], rowNames=FALSE, colNames=TRUE), this$.conn, br=TRUE)
		}
	}		
})

#############################################################################################################
#			Report Methods - Optimization Results
#############################################################################################################

method("addResults", "PortfolioRunReport", function(this, ...){
	hwrite("Optimization Results", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(this$.dataTable(t(data.frame(OptimalSource=this$.prun$optimalSource())), rowNames=TRUE, colNames=FALSE), this$.conn)
	this$addWeights()
	this$addSizing()
	this$addMetrics()
	hwrite("PnL Tables", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	this$addCalendarPnL()
	hwrite("Correlations", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	this$addCorrelations()
})

method("addWeights", "PortfolioRunReport", function(this, ...){
	hwrite("Curve Weights", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	dataOrNA <- function(inData){
		if (is.null(inData)) return(NA)
		return(inData)
	}
	data <- data.frame(this$.prun$optimalWeights())
	row.names(data) <- this$.prun$curveNames()
	data <- cbind(data, dataOrNA(this$.prun$seeds()))
	data <- cbind(data, dataOrNA(this$.prun$minMaxWeights()$mins))
	data <- cbind(data, dataOrNA(this$.prun$minMaxWeights()$maxs))
	names(data) <- c('Optimal','Seed','Min','Max')
	divScores <- this$.prun$diversityScore()
	if (!is.null(divScores)){
		data <- cbind(data, divScores)
		names(data) <- c(names(data)[1:(length(names(data))-1)],"DiversityScore")
	}
	hwrite(this$.dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn)
})

method("addSizing", "PortfolioRunReport", function(this, ...){
	if (length(grep(':',this$.prun$curveNames())) == 0){	#This is set so it does not run if the group has MSIV in top level
		hwrite("Sizing", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
		exposure <- Exposure(this$.prun$groupName())
		weights <- this$.prun$optimalWeights()
		names(weights) <- this$.prun$curveNames()
		exposure$weights(weights)
		data <- exposure$sizing()
		hwrite(this$.dataTable(this$.formatNumbers(data), rowNames=FALSE, colNames=TRUE), this$.conn)
	}
})

method("addMetrics", "PortfolioRunReport", function(this, ...){
	hwrite("Metrics", this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	this$addPortfolioMetrics()
	this$addCurveMetrics()		
})

method("addPortfolioMetrics", "PortfolioRunReport", function(this, ...){
	hwrite("Portfolio", this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	for (curve in this$.prun$.lists$curves)
		for (slip in this$.prun$.lists$slips){
			hwrite(squish(curve,':',slip), this$.conn, heading=5, center=FALSE, style='font-family: sans-serif')
			original <- this$.prun$metric('original',curve,slip,'Portfolio')
			optimal <- this$.prun$metric('optimal',curve,slip,'Portfolio')
			failIf((is.null(original) || is.null(optimal)), "There must be metrics calculated to generate the report.")
			hwrite(this$.dataTable(t(rbind(optimal,original)), rowNames=TRUE, colNames=TRUE), this$.conn)
		}	
})

method("addCurveMetrics", "PortfolioRunReport", function(this, ...){
	hwrite("Curves", this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	for (source in this$.prun$.lists$sources)
		for (curve in this$.prun$.lists$curves)
			for (slip in this$.prun$.lists$slips){
				hwrite(squish(source,':',curve,':',slip), this$.conn, heading=5, center=FALSE, style='font-family: sans-serif')
				data <- c()
				for (curveName in this$.prun$curveNames(source,curve,slip)){
					priorRownames <- row.names(data)
					data <- rbind(data,this$.prun$metric(source,curve,slip,curveName))
					row.names(data) <- c(priorRownames,curveName)
				}
				hwrite(this$.dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn)
				sums <- rep(colSums(data), each=nrow(data))
				percent <- data/sums
				hwrite(this$.dataTable(format(percent,nsmall=2,scientific=FALSE,digits=2), rowNames=TRUE, colNames=TRUE), this$.conn)
			}	
})

#############################################################################################################
#			Report Methods - Calendar Results
#############################################################################################################

method("addCalendarPnL", "PortfolioRunReport", function(this, ...){
	hwrite("Portfolio", this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	this$addCalendarPnLPortfolio()
	this$addCalendarPnLChildren()
})

method("addCalendarPnLPortfolio", "PortfolioRunReport", function(this, ...){
	pnl <- this$.prun$portfolioCurve('optimal', 'raw', 'slipped')$pnl()
	pnl.eom <- Interval$MONTHLY$collapse(pnl,sum)
	data <- t(table.CalendarReturns(pnl.eom,returns=FALSE))
	hwrite(this$.dataTable(this$.formatDollars(data), rowNames=TRUE, colNames=TRUE), this$.conn)
})

method("addCalendarPnLChildren", "PortfolioRunReport", function(this, ...){
	children <- this$.prun$childWeightedCurves('optimal', 'raw', 'slipped')
	for (child in this$.prun$curveNames('optimal','raw','slipped')){
		hwrite(child, this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
		pnl <- children[[child]]$pnl()
		pnl.eom <- Interval$MONTHLY$collapse(pnl,sum)
		data <- t(table.CalendarReturns(pnl.eom,returns=FALSE))
		hwrite(this$.dataTable(this$.formatDollars(data), rowNames=TRUE, colNames=TRUE), this$.conn)
	}
})

#############################################################################################################
#			Report Methods - Correlation Results
#############################################################################################################

method("addCorrelations", "PortfolioRunReport", function(this, ...){
	hwrite("Levels", this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	this$addCorrelationsLevels()
	hwrite("5d Changes", this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	this$addCorrelationsChanges()	
})

method("addCorrelationsLevels", "PortfolioRunReport", function(this, ...){
	for (curve in this$.prun$.lists$curves){
		curves.zoo <- this$.prun$allCurvesEquity('optimal',curve,'slipped')
		hwrite(squish("optimal:",curve,":slipped"), this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
		data <- cor(curves.zoo, use="pairwise.complete.obs", method="pearson")
		hwrite(this$.dataTable(this$.formatNumbers(data), rowNames=TRUE, colNames=TRUE), this$.conn)
	}
})

method("addCorrelationsChanges", "PortfolioRunReport", function(this, ...){
	for (curve in this$.prun$.lists$curves){
		curves.zoo <- this$.prun$allCurvesEquity('optimal',curve,'slipped')
		curves.zoo <- diff(curves.zoo,lag=5)
		hwrite(squish("optimal:",curve,":slipped"), this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
		data <- cor(curves.zoo, use="pairwise.complete.obs", method="pearson")
		hwrite(this$.dataTable(this$.formatNumbers(data), rowNames=TRUE, colNames=TRUE), this$.conn)
	}
})

#############################################################################################################
#			Report Methods - Returns
#############################################################################################################

method("addCalendarReturns", "PortfolioRunReport", function(this, ...){
	data <- t(table.CalendarReturns(this$.monthlyReturns(), digits=4, as.perc=TRUE, returns=FALSE))*100
	hwrite(this$.dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn) 
})

method("addDrawdowns", "PortfolioRunReport", function(this, ...){
	data <- table.Drawdowns(round(this$.monthlyReturns()*100,2), top=5, method="returns")
	hwrite(this$.dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn) 			
})

#############################################################################################################
#			Report Methods - CMF
#############################################################################################################

method("addCommonMarketFactors", "PortfolioRunReport", function(this, ...){
	source <- 'optimal'
	slip <- 'slipped'
	portfolioCurves <- list()
	for (curve in this$.prun$.lists$curves){
		curveName <- paste(source,curve,slip,sep="_")
		equity <- this$.prun$portfolioCurveEquity(source, curve, slip)
		names(equity) <- curveName
		portfolioCurves[[curveName]] <- equity
	}
	portfolioZoo <- do.call(merge,portfolioCurves)
	if (!is.null(names(index(portfolioZoo)))) names(index(portfolioZoo)) <- c()
	
	filename <- last(strsplit(this$filenames()$html,"/")[[1]])
	directory <- leftStr(this$filenames()$html, nchar(this$filenames()$html) - nchar(filename))
	
	fprefix <- squish(leftStr(filename,nchar(filename)-5),"_CMF")
	rept <- CommonMarketFactorsReport(outputDirectory=directory, filenamePrefix=fprefix, analysisZoo=portfolioZoo)
	cmf <- rept$commonMarketFactorsObject()
	
	fileDir <- squish(system.file("testdata", package="QFPortfolio"),'/CommonMarketFactors/')
	standard.names <- c('10ySwapRate','CDX.IG5Y','5ySwapSpread','3m5yBpVolDaily','2y10yBpVolDaily','ES')
	standard.tickers <- c('irs_usd_rate_10y_mid','cdx-na-ig_market_spread_5y_otr','irs_usd_spread_5y_1n','swaption_usd_3m5y_atm_payer_vol_bp_daily_mid','swaption_usd_2y10y_atm_payer_vol_bp_daily_mid','ES.1C')
	standard.sources <- c('internal','internal','internal','jpmorgan','jpmorgan','internal')
	standard.containers <- c('tsdb','tsdb','tsdb','tsdb','tsdb','systemdb')
	cmf$addFactors(standard.names, standard.tickers, standard.sources, standard.containers, standard.containers)	
	cmf$loadFactors()
	
	rept$generateReport()
	rept$closeConnection()
	this$addCommonMarketFactorsLink(rept$filename())
})

method("addCommonMarketFactorsLink", "PortfolioRunReport", function(this, filename, ...){
	hwrite("Common Market Factors", this$.conn, heading=2, center=TRUE, style='font-family: sans-serif', br=TRUE)
	hwrite("Common Market Factors Report", this$.conn, link=squish('file:///',makeWindowsFilename(filename)))
})

#############################################################################################################
#			Support Functions
#############################################################################################################

method(".makeFilenames", "PortfolioRunReport", function(this, ...){
	this$.filenames <- list()
	if(is.null(this$.filename)) this$.filename <- squish(tempDirectory(),'PortfolioRunReport',format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0))
	this$.filenames$html <- this$.filename
	if(rightStr(this$.filenames$html,5)!='.html') this$.filenames$html <- squish(this$.filename,'.html')
	this$.filenames$pdf <- squish(leftStr(this$.filenames$html,nchar(this$.filenames$html)-5),'.pdf')
})

method(".openConnections", "PortfolioRunReport", function(this, ...){
	this$.conn <- openPage(filename=this$.filenames$html, title=this$.filename)
	file.remove(this$.filenames$pdf)
	pdf(this$.filenames$pdf)
})

method("closeConnection", "PortfolioRunReport", function(this, ...){
	closePage(this$.conn)
	dev.off()
	this$.conn <- NULL	
})

method("openReport", "PortfolioRunReport", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$.filenames$html)
})

method("filenames", "PortfolioRunReport",function(this, ...){
	return(this$.filenames)		
})

method(".dataTable", "PortfolioRunReport", function(static, data, rowNames=TRUE, colNames=TRUE, ...){
	needs (data='matrix|data.frame|character', rowNames='logical', colNames='logical')
	
	return(hwrite(static$.formatNumbers(data),
			style		='font-family: monospace; border: 1px dotted #999',
			row.names	=rowNames,
			col.names	=colNames,
			row.style 	=list('font-weight:bold; border: 1px dotted #999'),
			col.style	=list('font-weight:bold; border: 1px dotted #999'),
			cellpadding	=3,
			row.bgcolor	=list('lightgreen')
			))
	})

method(".listToColumn", "PortfolioRunReport", function(static, listData, ...){
	needs (listData='list')
	for (element in names(listData)) if(is.null(listData[[element]])) listData[[element]] <- "NULL"
	return(t(data.frame(listData)))
})

method(".formatNumbers", "PortfolioRunReport", function(static, x, ...){
	return(format(x,scientific=FALSE,big.mark=",",digits=5))	
})

method(".formatDollars", "PortfolioRunReport", function(static, data, ...){
	return(format(data,nsmall=0,scientific=FALSE,digits=1,justify="right",big.mark=","))	
})

method(".monthlyReturns", "PortfolioRunReport", function(this,...){
	equity <- this$.prun$portfolioCurveEquity('optimal', 'raw', 'slipped') + this$.aum
	equity.eom <- Interval$MONTHLY$collapse(equity,last)
	returns <- CalculateReturns(equity.eom, method="constantBase")
	colnames(returns)<- "Portfolio"
	return(returns)	
})

method(".monthlyChildReturns", "PortfolioRunReport", function(this,...){
	equity <- this$.prun$childCurvesEquity('optimal', 'raw', 'slipped') + this$.aum
	equity.eom <- Interval$MONTHLY$collapse(equity,last)
	returns <- CalculateReturns(equity.eom, method="constantBase")
	return(returns)	
})

method(".startEndRange", "PortfolioRunReport", function(this, ...){
	if (!is.null(this$.prun$startDate()) && !is.null(this$.prun$endDate()))
		return(format(seq(this$.prun$startDate(), this$.prun$endDate(),by="1 month"),"%m/%y"))
	return(NULL)
})
