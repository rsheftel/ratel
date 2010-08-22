constructor("TradeCubeBuilder", function(sto = NULL,reverseDateLogic = NULL) {
	library("SystemDB")
	this <- extend(RObject(), "TradeCubeBuilder",.sto = sto,.reverseDateLogic = reverseDateLogic)
	constructorNeeds(this, sto = "STO",reverseDateLogic = 'character')
	if(inStaticConstructor(this)) return(this)
	assert(reverseDateLogic %in% c('closeSystemAdjustment','50/50'))
	this
})

method(".loadOnePositionEquityCurve", "TradeCubeBuilder", function(this,msiv,run,...) {
	CurveFileLoader$readBin(file = CurveCube$filename(squish(this$.sto$.dir,'/CurvesBin'), msiv, run))
})

method(".stripTimesIfNotIntraday", "TradeCubeBuilder", function(this,pe,...) {	
	.pe <- strip.times.zoo(pe)
	if(NROW(unique(index(.pe))) == NROW(pe)) .pe else pe
})

method(".buildOneTradeCube", "TradeCubeBuilder", function(this,msiv,run,pe = NULL,...) {
				
	if(is.null(pe))pe <- this$.loadOnePositionEquityCurve(msiv,run)	
	pe <- this$.stripTimesIfNotIntraday(pe)
	peIndex <- index(pe)
		
	if(all(is.na(pe[,'position'])))
		throw(squish(msiv$.market,' is a portfolio (no position data available)!'))
	
	if(this$.reverseDateLogic == 'closeSystemAdjustment')
		slippage <- as.numeric(SystemDB$getFieldForName('Slippage','Market',msiv$.market))
	
	peMergeSign <- sign(merge(pe[,'position'],lag(pe[,'position'],-1)))
	signChangeBool <- peMergeSign[,1] != peMergeSign[,2]
	entryDates <- index(peMergeSign[signChangeBool & peMergeSign[,1] != 0,])
	if(peMergeSign[1,1] == 1)entryDates <- c(first(index(peMergeSign)),entryDates) 
	exitDates <- index(peMergeSign[signChangeBool & peMergeSign[,2] != 0,])
	reverseDates <- index(peMergeSign[signChangeBool & peMergeSign[,1] != 0 & peMergeSign[,2] != 0,])  
	nTrades <- NROW(entryDates); if(nTrades == 0)return(NULL)
	
	lastTradeDates <- exitDates
	if(nTrades > NROW(exitDates)) lastTradeDates[nTrades] <- last(index(peMergeSign))
				
	peCutList <- lapply(1:nTrades,function(x)pe[peIndex>=entryDates[x] & peIndex<=lastTradeDates[x]])	
	curvesSizeVec <- sapply(1:nTrades,function(x) NROW(peCutList[[x]]))
	firstEntrySizeVec <- sapply(1:nTrades,function(x) first(peCutList[[x]][,'position']))
	characterDatesVec <- sapply(1:nTrades,function(x) as.character(index(peCutList[[x]][,'pnl'])))
	curvesLastIndices <- sapply(1:nTrades,function(x) last(index(peCutList[[x]])))
	maxBarsInTrades <- sapply(1:nTrades,function(x)max(curvesSizeVec[1:x]))
	entryDatesInReverseDates <- entryDates %in% reverseDates	
	
	if(this$.reverseDateLogic == 'closeSystemAdjustment'){
		reverseDateAdjustment <- function(x){- abs(first(peCutList[[x]][,'position'])*slippage)}
		reverseExitDateAdjustment <- function(peCut,x){			
			peCut[NROW(peCut),'pnl'] + abs(peCut[entryDates[x+1],'position'] * slippage)
		}
	}else{
		reverseDateAdjustment <- function(x){0.5 * peCutList[[x]][1,'pnl']}
		reverseExitDateAdjustment <- function(peCut,x){0.5 * peCut[NROW(peCut),'pnl']}
	}
	
	for(i in (1:nTrades)[entryDatesInReverseDates])
		peCutList[[i]][1,'pnl'] <- reverseDateAdjustment(i)
				
	bool <- !((entryDates == lastTradeDates) & entryDates == curvesLastIndices) | this$.reverseDateLogic != 'closeSystemAdjustment'
	lastTradeDatesInReverseDates <- lastTradeDates %in% reverseDates & bool
	
	for(i in (1:nTrades)[lastTradeDatesInReverseDates])
		peCutList[[i]][curvesSizeVec[[i]],'pnl'] <- reverseExitDateAdjustment(peCutList[[i]],i)
	
	# Build TradeCube
	
	pnlFrame <- this$formatZooFrame(zoo(t(data.frame(rep(0,last(maxBarsInTrades)))),1:nTrades))	
	dateFrame <- pnlFrame	
	for(i in 1:nTrades){
		pnlFrame[i,1:curvesSizeVec[[i]]] <- as.numeric(peCutList[[i]][,'pnl'])
		dateFrame[i,1:curvesSizeVec[[i]]] <- characterDatesVec[[i]]		
	}	
	TradeCube(pnlFrame,zoo(curvesSizeVec,1:nTrades),dateFrame,zoo(firstEntrySizeVec,1:nTrades))
})

method("run", "TradeCubeBuilder", function(this,msivs, runs,...) {
	needs(msivs="list(MSIV)", runs="numeric")
	checkLength(runs, length(msivs))
	isNotInitialized <- TRUE
	for(i in 1:length(msivs)){		
		print(squish('Working on ',msivs[[i]]$.market, ', run number ',runs[i]))
		tradeCube <- this$.buildOneTradeCube(msivs[[i]],runs[i])
		if(!is.null(tradeCube)){
			if(isNotInitialized){			
				pnlFrameComb <- tradeCube$.pnlFrame
				barsInTradeFrameComb <- tradeCube$.barsInTradeFrame
				dateFrameComb <- tradeCube$.dateFrame
				entrySizeFrameComb <- tradeCube$.entrySizeFrame			
				isNotInitialized <- FALSE
			}else{
				pnlFrameComb <- this$combineFrames(pnlFrameComb,tradeCube$.pnlFrame)
				barsInTradeFrameComb <- this$combineFrames(barsInTradeFrameComb,tradeCube$.barsInTradeFrame)
				dateFrameComb <- this$combineFrames(dateFrameComb,tradeCube$.dateFrame)
				entrySizeFrameComb <- this$combineFrames(entrySizeFrameComb,tradeCube$.entrySizeFrame)
			}
		}
	}
	return(TradeCube(pnlFrameComb,barsInTradeFrameComb,dateFrameComb,entrySizeFrameComb))
})

method("runMultipleRunsForMsivList", "TradeCubeBuilder", function(this,msivs, runs,...) {
	needs(msivs="list(MSIV)", runs="numeric")
	msivVec <- NULL
	for (i in 1:NROW(runs))msivVec <- c(msivVec,msivs)	
	this$run(msivs = as.list(msivVec),runs = as.numeric((sapply(runs,function(x){rep(x,NROW(msivs))}))))
})

method("formatZooFrame", "TradeCubeBuilder", function(this,zooFrame,columnResize = NULL,...) {
	if(is.null(dim(zooFrame))) zooFrame <- zoo(data.frame(zooFrame))	
	if(!is.null(columnResize)){
		zooFrameToAdd <- zoo(t(data.frame(rep(0,columnResize))),1:NROW(zooFrame))
		zooFrame <- cbind(zooFrame,zooFrameToAdd)
	}
	rownames(zooFrame) <- 1:NROW(zooFrame)
	colnames(zooFrame) <- 1:NCOL(zooFrame)
	zooFrame
})

method("combineFrames", "TradeCubeBuilder", function(this,frameOne,frameTwo,...) {
	nCols <- max(NCOL(frameOne),NCOL(frameTwo))
	if(NCOL(frameOne) < nCols) frameOne <- this$formatZooFrame(frameOne,nCols - NCOL(frameOne))
	if(NCOL(frameTwo) < nCols) frameTwo <- this$formatZooFrame(frameTwo,nCols - NCOL(frameTwo))
	index(frameTwo) <- (NROW(frameOne) + 1):(NROW(frameOne) + NROW(frameTwo))
	this$formatZooFrame(zoo(data.frame(rbind(frameOne,frameTwo))))
})