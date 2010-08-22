# Functions from PerformanceAnalytics
# 
# Comments: http://www.quadrafund.com/cgi-bin/TWiki/bin/view/Technology/PerformanceAnalytics
###############################################################################

###############################################33
#	Base functions

checkData <- function (x, method = c("zoo", "matrix", "vector", "data.frame"), na.rm = FALSE, quiet = TRUE, ...) 
{
	method = method[1]
	if (!is.zoo(x)) {
		x = as.matrix(x)
		if (is.null(ncol(x))) 
			stop("There don't seem to be any columns in the data provided.  If you are trying to pass in names from a zoo object with one column, you should use the form 'data.zoo[rows, columns, drop = FALSE]'.")
		if (is.null(nrow(x))) 
			stop("No rows in the data provided.")
		if (method != "vector" & is.null(colnames(x))) {
			columns = ncol(x)
			if (!quiet) 
				warning("No column names in the data provided. To pass in names from a data.frame, you should use the form 'data[rows, columns, drop = FALSE]'.")
			columnnames = NULL
			for (column in 1:columns) {
				columnnames = c(columnnames, paste("Column.", 
						column, sep = ""))
			}
			colnames(x) = columnnames
		}
		if (method != "vector" & is.null(rownames(x))) 
			if (!quiet) 
				warning("No row names in the data provided. To pass in names from a data.frame, you should use the form 'data[rows, columns, drop = FALSE]'.")
		if (method == "zoo") {
			if (is.null(rownames(x))) 
				x = zoo(x)
			else x = zoo(x, order.by = rownames(x))
		}
	}
	else {
		if (is.null(ncol(x)) & dim(as.matrix(x))[2] == 1) {
			y = as.matrix(x)
			colnames(y) = "Column"
			x = zoo(y, order.by = time(x))
		}
	}
	if (method == "matrix") 
		x = as.matrix(x)
	if (method == "data.frame") 
		x = as.data.frame(x)
	if (method == "vector") {
		if (NCOL(x) > 1) {
			if (!quiet) 
				warning("The data provided is not a vector or univariate time series.  Used only the first column")
			x = x[, 1]
		}
		if (any(is.na(x))) {
			if (na.rm) {
				x = na.omit(x)
				if (!quiet) {
					warning("The following slots have NAs.")
					warning(paste(x@na.removed, ","))
				}
			}
			else {
				if (!quiet) 
					warning("Data contains NA's.")
			}
		}
		if (!is.numeric(x)) {
			if (!quiet) 
				warning("The data does not appear to be numeric.")
		}
		if (NROW(x) <= 1) {
			if (!quiet) 
				warning("Only one row provided.")
		}
		x = as.vector(x)
	}
	return(x)
}

apply.fromstart <- function (R, FUN = "mean", gap = 1, ...) 
{
	R = checkData(R, method = "zoo")
	columns = ncol(R)
	columnnames = colnames(R)
	for (column in 1:columns) {
		column.Return.calc = zoo(NA, order.by = as.Date(time(R)))
		for (i in gap:length(time(R))) {
			data.zoo = window(R, start = start(R), end = time(R[i]))
			column.Return.calc[i] = apply(as.matrix(data.zoo[, 
						, drop = FALSE]), FUN = FUN, ..., MARGIN = 2)
		}
		if (column == 1) 
			Return.calc = column.Return.calc
		else Return.calc = merge(Return.calc, column.Return.calc)
	}
	if (!is.null(ncol(Return.calc))) 
		colnames(Return.calc) = columnnames
	return(Return.calc)
}

#################################
#	Color definitions		


rich6equal <- c("#000043","#0033FF","#01CCA4","#BAFF12","#FFCC00","#FF3300")
redfocus <- c("#CB181D","#252525","#525252","#737373","#969696","#BDBDBD","#D9D9D9","#F0F0F0")
bluefocus <- c("#0033FF","#252525","#525252","#737373","#969696","#BDBDBD","#D9D9D9","#F0F0F0")
greenfocus <- c("#41AB5D","#252525","#525252","#737373","#969696","#BDBDBD","#D9D9D9","#F0F0F0")
rainbow12focus <- c("#BF4D4D","#BF864D","#BFBF4D","#86BF4D","#4DBF4D","#4DBF86","#4DBFBF","#4D86BF","#4D4DBF","#864DBF","#BF4DBF","#BF4D86")
dark8equal <- c("#1B9E77","#666666","#66A61E","#7570B3","#A6761D","#D95F02","#E6AB02","#E7298A")
rich12equal <- c("#000040","#000093","#0020E9","#0076FF","#00B8C2","#04E466","#49FB25","#E7FD09","#FEEA02","#FFC200","#FF8500","#FF3300")
paired <-  c("#A6CEE3","#1F78B4","#B2DF8A","#33A02C","#FB9A99","#E31A1C","#FDBF6F","#FF7F00","#CAB2D6","#6A3D9A","#FFFF99","#B15928")

#################################
#	PlotUtils		


chart.TimeSeries <- function (R, reference.grid = TRUE, xaxis = TRUE, type = "l", 
	lty = 1, lwd = 1, main = NULL, ylab = NULL, xlab = "Date", 
	date.format.in = "%Y-%m-%d", date.format = "%m/%y", xlim = NULL, 
	ylim = NULL, event.lines = NULL, event.labels = NULL, period.areas = NULL, 
	event.color = "darkgray", period.color = "lightgray", colorset = (1:12), 
	pch = (1:12), darken = FALSE, legend.loc = NULL, ylog = FALSE, 
	...) 
{
	y = checkData(R, method = "zoo")
	columns = ncol(y)
	rows = nrow(y)
	columnnames = colnames(y)
	rownames = as.Date(time(y))
	rownames = format(strptime(rownames, format = date.format.in), 
		date.format)
	logaxis = ""
	if (ylog) {
		logaxis = "y"
	}
	if (is.null(ylab)) {
		if (ylog) 
			ylab = "ln(Value)"
		else ylab = "Value"
	}
	if (darken) 
		elementcolor = "darkgray"
	else elementcolor = "lightgray"
	plot.new()
	if (is.null(xlim[1])) 
		xlim = c(1, rows)
	if (is.null(ylim[1])) {
		ylim = range(y, na.rm = TRUE)
	}
	plot.window(xlim, ylim, xaxs = "r", log = logaxis)
	dimensions = par("usr")
	if (!is.null(period.areas)) {
		period.ind = NULL
		for (period in 1:length(period.areas)) {
			period.ind = list(grep(period.areas[[period]][1], 
					rownames), grep(period.areas[[period]][2], rownames))
			rect(period.ind[1], dimensions[3], period.ind[2], 
				dimensions[4], col = period.color, border = NA)
		}
	}
	if (xlim[2] >= 200) 
		tickspace = 24
	if (xlim[2] >= 100) 
		tickspace = 12
	if (xlim[2] >= 50) 
		tickspace = 6
	else tickspace = 4
	lab.ind = seq(1, rows, by = tickspace/2)
	grid.ind = seq(1, rows, by = tickspace)
	if (reference.grid) {
		grid(nx = NA, ny = NULL, col = elementcolor)
		abline(v = grid.ind, col = elementcolor, lty = "dotted")
	}
	abline(h = 0, col = elementcolor)
	if (!is.null(event.lines)) {
		event.ind = NULL
		for (event in 1:length(event.lines)) {
			event.ind = c(event.ind, grep(event.lines[event], 
					rownames))
		}
		number.event.labels = ((length(event.labels) - length(event.ind) + 
					1):length(event.labels))
		abline(v = event.ind, col = event.color)
		if (!is.null(event.labels)) {
			text(x = event.ind, y = ylim[2], label = event.labels[number.event.labels], 
				offset = 0.2, pos = 2, cex = 0.7, srt = 90, col = event.color)
		}
	}
	if (length(lwd) < columns) 
		lwd = rep(lwd, columns)
	if (length(lty) < columns) 
		lty = rep(lty, columns)
	if (length(pch) < columns) 
		pch = rep(pch, columns)
	for (column in columns:1) {
		lines(1:rows, y[, column], col = colorset[column], lwd = lwd[column], 
			pch = pch[column], lty = lty[column], type = type, 
			...)
	}
	if (xaxis) {
		axis(1, at = lab.ind, lab = rownames[lab.ind], cex.axis = 0.8, 
			col = elementcolor)
		title(xlab = xlab)
	}
	axis(2, cex.axis = 0.8, col = elementcolor, ylog = ylog)
	box(col = elementcolor)
	if (!is.null(legend.loc)) {
		legend(legend.loc, inset = 0.02, text.col = colorset, 
			col = colorset, cex = 0.8, border.col = elementcolor, 
			lwd = 2, bg = "white", legend = columnnames)
	}
	if (is.null(main)) 
		main = columnnames[1]
	title(ylab = ylab)
	title(main = main)
}



legend <- function (x, y = NULL, legend, fill = NULL, col = par("col"), 
	lty, lwd, pch, angle = 45, density = NULL, bty = "o", bg = par("bg"), 
	pt.bg = NA, cex = 1, pt.cex = cex, pt.lwd = lwd, xjust = 0, 
	yjust = 1, x.intersp = 1, y.intersp = 1, adj = c(0, 0.5), 
	text.width = NULL, text.col = par("col"), merge = do.lines && 
		has.pch, trace = FALSE, plot = TRUE, ncol = 1, horiz = FALSE, 
	title = NULL, inset = 0, border.col = NULL, border.lwd = 1, 
	border.lty = "solid") 
{
	if (missing(legend) && !missing(y) && (is.character(y) || 
			is.expression(y))) {
		legend <- y
		y <- NULL
	}
	mfill <- !missing(fill) || !missing(density)
	if (length(title) > 1) 
		stop("invalid title")
	n.leg <- if (is.call(legend)) 
			1
		else length(legend)
	if (n.leg == 0) 
		stop("'legend' is of length 0")
	auto <- if (is.character(x)) 
			match.arg(x, c("bottomright", "bottom", "bottomleft", 
					"left", "topleft", "top", "topright", "right", "center"))
		else NA
	if (is.na(auto)) {
		xy <- xy.coords(x, y)
		x <- xy$x
		y <- xy$y
		nx <- length(x)
		if (nx < 1 || nx > 2) 
			stop("invalid coordinate lengths")
	}
	else nx <- 0
	xlog <- par("xlog")
	ylog <- par("ylog")
	rect2 <- function(left, top, dx, dy, density = NULL, angle, 
		border = border.col, lty = border.lty, lwd = border.lwd, 
		...) {
		r <- left + dx
		if (xlog) {
			left <- 10^left
			r <- 10^r
		}
		b <- top - dy
		if (ylog) {
			top <- 10^top
			b <- 10^b
		}
		rect(left, top, r, b, angle = angle, density = density, 
			border = border, lty = lty, lwd = lwd, ...)
	}
	segments2 <- function(x1, y1, dx, dy, ...) {
		x2 <- x1 + dx
		if (xlog) {
			x1 <- 10^x1
			x2 <- 10^x2
		}
		y2 <- y1 + dy
		if (ylog) {
			y1 <- 10^y1
			y2 <- 10^y2
		}
		segments(x1, y1, x2, y2, lend = "butt", ...)
	}
	points2 <- function(x, y, ...) {
		if (xlog) 
			x <- 10^x
		if (ylog) 
			y <- 10^y
		points(x, y, ...)
	}
	text2 <- function(x, y, ...) {
		if (xlog) 
			x <- 10^x
		if (ylog) 
			y <- 10^y
		text(x, y, ...)
	}
	if (trace) 
		catn <- function(...) do.call("cat", c(lapply(list(...), 
						formatC), list("\n")))
	cin <- par("cin")
	Cex <- cex * par("cex")
	if (is.null(text.width)) 
		text.width <- max(strwidth(legend, units = "user", cex = cex))
	else if (!is.numeric(text.width) || text.width < 0) 
		stop("'text.width' must be numeric, >= 0")
	xc <- Cex * xinch(cin[1], warn.log = FALSE)
	yc <- Cex * yinch(cin[2], warn.log = FALSE)
	xchar <- xc
	xextra <- 0
	yextra <- yc * (y.intersp - 1)
	ymax <- max(yc, strheight(legend, units = "user", cex = cex))
	ychar <- yextra + ymax
	if (trace) 
		catn("  xchar=", xchar, "; (yextra,ychar)=", c(yextra, 
				ychar))
	if (mfill) {
		xbox <- xc * 0.8
		ybox <- yc * 0.5
		dx.fill <- xbox
	}
	do.lines <- (!missing(lty) && (is.character(lty) || any(lty > 
						0))) || !missing(lwd)
	n.legpercol <- if (horiz) {
			if (ncol != 1) 
				warning("horizontal specification overrides: Number of columns := ", 
					n.leg)
			ncol <- n.leg
			1
		}
		else ceiling(n.leg/ncol)
	if (has.pch <- !missing(pch) && length(pch) > 0) {
		if (is.character(pch) && !is.na(pch[1]) && nchar(pch[1], 
			type = "c") > 1) {
			if (length(pch) > 1) 
				warning("not using pch[2..] since pch[1] has multiple chars")
			np <- nchar(pch[1], type = "c")
			pch <- substr(rep.int(pch[1], np), 1:np, 1:np)
		}
		if (!merge) 
			dx.pch <- x.intersp/2 * xchar
	}
	x.off <- if (merge) 
			-0.7
		else 0
	if (is.na(auto)) {
		if (xlog) 
			x <- log10(x)
		if (ylog) 
			y <- log10(y)
	}
	if (nx == 2) {
		x <- sort(x)
		y <- sort(y)
		left <- x[1]
		top <- y[2]
		w <- diff(x)
		h <- diff(y)
		w0 <- w/ncol
		x <- mean(x)
		y <- mean(y)
		if (missing(xjust)) 
			xjust <- 0.5
		if (missing(yjust)) 
			yjust <- 0.5
	}
	else {
		h <- (n.legpercol + (!is.null(title))) * ychar + yc
		w0 <- text.width + (x.intersp + 1) * xchar
		if (mfill) 
			w0 <- w0 + dx.fill
		if (has.pch && !merge) 
			w0 <- w0 + dx.pch
		if (do.lines) 
			w0 <- w0 + (2 + x.off) * xchar
		w <- ncol * w0 + 0.5 * xchar
		if (!is.null(title) && (tw <- strwidth(title, units = "user", 
					cex = cex) + 0.5 * xchar) > w) {
			xextra <- (tw - w)/2
			w <- tw
		}
		if (is.na(auto)) {
			left <- x - xjust * w
			top <- y + (1 - yjust) * h
		}
		else {
			usr <- par("usr")
			inset <- rep(inset, length.out = 2)
			insetx <- inset[1] * (usr[2] - usr[1])
			left <- switch(auto, bottomright = , topright = , 
				right = usr[2] - w - insetx, bottomleft = , left = , 
				topleft = usr[1] + insetx, bottom = , top = , 
				center = (usr[1] + usr[2] - w)/2)
			insety <- inset[2] * (usr[4] - usr[3])
			top <- switch(auto, bottomright = , bottom = , bottomleft = usr[3] + 
					h + insety, topleft = , top = , topright = usr[4] - 
					insety, left = , right = , center = (usr[3] + 
						usr[4] + h)/2)
		}
	}
	if (plot && bty != "n") {
		if (trace) 
			catn("  rect2(", left, ",", top, ", w=", w, ", h=", 
				h, ", ...)", sep = "")
		rect2(left, top, dx = w, dy = h, col = bg, density = NULL, 
			border = border.col)
	}
	xt <- left + xchar + xextra + (w0 * rep.int(0:(ncol - 1), 
			rep.int(n.legpercol, ncol)))[1:n.leg]
	yt <- top - 0.5 * yextra - ymax - (rep.int(1:n.legpercol, 
				ncol)[1:n.leg] - 1 + (!is.null(title))) * ychar
	if (mfill) {
		if (plot) {
			fill <- rep(fill, length.out = n.leg)
			rect2(left = xt, top = yt + ybox/2, dx = xbox, dy = ybox, 
				col = fill, density = density, angle = angle, 
				border = bg)
		}
		xt <- xt + dx.fill
	}
	if (plot && (has.pch || do.lines)) 
		col <- rep(col, length.out = n.leg)
	if (missing(lwd)) 
		lwd <- par("lwd")
	if (do.lines) {
		seg.len <- 2
		if (missing(lty)) 
			lty <- 1
		lty <- rep(lty, length.out = n.leg)
		lwd <- rep(lwd, length.out = n.leg)
		ok.l <- !is.na(lty) & (is.character(lty) | lty > 0)
		if (trace) 
			catn("  segments2(", xt[ok.l] + x.off * xchar, ",", 
				yt[ok.l], ", dx=", seg.len * xchar, ", dy=0, ...)")
		if (plot) 
			segments2(xt[ok.l] + x.off * xchar, yt[ok.l], dx = seg.len * 
					xchar, dy = 0, lty = lty[ok.l], lwd = lwd[ok.l], 
				col = col[ok.l])
		xt <- xt + (seg.len + x.off) * xchar
	}
	if (has.pch) {
		pch <- rep(pch, length.out = n.leg)
		pt.bg <- rep(pt.bg, length.out = n.leg)
		pt.cex <- rep(pt.cex, length.out = n.leg)
		pt.lwd <- rep(pt.lwd, length.out = n.leg)
		ok <- !is.na(pch) & (is.character(pch) | pch >= 0)
		x1 <- (if (merge) 
				xt - (seg.len/2) * xchar
			else xt)[ok]
		y1 <- yt[ok]
		if (trace) 
			catn("  points2(", x1, ",", y1, ", pch=", pch[ok], 
				", ...)")
		if (plot) 
			points2(x1, y1, pch = pch[ok], col = col[ok], cex = pt.cex[ok], 
				bg = pt.bg[ok], lwd = pt.lwd[ok])
		if (!merge) 
			xt <- xt + dx.pch
	}
	xt <- xt + x.intersp * xchar
	if (plot) {
		if (!is.null(title)) 
			text2(left + w/2, top - ymax, labels = title, adj = c(0.5, 
					0), cex = cex, col = text.col)
		text2(xt, yt, labels = legend, adj = adj, cex = cex, 
			col = text.col)
	}
	invisible(list(rect = list(w = w, h = h, left = left, top = top), 
			text = list(x = xt, y = yt)))
}



charts.PerformanceSummary <- function (R, rf = 0, main = NULL, method = c("ModifiedVaR", "VaR", "StdDev"), 
	width = 0, event.labels = NULL, ylog = FALSE, wealth.index = FALSE, 
	gap = 12, begin = c("first", "axis"), legend.loc = "topleft", return.method="default", ...)
{
	failIf(method!="StdDev", "Only method StdDev is implemented")
	begin = begin[1]
	x = checkData(R, method = "zoo")
	colnames = colnames(x)
	ncols = ncol(x)
	length.column.one = length(x[, 1])
	start.row = 1
	start.index = 0
	while (is.na(x[start.row, 1])) {
		start.row = start.row + 1
	}
	x = x[start.row:length.column.one, ]
	if (ncols > 1) 
		legend.loc = legend.loc
	else legend.loc = NULL
	if (is.null(main)) 
		main = paste(colnames[1], "Performance", sep = " ")
	if (ylog) 
		wealth.index = TRUE
	layout(matrix(c(1, 2, 3)), height = c(2, 1, 1.3), width = 1)
	par(mar = c(1, 4, 4, 2))
	chart.CumReturns(x, main = main, xaxis = FALSE, ylab = NULL, 
		legend.loc = legend.loc, event.labels = event.labels, 
		ylog = ylog, wealth.index = wealth.index, begin = begin, method=return.method,	...)
	par(mar = c(1, 4, 0, 2))
	chart.BarVaR(x, main = "", xaxis = FALSE, width = width, 
		ylab = "Monthly Return", method = method, event.labels = NULL, 
		ylog = FALSE, gap = gap, ...)
	par(mar = c(5, 4, 0, 2))
	chart.Drawdown(x, method=return.method, main = "", ylab = "From Peak", event.labels = NULL, 
		ylog = FALSE, ...)
}

chart.CumReturns <- function (R, wealth.index = FALSE, legend.loc = NULL, colorset = (1:12), 
								begin = c("first", "axis"), method="default", ...) {
	begin = begin[1]
	x = checkData(R, method = "zoo")
	columns = ncol(x)
	columnnames = colnames(x)
	one = 0
	if (!wealth.index) 
		one = 1
	if (begin == "first") {
		length.column.one = length(x[, 1])
		start.row = 1
		start.index = 0
		while (is.na(x[start.row, 1])) {
			start.row = start.row + 1
		}
		x = x[start.row:length.column.one, ]
		reference.index = cumprod(1 + na.omit(x[, 1]))
	}
	for (column in 1:columns) {
		if (begin == "axis"){ 
			start.index = 1
		}else {
			start.row = 1
			start.index = 0
			while (is.na(x[start.row, column])) {
				start.row = start.row + 1
			}
			if (start.row == 1) {
				start.index = 0
			} else {
				start.index = reference.index[(start.row - 1)]
			}
		}
		z = zoo(0)
		if ((start.index > 1) && (method=='default')) {
			z = rbind(start.index, 1 + na.omit(x[, column]))
		}else z = 1 + na.omit(x[, column])
		if (method=="default"){
			column.Return.cumulative = (cumprod(z) - one)
		}else if (method=="returns"){
			column.Return.cumulative = 1 + cumsum(z-1)
		}else if (method=="pnl"){
			column.Return.cumulative = cumsum(z-1)
		}else{
			column.Return.cumulative = CalculateReturns(z-1,method=method)
		}
		if (column == 1){ 
			Return.cumulative = column.Return.cumulative
		} else Return.cumulative = merge(Return.cumulative, column.Return.cumulative)
	}
	if (columns == 1) 
		Return.cumulative = as.matrix(Return.cumulative)
	colnames(Return.cumulative) = columnnames
	chart.TimeSeries(Return.cumulative, col = colorset, legend.loc = legend.loc, ...)
}


chart.BarVaR <- function (R, width = 0, gap = 12, risk.line = TRUE, method = c("ModifiedVaR", 
		"VaR", "StdDev"), reference.grid = TRUE, xaxis = TRUE, main = "Title", 
	ylab = "Value", xlab = "Date", date.format = "%m/%y", xlim = NA, 
	ylim = NA, lwd = 1, colorset = (1:12), p = 0.99, lty = "13", 
	all = FALSE, ...) 
{
	failIf(method!="StdDev", "Only method StdDev is implemented")
	x = checkData(R, method = "zoo")
	columns = ncol(x)
	rows = nrow(x)
	columnnames = colnames(x)
	rownames = time(x)
	rownames = format(strptime(as.Date(rownames), format = "%Y-%m-%d"), 
		date.format)
	time(x) = as.Date(time(x))
	method = method[1]
	risk = zoo(0, order.by = time(x))
	column.risk = zoo(0, order.by = time(x))
	if (!all) 
		columns = 1
	if (risk.line) {
		for (column in 1:columns) {
			switch(method, StdDev = {
					symmetric = TRUE
					if (width > 0) {
						column.risk = apply.rolling(na.omit(x[, column, 
									drop = FALSE]), width = width, FUN = "sd")
						legend.txt = paste("Rolling ", width, "-month Std Dev", 
							sep = "")
					}
					else {
						column.risk = apply.fromstart(na.omit(x[, column, 
									drop = FALSE]), gap = gap, FUN = "sd")
						legend.txt = "Std Dev"
					}
				}, VaR = {
					symmetric = TRUE
					if (width > 0) {
						column.risk = apply.rolling(na.omit(x[, column, 
									drop = FALSE]), width = width, FUN = "VaR.CornishFisher", 
							p = p, modified = FALSE)
						legend.txt = paste("Rolling ", width, "-Month VaR (1 Mo, ", 
							p * 100, "%)", sep = "")
					}
					else {
						column.risk = apply.fromstart(na.omit(x[, column, 
									drop = FALSE]), gap = gap, FUN = "VaR.CornishFisher", 
							p = p, modified = FALSE)
						legend.txt = paste("Traditional VaR (1 Mo, ", 
							p * 100, "%)", sep = "")
					}
				}, ModifiedVaR = {
					symmetric = FALSE
					if (width > 0) {
						column.risk = apply.rolling(na.omit(x[, column, 
									drop = FALSE]), width = width, FUN = "VaR.CornishFisher", 
							p = p, modified = TRUE)
						legend.txt = paste("Rolling ", width, "-Month Modified VaR (1 Mo, ", 
							p * 100, "%)", sep = "")
					}
					else {
						column.risk = apply.fromstart(na.omit(x[, column, 
									drop = FALSE]), gap = gap, FUN = "VaR.CornishFisher", 
							p = p, modified = TRUE)
						legend.txt = paste("Modified VaR (1 Mo, ", 
							p * 100, "%)", sep = "")
					}
				})
			if (column == 1) 
				risk = merge.zoo(x[, 1], column.risk)
			else risk = merge.zoo(risk, column.risk)
		}
	}
	else {
		risk = 0
		legend.txt = ""
	}
	if (is.na(ylim[1])) {
		ylim = range(c(na.omit(as.vector(x[, 1])), na.omit(as.vector(risk)), 
				-na.omit(as.vector(risk))))
	}
	chart.TimeSeries(x[, 1, drop = FALSE], type = "h", col = colorset, 
		legend.loc = NULL, ylim = ylim, reference.grid = reference.grid, 
		xaxis = xaxis, main = main, ylab = ylab, xlab = xlab, 
		lwd = lwd, lend = "butt", ...)
	if (risk.line) {
		if (symmetric) {
			for (column in (columns + 1):2) {
				lines(1:rows, risk[, column], col = colorset[column - 
							1], lwd = 1, type = "l", lty = lty)
			}
		}
		for (column in (columns + 1):2) {
			lines(1:rows, -risk[, column], col = colorset[column - 
						1], lwd = 1, type = "l", lty = lty)
		}
	}
	if (legend.txt != "") 
		legend("bottomleft", inset = 0.02, text.col = colorset, 
			col = colorset, cex = 0.8, border.col = "grey", lwd = 1, 
			lty = lty, bty = "n", legend = legend.txt)
}

chart.Drawdown <- function (R, legend.loc = NULL, colorset = (1:12), method="default", ...) 
{
	x = checkData(R, method = "zoo")
	columns = ncol(x)
	columnnames = colnames(x)
	for (column in 1:columns) {
#		Return.cumulative = cumprod(1 + na.omit(x[, column]))
#		maxCumulativeReturn = cummax(c(1, Return.cumulative))[-1]
#		column.drawdown = Return.cumulative/maxCumulativeReturn - 1
		
		column.drawdown <- drawdownCurve(x[, column],method=method)
		if (column == 1) 
			drawdown = column.drawdown
		else drawdown = merge(drawdown, column.drawdown)
	}
	if (columns == 1) 
		drawdown = as.matrix(drawdown)
	colnames(drawdown) = columnnames
	chart.TimeSeries(drawdown, col = colorset, legend.loc = legend.loc, ...)
}

chart.Boxplot <- function (R, horizontal = TRUE, names = TRUE, as.Tufte = FALSE, 
	sort.by = c(NULL, "mean", "median", "variance"), colorset = "black", 
	symbol.color = "red", mean.symbol = 1, median.symbol = "|", 
	outlier.symbol = 1, show.data = FALSE, darken = FALSE, add.mean = TRUE, 
	sort.ascending = FALSE, xlab = "Return", main = "Return Distribution Comparison", 
	...) 
{
	R = checkData(R, method = "data.frame")
	columns = ncol(R)
	rows = nrow(R)
	columnnames = colnames(R)
	column.order = NULL
	sort.by = sort.by[1]
	if (names) {
		par(mar = c(5, 12, 4, 2) + 0.1)
	}
	if (length(colorset) < columns) 
		colorset = rep(colorset, length.out = columns)
	if (length(symbol.color) < columns) 
		symbol.color = rep(symbol.color, length.out = columns)
	if (length(mean.symbol) < columns) 
		mean.symbol = rep(mean.symbol, length.out = columns)
	if (darken) 
		elementcolor = "darkgray"
	else elementcolor = "lightgray"
	means = sapply(R, mean, na.rm = TRUE)
	switch(sort.by, mean = {
			column.order = order(means)
			ylab = paste("Sorted by Mean", sep = "")
		}, median = {
			medians = sapply(R, median, na.rm = TRUE)
			column.order = order(medians)
			ylab = paste("Sorted by Median", sep = "")
		}, variance = {
			variances = sapply(R, var, na.rm = TRUE)
			column.order = order(variances)
			ylab = paste("Sorted by Variance", sep = "")
		}, {
			column.order = 1:columns
			ylab = paste("Unsorted", sep = "")
		})
	if (as.Tufte) {
		boxplot(R[, column.order], horizontal = TRUE, names = names, 
			main = main, xlab = xlab, ylab = "", pars = list(boxcol = "white", 
				medlty = "blank", medpch = median.symbol, medlwd = 2, 
				medcex = 0.8, medcol = colorset[column.order], 
				whisklty = c(1, 1), whiskcol = colorset[column.order], 
				staplelty = "blank", outpch = outlier.symbol, 
				outcex = 0.5, outcol = colorset[column.order]), 
			axes = FALSE, ...)
	}
	else {
		boxplot(R[, column.order], horizontal = TRUE, names = names, 
			main = main, xlab = xlab, ylab = "", pars = list(boxcol = colorset[column.order], 
				medlwd = 1, medcol = colorset[column.order], 
				whisklty = c(1, 1), whiskcol = colorset[column.order], 
				staplelty = 1, staplecol = colorset[column.order], 
				staplecex = 0.5, outpch = outlier.symbol, outcex = 0.5, 
				outcol = colorset[column.order]), axes = FALSE, 
			boxwex = 0.6, ...)
	}
	if (add.mean) 
		points(means[column.order], 1:columns, pch = mean.symbol[column.order], 
			col = symbol.color[column.order])
	if (names) {
		labels = columnnames
		axis(2, cex.axis = 0.8, col = elementcolor, labels = labels[column.order], 
			at = 1:columns, las = 1)
	}
	else {
		labels = ""
		axis(2, cex.axis = 0.8, col = elementcolor, labels = labels[column.order], 
			at = 1:columns, las = 1, tick = FALSE)
	}
	axis(1, cex.axis = 0.8, col = elementcolor)
	box(col = elementcolor)
	abline(v = 0, lty = "solid", col = elementcolor)
}

chart.Histogram <- function (R, breaks = "FD", main = NULL, xlab = "Returns", ylab = "Frequency", 
	methods = c("none", "add.density", "add.normal", "add.centered", 
		"add.cauchy", "add.sst", "add.rug", "add.risk", "add.qqplot"), 
	show.outliers = TRUE, colorset = c("lightgray", "#00008F", 
		"#005AFF", "#23FFDC", "#ECFF13", "#FF4A00", "#800000"), 
	border.col = "white", lwd = 2, xlim = NULL, ylim = NULL, 
	elementcolor = "gray", note.lines = NULL, note.labels = NULL, 
	note.color = "darkgray", probability = FALSE, p = 0.99, ...) {
	stdev <- function (x, na.rm = FALSE) 
	{
		if (is.matrix(x)) 
			apply(x, 2, sd, na.rm = na.rm)
		else if (is.vector(x)) 
			sqrt(var(x, na.rm = na.rm))
		else if (is.data.frame(x)) 
			sapply(x, sd, na.rm = na.rm)
		else sqrt(var(as.vector(x), na.rm = na.rm))
	}
	
	
	y = checkData(R)
	x = checkData(na.omit(y[, 1]), method = "vector")
	columns = ncol(y)
	rows = nrow(y)
	columnnames = colnames(y)
	n = length(x)
	rangedata = 0
	if (is.null(main)) {
		main = columnnames[1]
	}
	if (is.null(methods) || methods[1] == "none") {
		methods = NULL
	}
	
	### Take out the VaRs
	#b = c(-VaR.CornishFisher(x, p = p), -VaR.traditional(x, p = p))
	b = 0
	b.labels = c(paste(p * 100, "% ModVaR", sep = " "), paste(p * 
				100, "% VaR", sep = ""))
	if (show.outliers) 
		rangedata = c(min(x), max(x))
	else rangedata = c(qnorm(0.001, mean(x), stdev(x)), qnorm(0.999, 
				mean(x), stdev(x)))
	if (!is.null(note.lines)) {
		rangedata = c(rangedata, note.lines)
	}
	yrange = 0
	if (is.null(xlim)) 
		xlim = range(rangedata)
	s = seq(xlim[1], xlim[2], length = 500)
	for (method in methods) {
		switch(method, add.density = {
				den = density(x, n = length(x))
			}, add.stable = {
				if (!require("fBasics")) 
					stop("fBasics package not available")
				fit.stable = stableFit(x, doplot = FALSE)
				fitted.stable = dstable(s, alpha = fit.stable@fit$estimate[[1]], 
					beta = fit.stable@fit$estimate[[2]], gamma = fit.stable@fit$estimate[[3]], 
					delta = fit.stable@fit$estimate[[4]], pm = 0)
				yrange = c(yrange, max(fitted.stable))
				probability = TRUE
			}, add.cauchy = {
				if (!require("MASS")) 
					stop("MASS package not available")
				fit = fitdistr(x, "cauchy")
				xlab = paste("Cauchy (location = ", round(fit$estimate[[1]], 
						2), ", scale = ", round(fit$estimate[[2]], 2), 
					")", sep = "")
				fitted.cauchy = dcauchy(s, location = fit$estimate[[1]], 
					scale = fit$estimate[[2]], log = FALSE)
				yrange = c(yrange, max(fitted.cauchy))
				probability = TRUE
			}, add.sst = {
				fit = st.mle(y = x)
				fitted.sst = dst(s, location = fit$dp[[1]], scale = fit$dp[[2]], 
					shape = fit$dp[[3]], df = fit$dp[[4]], log = FALSE)
				yrange = c(yrange, max(fitted.sst))
				probability = TRUE
			}, add.lnorm = {
				fit = fitdistr(1 + x, "log-normal")
				fitted.lnorm = dlnorm(1 + s, meanlog = fit$estimate[[1]], 
					sdlog = fit$estimate[[2]], log = FALSE)
				yrange = c(yrange, max(fitted.lnorm))
				probability = TRUE
			}, add.normal = {
				fitted.normal = dnorm(s, mean(x), stdev(x))
				yrange = c(yrange, max(fitted.normal))
				probability = TRUE
			}, add.centered = {
				fitted.centered = dnorm(s, 0, stdev(x))
				yrange = c(yrange, max(fitted.centered))
				probability = TRUE
			}, add.risk = {
				rangedata = c(rangedata, b)
			})
	}
	yrange = c(yrange, max(hist(x, plot = FALSE)$intensities) * 
			1.1)
	ylim = c(0, ceiling(max(yrange)))
	hist(x = x, probability = probability, xlim = xlim, ylim = ylim, 
		col = colorset[1], border = border.col, xlab = xlab, 
		main = main, breaks = breaks, cex.axis = 0.8, axes = FALSE, 
		...)
	axis(1, cex.axis = 0.8, col = elementcolor)
	axis(2, cex.axis = 0.8, col = elementcolor)
	box(col = elementcolor)
	for (method in methods) {
		switch(method, add.density = {
				lines(den, col = colorset[2], lwd = lwd)
			}, add.normal = {
				lines(s, fitted.normal, col = colorset[3], lwd = lwd)
			}, add.centered = {
				lines(s, fitted.centered, col = colorset[3], lwd = lwd)
			}, add.lnorm = {
				lines(s, fitted.lnorm, col = colorset[4], lwd = lwd)
			}, add.cauchy = {
				lines(s, fitted.cauchy, col = colorset[4], lwd = lwd)
			}, add.stable = {
				lines(s, fitted.stable, col = colorset[4], lwd = lwd)
			}, add.sst = {
				lines(s, fitted.sst, col = colorset[4], lwd = lwd)
			}, add.rug = {
				rug(x, col = elementcolor)
			}, add.risk = {
				h = rep(0.2 * par("usr")[3] + 1 * par("usr")[4], 
					length(b))
				abline(v = b, col = "darkgray", lty = 2)
				text(b, h, b.labels, offset = 0.2, pos = 2, cex = 0.8, 
					srt = 90)
			}, add.qqplot = {
				op <- par(fig = c(0.02, 0.5, 0.5, 0.98), new = TRUE)
				qqnorm(x, xlab = "", ylab = "", main = "", axes = FALSE, 
					pch = ".", col = colorset[2])
				qqline(x, col = colorset[3])
				box(col = elementcolor)
			})
	}
	if (!is.null(note.lines)) {
		abline(v = note.lines, col = note.color, lty = 2)
		if (!is.null(note.labels)) {
			h = rep(0.2 * par("usr")[3] + 1 * par("usr")[4], 
				length(b))
			text(note.lines, h, note.labels, offset = 0.2, pos = 2, 
				cex = 0.7, srt = 90, col = note.color)
		}
	}
}

###########################################3
#	Math functions

Return.calculate <- function (prices, method = c("compound", "simple", "constantBase")){
	method = method[1]
	prices = checkData(prices, method = "zoo")
	switch(method,
			simple = return(prices/lag(prices, -1) - 1),
			compound = return(diff(log(prices))),
			constantBase = return(diff(prices)/first(na.omit(prices))))
}

CalculateReturns <- function (prices, method = c("compound", "simple", "constantBase")){
	Return.calculate(prices = prices, method = method)
}

sortDrawdowns <- function (runs) {
	for (i in 1:length(runs$return)) {
		for (j in 1:(length(runs$return) - 1)) {
			if (runs$return[j] > runs$return[j + 1]) {
				tempRet = runs$return[j]
				tempFor = runs$from[j]
				tempTo = runs$to[j]
				tempLen = runs$length[j]
				tempPtt = runs$peaktotrough[j]
				tempTro = runs$trough[j]
				tempRec = runs$recovery[j]
				runs$return[j] = runs$return[j + 1]
				runs$from[j] = runs$from[j + 1]
				runs$to[j] = runs$to[j + 1]
				runs$length[j] = runs$length[j + 1]
				runs$peaktotrough[j] = runs$peaktotrough[j + 
						1]
				runs$trough[j] = runs$trough[j + 1]
				runs$recovery[j] = runs$recovery[j + 1]
				runs$return[j + 1] = tempRet
				runs$from[j + 1] = tempFor
				runs$to[j + 1] = tempTo
				runs$length[j + 1] = tempLen
				runs$peaktotrough[j + 1] = tempPtt
				runs$trough[j + 1] = tempTro
				runs$recovery[j + 1] = tempRec
			}
		}
	}
	runs
}

drawdownCurve <- function (x, method="default"){
	x <- na.omit(x)
	if (method == "default"){
		Return.cumulative <- cumprod(1 + na.omit(x))
		maxCumulativeReturn = cummax(c(1, Return.cumulative))[-1]
	}else if (method == "constantBase"){
		Return.cumulative <- x/x[[1]]
		maxCumulativeReturn = cummax(c(1, Return.cumulative))[-1]
	}else if (method == "equity"){
		Return.cumulative <- x
		maxCumulativeReturn <- cummax(c(0, Return.cumulative))[-1]
	}else if ((method == "returns") || (method == "pnl")){
		Return.cumulative <- cumsum(x)
		maxCumulativeReturn <- cummax(c(0, Return.cumulative))[-1] 	
	}else fail("Not a valid input for method")
	
	if (method == "default"){
		drawdowns = Return.cumulative/maxCumulativeReturn - 1
	}else{
		drawdowns = Return.cumulative - maxCumulativeReturn
	}
	return(drawdowns)
}

findDrawdowns <- function (R, return.method="default") {
	R <- na.omit(R)
	x = checkData(R, method = "vector")
	drawdowns <- drawdownCurve(x, return.method)
	draw = c()
	begin = c()
	end = c()
	length = c(0)
	trough = c(0)
	index = 1
	if (drawdowns[1] >= 0) 
		priorSign = 1
	else priorSign = 0
	from = 1
	sofar = drawdowns[1]
	to = 1
	dmin = 1
	for (i in 1:length(drawdowns)) {
		thisSign <- ifelse(drawdowns[i] < 0, 0, 1)
		if (thisSign == priorSign) {
			if (drawdowns[i] < sofar) {
				sofar = drawdowns[i]
				dmin = i
			}
			to = i + 1
		}
		else {
			draw[index] = sofar
			begin[index] = from
			trough[index] = dmin
			end[index] = to
			from = i
			sofar = drawdowns[i]
			to = i + 1
			dmin = i
			index = index + 1
			priorSign = thisSign
		}
	}
	draw[index] = sofar
	begin[index] = from
	trough[index] = dmin
	end[index] = to
	list(return = draw, from = begin, trough = trough, to = end, 
		length = (end - begin + 1), peaktotrough = (trough - 
				begin + 1), recovery = (end - trough))
}


#################################
#	Tables		

table.CalendarReturns <- function (R, digits = 1, as.perc = TRUE, returns=TRUE) {
	ri = checkData(R, method = "matrix")
	
	columns = ncol(ri)
	columnnames = colnames(ri)
	
	rownames = rownames(ri)
	firstyear = as.numeric(format(strptime(rownames(ri)[1], "%Y-%m-%d"), "%Y"))
	lastyear = as.numeric(format(strptime(rownames(ri)[length(ri[,1])], "%Y-%m-%d"), "%Y"))
	year = format(strptime(rownames(ri), "%Y-%m-%d"), "%Y")
	month = format(strptime(rownames(ri), "%Y-%m-%d"), "%b")
	
	monthlabels = c("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
		"Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
	rowlabels = (firstyear:lastyear)
	for (column in 1:columns) {
		target.df = as.data.frame(matrix(data = as.numeric(NA), 
				length(rowlabels), length(monthlabels), dimnames = list(rowlabels, 
					monthlabels)))
		for (i in 1:length(ri[, 1])) {
			if (!is.na(ri[i, column])) {
				target.df[year[i], month[i]] = ri[i, column]
			}
		}
		yearcol = as.data.frame(matrix(data = as.numeric(NA), 
				length(rowlabels), 1, dimnames = list(rowlabels, 
					columnnames[column])))
		for (i in 1:length(yearcol[, 1])) {
			if (returns) yearcol[i, columnnames[column]] = prod(1 + na.omit(as.numeric(target.df[i,]))) - 1
			else yearcol[i, columnnames[column]] = sum(na.omit(as.numeric(target.df[i,])))
			
			if (yearcol[i, columnnames[column]] == 0) 
				yearcol[i, columnnames[column]] = NA
		}
		target.df = cbind(target.df, yearcol)
		if (as.perc && returns) 
			multiplier = 100
		else multiplier = 1
		target.df = target.df * multiplier
		target.df = base::round(target.df, digits)
		if (column == 1) 
			result.df = target.df
		else {
			result.df = cbind(result.df, target.df[, 13])
		}
	}
	colnames(result.df) = c(monthlabels, columnnames)
	result.df
}


table.Drawdowns <- function (R, top = 5, method="default", ...) 
{
	R = checkData(R, method = "zoo", ...)
	x = sortDrawdowns(findDrawdowns(R,return.method=method))
	ndrawdowns = length(x$from)
	if (ndrawdowns < top) {
		warning(paste("Only ", ndrawdowns, " available in the data.", 
				sep = ""))
		top = ndrawdowns
	}
	result = data.frame(time(R)[x$from[1:top]], time(R)[x$trough[1:top]], 
		time(R)[x$to[1:top]], x$return[1:top], x$length[1:top], 
		x$peaktotrough[1:top], x$recovery[1:top])
	colnames(result) = c("From", "Trough", "To", "Depth", "Length", 
		"To Trough", "Recovery")
	result
}
