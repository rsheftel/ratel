characterToNumericTenor <- function(tenors,...)
{   
    assert(all(tenors %in% TermStructure$all),"Invalid character tenor") 
    result <- array()
    for(i in 1:length(tenors)){
        assert(any(tenors[i]== TermStructure$all),paste(tenors[i],"is not a valid tenor"))
        lastLetter <- substr(tenors[i],nchar(tenors[i]),nchar(tenors[i]))
        if(lastLetter=="y"){
            result[i] <- substr(tenors[i],1,nchar(tenors[i])-1)
        }else if(lastLetter=="m"){
            result[i] <- as.numeric(substr(tenors[i],1,nchar(tenors[i])-1))/12
        }else if(lastLetter=="w"){
            result[i] <- as.numeric(substr(tenors[i],1,nchar(tenors[i])-1))/52
        }
        if(tenors[i]=="on"){
            result[i] <- as.numeric(1/365)
        }
    }
    return(as.numeric(result))
}

numericToCharacterTenor <- function(tenors,...)
{                 
    characterTenors <- c("6m","18m",TermStructure$all[substr(TermStructure$all,nchar(TermStructure$all),nchar(TermStructure$all))=="y"])
    numericTenors <- characterToNumericTenor(characterTenors)
    assert(all(tenors %in% numericTenors),"Invalid numeric tenor") 
    characterTenors[match(tenors,numericTenors)]
}

getTermStructureForTimeSeries <- function(timeSeriesName,tenorList,source,startDate = NULL,endDate = NULL,lookFor = "tenor"){
    if(!is.null(startDate))startDate<- as.POSIXct(startDate)
    if(!is.null(endDate))endDate<- as.POSIXct(endDate)
    needs(timeSeriesName = "character",tenorList = "character",source = "character")

    tsNames <- NULL; for(i in 1:NROW(tenorList))tsNames <- c(tsNames,sub(lookFor,tenorList[i],timeSeriesName))
    tsdb <- TimeSeriesDB()
    data <- tsdb$retrieveTimeSeriesByName(tsNames,data.source = source,start = startDate,end = endDate,arrange.by = lookFor) 
    data <- TSDataLoader$matrixToZoo(data)
    if(is.null(data))return(data)
    
    # set column names and leave NA columns for tenors that are empty
	tenors <-  tsdb$lookupAttributesForTimeSeries(colnames(data), attributes = lookFor)
	sorted.tenors <- sapply(colnames(data), function(time.series.name) as.character(tenors[time.series.name, lookFor]))
	data <- data[,match(tenorList,sorted.tenors)]
   	data <- getZooDataFrame(data)
  	colnames(data) <- tenorList
  	
    rm(tsdb)
    return(data)
}

getInterpolatedTermStructure <- function(zooCurve,numTenorList,numTenorListTarget,type = 1,acrossTime = FALSE){    
    # type (see fincad "aaInterp"  function):
        # 1 - linear 
        # 2 - cubic spline 
        # 3 - exponential 
        # 4 - linear spot rates 
        
    needs(zooCurve = "zoo",numTenorList = "numeric",type = "numeric",numTenorListTarget = "numeric")
    nCol <- NROW(numTenorListTarget) 
    colNames <- numTenorListTarget 
    if(acrossTime){
        assert(NROW(zooCurve)==NROW(numTenorListTarget))
        assert(NCOL(numTenorListTarget)==1)
        nCol <- 1
        colNames <- "interpolated"
    }

    colnames(zooCurve) = numTenorList
    indexZoo <- index(zooCurve)
    valueZoo <- array(,dim = c(NROW(indexZoo),nCol))
    for(i in 1:NROW(zooCurve)){
        notNaVector <- !is.na(zooCurve[i,])
        cleanCurve <- as.numeric(zooCurve[i,notNaVector])
        numColNames <- as.numeric(numTenorList[notNaVector])
    
        tenorWanted <- numTenorListTarget 
        if(acrossTime)tenorWanted <- numTenorListTarget[i] 
    
        if(NROW(cleanCurve) > 0){
            res <- fincad("aaInterp",x_list = tenorWanted,xy_list = data.frame(numColNames,cleanCurve),intrp = type)
            valueZoo[i,] <- t(res)
        }
    }
    res <- getZooDataFrame(zoo(valueZoo,indexZoo))
    colnames(res) <- colNames
    return(res)
}

