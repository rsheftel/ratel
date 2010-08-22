# PnlReport Class
# 
# Author: rsheftel
###############################################################################


constructor("PnlReport", function(filename=NULL, pnlAnalysis=NULL){
	this <- extend(RObject(), "PnlReport", .filename = filename, .pnlAnalysis = pnlAnalysis)
	constructorNeeds(this, filename="character", pnlAnalysis="PnlAnalysis")
	if (inStaticConstructor(this)) return(this)
	this$.report <- Report('PnlReport',filename)	
	return(this)
})

method("underlying", "PnlReport", function(this, ...){
	this$.report
})


#############################################################################################################
#			Report Functions
#############################################################################################################

method("addDailyPnl", "PnlReport", function(this,pnlAnalysis,...){
	hwrite("Daily PnL", this$.report$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	
	for (source in pnlAnalysis$sources()){
		hwrite(squish("Source - ",source), this$.report$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
		data <- pnlAnalysis$dailyPnl(source=source,addTotal=TRUE)
		hwrite(HWriterUtils$dataTable(HWriterUtils$formatDollars(data.frame(data)), rowNames=TRUE, colNames=TRUE), this$.report$.conn, br=FALSE)
	}	
})

method("addDailyEquity", "PnlReport", function(this,pnlAnalysis,...){
	hwrite("Daily Equity", this$.report$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	
	for (source in pnlAnalysis$sources()){
		hwrite(squish("Source - ",source), this$.report$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
		data <- pnlAnalysis$dailyEquity(source=source,addTotal=TRUE)
		hwrite(HWriterUtils$dataTable(HWriterUtils$formatDollars(data.frame(data)), rowNames=TRUE, colNames=TRUE), this$.report$.conn, br=FALSE)
	}	
})

#############################################################################################################
#			Plot Methods
#############################################################################################################

method("addPlotEquity", "PnlReport", function(this,pnlAnalysis,...){
	for (source in pnlAnalysis$sources()){
		pnlAnalysis$plotEquity(source=source,addTotal=TRUE)
	}
})
