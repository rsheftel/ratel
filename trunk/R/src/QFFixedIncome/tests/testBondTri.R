## Test file for the BondTri object
library(QFFixedIncome)

test.BondTri.getTimeSeries <- function(){
    this <- BondTri()
    
    startDate = "2007-11-08"
    endDate = "2007-11-09"
    ccy = "usd"
    maturity = "10y"
    sector = "government"
    issuer = "us_treasury"
    data.source = "internal"
    timeStamp = "15:00:00"
    
    seriesList <- this$getTimeSeries(startDate,endDate,ccy,maturity,sector,issuer,data.source,timeStamp)
    target <- list(
        price.lead = zoo(matrix(c(99.82031,100.21875),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        price.lag = zoo(matrix(c(103.73828,104.11914),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        maturity.lead = zoo(matrix(c(20171115, 20171115),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        maturity.lag = zoo(matrix(c(20170815, 20170815),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        coupon.lead = zoo(matrix(c(4.25, 4.25),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        coupon.lag = zoo(matrix(c(4.75, 4.75),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        repo_on.lead = zoo(matrix(c(4.35, 3.94),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09"))),
        repo_on.lag = zoo(matrix(c(3.825, 3.940),ncol = 1,nrow = 2),as.POSIXct(c("2007-11-08","2007-11-09")))
    )
    checkEquals(target,seriesList)
}

test.BondTri.cleanSeries <- function(){
    seriesList <- list(
        price.lead = zoo(c(99.82031,100.21875),as.POSIXct(c("2007-11-08","2007-11-09"))),
        price.lag = zoo(c(103.73828,104.11914),as.POSIXct(c("2007-11-08","2007-11-09"))),
        maturity.lead = zoo(c(20171115, 20171115),as.POSIXct(c("2007-11-08","2007-11-09"))),
        maturity.lag = zoo(c(20170815, 20170815),as.POSIXct(c("2007-11-08","2007-11-09"))),
        coupon.lead = zoo(c(4.25, 4.25),as.POSIXct(c("2007-11-08","2007-11-09"))),
        coupon.lag = zoo(c(4.75, 4.75),as.POSIXct(c("2007-11-08","2007-11-09"))),
        repo_on.lead = zoo(c(4.35, 3.94),as.POSIXct(c("2007-11-08","2007-11-09"))),
        repo_on.lag = zoo(c(3.825, 3.940),as.POSIXct(c("2007-11-08","2007-11-09")))
    )
    this <- BondTri()
    
    # case where no adjustment is necessary
    
    result <- this$cleanSeries(seriesList)
    checkEquals(unlist(result),unlist(seriesList))
    
    # case where lead repo is missing
    
    seriesList$repo_on.lead[1] = NA
    result <- this$cleanSeries(seriesList)
    
    target <- list(
        price.lead = zoo(c(100.21875),as.POSIXct(c("2007-11-09"))),
        price.lag = zoo(c(104.11914),as.POSIXct(c("2007-11-09"))),
        maturity.lead = zoo(c(20171115),as.POSIXct(c("2007-11-09"))),
        maturity.lag = zoo(c(20170815),as.POSIXct(c("2007-11-09"))),
        coupon.lead = zoo(c(4.25),as.POSIXct(c("2007-11-09"))),
        coupon.lag = zoo(c(4.75),as.POSIXct(c("2007-11-09"))),
        repo_on.lead = zoo(c(3.94),as.POSIXct(c("2007-11-09"))),
        repo_on.lag = zoo(c(3.940),as.POSIXct(c("2007-11-09")))
    )
    checkEquals(unlist(result),unlist(target))
    
    # case where lag price is missing
    
    seriesList$repo_on.lead[1] = 4.35
    seriesList$price.lag[1] = NA
    result <- this$cleanSeries(seriesList)
    checkEquals(unlist(result),unlist(seriesList))
}

test.BondTri.fincadOutputs <- function(){
    settlementDate <- as.POSIXct("2007-11-09")
    fincadCountryCode <- "US"
    price <- 99.82031
    coupon <- 4.25
    maturityDateNumeric <- 20171115
    this <- BondTri()
    result <- this$getFincadOutputs(settlementDate,fincadCountryCode,price,coupon,maturityDateNumeric)
    target <- list(couponDays = 184, dv01 = 8.06762059168179, convexity = 0.761447172511277,yield = 4.272151083985166)
    checkEquals(target,result)
}

test.BondTri.run <- function(){
    startDate = "2007-11-05"
    endDate = "2007-11-09"
    ccy = "usd"
    financialCenter = "nyb"
    maturity = "10y"
    sector = "government"
    issuer = "us_treasury"
    data.source = "internal"
    timeStamp = "15:00:00"
    unitSett = "d"
    numUnitsSett = 1
    accBond = 3
    accRepo = 2
    fincadCountryCode = "US"
    updateTSDB = FALSE
   
    this <- BondTri()
    result <- this$run(startDate,endDate,ccy,financialCenter,data.source,maturity,sector,issuer,timeStamp,numUnitsSett,unitSett,accRepo,accBond,fincadCountryCode,FALSE)

    dates <- as.POSIXct(c("2007-11-06 15:00:00","2007-11-07 15:00:00","2007-11-08 15:00:00","2007-11-09 15:00:00"))
    zooTriDaily <- getZooDataFrame(zoo(c(-0.3029529468599081, 0.1818931642512124, 0.4556266908212608, 0.3979055797101386),dates))
    zooDv01 <- getZooDataFrame(zoo(c(8.033756886346536, 8.025609218246931, 8.067620591681788, 8.098675122639650),dates))
    zooConvexity <- getZooDataFrame(zoo(c(0.7202322302225593, 0.7608063374000199, 0.7614471725112772, 0.7606422167994275),dates))    
    zooYield <- getZooDataFrame(zoo(c(4.355097313299302, 4.328774763725090, 4.272151083985166, 4.222937493283388),dates))
    zooRepoTriDaily <- getZooDataFrame(zoo(c(0.011180555555555556, 0.010694444444444446, 0.011972222222222221, 0.012083333333333333),dates))

    checkEquals(list(zooTriDaily = zooTriDaily,zooDv01 = zooDv01,zooConvexity = zooConvexity,zooYield = zooYield,zooRepoTriDaily = zooRepoTriDaily), result)
    
    # one date
    
    startDate = "2007-11-05"
    endDate = "2007-11-06"
    result <- this$run(startDate,endDate,ccy,financialCenter,data.source,maturity,sector,issuer,timeStamp,numUnitsSett,unitSett,accRepo,accBond,fincadCountryCode,FALSE)
    checkEquals(list(zooTriDaily = getZooDataFrame(zooTriDaily[1]),zooDv01 = getZooDataFrame(zooDv01[1]),zooConvexity = getZooDataFrame(zooConvexity[1]),zooYield = getZooDataFrame(zooYield[1]),zooRepoTriDaily = getZooDataFrame(zooRepoTriDaily[1])), result)
}