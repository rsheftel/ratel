## Test file for the EquityListDataLoader object
library("QFEquity")

testGetClosePrices <- function()
{        
    loaderSample <- EquityListDataLoader()

    shouldBomb(loaderSample$getClosePrices())
    shouldBomb(loaderSample$getClosePrices(securityIDList = TRUE,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getClosePrices(securityIDList = c("DS",1),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getClosePrices(securityIDList = c(105175,110433),source = "ivy",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "SD",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "SD"))
    shouldBomb(loaderSample$getClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2000-01-03"))
    shouldBomb(loaderSample$getClosePrices(securityIDList = c(0),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))

    target <- getZooDataFrame(zoo(matrix(c(40.3,24.85),nrow = 1, ncol = 2),order.by = "2005-01-03"))
    colnames(target) <- c(105175,110433)

    checkEquals(
        target,
        loaderSample$getClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
    checkEquals(
        target,
        loaderSample$getClosePrices(securityIDList = c(105175,110433),source = "internal",startDate = "2005-01-01",endDate = "2005-01-03")
    )
}

testGetPrices <- function()
{        
	target <- getZooDataFrame(zoo(matrix(c(40.65,24.83),nrow = 1, ncol = 2),order.by = "2005-01-03"))
	colnames(target) <- c(105175,110433)
	checkEquals(
		target,
		EquityListDataLoader$getPrices(securityIDList = c(105175,110433),"open","price",source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
	)
}

testGetAdjClosePrices <- function()
{        
    loaderSample <- EquityListDataLoader()

    shouldBomb(loaderSample$getAdjClosePrices())
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = TRUE,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = c("DS",1),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = c(105175,110433),source = "ivy",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "SD",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "SD"))
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2000-01-03"))
    shouldBomb(loaderSample$getAdjClosePrices(securityIDList = c(0),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
}

testGetVolumes <- function()
{        
    loaderSample <- EquityListDataLoader()

    shouldBomb(loaderSample$getVolumes())
    shouldBomb(loaderSample$getVolumes(securityIDList = TRUE,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getVolumes(securityIDList = c("DS",1),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getVolumes(securityIDList = c(105175,110433),source = "ivy",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getVolumes(securityIDList = c(105175,110433),source = "ivydb",startDate = "SD",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getVolumes(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "SD"))
    shouldBomb(loaderSample$getVolumes(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2000-01-03"))
    shouldBomb(loaderSample$getVolumes(securityIDList = c(0),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))

    target <- getZooDataFrame(zoo(matrix(c(6518500,13407700),nrow = 1, ncol = 2),
                    order.by = "2005-01-03"))
    colnames(target) <- c(105175,110433)

    checkEquals(
        target,
        loaderSample$getVolumes(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
}

test.getTotalLiabilities <- function()
{        
    loaderSample <- EquityListDataLoader()

    shouldBomb(loaderSample$getTotalLiabilities())
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = TRUE,source = "bloomberg",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = c("DS",1),source = "bloomberg",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = c(105175,10433),source = "ivy",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = c(105175,110433),source = "bloomberg",startDate = "SD",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = c(105175,110433),source = "bloomberg",startDate = "2005-01-01",endDate = "SD"))
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = c(105175,110433),source = "bloomberg",startDate = "2005-01-01",endDate = "2000-01-03"))
    shouldBomb(loaderSample$getTotalLiabilities(securityIDList = c(0),source = "bloomberg",startDate = "2005-01-01",endDate = "2005-01-03"))

    target <- getZooDataFrame(zoo(matrix(c(454590,27553),nrow = 1, ncol = 2),
                    order.by = "2005-01-03"))
    colnames(target) <- c(105175,110433)

    checkEquals(
        target,
        loaderSample$getTotalLiabilities(securityIDList = c(105175,110433),source = "bloomberg",startDate = "2005-01-01",endDate = "2005-01-03")
    )
}

testGetSharesOutstanding <- function()
{        
    loaderSample <- EquityListDataLoader()

    shouldBomb(loaderSample$getSharesOutstanding())
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = TRUE,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = c("DS",1),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = c(105175,110433),source = "ivy",startDate = "2005-01-01",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = c(105175,110433),source = "ivydb",startDate = "SD",endDate = "2005-01-03"))
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "SD"))
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2000-01-03"))
    shouldBomb(loaderSample$getSharesOutstanding(securityIDList = c(0),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03"))

    target <- getZooDataFrame(zoo(matrix(c(564.826,1385.222),nrow = 1, ncol = 2),
                    order.by = "2005-01-03"))
    colnames(target) <- c(105175,110433)

    checkEquals(
        target,
        loaderSample$getSharesOutstanding(securityIDList = c(105175,110433),source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
}
