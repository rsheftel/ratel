## Test file for the SystemDTD object
library(QFCredit)

getDTDObject <- function(){
	this <- SystemDTD()
	this$runDTD(
		transformationName = "dtd_1.0",
		volExpiry = "91d",
		optionType = "put",
		tickerList = c("f","s"),
		tenor = "5y",
		startDate = "2007-06-11",
		endDate = "2007-06-12",
		updateTSDB = FALSE,
		useConstrOptim = TRUE
	)
	this
}

testPrepareSeedData <- function()
{
	seedZoo <- getZooDataFrame(zoo(
		data.frame(f = c(1,0.5,0),gm = c(2,1.5,1)),
		as.POSIXct(c('2009-04-06','2009-04-07','2009-04-08'))
	))
	# Two columns zoo
	dtd <- SystemDTD()
	res <- dtd$prepareSeedData(seedZoo)	
	target <- getZooDataFrame(zoo(
		data.frame(f = c(1,0.5),gm = c(2,1.5)),
		as.POSIXct(c('2009-04-07','2009-04-08'))
	))
	checkSame(target,res)
	# 1 column zoo
	checkSame(getZooDataFrame(target[,1],'f'),SystemDTD$prepareSeedData(getZooDataFrame(seedZoo[,1],'f')))
	
	# seed missing
	seedZoo[2,1] <- NA
	target[2,1] <- 1 
	dtd <- SystemDTD()
	checkSame(target,dtd$prepareSeedData(seedZoo))	
	seedZoo[1,1] <- NA
	dtd <- SystemDTD()
	checkSame(NROW(SystemDTD$prepareSeedData(seedZoo)),0)	
}

testRunDTD <- function()
{
   	this <- getDTDObject()
    checkEquals(this$.startDate,as.POSIXlt("2007-06-11"))
    checkEquals(this$.endDate,as.POSIXlt("2007-06-12"))
    checkEquals(this$.tenor,"5y")
    checkEquals(this$.useConstrOptim,TRUE)    
    checkEquals(this$.tickerList,c("f","s"))    
    checkEquals(this$.volExpiry,"91d")
    checkEquals(this$.optionType,"put")
    checkEquals(this$.transformationName,"dtd_1.0")
    checkEquals(this$.updateTSDB,FALSE)  
        
    target <- getZooDataFrame(
        zoo(matrix(c(NA,NA,NA,NA),nrow = 2, ncol = 2),order.by = c("2007-06-11","2007-06-12"))
    )
    colnames(target) <- c("f","s")
    
    res <- this$.dataDTD
    checkEquals(dim(res),c(2,2))
    checkEquals(index(res),index(target))
    checkEquals(colnames(res),colnames(target))
	checkSame(last(res),2.00753749428575)
    
    target <- getZooDataFrame(
        zoo(matrix(c(NA,NA,NA,NA),nrow = 2, ncol = 2),order.by = c("2007-06-11","2007-06-12"))
    )
    colnames(target) <- c("f","s")
    
    res <- this$.dataA
    checkEquals(dim(res),c(2,2))
    checkEquals(index(res),index(target))
    checkEquals(colnames(res),colnames(target))
	checkSame(last(res),103090.210431075)
    
    target <- getZooDataFrame(
        zoo(matrix(c(NA,NA,NA,NA),nrow = 2, ncol = 2),order.by = c("2007-06-11","2007-06-12"))
    )
    colnames(target) <- c("f","s")
    
    res <- this$.dataSigmaA
    checkEquals(dim(res),c(2,2))
    checkEquals(index(res),index(target))
    checkEquals(colnames(res),colnames(target))
	checkSame(last(res),0.17894788817823)
}

testFilterDTD <- function()
{
	this <- SystemDTD()
	z <- zoo(c(-9,8),as.POSIXct(c('2009-01-01','2009-01-02')))
	dtdResult <- list(dtdOutputDataDTD=z,dtdOutputDataA=z,dtdOutputDataSigmaA=z)
	checkSame(this$filterDTD(dtdResult),dtdResult)
	z <- zoo(c(-11,8),as.POSIXct(c('2009-01-01','2009-01-02')))	
	zTarget <- zoo(c(NA,8),as.POSIXct(c('2009-01-01','2009-01-02')))
	checkSame(
		this$filterDTD(list(dtdOutputDataDTD=z,dtdOutputDataA=z,dtdOutputDataSigmaA=z)),
		list(dtdOutputDataDTD=zTarget,dtdOutputDataA=zTarget,dtdOutputDataSigmaA=zTarget)
	)
}