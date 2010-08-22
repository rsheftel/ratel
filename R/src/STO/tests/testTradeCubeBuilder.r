library(STO)
source(system.file("testHelper.r", package = "STO"))

loadFrame <- function(frameName){
	target <- zoo(data.frame(read.csv(system.file("testdata/TradeCubeSTO",frameName, package = "STO"), sep = ",", header = TRUE))[,-1,])
	target <- zoo(data.frame(target))
	colnames(target) <- 1:NCOL(target)
	target
}

testConstructor <- function() {
	sto <- STO(stoDirectory(), "TradeCubeSTO")
	cubeBuilder <- TradeCubeBuilder(sto,'closeSystemAdjustment')
	checkSame(cubeBuilder$.sto,sto)
	checkSame(cubeBuilder$.reverseDateLogic,'closeSystemAdjustment')
	cubeBuilder <- TradeCubeBuilder(sto,'50/50')
	checkSame(cubeBuilder$.reverseDateLogic,'50/50')
	shouldBomb(TradeCubeBuilder("ThisIsNotASto",'closeSystemAdjustment'))
	shouldBomb(TradeCubeBuilder(sto,'unknown'))
}

testLoadOnePositionEquityCurve <- function() {
	sto <- STO(stoDirectory(), "TradeCubeSTO")
	cubeBuilder <- TradeCubeBuilder(sto,'closeSystemAdjustment')
	eqCurve <- cubeBuilder$.loadOnePositionEquityCurve(sto$msivs()[[1]],1)
	checkShape(eqCurve,2673,2,colnames = c('pnl','position'))
	shouldBomb(cubeBuilder$.loadOnePositionEquityCurve(sto$msivs()[1],1))
	shouldBomb(cubeBuilder$.loadOnePositionEquityCurve(sto$msivs()[[1]],'run'))
}

testBuildOneTradeCubeCloseSystemAdjustment <- function() {
	sto <- STO(stoDirectory(), "TradeCubeSTO")
	this <- TradeCubeBuilder(sto,'closeSystemAdjustment')
	msiv <- sto$msivs()[[1]]
	run <- 1
	eqCurveBase <- this$.loadOnePositionEquityCurve(msiv,run)
	# with slippage = 0			
	eqCurve <- Range('2003-01-09','2003-02-05')$cut(eqCurveBase)
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurve)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.PNL.csv"),TradeCubeRes$.pnlFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv"),TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv"),TradeCubeRes$.entrySizeFrame)
	# Test no trade
	eqCurveNoTrade <- eqCurve[1:2,]
	checkSame(NULL,this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurveNoTrade))
	# Test trade on the first day
	eqCurveFirstDayTrade <- eqCurve[3:NROW(eqCurve),]
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurveFirstDayTrade)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.PNL.csv"),TradeCubeRes$.pnlFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv"),TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv"),TradeCubeRes$.entrySizeFrame)
	# Test exit on the last day
	eqCurveLastDayExit <- eqCurve[1:(NROW(eqCurve)-2),]
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurveLastDayExit)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.PNL.csv"),TradeCubeRes$.pnlFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv"),TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv"),TradeCubeRes$.entrySizeFrame)
	# Test trade on the last day
	eqCurveLastDayTrade <- eqCurve[1:(NROW(eqCurve)-3),]
	resPNL <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.PNL.csv")	
	resPNL[5,4] <- 0
	resDATE <- loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv")
	resDATE[5,4] <- 0
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurveLastDayTrade)
	checkSame(resPNL,TradeCubeRes$.pnlFrame)
	checkSame(resDATE,TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv"),TradeCubeRes$.entrySizeFrame)
	# Test curve only for one date
	eqCurveOneDate <- eqCurve[1,]
	checkSame(NULL,this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurveOneDate))
	eqCurveOneDate <- eqCurve[3,]
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurveOneDate)
	checkSame(-1885,as.numeric(TradeCubeRes$.pnlFrame))
	checkSame('2003-01-13',as.character(TradeCubeRes$.dateFrame))
	checkSame(13,as.numeric(TradeCubeRes$.entrySizeFrame))
	# with slippage = 150
	msiv$.market = 'PTT10.ESIG'
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurve)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SLIP.PNL.csv"),TradeCubeRes$.pnlFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv"),TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv"),TradeCubeRes$.entrySizeFrame)
	# with slippage = 150
	eqCurve[10,2] <- -13
	eqCurve[11,2] <- 20
	eqCurve[8,2] <- -15
	eqCurve <- eqCurve[-c(17,18,19),]
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurve)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SLIP.PNL.2.csv"),TradeCubeRes$.pnlFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.2.csv"),TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.2.csv"),TradeCubeRes$.entrySizeFrame)
}


testBuildOneTradeCube5050 <- function() {
	sto <- STO(stoDirectory(), "TradeCubeSTO")
	this <- TradeCubeBuilder(sto,'50/50')
	msiv <- sto$msivs()[[1]]
	run <- 1
	eqCurveBase <- this$.loadOnePositionEquityCurve(msiv,run)			
	eqCurve <- Range('2003-01-09','2003-02-05')$cut(eqCurveBase)
	TradeCubeRes <- this$.buildOneTradeCube(msiv = msiv,run = run, pe = eqCurve)
	checkSame(loadFrame("testBuildOneTradeCube.5050.PNL.csv"),TradeCubeRes$.pnlFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.DATE.csv"),TradeCubeRes$.dateFrame)
	checkSame(loadFrame("testBuildOneTradeCubeCloseSystemAdj.SIZE.csv"),TradeCubeRes$.entrySizeFrame)
}

testRun <- function() {
	# 2 different runs on one MSIV 
	sto <- STO(stoDirectory(), "TradeCubeSTO")
	this <- TradeCubeBuilder(sto,'50/50')
	msivs <- list(sto$msivs()[[2]],sto$msivs()[[2]])
	runs <- c(1,2)
	tradeCube <- this$run(msivs,runs)
	checkSame(loadFrame("testRun.5050.PNL.csv"),tradeCube$.pnlFrame)
	checkSame(loadFrame("testRun.5050.DATE.csv"),tradeCube$.dateFrame)
	checkSame(loadFrame("testRun.5050.SIZE.csv"),tradeCube$.entrySizeFrame)	
	# 1 run on one MSIV
	tradeCube <- this$run(msivs[1],1)
	checkSame(loadFrame("testRun.5050.PNL.OneRun.csv"),tradeCube$.pnlFrame)
	checkSame(loadFrame("testRun.5050.DATE.OneRun.csv"),tradeCube$.dateFrame)
	checkSame(loadFrame("testRun.5050.SIZE.OneRun.csv"),tradeCube$.entrySizeFrame)	
}