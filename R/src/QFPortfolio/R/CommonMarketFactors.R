# Analysis of data versus Common Market Factors
# 
# Author: RSheftel
###############################################################################


constructor("CommonMarketFactors", function(addStandardFactors = TRUE){
    library(QFPairsTrading)
	this <- extend(RObject(), "CommonMarketFactors")
	if (inStaticConstructor(this)) return(this)
	constructorNeeds(this, addStandardFactors = "logical")
	
	#Set up initial variables
	this$.factors$names <- c()
	this$.factors$tickers <- c()
	this$.factors$sources <- c()
	this$.factors$containers <- c()
	if(addStandardFactors)this$addStandardFactors()
	return(this)
})

method("addStandardFactors", "CommonMarketFactors", function(this, ...){	
	standard.names <- c('10ySwapRate','CDX.IG5Y','5ySwapSpread','3m5yBpVolDaily','2y10yBpVolDaily')
	standard.tickers <- c('irs_usd_rate_10y_mid','cdx-na-ig_market_spread_5y_otr','irs_usd_spread_5y_1n','swaption_usd_3m5y_atm_payer_vol_bp_daily_mid','swaption_usd_2y10y_atm_payer_vol_bp_daily_mid')
	standard.sources <- c('internal','internal','internal','jpmorgan','jpmorgan')
	standard.containers <- c('tsdb','tsdb','tsdb','tsdb','tsdb')
	this$addFactors(standard.names, standard.tickers, standard.sources, standard.containers)	
})

method("addFactors", "CommonMarketFactors", function(this, factor.names, factor.tickers, factor.sources, factor.containers, ...){
	needs(factor.names='character', factor.tickers='character', factor.sources='character', factor.containers='character')	
	failIf(any(is.null(factor.containers)),'Factors require a container. For external factors set to "external"')	
	this$.factors$names <- c(this$.factors$names,factor.names)
	this$.factors$tickers <- c(this$.factors$tickers,factor.tickers)
	this$.factors$sources <- c(this$.factors$sources,factor.sources)
	this$.factors$containers <- c(this$.factors$containers, factor.containers)
})

method("addExternalFactors", "CommonMarketFactors", function(this, factor.names,factor.zoos,...){
	needs(factor.names='character', factor.zoos='zoo')    
    this$addFactors(factor.names,factor.names,rep("external",NROW(factor.names)),rep("external",NROW(factor.names)))
	factors.zoos <- getZooDataFrame(do.call(merge.zoo.null,list(this$.factors$zoo,factor.zoos)))
	colnames(factors.zoos) <- c(colnames(this$.factors$zoo),factor.names)
	this$.external$zoo <- factors.zoos 
})

method("loadFactors", "CommonMarketFactors", function(this, stripTimes=FALSE, ...){
	needs(stripTimes='logical')
	factorZoos <- list()
	for (count in seq_along(this$.factors$names)){
		if(this$.factors$containers[[count]]=="external"){
			factorZoos[[count]] <- this$.external$zoo[,this$.factors$names[count]] 
		}else{
			factorZoos[[count]] <- TSDataLoader$getDataByName(	container=this$.factors$containers[[count]],
																tickerName=this$.factors$tickers[[count]],
																source=this$.factors$sources[[count]],
																field='close') 
	   }
	}
	if (stripTimes) factorZoos <- lapply(factorZoos,strip.times.zoo)
	this$.factors$zoo <- getZooDataFrame(do.call(merge,factorZoos))
	colnames(this$.factors$zoo) <- this$.factors$names
})

method("stripTimes", "CommonMarketFactors", function(this, ...){
	this$.factors$zoo <- strip.times.zoo(this$.factors$zoo)
	this$.analysisVariable$zoo <- strip.times.zoo(this$.analysisVariable$zoo)	
})

method("factors", "CommonMarketFactors", function(this, ...){
    return(list(names=this$.factors$names, tickers=this$.factors$tickers, sources=this$.factors.sources))		
})

method("analysisVariable", "CommonMarketFactors", function(this, analysisZoo=NULL, variableName=NULL, ...){
	needs(analysisZoo='zoo?', variableName='character?')	
	if (is.null(analysisZoo)) return(this$.analysisVariable$zoo)
	if (is.null(variableName)) variableName <- 'Analysis Variable'		
	this$.analysisVariable$zoo <- getZooDataFrame(analysisZoo)
	colnames(this$.analysisVariable$zoo) <- variableName
})

method("generateReport", "CommonMarketFactors", function(this, outputFilename, ...){
	needs(outputFilename='character')
	if(rightStr(outputFilename,3)!='pdf') print('#Warning: output file is a pdf, extension should be set to .pdf')
	pdf(outputFilename)
	for (factor.name in this$.factors$names){
		this$plotTimeSeries(factor.name)
		this$plotScatter(factor.name)
	}
	dev.off()
	this$statisticsForFactors()
})

method("plotTimeSeries", "CommonMarketFactors", function(this, factor.name, ...){
	needs(factor.name='character')
	factor.zoo <- na.omit(this$.factors$zoo[,factor.name])
	if (!indiciesOverlap(this$.analysisVariable$zoo,factor.zoo)) return(squish('#Error: No overlapping dates for factor: ',factor.name))
	mergedZoo <- zoo(na.omit(merge(this$.analysisVariable$zoo, factor.zoo, all=FALSE)))

	origPar <- par(no.readonly=TRUE)
	on.exit(par(origPar))
		
	opar <- par(mai = c(.8, .8, .2, .8))
	plot(mergedZoo[,1],type="l",xlab="Date",ylab=colnames(this$.analysisVariable$zoo))
	par(new=TRUE)
	plot(mergedZoo[,2],type="l",ann=FALSE,yaxt='n',col='blue')
	Axis(side=4)
	legend(x='topleft',bty='n',lty=c(1,1),col=c('black','blue'),legend = paste(c(colnames(this$.analysisVariable$zoo),factor.name), c("(left scale)", "(right scale)")))
	usr <- par('usr')
	text(usr[2] + .1 * diff(usr[1:2]), mean(usr[3:4]), factor.name,srt = -90, xpd = TRUE, col = "blue")
	par(opar)
})

method("plotScatter", "CommonMarketFactors", function(this, factor.name, ...){
	needs(factor.name='character')
	factor.zoo <- na.omit(this$.factors$zoo[,factor.name])
	if (!indiciesOverlap(this$.analysisVariable$zoo,factor.zoo)) return(squish('#Error: No overlapping dates for factor: ',factor.name))
	mergedZoo <- zoo(na.omit(merge(this$.analysisVariable$zoo, factor.zoo, all=FALSE)))
	
	origPar <- par(no.readonly=TRUE)
	on.exit(par(origPar))
	
	plot(mergedZoo[,2],mergedZoo[,1],xlab=factor.name,ylab=colnames(this$.analysisVariable$zoo))
	panel.smooth(mergedZoo[,2],mergedZoo[,1])
	factor.stats <- this$statisticsForFactor(factor.name)
	cor.levels <- format(factor.stats[['levels.cor']],digits=2)
	cor.changes <- format(factor.stats[['changes.cor']],digits=2)
	title (main=squish('Correlation: Levels=',cor.levels,', Changes=',cor.changes))
})

method("statisticsForFactor", "CommonMarketFactors", function(this, factor.name,calculate.percent.returns = FALSE,...){
	needs(factor.name='character', calculate.percent.returns='logical')
	
	stat.df <- list()
	factor.zoo <- na.omit(this$.factors$zoo[,factor.name])
	
	if (!indiciesOverlap(this$.analysisVariable$zoo,factor.zoo)) {
		noData <- c(NA)
		names(noData) <- factor.name
		return(noData)
	}
	
	mergedZoo <- zoo(na.omit(merge(this$.analysisVariable$zoo, factor.zoo, all=FALSE)))
	mergedZooScholesWilliam <- zoo(na.omit(merge(this$.analysisVariable$zoo,factor.zoo,lag(factor.zoo,-1),lag(factor.zoo,1),all=FALSE)))
	
	#Levels statistics
	stat.df <- c(stat.df, count=NROW(mergedZoo))
	stat.df <- c(stat.df, levels.cor=cor(mergedZoo)[1,2])
    	
	#Change statistics	
	dailyChangesZoo <- getChangesZooCMF(mergedZoo,1,calculate.percent.returns)
    weeklyChangesZoo <- getChangesZooCMF(mergedZoo,5,calculate.percent.returns)
    dailyChangesZooScholesWilliam <- getChangesZooCMF(mergedZooScholesWilliam,1,calculate.percent.returns)
    dailyChangesZooScholesWilliam <- merge(dailyChangesZooScholesWilliam[,1:2],dailyChangesZooScholesWilliam[,2] + dailyChangesZooScholesWilliam[,3] + dailyChangesZooScholesWilliam[,4])
    
    stat.df <- c(stat.df, weekly.changes.cor=cor(weeklyChangesZoo)[1,2])	
    stat.df <- c(stat.df, weekly.changes.beta=summary(lm(weeklyChangesZoo[,1] ~ weeklyChangesZoo[,2]))$coefficients[2,"Estimate"])
	stat.df <- c(stat.df, daily.changes.cor=cor(dailyChangesZoo)[1,2])	
	stat.df <- c(stat.df, daily.changes.beta = summary(lm(dailyChangesZoo[,1] ~ dailyChangesZoo[,2]))$coefficients[2,"Estimate"])
	stat.df <- c(stat.df, scholes.william.beta = cov(dailyChangesZooScholesWilliam)[1,3]/cov(dailyChangesZooScholesWilliam)[2,3])	
	stat.df <- c(stat.df, signTest.percentMatching=HypothesisTests$signTest(dailyChangesZoo)$estimate[[1]])
	stat.df <- c(stat.df, signTest.pValue=HypothesisTests$signTest(dailyChangesZoo)$p.value)
	
	stat.df <- round(data.frame(stat.df),3)
	rownames(stat.df) <- factor.name
	return(stat.df)
})

method("statisticsForFactors", "CommonMarketFactors", function(this,calculate.percent.returns = FALSE,...){
	needs(calculate.percent.returns='logical')
	this$.statistics <- data.frame()
	for (factor.name in this$.factors$names){
		this$.statistics <- rbind(this$.statistics, this$statisticsForFactor(factor.name,calculate.percent.returns))
	}
	if(calculate.percent.returns) print("Overlapping Percent Changes") else print("Overlapping Changes")
	return(this$.statistics)
})

getChangesZooCMF <- function(zoo,lag,calculate.percent.returns){
    if(calculate.percent.returns){
        for(i in 1:ncol(zoo))zoo <- zoo[zoo[,i] != 0]
        diff(zoo, lag=lag, differences=1,arithmatic=TRUE,na.pad=FALSE)/lag(zoo,-lag)
    }else{
        diff(zoo, lag=lag, differences=1,arithmatic=TRUE,na.pad=FALSE) 
    }
}

method("getFactorZoo", "CommonMarketFactors", function(this, factor.names, ...){
	return(this$.factors$zoo[,factor.names])	
})