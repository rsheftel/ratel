
#Test functions for the TBAMatrix class

library(QFMortgage)

test.TBAMatix <- function(){

    tbaMatrix <- TBAMatrix('fncl',c(4.5,5.0,5.5))
    checkInherits(tbaMatrix,'TBAMatrix')
}

test.setHistoryFromTSDB <- function() {

    tbaMatrix <- TBAMatrix('fncl',c(4.5,5.0,5.5))
    tbaMatrix$setHistoryFromTSDB()
}

test.getPriceMatrix <- function() {

    tbaMatrix <- TBAMatrix('fncl',c(5.0,5.5,6,6.5))
    tbaMatrix$setHistoryFromTSDB()
    dataDate <- as.POSIXct('2007-02-07 15:00:00')
    
    #Normal
    expected <- c(96.55469,96.55859625,96.56640875,96.570315)
    expected <- rbind(expected, c(98.85938,98.84766125,98.8359425,98.8203175))
    expected <- rbind(expected, c(100.65625,100.625,100.5976563,100.5664063))
    expected <- rbind(expected, c(101.82812,101.8046825,101.8046825,101.781245))
    
    checkSameLooking(expected, tbaMatrix$getPriceMatrix(dataDate, c(5.0,5.5,6.0,6.5)))
    
    #Bad coupons
    expected <- c(NA,NA,NA,NA)
    expected <- rbind(expected, c(98.85938,98.84766125,98.8359425,98.8203175))
    expected <- rbind(expected, c(NA,NA,NA,NA))
    expected <- rbind(expected, c(101.82812,101.8046825,101.8046825,101.781245))

    checkSameLooking(expected, tbaMatrix$getPriceMatrix(dataDate, c(4.5,5.5,9,6.5)))
}

test.getSettleDateMatrix <- function() {

    tbaMatrix <- TBAMatrix('fncl',c(5.0,5.5,6,6.5))
    tbaMatrix$setHistoryFromTSDB()
    dataDate <- as.POSIXct('2007-02-07 15:00:00')

    #Normal
    expected <- c(20070313,20070412,20070514,20070612)
    expected <- rbind(expected, c(20070313,20070412,20070514,20070612))
    expected <- rbind(expected, c(20070313,20070412,20070514,20070612))
    expected <- rbind(expected, c(20070313,20070412,20070514,20070612))

    checkSameLooking(expected, tbaMatrix$getSettleDateMatrix(dataDate, c(5.0,5.5,6.0,6.5)))

    #Bad coupons
    expected <- c(NA,NA,NA,NA)
    expected <- rbind(expected, c(20070313,20070412,20070514,20070612))
    expected <- rbind(expected, c(NA,NA,NA,NA))
    expected <- rbind(expected, c(20070313,20070412,20070514,20070612))

    checkSameLooking(expected, tbaMatrix$getSettleDateMatrix(dataDate, c(4.5,5.5,9,6.5)))
}

test.getPriceZoo <- function() {

    tbaMatrix <- TBAMatrix('fncl',c(4.5,5.0,5.5))
    tbaMatrix$setHistoryFromTSDB()
    
    expected <- 97.25
    
    checkSame(expected, tbaMatrix$getPriceZoo()[1,11][[1]])
}

test.getSettleZoo <- function() {

    tbaMatrix <- TBAMatrix('fncl',c(4.5,5.0,5.5))
    tbaMatrix$setHistoryFromTSDB()

    expected <- 19981210

    checkSame(expected, tbaMatrix$getSettleZoo()[1,11][[1]])
}
