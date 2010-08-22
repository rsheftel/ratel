## Test file for the PairRollingRegression object
library(QFPairsTrading)

print("PairRollingRegression Test: this is slow!")

window <- 10
dates <- as.POSIXct(c("2001-01-01","2001-01-02","2001-01-03","2001-01-04"))
seriesX <- zoo(c(100,102,101,99),dates)
seriesY <- zoo(c(1,0.5,0.8,2),dates)
slope <- zoo(c(2,1.5,1,2),dates)
multiplierY <- zoo(c(1,1,1,1),dates)
multiplierX <- zoo(c(10,9.9,10.1,9),dates)
triY <- zoo(c(100,98,98.2,98.5,99),dates)
triX <- zoo(c(200,195,194,193,197),dates)

test.data <- data.frame(read.csv(system.file("testdata","testfilePairRollingRegression.data.csv", package = "QFPairsTrading"), sep = ",", header = TRUE))
test.data <- zoo(test.data[,-1],as.POSIXct(test.data[,1]))    
pair <- Pair(test.data[,1],test.data[,2],test.data[,3],test.data[,4],test.data[,5],test.data[,6])     
result.percent.changes.no.constant <- pair$runPercentChangesRollingRegression(window,constant = FALSE,storeIn = "ST.no.constant")

test.PairRollingRegression.constructor <- function(){
    this <- Pair(seriesY,seriesX,triY,triX,multiplierY,multiplierX)
    assert(all(c("PairModels","PairRollingRegression","Pair") %in% class(this)))
}

test.PairRollingRegression.runPercentChangesRollingRegression <- function(){
    # No constant
    test.result <- data.frame(read.csv(system.file("testdata","testfilePairRollingRegression.runPercentChangesNoConstant.result.csv", package = "QFPairsTrading"), sep = ",", header = TRUE))
    test.result <- zoo(test.result[,-1],as.POSIXct(test.result[,1]))
    checkEquals(result.percent.changes.no.constant,getZooDataFrame(test.result)) 
    checkEquals(pair$.ST.no.constant.10,result.percent.changes.no.constant)
}

test.PairRollingRegression.getTriChangesZoo <- function(){
    getTriChangesZoo.res <- pair$getTriChangesZoo("ST.no.constant.10",diffSeq = c(1,5,10,15,30,60,90,180))
    test.result <- data.frame(read.csv(system.file("testdata","testfilePairRollingRegression.getTriChangesZoo.result.csv", package = "QFPairsTrading"), sep = ",", header = TRUE))
    test.result <- getZooDataFrame(zoo(test.result[,-1],as.POSIXct(test.result[,1])))
    options(scipen = 5000)
    checkEquals(as.numeric(test.result),as.numeric(getTriChangesZoo.res))
    assert(index(test.result)==index(getTriChangesZoo.res))
    
    # Should bombs
    shouldBomb(pair$getTriChangesZoo("ST.no.constant.10",diffSeq = c(1,5,10,15,30,60,90,1207)))
    shouldBomb(pair$getTriChangesZoo("ST.no.constant.10",diffSeq = TRUE))
    shouldBomb(pair$getTriChangesZoo(TRUE))
}

test.getModelResults <- function(){
    checkSame(pair$.ST.no.constant.10,pair$getModelResults("ST.no.constant",10))
    shouldBomb(pair$getModelResuts(TRUE,10))
    shouldBomb(pair$getModelResuts("LT.no.constant","junk"))
    shouldBombMatching(pair$getModelResults("does.not.exist",10),"No rolling regression in memory")
    shouldBombMatching(pair$getModelResults("LT.no.constant",50),"No rolling regression in memory")    
}