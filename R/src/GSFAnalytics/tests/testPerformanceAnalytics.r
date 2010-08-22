## tests for PerformanceAnalytics
#######################################################

library("GSFAnalytics")

testdata <- squish(system.file("testdata", package="GSFAnalytics"),'/PerformanceAnalytics/')

loadManagers <- function(){
	x <- read.csv(squish(testdata,'managers.csv'))
	z <- zoo(x[,2:11],x[,1])
	vv <- as.yearmon(as.character(index(z)),"%B %Y")
	index(z) <- vv
	return(z)
}

# Global Variables
	managers <- loadManagers()
	manager.col = 1
	peers.cols = c(2, 3, 4, 5, 6)
	indexes.cols = c(7, 8)
	Rf.col = 10

test.calculateReturns <- function(){
	dates <- as.POSIXct(paste('2000','01',1:7,sep="-"))
	inputs <- zoo(c(10,11,12,14,10,8,5),dates)
	
	expected <- zoo(c(0.1,0.1,0.2,-0.4,-0.2,-0.3),dates[1:6])
	checkSameLooking(expected, CalculateReturns(inputs,"constantBase"))
	
	expected <- zoo(c(0.1,0.090909,0.166667,-0.285714,-0.2,-0.375),dates[1:6])
	checkSameLooking(expected, round(CalculateReturns(inputs,"simple"),6))
	
	expected <- zoo(c(0.09531,0.087011,0.154151,-0.336472,-0.223144,-0.470004),dates[1:6])
	checkSameLooking(expected, round(CalculateReturns(inputs,"compound"),6))
	
	#With NA in the data and constantBase uses the first non-NA to get base
	inputs <- zoo(c(NA,NA,12,14,10,8,5),dates)
	expected <- zoo(c(NA,NA,2/12,-4/12,-2/12,-3/12),dates[1:6])
	checkSameLooking(expected, CalculateReturns(inputs,"constantBase"))
}	
	
test.table.CalendarReturns <- function(){
	#Not sure why this zoo doesn't work, but others do.
	rownames =  strptime(paste("01",as.character(index(managers))),"%d %B %Y")
	managers.fix <- as.matrix(managers)
	rownames(managers.fix) <- as.character(rownames)
	##
	
	result <- t(table.CalendarReturns(managers.fix[, c(manager.col, indexes.cols)]))
	saved <- dget(squish(testdata,'table.CalendarReturns.dput'))
	checkSame(result,saved)
	
	#pnl to get raw data, not compounded
	result <- t(table.CalendarReturns(managers.fix[, c(manager.col, indexes.cols)]*100,returns=FALSE))
	saved <- dget(squish(testdata,'table.CalendarPnL.dput'))
	checkSame(result,saved)
}

test.drawdownCurve <- function(){
	dates <- as.POSIXct(paste('2000','01',1:30,sep="-"))
	equity <- zoo(c(10,9,10,11,15,20,19,18,17,18,19,20,21,30,40,30,28,28,30,45,50,51,51,51,50,50,51,52,55,50),dates)
	
	expected <- zoo(c(0,-0.1,0,0,0,0,-0.1,-0.2,-0.3,-0.2,-0.1,0,0,0,0,-1,-1.2,-1.2,-1,0,0,0,0,0,-0.1,-0.1,0,0,0,-0.5),dates)
	checkSame(expected, round(drawdownCurve(equity,"constantBase"),2))
	
	pnl <- rbind(zoo(0,dates[1]),diff(equity,1))
	expected <- zoo(c(0,-1,0,0,0,0,-1,-2,-3,-2,-1,0,0,0,0,-10,-12,-12,-10,0,0,0,0,0,-1,-1,0,0,0,-5),dates)	
	checkSameLooking(expected, drawdownCurve(pnl,"returns"))
	checkSameLooking(expected, drawdownCurve(pnl,"pnl"))
	checkSame(expected, drawdownCurve(equity,"equity"))
}

test.table.Drawdowns <- function(){
	result <- table.Drawdowns(managers[, 1, drop = FALSE], top=5)
	saved <- dget(squish(testdata,'table.Drawdowns.dput'))
	checkSame(result,saved)
	
	dates <- as.POSIXct(paste('2000','01',1:30,sep="-"))
	equity <- zoo(c(10,9,10,11,15,20,19,18,17,18,19,20,21,30,40,30,28,28,30,45,50,51,51,51,50,50,51,52,55,50),dates)
	saved <- dget(squish(testdata,"table.Drawdowns.constantBase.dput"))
	checkSame(saved, table.Drawdowns(equity, top=5, method="constantBase"))

	saved <- dget(squish(testdata,"table.Drawdowns.dollars.dput"))
	checkSame(saved, table.Drawdowns(equity, top=5, method="equity"))
	pnl <- rbind(zoo(0,dates[1]),diff(equity,1))
	checkSameLooking(saved, table.Drawdowns(pnl, top=5, method="returns"))
	checkSameLooking(saved, table.Drawdowns(pnl, top=5, method="pnl"))	
}