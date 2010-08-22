constructor("TradeCube", function(pnlFrame = NULL,barsInTradeFrame = NULL,dateFrame = NULL,entrySizeFrame = NULL) {
	this <- extend(RObject(), "TradeCube")
	constructorNeeds(this,pnlFrame = 'zoo',barsInTradeFrame = 'zoo',dateFrame = 'zoo',entrySizeFrame = 'zoo')
	if(inStaticConstructor(this)) return(this)
	assert(NROW(pnlFrame)==NROW(barsInTradeFrame))
	assert(NROW(barsInTradeFrame)==NROW(dateFrame))
	assert(NROW(entrySizeFrame)==NROW(dateFrame))
	assert(NCOL(pnlFrame)==NCOL(dateFrame))
	assert(NCOL(barsInTradeFrame)==NCOL(entrySizeFrame))
	this$.pnlFrame <- TradeCubeBuilder$formatZooFrame(pnlFrame)	
	this$.barsInTradeFrame <- TradeCubeBuilder$formatZooFrame(barsInTradeFrame)
	this$.dateFrame <- TradeCubeBuilder$formatZooFrame(dateFrame)
	this$.entrySizeFrame <- TradeCubeBuilder$formatZooFrame(entrySizeFrame)	
	this
})

method("conditionalPnlFrame", "TradeCube", function(this,fromThisBar = TRUE,...) {
	if(!is.null(this$.conditionalPnlFrameFromThisBar) && fromThisBar) return(this$.conditionalPnlFrameFromThisBar)
	if(!is.null(this$.conditionalPnlFrameToThisBar) && !fromThisBar) return(this$.conditionalPnlFrameToThisBar)	
	tempFrame <- this$.pnlFrame
	result <- tempFrame
	if(NCOL(tempFrame) > 1){
		for (i in 2:NCOL(tempFrame)) tempFrame[,i] <- tempFrame[,i] + tempFrame[,i-1]
		if(fromThisBar){							
			for (i in 2:NCOL(tempFrame)) result[,i] <- tempFrame[,NCOL(tempFrame)] - tempFrame[,i-1]
			result[,1] <- tempFrame[,NCOL(tempFrame)]
		}else result <- tempFrame
	}	
	if(fromThisBar)
		this$.conditionalPnlFrameFromThisBar <- this$pnlFrameWithNAs(result)
	else this$.conditionalPnlFrameToThisBar <- result
})

method("filterFrame", "TradeCube", function(this,...) {
	if(is.null(this$.filterFrame)){	
		this$.filterFrame <- data.frame(tradeNumber = as.numeric(rownames(this$.pnlFrame)),entryDate = as.character(this$.dateFrame[,1]),entrySizeFrame = as.numeric(this$.entrySizeFrame),barsInTrade = as.numeric(this$.barsInTradeFrame),this$.pnlFrame,stringsAsFactors = FALSE)
		colnames(this$.filterFrame) <- c('tradeNumber','entryDate','entrySize','barsInTrade',paste('bar',colnames(this$.pnlFrame),sep = ''))
	}
	this$.filterFrame	
})

method("filter", "TradeCube", function(this,expr,...) {
	filterFrame <- this$filterFrame()
	tradeNumbers <- filterFrame$tradeNumber[eval(eval(substitute(expression(expr))), filterFrame)]
	if(NROW(tradeNumbers) == 0)throw('No data for passed expression!')
	boolCols <- 1:max(this$.barsInTradeFrame[tradeNumbers])
	TradeCube(this$.pnlFrame[tradeNumbers,boolCols],this$.barsInTradeFrame[tradeNumbers],this$.dateFrame[tradeNumbers,boolCols],this$.entrySizeFrame[tradeNumbers])
})

method("mainMessage", "TradeCube", function(this,conditional,qTrim,...) {
	squish('qTrim = ',qTrim,', conditional = ',conditional)
})

method("summaryCharacters", "TradeCube", function(this,...) {
	c(
		squish('Cumulative PNL = ',this$format(sum(this$.pnlFrame)),'\n'),
		squish('# Trades = ',this$format(NROW(this$.pnlFrame)),'\n'),
		squish('Entry Date Start = ',min(as.character(this$.dateFrame[,1])),'\n'),
		squish('Entry Date End = ',max(as.character(this$.dateFrame[,1])),'\n')
	)
})

method("format", "TradeCube", function(this,num,round = 0,...) {
	as.character(format(round(as.numeric(num),round), big.mark=",", sci=FALSE))
})

method("formatPercent", "TradeCube", function(this,num,...) {
	squish(round(100 * num,2),'%')
})

method("yTicks", "TradeCube", function(this,y,...) {
	pretty(c(min(y), max(y)))
})

method("addAxis", "TradeCube", function(this,x,xPlot = TRUE,yPlot = TRUE,...) {
	if(xPlot)axis(1, this$format(x))
	if(yPlot){
		a = axis(2,labels = FALSE)
		axis(2,a,labels = this$format(a))
	}
})

method("pnlFrameWithNAs", "TradeCube", function(this,pnlFrame,...) {
	for(i in 1:NCOL(pnlFrame)) pnlFrame[this$.dateFrame[,i]==0,i] <- NA
	pnlFrame
})

method("trimFrame", "TradeCube", function(this,frame,qTrim = 0,...) {
	qMin <- qTrim
	qMax <- 1 - qTrim
	for(i in 1:NCOL(frame)){		
		qMinData <- as.numeric(quantile(na.omit(as.numeric(frame[,i])),qMin))
		qMaxData <- as.numeric(quantile(na.omit(as.numeric(frame[,i])),qMax))
		frame[frame[,i] < qMinData | frame[,i] > qMaxData,i] <- NA
	}
	return(frame)
})

method("lwd", "TradeCube", function(this,nCols,...) {
	if(nCols < 20)return(10)
	return(5)
})

method("prepareFrame", "TradeCube", function(this,conditional,qTrim,fromThisBar = TRUE,...) {
	if(conditional)frame <- this$conditionalPnlFrame(fromThisBar)
	else frame <- this$pnlFrameWithNAs(this$.pnlFrame)
	return(this$trimFrame(frame,qTrim = qTrim))
})

method("tradeLengthDistribution", "TradeCube", function(this,freq = FALSE,...) {
	par(mfrow = c(1,1))
	y <- as.numeric(this$.barsInTradeFrame)
	
	r <- hist(y,main = 'Trade Length Distribution',xlab = 'BARS',freq = freq, col="gray",breaks = length(unique(y)),include.lowest = TRUE)	
	abline(v = mean(y),col = 'red', lwd = 1,lty = 2)
	text(mean(y),0.025,squish('Mean = ',round(mean(y),2)),col='red',pos = 4,cex = 1)
	grid()	
})

method("tradePnlDistribution", "TradeCube", function(this,freq = TRUE,...) {
	frame <- this$conditionalPnlFrame()
	this$.plotDistribution(
		y = sort(as.numeric(frame[,1])),
		xlab = 'Trade PNL',
		main = 'Trade PnL Distribution',
		colLeft = "red",
		colRight = "green",
		freq = freq
	)	
})

method("entrySizeDistribution", "TradeCube", function(this,freq = TRUE,...) {
	this$.plotDistribution(
		y = abs(as.numeric(this$.entrySizeFrame)),
		xlab = 'Entry Size',
		main = 'Entry Size Distribution',
		colLeft = "lightgrey",
		colRight = "lightgrey",
		freq = freq
	)
})

method(".plotDistribution", "TradeCube", function(this,y,xlab,main,colLeft,colRight,freq,...) {
	par(mfrow = c(1,1))	
	max <- max(y); min = min(y)
	breaks <- seq(min, max, (max - min) / 100)		
	color <- function(end,stats,colLeft,colRight) {
		if(end < stats[2])return(colLeft)		
		if(end > stats[4])return(colRight)		
		return('darkgrey')
	}	
	b <- boxplot(y, plot=FALSE)
	h <- hist(y, breaks=breaks, plot=FALSE)
	colors <- sapply(seq_along(h$counts), function(c) color(h$breaks[[c+1]], b$stats,colLeft,colRight))
	r <- hist(y,xlab = xlab,freq = freq, col=colors,breaks = breaks,include.lowest = TRUE,main = main)	
	abline(v = 0,col = 1, lwd = 2,lty = 2)
	grid()
	yNorm <- (y - mean(y))/sd(y)		
	legend('topleft',squish(
		'min      = ',this$format(min), ' \n',
		'q25%  = ',this$format(b$stats[2]), ' \n',
		'q50%  = ',this$format(b$stats[3]), ' \n',		
		'q75%  = ',this$format(b$stats[4]), ' \n',
		'max     = ',this$format(max), ' \n',
		'count   = ',NROW(y), ' \n',
		' \n',
		'mean   = ',this$format(mean(y)), ' \n',
		'sigma  = ',this$format(sd(y)), ' \n',
		'skew.   = ',this$format(skewness(yNorm),2), ' \n',
		'kurt.     = ',this$format(kurtosis(yNorm),2), ' \n'		
	)
	,bty = 'n')		
})



method("expectancyPlot", "TradeCube", function(this,func = mean,fromThisBar = TRUE,title = 'Average Conditional / Unconditional PNL',...) {	
	par(mfrow = c(1,1))
	conditional <- this$prepareFrame(TRUE,0,fromThisBar)
	unconditional <- this$prepareFrame(FALSE,0,fromThisBar)	
	stats <- data.frame(funcConditional = NULL,funcUnconditional = NULL)
	cols <- NULL
	for(i in 1:NCOL(conditional)){		
		stats <- rbind(stats,data.frame(
			funcConditional = func(na.omit(as.numeric(conditional[,i]))),
			funcUnconditional = func(na.omit(as.numeric(unconditional[,i])))
		))		
		cols <- c(cols,ifelse(stats[i,1] > 0,'green','red'),ifelse(stats[i,2] > 0,'darkgreen','darkred'))
	}
	rownames(stats) <- colnames(this$.pnlFrame)
	
	barplot(t(stats),beside = TRUE,main = title,xlab = 'BARS',ylab = 'PNL',yaxt = 'n',col = cols)	
	par(new = TRUE)
	this$addAxis(x,FALSE,TRUE)
	abline(0,0)
	grid()	
})

method("boxPlot", "TradeCube", function(this,conditional = FALSE,qTrim = 0,title = this$mainMessage(conditional,qTrim),...) {		
	frame <- this$prepareFrame(conditional,qTrim)
	bx <- boxplot(data.frame(frame), main = title,col = 'gray',names = colnames(frame),xlab = 'BARS',ylab = 'PNL',outline = TRUE,xaxt = 'n',yaxt = 'n')	
	for(i in 1:NROW(bx$n))text(i,bx$stats[3,i],round(bx$stats[3,i],0),pos = 1,cex = 0.75,col = 'black')
	for(i in 1:NROW(bx$n))text(i,bx$stats[3,i],bx$n[i],pos = 3,cex = 0.75,col = 'red')
		
	this$addAxis(1:NROW(bx$n))
	grid(NA, NULL)
	legend('topleft',legend = c('Count','Median'),col = c(2,1),text.col = c(2,1),cex = 1,bty = 'n')
})

method("whiskerPlot", "TradeCube", function(this,conditional = FALSE,qTrim = 0,title = this$mainMessage(conditional, qTrim),...) {
	frame <- this$prepareFrame(conditional,qTrim)
	values <- NULL
	for (i in 1:NCOL(frame)) values[[i]] <- as.numeric(na.omit(frame[,i]))
	names(values) <- colnames(frame)
	
	min <- min(sapply(values, min))
	max <- max(sapply(values, max))
	step <- (max - min) / 100
	yTicks <- pretty(c(min, max))
	yLabels <- format(pretty(c(min, max)), big.mark=",", sci=FALSE)
	maxLenX <- max(sapply(names(values), nchar))
	maxLenY <- max(sapply(yLabels, nchar))
	
	breaks <- seq(min, max, step)	
	
	color <- function(start, end, stats) {
		median <- stats[[3]]
		if(median >= start && median < end)
			return("black")
		if(end <= stats[[1]] || start >= stats[[5]])
			return("yellow")
		if(end <= stats[[2]] || start >= stats[[4]])
			return("orange")
		return("red")
	}
	plot(0,yaxt = 'n',xaxt = 'n',ylab = 'PNL',xlab = 'BARS IN TRADE',col = 'white',main = title)
	plot.window(c(0.5,length(values)+0.5), c(min, max))
	axis(2, yTicks, yLabels, cex.axis=0.65)
	grid(NA, NULL)
	b <- boxplot(values, plot=FALSE)
	
	for(i in seq_along(values)) {
		par(new = TRUE)
		h <- hist(values[[i]], breaks=breaks, plot=FALSE)
		colors <- sapply(seq_along(h$counts), function(c) color(h$breaks[[c]], h$breaks[[c+1]], b$stats[,i]))
		barplot(
				h$counts/max(h$counts)*0.8, 
				horiz=TRUE, 
				axes=FALSE, 
				xlim=c(-1, length(values)), 
				ylim=c(0, max-min), 
				space=0,
				col=colors,
				width=h$breaks[2]-h$breaks[1],
				offset=i-1
		)
	}
	axis(1, seq_along(values)-1, names(values), cex.axis=0.65)
	box()
})

method("pdfReport", "TradeCube", function(this,pdfPath,...) {
	pdf(pdfPath,paper="special",width=10,height=10)
		this$expectancyPlot(mean,fromThisBar = TRUE,title = 'Average PnL Starting from, and Including, this Bar through the last bar')
		this$expectancyPlot(sum,fromThisBar = TRUE,title = 'Cumulative PnL Starting from, and Including, this Bar through the last bar')
		this$expectancyPlot(mean,fromThisBar = FALSE,title = 'Average PnL From the start through this bar')
		this$expectancyPlot(sum,fromThisBar = FALSE,title = 'Cumulative PnL From the start through this bar')		
		this$boxPlot(conditional = FALSE,qTrim = 0,title = 'Unconditional')
		this$whiskerPlot(conditional = FALSE,qTrim = 0,title = 'Unconditional')
		this$tradePnlDistribution()
		this$entrySizeDistribution()
		this$tradeLengthDistribution()
	dev.off()
})