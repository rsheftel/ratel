# PnlReport Class
# 
# Author: rsheftel
###############################################################################

constructor("PnlDifferenceReport", function(filename=NULL, pnlAnalysis=NULL, sources=NULL){
	this <- extend(RObject(), "PnlDifferenceReport", .filename = filename, .pnlAnalysis = pnlAnalysis, .sources = sources)
	constructorNeeds(this, filename="character", pnlAnalysis="PnlAnalysis", sources="character")
	if (inStaticConstructor(this)) return(this)
	this$.makeFilenames()
	this$.openConnections()
	return(this)
})

#############################################################################################################
#			General Functions
#############################################################################################################
method("addTitle", "PnlDifferenceReport", function(this, ...){
	hwrite("Pnl Difference", this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

method("addPlotLink", "PnlDifferenceReport", function(this, ...){
	hwrite("Slippage Plots", this$.conn, link=squish('file:///',makeWindowsFilename(this$.filenames$pdf)))
})

method("addGroupTags", "PnlDifferenceReport", function(this, ...){
	hwrite("Group Tags", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	for (group in this$.pnlAnalysis$groupNames()){		
		tags <- this$.pnlAnalysis$groupTags(group)
		tags <- data.frame(t(tags))
		rownames(tags) <- group
		hwrite(HWriterUtils$dataTable(tags, rowNames=TRUE, colNames=FALSE), this$.conn, br=FALSE)
	}			
})

#############################################################################################################
#			Plot Methods
#############################################################################################################
method("addPlotSlippageCurves", "PnlDifferenceReport", function(this,groups = this$.pnlAnalysis$groupNames(),range = NULL,mainTitle = '',...){
	sources <- c('Hypo_Daily','GlobeOp')
	data <- this$.pnlAnalysis$pnlDifferenceZoo(groups,sources,replaceNA = 0,range = range)
	for (i in 1:length(data)){
		if(!all(as.numeric(data[[i]][,'Hypo_Daily'])==0)){		
			slippageCurve <- -getZooDataFrame(data[[i]][,'CumDiff'],colNames = names(data)[i])
			slippageCurve <- slippageCurve[as.numeric(slippageCurve)!=0]
			if(NROW(slippageCurve)>0)
				chart.TimeSeries(slippageCurve, ylab="Slippage", main=squish(names(data)[i],' - ',mainTitle), lwd=2,colorset=1:20)
		}
	}
})

#############################################################################################################
#			Report Functions
#############################################################################################################
method("addDifferenceGrid", "PnlDifferenceReport", function(this, range=NULL, ...){
	needs(range="Range?")
	hwrite("Difference Grid", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	if (!is.null(range)) hwrite(as.character(range), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	data <- this$.pnlAnalysis$pnlDifference(sources=this$.sources,range=range)
	hwrite(HWriterUtils$dataTable(HWriterUtils$formatDollars(data.frame(data)), rowNames=FALSE, colNames=TRUE), this$.conn, br=FALSE)
})

method("addDailyDifference", "PnlDifferenceReport", function(this, replaceNA=0, ...){
	hwrite("Daily Equity", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	
	diffList <- this$.pnlAnalysis$pnlDifferenceZoo(sources=this$.sources,replaceNA=replaceNA)
	failIf(all(is(diffList) != "list"),"Currently only works for more than one group.")
	for (group in names(diffList)){
		hwrite(squish("Group - ",group), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
		data <- diffList[[group]]
		hwrite(HWriterUtils$dataTable(HWriterUtils$formatDollars(data.frame(data)), rowNames=TRUE, colNames=TRUE), this$.conn, br=FALSE)
	}
})

#############################################################################################################
#			Support Functions
#############################################################################################################

method(".makeFilenames", "PnlDifferenceReport", function(this, ...){
	this$.filenames <- list()
	if(is.null(this$.filename)) this$.filename <- squish(tempDirectory(),'PnlDifferenceReport',format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0))
	this$.filenames$html <- this$.filename
	if(rightStr(this$.filenames$html,5)!='.html') this$.filenames$html <- squish(this$.filename,'.html')
	this$.filenames$pdf <- squish(leftStr(this$.filenames$html,nchar(this$.filenames$html)-5),'.pdf')
})

method(".openConnections", "PnlDifferenceReport", function(this, ...){
	this$.conn <- openPage(filename=this$.filenames$html, title=this$.filename)
	file.remove(this$.filenames$pdf)
	pdf(this$.filenames$pdf)
})

method("closeConnection", "PnlDifferenceReport", function(this, ...){
	closePage(this$.conn)
	dev.off()
	this$.conn <- NULL	
})

method("openReport", "PnlDifferenceReport", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$.filenames$html)
})

method("filenames", "PnlDifferenceReport",function(this, ...){
	return(this$.filenames)		
})
