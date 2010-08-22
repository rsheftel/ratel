## Test file for the IRSSpreadTri object
library(QFFixedIncome)

startDate <- "2008-01-01"
endDate <- "2008-01-05"
ccy <- "usd"
swapTenor <- "10y"
dataSource <- "internal"
dataTimeStamp <- Close$NY.irs
bondIssuer <- "us_treasury"
bondSector <- "government"
exportSource <- "internal"
couponType <- "otr"
modified <- "1c"
updateTSDB <- FALSE

test.IRSSpreadTri.calcTriSeries <- function(){
    
    this <- IRSSpreadTri(ccy = ccy,dataSource = dataSource,dataTimeStamp = dataTimeStamp,
        bondIssuer = bondIssuer,bondSector = bondSector,exportSource = exportSource) 
    
    # several dates
    m <- this$getMergedSeries(swapTenor,modified,couponType,startDate,endDate)
    result <- this$calcDailyTriSeries(m)
    target <- structure(c(-0.113272608749595, 0.185300828217895), index = structure(c(1199336400, 
        1199422800), class = c("POSIXt", "POSIXct")), class = "zoo")
    checkEquals(result,target)    
    result <- this$updateSpreadTriByTenor(swapTenor,modified,couponType,startDate,endDate,updateTSDB = FALSE)
    target <- structure(c(-0.113272608749595, 0.185300828217895), index = structure(c(1199390400, 
        1199476800), class = c("POSIXt", "POSIXct"), tzone = ""), class = "zoo")
    checkEquals(result,target)
    
    # one date
    startDate = "2008-01-03"
    endDate = "2008-01-04"
    m <- this$getMergedSeries(swapTenor,modified,couponType,startDate,endDate)
    result <- this$calcDailyTriSeries(m)
    target.2 <- structure(0.185300828217895, index = structure(1199422800, class = c("POSIXt", 
        "POSIXct")), class = "zoo")
    checkEquals(result,target.2)    
    result <- this$updateSpreadTriByTenor(swapTenor,modified,couponType,startDate,endDate,updateTSDB = FALSE)
    checkEquals(result,target[2])
    
    # should bombs
    startDate = "2008-01-03"
    endDate = "2008-01-03"
    shouldBomb(this$getMergedSeries(swapTenor,modified,couponType,startDate,endDate))
}

test.IRSSpreadTri.constructor <- function(){
     this <- IRSSpreadTri(ccy = "eur",dataSource = dataSource,dataTimeStamp = dataTimeStamp,
        bondIssuer = bondIssuer,bondSector = bondSector,exportSource = exportSource) 
        
    checkEquals(this$.principal,100)
    checkEquals(this$.ccy,"eur")
    checkEquals(this$.swapInstrument,"irs")        
    checkEquals(this$.dataTimeStamp,Close$NY.irs)
    checkEquals(this$.bondInstrument,"bond")
    checkEquals(this$.bondIssuer,"us_treasury")    
    checkEquals(this$.bondSector,"government")
    checkEquals(this$.dataSource,"internal")
    checkEquals(this$.exportSource,"internal")
    assert("TimeSeriesDB" %in% class(this$.tsdb))
}