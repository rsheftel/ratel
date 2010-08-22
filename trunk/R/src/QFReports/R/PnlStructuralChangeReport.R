constructor("PnlStructuralChangeReport", function(name = NULL,filename=NULL){
	this <- extend(RObject(), "PnlStructuralChangeReport")	
	if (inStaticConstructor(this)) return(this)
	this$.report <- Report(name,filename)	
	return(this)
})

method("underlying", "PnlStructuralChangeReport", function(this, ...){
	this$.report
})

method("addKSGrid", "PnlStructuralChangeReport", function(this,header,curvesA,curvesB,alternative,ksColnames,...){				
	hwrite(header, this$.report$.conn, heading=2, center=FALSE, style='font-family: sans-serif')	
	data <- PnlStructuralChange$getKSFrame(curvesA,curvesB,alternative,ksColnames) 
	hwrite(HWriterUtils$dataTable(data, rowNames=FALSE, colNames=TRUE), this$.report$.conn, br=FALSE)
})

method("addPlot", "PnlStructuralChangeReport", function(this,curvesA,curvesB,...){
	assert(length(curvesA) == length(curvesB))
	for(i in sequence(length(curvesA))){				
		getCurves <- function(curveList){res <- as.numeric(curveList[[i]]$pnl()); res[res!=0]}
		a <- getCurves(curvesA); b <- getCurves(curvesB)
		if(length(a)>0 && length(b)>0){			
			this$plotLiveBacktestCurves(curvesB[[i]]$equity(),curvesA[[i]]$equity(),title = names(curvesA)[i])
			cd.a <- ecdf(a); cd.b <- ecdf(b)
			plot.stepfun(cd.a,col.hor = 1,col.vert = 1,do.points = FALSE,main = names(curvesA)[i],ylab = 'CUMULATIVE DISTRIBUTION',xlab = 'PNL')
			plot.stepfun(cd.b,col.hor = 2,col.vert = 2,do.points = FALSE,add = TRUE)
			grid()
			legend('topleft',c('Live','BackTest'),col = 1:2,lty = c(1,1))
		}		
	}
})

method("plotLiveBacktestCurves", "PnlStructuralChangeReport", function(this,equityZooBacktest,equityZooLive,title,...){
	m <- merge(equityZooBacktest,equityZooLive)
	colnames(m) <- c('Backtest','Live')
	if(NROW(na.omit(m)) > 0){
		simpleZooPlot(m,title = title)
	}else{			
		pnlZooTotal <- rbind(diff(equityZooBacktest),diff(equityZooLive))
		equityZooTotal <- getCumTriFromDailyTri(pnlZooTotal,baseTri = 0)
		simpleZooPlot(equityZooTotal,title = title)
		abline(v = as.numeric(as.POSIXct(first(index(equityZooLive)))),col = 'red')
		abline(v = as.numeric(as.POSIXct(last(index(equityZooLive)))),col = 'red')
	}
})