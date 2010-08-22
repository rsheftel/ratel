library(QFFixedIncome)

instrument = "irs"
ccy = "usd"
dataTimeStamp = Close$NY.irs

startDate <- "2008-01-01"
endDate <- "2008-01-05"
swapTenorShort <- "5y"
swapTenorLong <- "10y"
updateTSDB <- FALSE


test.IRSSlopeTri.constructor <- function(){
    this <- IRSSlopeTri(instrument = "irs",ccy = "usd",dataTimeStamp = Close$NY.irs)

    checkEquals(this$.principal,100)
    checkEquals(this$.ccy,"usd")
    checkEquals(this$.instrument,"irs")
    checkEquals(this$.dataTimeStamp,Close$NY.irs)
    checkEquals(this$.triSource,"internal")
    assert("TimeSeriesDB" %in% class(this$.tsdb))
}

test.IRSSlopeTri.calcTriSeries <- function(){

    this <- IRSSlopeTri(instrument = "irs",ccy = "usd",dataTimeStamp = Close$NY.irs)

    # several dates
    m <- this$getMergedSeries(swapTenorShort,swapTenorLong,startDate,endDate)
    result <- this$calcDailyTriSeries(m)
    
    target <- structure(c(-0.0972178552539449,-0.3099142916908365), index = structure(c(1199336400,
        1199422800), class = c("POSIXt", "POSIXct")), class = "zoo")
    checkEquals(result,target)
    result <- this$updateSlopeTriByTenor(swapTenorShort,swapTenorLong,startDate,endDate,updateTSDB = FALSE)
    target <- structure(c(-0.0972178552539449,-0.3099142916908365), index = structure(c(1199390400,
        1199476800), class = c("POSIXt", "POSIXct"), tzone = ""), class = "zoo")
    checkEquals(result,target)

    # one date
    startDate = "2008-01-03"
    endDate = "2008-01-04"
    m <- this$getMergedSeries(swapTenorShort,swapTenorLong,startDate,endDate)
    result <- this$calcDailyTriSeries(m)
    target.2 <- structure(-0.3099142916908365, index = structure(1199422800, class = c("POSIXt",
        "POSIXct")), class = "zoo")
    checkEquals(result,target.2)
    result <- this$updateSlopeTriByTenor(swapTenorShort,swapTenorLong,startDate,endDate, updateTSDB = FALSE)
    checkEquals(result,target[2])

    # should bombs
    startDate = "2008-01-03"
    endDate = "2008-01-03"
    shouldBomb(this$getMergedSeries(swapTenorShort,swapTenorLong,startDate,endDate))
    shouldBomb(this$updateSlopeTriByTenor(swapTenorShort = "10y",swapTenorLong = "5y",startDate = "2008-01-03",endDate = "2008-01-05"))
}