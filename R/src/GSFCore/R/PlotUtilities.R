sigDigitExponent <- function(x) {
    # kludge to pull out significand and exponent
    as.numeric(the(strsplit(format(signif(x, 1), sci=TRUE), "e")))
}

calcTickMarks <- function(x) {
    max <- max(x)
    min <- min(x)
    totalSize <- max - min
    theoStepSize <- totalSize/10
    numParts <- sigDigitExponent(theoStepSize)
    stepSize <- switch(first(numParts), 1, 2, 5, 5, 5, 10, 10, 10, 10) * 10^second(numParts)
    result <- vector("numeric", 20)
    count <- 1
    result[[count]] <- min
    count <- count + 1
    numParts <- sigDigitExponent(min)
    tick <- (first(numParts)-1) * 10^second(numParts)
    while (tick <= min) tick <- tick + stepSize
    while (tick < max) {
        if(isTRUE(all.equal(tick, max))) break
        result[[count]] <- tick
        count <- count + 1
        tick <- tick + stepSize
    }

    result[[count]] <- max

    result[1:count]
}

simpleMultiLinePlot <- function(rawX, rawY, rawZ, viewAlong, xName, yName) {
    x <- switch(viewAlong, x=rawY, y=rawX)
    data <- rawZ
    if(viewAlong=="x")
        data <- t(data)
    labels <- switch(viewAlong, x=rawX, y=rawY)
    palette(rainbow(length(labels)))
    plot.new()
    xRange <- c(min(x), max(x)*1.2)
    xTicks <- pretty(xRange, 9)
    yTicks <- pretty(data)
    par(las=2)
    plot.window(xRange, range(data))
    box()
    axis(1, xTicks)
    bigNumAxis(2, yTicks)
    prettyGrid(xTicks, yTicks)
    for(i in seq_along(labels))
        lines(x, column(data, i), col=i)
    legend(second(xRange), max(data), labels, fill=seq_along(labels), xjust=1, title=switch(viewAlong,x=xName,y=yName), bg="white")
    title(xlab=switch(viewAlong,x=yName,y=xName))
}

.colorsSimpleSurface <- function(z) {
    colorlookup <- rainbow(256, start=0, end=4/6)
    colorlookup[255*z+1]
}
    
.formatBigNum <- function(x) format(x, sci=FALSE, big.mark=',')

mix <- function(count, ...) {
    colors <- list(...)
    nGroups <- length(colors)-1
    failIf(count <= 0, "mix() is not defined for count <= 0")
    if(count <= nGroups)
        return(unlist(colors[1:count]))
    groups <- ceiling((1:count)/(count / nGroups))
    groupCount <- sapply(1:nGroups, function(i) length(which(groups == i)))
    unlist(lapply(1:nGroups, function(n) mixOneGroup(groupCount[[n]], colors[[n]], colors[[n+1]], n == nGroups)))
}

mixOneGroup <- function(count, color1, color2, includeLast) {
    count <- count + ifElse(includeLast, 0, 1)
    result <- hex(mixcolor(seq(0, 1, length.out=count), hex2RGB(color1), hex2RGB(color2)))
    ifElse(includeLast, result, result[-count])
}

genColors <- function(ticks, ...) {
    if(length(ticks) <= 1)
        return(character(0))        
    mix(length(ticks)-1, ...)
}

simpleContourPlot <- function(x, y, z, xName, yName, zName = "", zRange = NULL) {
    if(is.null(zRange))
        zRange <- range(z, finite=TRUE)
    zTicks <- pretty(zRange, 9)
    posTicks <- which(zTicks >= 0)
    negTicks <- which(zTicks <= 0)
    posColors <- genColors(posTicks, "#FFFFCC", "#FF9933", "#FF0000", "#990000")
    negColors <- genColors(negTicks, "#CCFFFF", "#000099")
    colors <- c(rev(negColors),posColors)
    par(mar=c(5,5,4,2), cex.axis=0.65)
    filled.contour(
        x, 
        y, 
        z, 
        levels=zTicks, 
        col = colors, 
        plot.axes={ 
            axis(1, x); 
            axis(2, y); 
            grid(length(x)-1, length(y)-1, col="black") 
        }, 
        key.axes={
            bigNumAxis(4, zTicks)
        }, 
        plot.title=title(main=zName, xlab=xName, ylab=yName)
    )
}

bigNumAxis <- function(side, ticks) {
    axis(side, ticks, format(ticks, scientific=FALSE, big.mark=","))
}

prettyGrid <- function(xTicks, yTicks) {
    abline(h=yTicks, col="lightgray", lty="dotted")
    abline(v=xTicks, col="lightgray", lty="dotted")
}

normalize <- function(x) { (x - min(x)) / (max(x) - min(x)) }

simpleZooPlot <- function(z, xLabel = "", yLabel = "", title = "") {
    yearNums <- unique(as.POSIXlt(index(z))$year) + 1900
    years <- as.POSIXct(paste(yearNums, "/01/01", sep=""))
    yTicks <- pretty(as.matrix(z), 9)
    if(is.null(ncol(z))) {
        colors <- "black"
        addLegend <- FALSE
    } else {
        colors <- 1:ncol(z)
        addLegend <- TRUE
    }

    par(las=2, mar=c(5,6,6,2), cex.axis=0.65)
    plot.zoo(z, xlab="", ylab="", xaxt="n", yaxt="n", plot.type='single', col=colors)
    axis(1, years, yearNums)
    bigNumAxis(2, yTicks)
    title(xlab=xLabel)
    title(ylab=yLabel, line=5)
    prettyGrid(years, yTicks)
    title(main=title)
    if(addLegend)
        legend("topleft",legend=colnames(z), fill=colors, bg="white")
}

simpleSurface3d <- function(rawX, rawY, rawZ, xName, yName) {
    open3d()

    x <- normalize(rawX)
    y <- normalize(rawY)
    z <- normalize(rawZ)
    colors <- .colorsSimpleSurface(z)
    surface3d(x,y,z, back="fill", color=colors)
    surface3d(x,y,z, front="lines", color="black")

    xTicks <- calcTickMarks(rawX)
    normXticks <- normalize(xTicks)
    yTicks <- calcTickMarks(rawY)
    normYticks <- normalize(yTicks)
    zTicks <- calcTickMarks(rawZ)
    normZticks <- normalize(zTicks)


    bbox3d(color = c("lightgray", "white"), expand = 1.05, nticks=0)

    axis3d('x++', at = normXticks, labels = .formatBigNum(xTicks))
    axis3d('y++', at = normYticks, labels = .formatBigNum(yTicks))
    axis3d('z-+', at = normZticks, labels = .formatBigNum(zTicks))

    for(i in normZticks) {
        axis3d('x', pos=c(NA, 1, i), labels=FALSE, tick=FALSE)
        axis3d('y', pos=c(1, NA, i), labels=FALSE, tick=FALSE)
    }
    for(i in normXticks) {
        axis3d('y', pos=c(i, NA, 0), labels=FALSE, tick=FALSE)
        axis3d('z', pos=c(i, 1, NA), labels=FALSE, tick=FALSE)
    }
    for(i in normZticks) {
        axis3d('x', pos=c(NA, i, 0), labels=FALSE, tick=FALSE)
        axis3d('z', pos=c(1, i, NA), labels=FALSE, tick=FALSE)
    }

    mtext3d(xName, 'x++', line=3)
    mtext3d(yName, 'y++', line=3)
    rgl.viewpoint(
        zoom = 0.9,
        userMatrix=matrix(c(
		0.477009326219559, 0.456084817647934, -0.751293659210205, 0, 
	       -0.874305188655853, 0.159001171588898, -0.458587288856506, 0, 
	       -0.0896987989544868, 0.875612676143646, 0.474603652954102, 0, 
		0, 0, 0, 1
        ), c(4,4))
    )

}

plotZooPanelByCalendar <- function(timeSeriesZoo){
	#Yearly only supported now
	
	needs(timeSeriesZoo='zoo')
	
	firstDate <- first(index(timeSeriesZoo))
	lastDate <- last(index(timeSeriesZoo))
	
	years <- (as.POSIXlt(firstDate)$year:as.POSIXlt(lastDate)$year)+1900
	
	#Setup the graphics
	#oldpar <- par(no.readonly=TRUE)
	windows()
	screen.cols <- ceiling(sqrt(length(years)))
	screen.rows <- ceiling(sqrt(length(years)))
	close.screen(all.screens=TRUE)
	split.screen(figs=c(screen.rows,screen.cols))
	
	screen.count <- 1
	for (year in years){
		print(screen.count)
		startDate <- as.POSIXct(squish(year,'-01-01'))
		endDate <- as.POSIXct(squish(year,'-12-31'))
		panelZoo <- Range(startDate,endDate)$cut(timeSeriesZoo)
		screen(screen.count)
		plot.zoo(panelZoo,xlab='',ylab='')
		title(main=as.character(year))
		screen.count <- screen.count + 1
	}
	
	#Reset the graphics to original
	#par(oldpar)
}


plotStitchedZoos <- function(listOfZoos, title=NULL, backAdjusted=TRUE, xlab="Date", ylab="Value", overlapRule = "notAllowed"){
#This can be expanded in the future for all the overlapTypes in the the stichedZoo function
	
	resultZoo <- stitchZoos(listOfZoos,backAdjusted=backAdjusted, overlapRule = overlapRule)
	
	plot(resultZoo$stitchedZoo,xlab=xlab,ylab=ylab)
	for (splitCount in index(resultZoo$splitDates)){
		abline(v=resultZoo$splitDates[[splitCount]])
	}
	title(main=title)
}


panel.cor <- function(x, y, digits=2, prefix="", cex.cor) 
{
	usr <- par("usr"); on.exit(par(usr)) 
	par(usr = c(0, 1, 0, 1)) 
	r <- abs(cor(x, y)) 
	txt <- format(c(r, 0.123456789), digits=digits)[1] 
	txt <- paste(prefix, txt, sep="") 
	if(missing(cex.cor)) cex <- 0.8/strwidth(txt) 
	
	test <- cor.test(x,y) 
	# borrowed from printCoefmat
	Signif <- symnum(test$p.value, corr = FALSE, na = FALSE, 
		cutpoints = c(0, 0.001, 0.01, 0.05, 0.1, 1),
		symbols = c("***", "**", "*", ".", " ")) 
	
	text(0.5, 0.5, txt, cex = cex * r) 
	text(.8, .8, Signif, cex=cex, col=2) 
}

plotCorrelationPanel <- function(dataFrame) pairs(dataFrame, lower.panel=panel.smooth, upper.panel=panel.cor)
