# STOSetupReport Class
# 
# Author: RSheftel
###############################################################################

constructor("STOSetupReport", function(stoSetup=NULL, filename=NULL){
	this <- extend(RObject(), "STOSetupReport", .sto = stoSetup, .filename=filename)
	constructorNeeds(this, stoSetup="STOSetup", filename="character?")
	if (inStaticConstructor(this)) return(this)
	this$.makeFilename()
	this$.openConnections()
	return(this)
})

method("reportAll", "STOSetupReport", function(this, ...){
	this$addTitle()
	this$addSettings()
	this$addMarkets()
	this$addPortfolioGrid()
	this$addParameters()	
})

method("addTitle", "STOSetupReport", function(this, ...){
	hwrite("STOSetup Report", this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

method("addSettings", "STOSetupReport", function(this, ...){
	data <- list(	System=this$.sto$system(),
					Interval=this$.sto$interval(),
					Version=this$.sto$version(),
					STODir=this$.sto$stoDirectory(),
					STOID=this$.sto$stoID(),
					SystemClass=this$.sto$strategyClass(),
					QClassName=this$.sto$systemQClassName())
	if (length(this$.sto$systemID()>0)) data <- appendSlowly(data,list(SystemID=this$.sto$systemID()))
	hwrite("Settings", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(HWriterUtils$listToColumn(data), rowNames=TRUE, colNames=FALSE), this$.conn, br=FALSE)
})

method("addMarkets", "STOSetupReport", function(this, ...){
	markets <- this$.sto$markets()
	names(markets) <- markets
	startDates <- this$.sto$startEndDates()$start
	endDates <- this$.sto$startEndDates()$end
	data <- data.frame(merge(zoo(markets,names(markets)),zoo(startDates,names(startDates)),zoo(endDates,names(endDates))))
	colnames(data) <- c('Market','StartDate','EndDate')
	hwrite("Markets", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(data, rowNames=FALSE, colNames=TRUE), this$.conn, br=FALSE)
})


method("addPortfolioList", "STOSetupReport", function(this, ...){
	portfolios <- this$.sto$portfolios()
	hwrite("Portfolios", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	for (portfolio in names(portfolios)){
		hwrite(portfolio, this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
		data <- data.frame(portfolios[[portfolio]])
		hwrite(HWriterUtils$dataTable(data, rowNames=FALSE, colNames=FALSE), this$.conn, br=FALSE)
	}
})

method("addPortfolioGrid", "STOSetupReport", function(this, ...){
	portfolios <- this$.sto$portfolios()
	data <- data.frame(do.call(merge, lapply(portfolios,zoo)))
	colnames(data) <- names(portfolios)
	hwrite("Portfolios", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(data, rowNames=FALSE, colNames=TRUE), this$.conn, br=FALSE)	
})

method("addParameters", "STOSetupReport", function(this, ...){
	data <- this$.sto$parameters()
	hwrite("Parameters", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(data, rowNames=FALSE, colNames=TRUE, formatNumbers=TRUE), this$.conn, br=FALSE)	
})

#############################################################################################################
#			Support Functions
#############################################################################################################
method("filename", "STOSetupReport",function(this, ...){
	return(this$.filename)		
})

method(".makeFilename", "STOSetupReport", function(this, ...){
	if(is.null(this$.filename))
		this$.filename <- squish(tempDirectory(),'STOSetupReport',format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0),'.html')
	if(rightStr(this$.filename,5)!='.html') this$.filename <- squish(this$.filename,'.html')
})

method(".openConnections", "STOSetupReport", function(this, ...){
	this$.conn <- openPage(filename=this$.filename, title=this$.filename)
})

method("closeConnection", "STOSetupReport", function(this, ...){
	closePage(this$.conn)
	this$.conn <- NULL	
})

method("openReport", "STOSetupReport", function(this, reportAll=FALSE, ...){
	if(reportAll) this$reportAll()
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$.filename)
})
