constructor("OptimizationReport", function(connection = NULL, curves = NULL, varConstraint = NULL) {
    this <- extend(RObject(), "OptimizationReport", .connection = connection, .curves = curves, .varConstraint = varConstraint,
			.metricList = list(AnnualizedNetProfit,CalmarRatio,KRatio,MaxDrawDown,ConditionalTenPercentileDrawDown,ConditionalTwentyPercentileDrawDown,OmegaRatio,UpsidePotentialRatio,
		SortinoRatio,SharpeRatioWeekly))
    constructorNeeds(this, connection = "connection", curves = "list(PositionEquityCurve)", varConstraint = "WeightedCurvesConstraint")
    this
})

method("current", "OptimizationReport", function(this, weights, ...) {
    cat("\n", "Current Portfolio Weightings","\n",file=this$.connection)
    this$report(weights)
})

method("optimal", "OptimizationReport", function(this, weights, metric, ...) {
    cat("\n", "Result of Optimization:",as.character(metric),"\n",file=this$.connection)
    this$report(weights)
})

method("output", "OptimizationReport", function(this, ...) {
    cat(..., "\n", file=this$.connection)
})

method("report", "OptimizationReport", function(this, weights, ...) {
    wc <- WeightedCurves(this$.curves,weights)
    portfolio <- wc$curve()
    this$output("Weights:")
    for (i in seq_along(weights)) this$output(names(this$.curves)[i],",",weights[i])
    this$output()
	for (metric in this$.metricList) {
		this$output(as.character(metric),",",portfolio$metric(metric))
	}
	this$sensitivity(weights)
    this$.varConstraint$report(wc, this$.connection)
})

method("sensitivity","OptimizationReport",function(this, weights,...) 
	{
		for (metric in this$.metricList) {
			for (i in seq_along(weights)){
				
				pertWeights <- weights
				base <- WeightedCurves(this$.curves,pertWeights)$curve()$metric(metric)
				
				pertWeights[i] <- weights[i] * 1.01
				up <- WeightedCurves(this$.curves,pertWeights)$curve()$metric(metric)
			
				pertWeights <- weights
				pertWeights[i] <- weights[i] * 0.99
				down <- WeightedCurves(this$.curves,pertWeights)$curve()$metric(metric)
			
			 	dur <- (up - down)/(base * 2 * 0.01* weights[i])
				this$output(as.character(metric),",",names(this$.curves[i]),",",dur)
			}
			
		}
	})