## Test file for the IRSDataLoader object
library(QFFixedIncome)

testIRSDataLoader <- function()
{
    # test bad inputss
    
    loaderSample <- IRSDataLoader()
    irsSample <- GenericIRS()
    irsSample$setDefault(tenor = "5y")
    
    shouldBomb(loaderSample$getSpreads())
    shouldBomb(loaderSample$getSpreads(irsObj = IRS(),source = "internal",startDate = "2007-05-03",endDate = "2007-07-03"))
    shouldBomb(loaderSample$getSpreads(irsObj = irsSample,source = "int",startDate = "2007-05-03",endDate = "2007-07-03"))
    shouldBomb(loaderSample$getSpreads(irsObj = irsSample,source = "internal",startDate = TRUE,endDate = "2007-07-03"))
    shouldBomb(loaderSample$getSpreads(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = FALSE))
    shouldBomb(loaderSample$getSpreads(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = "2000-07-03"))
    
    shouldBomb(loaderSample$getOneDateSpread())
    shouldBomb(loaderSample$getOneDateSpread(irsObj = IRS(),source = "internal",myDate = "2007-07-03",defaultLimit = 3))
    shouldBomb(loaderSample$getOneDateSpread(irsObj = irsSample,source = "int",myDate = "2007-07-03",defaultLimit = 3))
    shouldBomb(loaderSample$getOneDateSpread(irsObj = irsSample,source = "internal",myDate = FALSE,defaultLimit = 3))
    shouldBomb(loaderSample$getOneDateSpread(irsObj = irsSample,source = "internal",myDate = "2007-07-03",defaultLimit = -1))

    shouldBomb(loaderSample$getCurves())
    shouldBomb(loaderSample$getCurves(irsObj = IRS(),source = "internal",startDate = "2007-05-03",endDate = "2007-07-03"))
    shouldBomb(loaderSample$getCurves(irsObj = irsSample,source = "int",startDate = "2007-05-03",endDate = "2007-07-03"))
    shouldBomb(loaderSample$getCurves(irsObj = irsSample,source = "internal",startDate = TRUE,endDate = "2007-07-03"))
    shouldBomb(loaderSample$getCurves(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = FALSE))
    shouldBomb(loaderSample$getCurves(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = "2000-07-03"))

    shouldBomb(loaderSample$getOneDateCurve())
    shouldBomb(loaderSample$getOneDateCurve(irsObj = IRS(),source = "internal",myDate = "2007-07-03",defaultLimit = 3))
    shouldBomb(loaderSample$getOneDateCurve(irsObj = irsSample,source = "int",myDate = "2007-07-03",defaultLimit = 3))
    shouldBomb(loaderSample$getOneDateCurve(irsObj = irsSample,source = "internal",myDate = FALSE,defaultLimit = 3))
    shouldBomb(loaderSample$getOneDateCurve(irsObj = irsSample,source = "internal",myDate = "2007-07-03",defaultLimit = -1))
    
    # test getCurves
    
    target <- getZooDataFrame(zoo(
                    matrix(c(
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342),nrow = 1, ncol = 16),
                    order.by = "2007-05-03"))
    colnames(target) <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")

    checkEquals(target,
        loaderSample$getCurves(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = "2007-05-03")
    )
    
    # test getOneDateCurve
     
    checkEquals(loaderSample$getCurves(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = "2007-05-03"),
        loaderSample$getOneDateCurve(irsObj = irsSample,source = "internal",myDate = "2007-05-03",defaultLimit = 0)
    )
    
    # test getOneDateSpread
     
    checkEquals(loaderSample$getSpreads(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = "2007-05-03"),
        loaderSample$getOneDateSpread(irsObj = irsSample,source = "internal",myDate = "2007-05-03",defaultLimit = 0)
    )
}

testIRSDataLoaderGetSpreadsWithDefaultFilter <- function()
{
    loaderSample <- IRSDataLoader()
    irsSample <- GenericIRS()
    irsSample$setDefault(tenor = "5y")
    
    target <- getZooDataFrame(zoo(c(5.06248,5.02598),order.by = c("2007-05-03","2007-05-04")))
    colnames(target) <- "5y"
    
    checkEquals(target,
        loaderSample$getSpreads(irsObj = irsSample,source = "internal",startDate = "2007-05-03",endDate = "2007-05-04")
    )
}

testIRSDataLoaderWhenInvalidFilterSupplied <- function() { 
    loader <- IRSDataLoader(filter = "13:00:00") # not the NY close that data exists for
    irs <- GenericIRS()
    irs$setDefault(tenor="5y")
    shouldBomb(loader$getSpreads(irsObj = irs, source = "internal", startDate =  "2007-05-03",endDate = "2007-05-04"))
    shouldBomb(loader$getCurves(irsObj = irs, source = "internal", startDate =  "2007-05-03",endDate = "2007-05-04"))
    shouldBomb(loader$getOneDateSpread(irsObj = irs, source = "internal", startDate =  "2007-05-03",endDate = "2007-05-04"))
    shouldBomb(loader$getOneDateCurve(irsObj = irs, source = "internal", startDate =  "2007-05-03",endDate = "2007-05-04"))
}