constructor("WFOPlots", function() {
	this <- extend(RObject(),"WFOPlots")	
	if(inStaticConstructor(this)) return(this)
	this
})

method("plotOneOptimizedEquityCurve", "WFOPlots", function(this,wfo,step,optimalRunsMethod,...){
	needs(step = "numeric|integer",optimalRunsMethod = "character")
	simpleZooPlot(wfo$oneOptimizedEquityCurve(step,optimalRunsMethod),title = squish("Step: ",step))
	abline(v = as.POSIXct(wfo$schedule()[step,"startDate"]),col = "green")
	abline(v = as.POSIXct(wfo$schedule()[step,"endDate"]),col = "green")
	if(step < wfo$nbSteps())abline(v = as.POSIXct(wfo$schedule()[step+1,"endDate"]),col = "red")
	grid()
})

method("plotOptimizedEquityCurves", "WFOPlots", function(this,wfo,steps,optimalRunsMethod,...){
	needs(steps = "numeric|integer",optimalRunsMethod = "character")
	for(i in 1:NROW(steps)){
		if(((i-1) %% 9) == 0){
			windows()
			if(NROW(steps) <= 4)par(mfrow = c(2,2))
			else par(mfrow = c(3,3))
		}
		this$plotOneOptimizedEquityCurve(wfo,steps[i],optimalRunsMethod)
	}
})

method("plotOneEquityCurve", "WFOPlots", function(this,
	wfo,curveName,optimalRunsMethod,equityCurveFunction = ec.untilNextOptimization
,...){
	needs(curveName = "character",optimalRunsMethod = "character",equityCurveFunction = "function")
	eqCurve <- getZooDataFrame(wfo$equityCurve(curveName,optimalRunsMethod,equityCurveFunction,...))
	colnames(eqCurve) <- 'Stitched Equity'
	simpleZooPlot(eqCurve)
	grid()
})

method("plotCurves", "WFOPlots", function(this,wfo,curveNames,...){
	needs(curveNames = "character")
	curveList <- NULL
	for(i in 1:NROW(curveNames))
		curveList[[i]] <- wfo$readStitchedCurve(curveNames[i])
	merged <- do.call(merge,curveList)
	simpleZooPlot(merged)
	grid()
})