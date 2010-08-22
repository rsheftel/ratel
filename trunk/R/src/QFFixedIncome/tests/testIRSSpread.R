## Test file for the IRSSpread object
library(QFFixedIncome)

startDate = "2008-01-01"
endDate = "2008-01-05"
ccy = "usd"
swapTenor = "10y"
dataSource = "internal"
dataTimeStamp = Close$NY.irs
bondIssuer = "us_treasury"
bondSector = "government"
exportSource = "internal"
updateTSDB = FALSE

test.IRSSpread.constructor <- function(){
     this <- IRSSpread(ccy = ccy,dataSource = dataSource,dataTimeStamp = dataTimeStamp,
        bondIssuer = bondIssuer,bondSector = bondSector,exportSource = exportSource) 

    checkEquals(this$.ccy,"usd")
    checkEquals(this$.swapInstrument,"irs")        
    checkEquals(this$.dataTimeStamp,Close$NY.irs)
    checkEquals(this$.bondInstrument,"bond")
    checkEquals(this$.bondIssuer,"us_treasury")    
    checkEquals(this$.bondSector,"government")
    checkEquals(this$.dataSource,"internal")
    checkEquals(this$.exportSource,"internal")
    assert("TimeSeriesDB" %in% class(this$.tsdb))
}

test.IRSSpread.updateSpreadSeriesByTenor <- function(){
     this <- IRSSpread(ccy = ccy,dataSource = dataSource,dataTimeStamp = dataTimeStamp,
        bondIssuer = bondIssuer,bondSector = bondSector,exportSource = exportSource)

    result <- this$updateSpreadSeriesByTenor(startDate = "2008-01-01",endDate = "2008-01-05",swapTenor = "10y",bondYieldType = "otr",modified = "1n")
    target <- zoo(c(0.6349974127449998,0.6475033600739999,0.6249969028690003),as.POSIXct(c("2008-01-02 15:00:00","2008-01-03 15:00:00","2008-01-04 15:00:00")))
    checkEquals(result,target)
    
    shouldBomb(this$updateSpreadSeriesByTenor(startDate = "1970-01-01",endDate = "1970-01-05",swapTenor = "10y",modified = "1n"))
}