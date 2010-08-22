constructor("PortfolioPnLReport", function(connection = NULL, curves = NULL, start = NULL, end = NULL) {
    this <- extend(RObject(), "PortfolioPnLReport", .connection= connection, .curves = curves, .start = start, .end = end)
    constructorNeeds(this, connection = "connection", curves = "list(PositionEquityCurve)")
    this
})

method("output", "PortfolioPnLReport", function(this,x,...) {
    if (any(class(x)=="character")) { 
		cat(x,"\n",file=this$.connection)
	}
	else { write.csv(x, quote=FALSE,file=this$.connection) }
})

method("report", "PortfolioPnLReport", function(this, weights, systemNames=NULL,...) {
    
	range <- Range(this$.start,this$.end)
	this$output("Weights:")
	for (i in seq_along(weights)) this$output(squish(names(this$.curves)[i],",",weights[i]))
	this$output("\n")
	systemResults <- list()	
	for (i in 1:length(this$.curves)) {
		resultCurve <- WeightedCurves(this$.curves[i],weights[i])
		result <- Interval$MONTHLY$collapse(pnl(resultCurve$curve()), sum)
		systemResults <- appendSlowly(systemResults,result)
	}
	names(systemResults) <- systemNames
	systemResults <- do.call(merge,systemResults)
	names(index(systemResults)) <- c()
	systemResults <- range$cut(systemResults)
	
	systemResults <- as.data.frame(systemResults)
    this$output(systemResults)
})


