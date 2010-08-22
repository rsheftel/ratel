library("STO")

source(system.file("testHelper.r", package = "STO"))

loadFrame <- function(frameName){
	target <- zoo(data.frame(read.csv(system.file("testdata/TradeCubeSTO",frameName, package = "STO"), sep = ",", header = TRUE))[,-1,])
	target <- zoo(data.frame(target))
	colnames(target) <- 1:NCOL(target)
	target
}

pnlFrame <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.PNL.csv")
dateFrame <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv")
entrySizeFrame <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv")
barsInTradeFrame <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.DAYSIN.csv")
tradeCube <- TradeCube(pnlFrame,barsInTradeFrame,dateFrame,entrySizeFrame)

testConstructor <- function() {
	checkSame(tradeCube$.pnlFrame,pnlFrame)
	checkSame(tradeCube$.dateFrame,dateFrame)
	checkSame(tradeCube$.entrySizeFrame,entrySizeFrame)
	checkSame(tradeCube$.barsInTradeFrame,barsInTradeFrame)
	shouldBomb(TradeCube(pnlFrame[1,],barsInTradeFrame,dateFrame,entrySizeFrame))
	shouldBomb(TradeCube(pnlFrame,barsInTradeFrame[1,],dateFrame,entrySizeFrame))
	shouldBomb(TradeCube(pnlFrame,barsInTradeFrame,dateFrame[1,],entrySizeFrame))
	shouldBomb(TradeCube(pnlFrame,barsInTradeFrame,dateFrame,entrySizeFrame[1,]))
	shouldBomb(TradeCube(pnlFrame,barsInTradeFrame,dateFrame,dateFrame))
	shouldBomb(TradeCube(barsInTradeFrame,barsInTradeFrame,dateFrame,entrySizeFrame))
}

testFilter <- function() {
	# Side
	shouldBomb(tradeCube$filter('junk'))
	shortTradeCube <- tradeCube$filter(entrySize <0)
	checkSame(shortTradeCube$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[c(2,4),]))
	checkSame(shortTradeCube$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[c(2,4),]))
	checkSame(shortTradeCube$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[c(2,4),]))
	checkSame(shortTradeCube$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[c(2,4),]))
	longTradeCube <- tradeCube$filter(entrySize > 0)
	checkSame(longTradeCube$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[c(1,3,5),1:4]))
	checkSame(longTradeCube$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[c(1,3,5),]))
	checkSame(longTradeCube$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[c(1,3,5),1:4]))
	checkSame(longTradeCube$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[c(1,3,5),]))
	# No shorts in the original TradeCube
	shouldBomb(longTradeCube$filter(entrySize <0))
	
	# Bars
	shouldBomb(tradeCube$filter(bar1 > 1))
	tradeCubeRes <- tradeCube$filter(bar1 >= 0)
	checkSame(tradeCubeRes,TradeCube(pnlFrame[2:5,],barsInTradeFrame[2:5,],dateFrame[2:5,],entrySizeFrame[2:5,]))
	tradeCubeRes <- tradeCube$filter(bar1 >= 0 & bar4 < -3000)
	checkSame(tradeCubeRes,TradeCube(pnlFrame[5,1:4],barsInTradeFrame[5,],dateFrame[5,1:4],entrySizeFrame[5,]))
	
	# Bars In Trade
	tradeCubeFiltered <- tradeCube$filter(barsInTrade %in% as.numeric(1:3))
	checkSame(tradeCubeFiltered$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[c(1,3),1:3]))
	checkSame(tradeCubeFiltered$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[c(1,3)]))
	checkSame(tradeCubeFiltered$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[c(1,3),1:3]))
	checkSame(tradeCubeFiltered$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[c(1,3)]))
	tradeCubeFiltered <- tradeCube$filter(barsInTrade %in% as.numeric(2))
	checkSame(tradeCubeFiltered$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[1,1:2]))
	checkSame(tradeCubeFiltered$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[1,]))
	checkSame(tradeCubeFiltered$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[1,1:2]))
	checkSame(tradeCubeFiltered$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[1,]))
	tradeCubeFiltered <- tradeCube$filter(barsInTrade %in% as.numeric(c(4,6)))
	checkSame(tradeCubeFiltered$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[c(2,4,5),1:6]))
	checkSame(tradeCubeFiltered$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[c(2,4,5),]))
	checkSame(tradeCubeFiltered$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[c(2,4,5),1:6]))
	checkSame(tradeCubeFiltered$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[c(2,4,5),]))
	
	# Entry Dates

	tradeCubeFiltered <- tradeCube$filter(entryDate >= "2003-01-14" & entryDate <= "2003-01-14")
	checkSame(tradeCubeFiltered$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[2,]))
	checkSame(tradeCubeFiltered$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[2,]))
	checkSame(tradeCubeFiltered$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[2,]))
	checkSame(tradeCubeFiltered$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[2,]))
	
	tradeCubeFiltered <- tradeCube$filter(entryDate >="2003-01-14"& entryDate <= "2003-01-23")
	checkSame(tradeCubeFiltered$.pnlFrame,TradeCubeBuilder$formatZooFrame(pnlFrame[c(2,3),]))
	checkSame(tradeCubeFiltered$.entrySizeFrame,TradeCubeBuilder$formatZooFrame(entrySizeFrame[c(2,3),]))
	checkSame(tradeCubeFiltered$.dateFrame,TradeCubeBuilder$formatZooFrame(dateFrame[c(2,3),]))
	checkSame(tradeCubeFiltered$.barsInTradeFrame,TradeCubeBuilder$formatZooFrame(barsInTradeFrame[c(2,3),]))
}

testPnlFrameWithNAs <- function() {
	pnlFrame <- tradeCube$pnlFrameWithNAs(tradeCube$.pnlFrame)
	pnlFrameTarget <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.PNL.csv")
	pnlFrameTarget[1,3:6] <- NA
	pnlFrameTarget[3,4:6] <- NA
	pnlFrameTarget[4:5,5:6] <- NA
	checkSame(pnlFrame,pnlFrameTarget)
	pnlFrame[2,4] <- 0
	pnlFrame <- tradeCube$pnlFrameWithNAs(pnlFrame)
	pnlFrameTarget[2,4] <- 0
	checkSame(pnlFrame,pnlFrameTarget)
	checkSame(tradeCube$pnlFrameWithNAs(pnlFrame),pnlFrame)
	tradeCube <- TradeCube(pnlFrame[2,],barsInTradeFrame[2],dateFrame[2,],entrySizeFrame[2])
	checkSame(tradeCube$pnlFrameWithNAs(tradeCube$.pnlFrame),tradeCube$.pnlFrame)
}

testConditionalPnlFrame <- function() {
	frame <- tradeCube$conditionalPnlFrame()
	target <- loadFrame("testConditionalPnlFrame.csv")	
	checkSame(frame,target)
}

testConditionalPnlFrameToThisBar <- function() {
	frame <- tradeCube$conditionalPnlFrame(fromThisBar = FALSE)
	target <- loadFrame("testConditionalPnlFrameToThisBar.csv")	
	checkSame(frame,target)
}

testTrimFrame <- function() {
	checkSame(tradeCube$trimFrame(tradeCube$.pnlFrame,q = 0),tradeCube$.pnlFrame)
	frameTrimed <- tradeCube$trimFrame(tradeCube$.pnlFrame,q = 0.25)
	frameTarget <- pnlFrame
	frameTarget[1,1] <- NA
	frameTarget[c(1,5),2] <- NA
	frameTarget[c(2,3),3] <- NA
	frameTarget[c(4,5),4] <- NA
	frameTarget[2,5:6] <- NA
	checkSame(frameTrimed,frameTarget)
}
