getCSIData <- function(symbol,startDate = NULL,endDate = NULL,fields = NULL)
{
    fullPath <- getCSIFullPath(symbol)
    cat("Loaded: ",fullPath,"\n")
    data <- read.table(fullPath,sep = ",",header = TRUE,stringsAsFactors = FALSE)
    
    result <- zoo(data[,-1],as.POSIXct(as.character(as.Date(as.character(data[,1]),format = "%Y%m%d"))))
    cNames <- colnames(result)
    
    if(!is.null(fields)){
        assert(fields %in% c("Open","High","Low","Close","Total.Volume","Total.Open.Interest","Numeric.Delivery.Month"))
        result <- result[,fields]
        cNames <- fields 
    }     
    
    if(!is.null(startDate)){
        startDate <- as.POSIXct(startDate)
        result <- result[index(result) >= startDate,]
    }
    if(!is.null(endDate)){
        endDate <- as.POSIXct(endDate)
        result <- result[index(result) <= endDate,]
    }
    
    result <- getZooDataFrame(result)
    colnames(result) <- cNames

    if(NROW(result)==0)return(NULL)
    return(result)
}

getCSIFullPath <- function(symbol)
{
    groupNames <- c(
        "Core Agricultural Futures","Core Bond Futures","Core Commodity Index Futures",
        "Core Energy Futures","Core Equity Futures","Core FX Futures","Core Livestock Futures",
        "Core Metal Futures,Core Short Rate Futures","Core Swap Futures"
    )
    
    for (i in 1:NROW(groupNames))
    {
        path <- paste(dataDirectory(),"Futures/CSI","/",groupNames[i],"/",squish(symbol,"_0_&0B"),".CSV",sep = "")
        if(file.exists(path))return(path)
        path <- paste(dataDirectory(),"Futures/CSI","/",groupNames[i],"/",squish(symbol,"_&0B"),".CSV",sep = "")
        if(file.exists(path))return(path)
        path <- paste(dataDirectory(),"Futures/CSI","/",groupNames[i],"/",squish(symbol,"_0010B"),".CSV",sep = "")
        if(file.exists(path))return(path)        
    }
    fail("Invalid file symbol: ",symbol)
}

getCSIFuturesMultiplier <- function(symbol){
    if(symbol == "ES")return(50)
    if(symbol == "TY")return(1000)
    fail("Not valid symbol")
}




