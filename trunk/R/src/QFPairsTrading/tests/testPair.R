## Test file for the Pair object
library(QFPairsTrading)


rm(list = ls())

dates <- as.POSIXct(c("2001-01-01","2001-01-02","2001-01-03","2001-01-04"))
datesWithTimes <- as.POSIXct(c("2001-01-01 3:00:00", "2001-01-02 0:00:00", "2001-01-03 3:00:00"))
slope <- zoo(c(2,1.5,1,2),dates)
seriesX <- zoo(c(100,102,101,99),dates)
seriesY <- zoo(c(1,0.5,0.8,2),dates)
seriesZ <- zoo(c(2, 3, 4), datesWithTimes)
multiplierY <- zoo(c(1,1,1,1),dates)
multiplierX <- zoo(c(10,9.9,10.1,9),dates)
triY <- zoo(c(100,98,98.2,98.5,99),dates)
triX <- zoo(c(200,195,194,193,197),dates)

test.Pair.constructor <- function(){
    this <- Pair(seriesY,seriesX) 
    checkEquals(this$.seriesY,getZooDataFrame(seriesY,"seriesY"))
    checkEquals(this$.seriesX,getZooDataFrame(seriesX,"seriesX"))
    checkEquals(getZooDataFrame(this$.triY,"seriesY"),this$.seriesY)
    checkEquals(getZooDataFrame(this$.triX,"seriesX"),this$.seriesX)
    checkEquals(this$.normalizeToY,TRUE)
    checkEquals(this$.hedgeMinY,NULL)
    checkEquals(this$.hedgeMinX,NULL)
    checkEquals(this$.hedgeMaxY,NULL)
    checkEquals(this$.hedgeMaxY,NULL)
    checkEquals(this$.holdCoefficients,TRUE)
    
    this <- Pair(seriesY,seriesX,triY,triX)
    checkEquals(this$.seriesY,getZooDataFrame(seriesY,"seriesY"))
    checkEquals(this$.seriesX,getZooDataFrame(seriesX,"seriesX"))    
    checkEquals(this$.triY,getZooDataFrame(triY,"triY"))
    checkEquals(this$.triX,getZooDataFrame(triX,"triX")) 
    checkEquals(this$.multiplierY,getZooDataFrame(zoo(1,index(triY)),"multiplierY"))
    checkEquals(this$.multiplierX,getZooDataFrame(zoo(1,index(triX)),"multiplierX"))
    
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX)    
    checkEquals(this$.seriesY,getZooDataFrame(seriesY,"seriesY"))
    checkEquals(this$.seriesX,getZooDataFrame(seriesX,"seriesX"))    
    checkEquals(this$.triY,getZooDataFrame(triY,"triY"))
    checkEquals(this$.triX,getZooDataFrame(triX,"triX")) 
    checkEquals(this$.multiplierY,getZooDataFrame(multiplierY,"multiplierY"))
    checkEquals(this$.multiplierX,getZooDataFrame(multiplierX,"multiplierX"))
    
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,hedgeMinX = -1)    
    checkEquals(this$.hedgeMinX,-1)
    checkEquals(this$.hedgeMinY,NULL)    
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,hedgeMinX = -1,hedgeMinY = 1,hedgeMaxX = 2,hedgeMaxY = 0)  
    checkEquals(this$.hedgeMinX,-1)
    checkEquals(this$.hedgeMinY,1)    
    checkEquals(this$.hedgeMaxX,2) 
    checkEquals(this$.hedgeMaxY,0)  
    
    # Should bombs
    
    shouldBomb(Pair(seriesY,TRUE,triY,triX,multiplierY,multiplierX))
    shouldBomb(Pair(seriesY,seriesX,triY,TRUE,multiplierY,multiplierX))
    shouldBomb(Pair(seriesY,seriesX,triY,triX,multiplierY,TRUE))    
    shouldBomb(Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,hedgeMinY = "SD"))       
    shouldBomb(Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,normalizeToY = "SD"))           
    shouldBomb(Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,holdCoefficients = "SD"))    
}

test.Pair.getSlopeHedgeRatios <- function(){
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX)
        
    # check base case multiplier provided
    result <- this$getSlopeHedgeRatios(slope)
    target <- zoo(matrix(c(1,1,1,1,-0.2,-0.1515151515151515,-0.0990099009900990,-0.2222222222222222),nrow = 4,ncol = 2),as.POSIXct(dates))
    colnames(target) <- c("hedgeY","hedgeX")
    assert(index(result)==index(target))       
    index(target) <- index(result)
    checkEquals(target,result)   
    this <- Pair(seriesY,seriesX,triY,triX,multiplierX = multiplierX)
    checkEquals(result,this$getSlopeHedgeRatios(slope))
    
    # check base case multiplier not provided
    this <- Pair(seriesY,seriesX,triY,triX)
    result <- this$getSlopeHedgeRatios(slope)
    target[,2] <- target[,2] * multiplierX
    checkEquals(target,result)
    
    # check one date
    this <- Pair(seriesY[1],seriesX[1],triY[1],triX[1],multiplierY[1],multiplierX[1])
    result <- this$getSlopeHedgeRatios(slope[1])
    target[,2] <- target[,2] / multiplierX
    target <- target[1,]
    assert(index(result)==index(target))        
    index(target) <- index(result)
    checkEquals(target,result)
    
    # check missing points
    target <- zoo(matrix(c(1,1,1,-0.1515151515151515,-0.0990099009900990,-0.2222222222222222),nrow = 3,ncol = 2),as.POSIXct(dates[-1]))
    colnames(target) <- c("hedgeY","hedgeX")
    this <- Pair(seriesY[-1],seriesX,triY,triX,multiplierY,multiplierX)
    result <- this$getSlopeHedgeRatios(slope)
    assert(index(result)==index(target))        
    index(target) <- index(result)
    checkEquals(target,result)
    this <- Pair(seriesY,seriesX[-1],triY,triX,multiplierY,multiplierX)
    result <- this$getSlopeHedgeRatios(slope)
    assert(index(result)==index(target))        
    index(target) <- index(result)
    checkEquals(target,result)
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX)
    result <- this$getSlopeHedgeRatios(slope[-1])
    assert(index(result)==index(target))        
    index(target) <- index(result)
    checkEquals(target,result)   
    
    # no data
    this <- Pair(seriesY[1],seriesX[2],triY,triX,multiplierY,multiplierX)
    result <- this$getSlopeHedgeRatios(slope[2])
    checkEquals(result,NULL)
    
    # when normalized to X
    
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,normalizeToY = FALSE)
        
    result <- this$getSlopeHedgeRatios(slope)
    target <- zoo(matrix(c(-5.0,-6.6,-10.1,-4.5,1,1,1,1),nrow = 4,ncol = 2),as.POSIXct(dates))
    colnames(target) <- c("hedgeY","hedgeX")    
    assert(index(result)==index(target))       
    index(target) <- index(result)
    checkEquals(target,result)
    
    # when normalized to X with a slope equal to 0
    
    slope[3] <- 0
    result <- this$getSlopeHedgeRatios(slope)
    target <- target[-3,]
    assert(index(result)==index(target))       
    index(target) <- index(result)
    checkEquals(target,result)
    
    # when hedge ratios are filtered
    
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX,normalizeToY = FALSE,hedgeMinY = -5)
    
    result <- this$getSlopeHedgeRatios(slope)
    target <- target[3,]
    colnames(target) <- c("hedgeY","hedgeX")    
    assert(index(result)==index(target))       
    index(target) <- index(result)
    checkEquals(target,result)   
}

test.Pair.getEqualExposureHedgeRatios <- function(){
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX)   
    predictedY <- zoo(c(2,1,0.5,0),dates)
    predictedX <- zoo(c(99,101,103,101),dates) 

    result <- this$getEqualExposureHedgeRatios(slope,predictedY,predictedX)
        
    target <- zoo(matrix(c(1,1,1,1,-0.1,-0.0505050505050505,-0.01485148514851485,-0.1111111111111111),nrow = 4,ncol = 2),as.POSIXct(dates))
    colnames(target) <- c("hedgeY","hedgeX")
    assert(index(result)==index(target))       
    index(target) <- index(result)
    checkEquals(target,result)   
}

test.Pair.calculateZooFromHedgeRatios <- function(){
	
	seriesY.test <- seriesY
	seriesX.test <- seriesX[c(1,2,4)]
	
	testPair <- Pair(seriesY.test,seriesX.test)
	
	hedgeRatios <- zoo(c(1,2,3),dates[1:3])
	expected <- zoo(c(-2.5,7.5),dates[c(2,4)])
	checkSameLooking(expected, testPair$calculateZooFromHedgeRatios(hedgeRatios))
	
	hedgeRatios <- zoo(c(1),dates[3])
	checkSame(testPair$calculateZooFromHedgeRatios(hedgeRatios),'Not enough dates to calculate')
	
	hedgeRatios <- zoo(c(1,2,3),dates[1:3])
	expected <- zoo(c(-2.5,7.5),dates[c(2,4)])
	checkSameLooking(expected, testPair$calculateZooFromHedgeRatios(hedgeRatios,stripTimes=TRUE))	
}

test.Pair.getFirstDateFromZooFromHedgeRatios <- function(){
	seriesY.test <- seriesY
	seriesX.test <- seriesX[c(1,2,4)]
	
	testPair <- Pair(seriesY.test,seriesX.test)
	
	hedgeRatios <- zoo(c(1,2,3),dates[1:3])
	expected <- dates[1]
	testPair$calculateZooFromHedgeRatios(hedgeRatios)
	checkSameLooking(expected, testPair$getFirstDateFromZooFromHedgeRatios())
}

test.Pair.constantHedgeRatioOfMarketValues <- function(){
    seriesY.test <- seriesY
    seriesX.test <- seriesX[c(1,2,4)]
    
    testPair <- Pair(seriesY.test, seriesX.test)
    
    expected <- zoo(c(-0.52, 1.51470588235294), dates[c(2,4)])
    checkSameLooking(expected, testPair$constantHedgeRatioOfMarketValues(1.0))
    
    expected <- zoo(c(-0.51, 1.50735294117647), dates[c(2,4)])
    checkSameLooking(expected, testPair$constantHedgeRatioOfMarketValues(0.5))
    
    shouldBombMatching(testPair$constantHedgeRatioOfMarketValues("test"), "not numeric is character")
}    
    
test.Pair.stripAndMerge <- function(){
    testPair <- Pair(seriesY, seriesZ)
    
    expected1 <- zoo(0.5, dates[2])
    expected2 <- zoo(3, dates[2])
    checkSameLooking(expected1, testPair$stripAndMerge(FALSE)[,1])
    checkSameLooking(expected2, testPair$stripAndMerge(FALSE)[,2])
    
    expected1 <- zoo(c(1.0,0.5,0.8), dates[1:3])
    expected2 <- zoo(c(2,3,4), dates[1:3])
    checkSameLooking(expected1, testPair$stripAndMerge(TRUE)[,1])
    checkSameLooking(expected2, testPair$stripAndMerge(TRUE)[,2])
    
    shouldBombMatching(testPair$stripAndMerge("test"), "not logical is character")
}  

test.Pair.constantHedgeRatioOfNotional <- function(){
    seriesY.test <- seriesY
    seriesX.test <- seriesX[c(1,2,4)]
    
    testPair <- Pair(seriesY.test, seriesX.test)
    
    expected <- zoo(c(-2.5,4.5), dates[c(2,4)])
    checkSameLooking(expected, testPair$constantHedgeRatioOfNotional(1.0))
    
    expected <- zoo(c(-1.5,3.0), dates[c(2,4)])
    checkSameLooking(expected, testPair$constantHedgeRatioOfNotional(0.5))
    
    shouldBombMatching(testPair$constantHedgeRatioOfNotional("test"), "not numeric is character")
}
    
