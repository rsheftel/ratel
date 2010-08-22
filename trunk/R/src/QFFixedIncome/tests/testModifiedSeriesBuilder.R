## Test file for the ModifiedSeriesBuilder object
library(QFFixedIncome)

test.ModifiedSeriesBuilder.constructor <- function(){
    this <- ModifiedSeriesBuilder()
    assert("TimeSeriesDB" %in% class(this$.tsdb))
}

test.ModifiedSeriesBuilder.getGapBackwardAdjustedZoo <- function(){
    this <- ModifiedSeriesBuilder()

    adjustment <- "simple"
    dates <- as.POSIXct(c("2007-08-01","2007-08-02","2007-08-04","2007-08-05","2007-08-06","2007-08-07","2007-08-08","2007-08-09","2007-08-10"))
    zooFront <- zoo(c(100,98,99,50,55,60,120,118,110),dates)
    zooBack <- zoo(c(200,198,199,98,102,105,45,43,42),dates)
    gapZooIndexDates <- as.POSIXct(c("2007-08-05","2007-08-08"))

    # General case
    result.1 <- this$getGapBackwardAdjustedZoo(zooFront,zooBack,gapZooIndexDates,adjustment = "ratio")
    checkEquals(result.1,zoo(c(136.0544217687075,133.3333333333333,134.6938775510204,133.3333333333333,146.6666666666667,160,120,118,110),dates))

    # Case where roll is last date
    result.2 <- this$getGapBackwardAdjustedZoo(zooFront[1:7],zooBack[1:7],gapZooIndexDates,adjustment = "ratio")
    checkEquals(result.1[1:7],result.2)

    # Case where rill is first date
    result.3 <- this$getGapBackwardAdjustedZoo(zooFront[4:9],zooBack[4:9],gapZooIndexDates,adjustment = "ratio")
    checkEquals(result.3,result.1[4:9])

    # Case where no adjustment necessary
    result.4 <- this$getGapBackwardAdjustedZoo(zooFront,zooBack,as.POSIXct("2006-01-01"),adjustment = "ratio")
    checkEquals(result.4,zooFront)

    # Case where we only have one date
    result.5 <- this$getGapBackwardAdjustedZoo(zooFront[1],zooBack[1],as.POSIXct("2007-08-01"),adjustment = "ratio")
    checkEquals(result.5,zooFront[1])

    # Should bombs
    shouldBomb(getGapBackwardAdjustedZoo(zooFront,zooBack[-1],gapZooIndexDates,adjustment = "ratio"))
    shouldBomb(getGapBackwardAdjustedZoo(as.numeric(zooFront),zooBack,gapZooIndexDates,adjustment = "ratio"))
    shouldBomb(getGapBackwardAdjustedZoo(zooFront,zooBack,gapZooIndexDates,adjustment = "sim"))
}

test.ModifiedSeriesBuilder.getGapDatesFromMaturityDateSeries <- function(){
    this <- ModifiedSeriesBuilder()
    zooFrontMaturity <- (this$.tsdb)$retrieveTimeSeriesByName("bond_government_usd_10y_otr_maturity",start = "2007-01-01",end = "2007-08-10",data.source = "internal")[[1]]
    zooBackMaturity <- (this$.tsdb)$retrieveTimeSeriesByName("bond_government_usd_10y_1o_maturity",start = "2007-01-01",end = "2007-08-10",data.source = "internal")[[1]]

    # case three dates
    res <- this$getGapDatesFromMaturityDateSeries(zooFrontMaturity,zooBackMaturity)
    checkEquals(structure(c(1170878400, 1178650800, 1186599600), class = c("POSIXt","POSIXct")),res)
    
    # case no date
    res <- this$getGapDatesFromMaturityDateSeries(zooFrontMaturity[1],zooBackMaturity[1])
    checkEquals(res,NULL)
    
    # case first date
    res <- this$getGapDatesFromMaturityDateSeries(zooFrontMaturity[20:26],zooBackMaturity[20:26])
    checkEquals(structure(1170878400, class = c("POSIXt", "POSIXct")),res)

    # case last date
    res <- this$getGapDatesFromMaturityDateSeries(zooFrontMaturity[25:30],zooBackMaturity[25:30])
    checkEquals(structure(1170878400, class = c("POSIXt", "POSIXct")),res)

    shouldBomb(this$getGapDatesFromMaturityDateSeries(zooFrontMaturity[1],zooBackMaturity))
    shouldBomb(this$getGapDatesFromMaturityDateSeries(TRUE,zooBackMaturity))
}