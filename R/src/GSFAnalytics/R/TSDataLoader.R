setConstructorS3("TSDataLoader", function(filter = NULL,...)
{
    this <- extend(RObject(), "TSDataLoader",
        .tsdb = NULL,
        .data = NULL,
        .isConnected = FALSE,
        .filter = filter,
        .source = NULL      
    )
})

setMethodS3("init","TSDataLoader",function(this,tsdb,...)
{
    tsdbList <- c("TimeSeriesDB","IvyDB")
    assert(any(class(tsdb) %in% tsdbList),paste(tsdb,"is not a valid time series DB"))    
    this$.tsdb <- tsdb
    this$.isConnected <- TRUE
})

setMethodS3("retrieveData", "TSDataLoader", function(this,source,finObj,retrieveFunction,startDate=NULL, endDate=NULL,...)
{
    if(!is.null(startDate))startDate <- as.POSIXlt(startDate)
    if(!is.null(endDate))endDate <- as.POSIXlt(endDate)
    if(!is.null(startDate) && !is.null(endDate))assert(startDate <= endDate,"end date must be greater than start date")
    
    # initialize

    if(!this$.isConnected)(this$init(this$getDB()))
    
    # check the source

    this$checkSource(source)
    
    # check object
    
    this$checkFinObject(finObj,...)

    # load attributes

    attributeList <- this$getAttributes(finObj,...)

    # load arrange attributes

    arrangeAttributes <- this$getArrangeAttributes(finObj,...)

    # call the database
  
    result <- retrieveFunction(attributes = attributeList,
        data.source = source,start = startDate, end = endDate,arrange.by = arrangeAttributes)

    # format result object
    result <- this$getFormat(result,...)
    result
})

setMethodS3("searchDate", "TSDataLoader", function(this, myDate, source,finObj,defaultLimit, baseFunction,...)
{   
    assert(class(defaultLimit)=="numeric" && (defaultLimit>=0),paste(defaultLimit,"should be numeric >=0"))

    # we look back if we can't find data on myDate
    
    myDate <- as.POSIXlt(myDate)

    count <- 0
    found <- FALSE

    while(!found && (count<=defaultLimit)){
        result <- baseFunction(source = source,finObj, startDate = myDate ,endDate = myDate,...)

    	if(!is.null(result)){
    	    found <- TRUE
    	}else{
            count <- count + 1
    	    myDate$mday <- (myDate$mday-1)
            myDate <- as.POSIXlt(as.character(myDate))        
    	}
    }    
    if(count > 0 && !is.null(result))warning("TSDataLoader$searchDate: actual date was not found")
    
	
    # format result object

    result <- this$getFormat(result,...)
    result
})

setMethodS3("matrixToZoo","TSDataLoader",function(this,myMatrix,...)
{
    result <- myMatrix

    if(class(myMatrix)=="matrix"){

        zooList <- NULL
        for (i in nrow(myMatrix):1)zooList <- c(list(myMatrix[[i]]),zooList)
        
        merged <- do.call(merge.zoo,as.list(zooList))
        
        colnames(merged) <- rownames(myMatrix)
        result <- merged
    }
    result
})

setMethodS3("getArrangeAttributes","TSDataLoader",function(this,...)
{
    # if this method is not specified in the child classes, NULL will be taken
 
    NULL
})

setMethodS3("checkFinObject","TSDataLoader",function(this,...)
{
    # if this method is not specified in the child classes, NULL will be taken
 
    NULL
})

setMethodS3("getDB","TSDataLoader",function(this,...)
{   
    # if this method is not specified in the child classes, TimeSeriesDB will be taken
    
    TimeSeriesDB()
})

method("filterIfNeeded", "TSDataLoader", function(this, data, ...) {
    if(is.null(data))return(data)
    needs(this = "TSDataLoader", data = "zoo")
    
    if (!is.null(this$.filter)) {
        original = nrow(data)
        data <- make.zoo.daily(data, this$.filter)
        if (original != 0 && nrow(data) == 0) throw("ALL ROWS FILTERED - filter error?")
    }
    data
})

getMergedTimeSeries <- function(tsdb,tsName, dataSource, startDate = NULL, endDate = NULL, filter = NULL){
    # improvments: multiple data sources, multiple filters
    data <- tsdb$retrieveTimeSeriesByName(name = tsName, data.source = dataSource, start = startDate, end = endDate)
    if(!is.null(data)){
        data <- TSDataLoader$matrixToZoo(data)
        if (!is.null(filter)){
            data <- make.zoo.daily(data, filter)
            if(NROW(data)==0)return(NULL)            
        }
      	if(NCOL(data)>1)data <- data[,na.omit(match(tsName,colnames(data)))]        
        data <- getZooDataFrame(data)
        return(data)
    }
    return(NULL)
}

uploadZooToTsdb <- function(tsdbValues, tsdbNames, tsdbSources, uploadMethod="file", uploadFilename=NULL, uploadPath=NULL){
	#uploadMethod can be either file (use the crawler) or "direct" for the straight to tsdb upload
	
	needs (tsdbValues="zoo", tsdbNames='character', tsdbSources='character', uploadMethod='character?', uploadFilename='character?', uploadPath='character?')
	failUnless(uploadMethod %in% c("file", "direct"), "only 'file' or 'direct' are allowed for uploadMethod.  uploadMethod is ", uploadMethod)
	failUnless( (length(tsdbSources)==1) || (length(tsdbSources) == length(tsdbNames)), 'tsdbSources must of length 1 or same length as tsdbNames')
	failIf( (NCOL(tsdbValues)!=length(tsdbNames)),'tsdbNames must be same length as zoo columns')
	
	#Set up the array of zoos
	tsdbDF <- cbind(index(tsdbValues),data.frame(tsdbValues))	
	colnames(tsdbDF) <- c('Date',paste(tsdbNames,tsdbSources, sep=":"))
	tsdbZooArray <- TimeSeriesFile$as.ts.array(tsdbDF)
	
	if (uploadMethod == 'file'){
		if (is.null(uploadPath)) uploadPath <- tsdbUploadDirectory() #squish(dataDirectory(),"TSDB_Upload/Today/")
		write.csv(tsdbDF, squish(uploadPath,uploadFilename,'.csv'),row.names=FALSE, quote=FALSE)			
	}

	if (uploadMethod == 'direct'){
		TimeSeriesDB$writeTimeSeries(tsdbZooArray)
	}
}

method("loadMatrixInTSDB","TSDataLoader",function(this,matrix,timeFilter,source,...){

    if(!this$.isConnected)(this$init(this$getDB()))

    if(is.null(matrix))return("there is no data to copy from")

    # strip off the time stamp

    if(!is.null(timeFilter)){
        for (i in 1:NROW(matrix))
            matrix[[i]] <- make.zoo.daily(matrix[[i]], timeFilter)
    }
    
    # copy to internal source

    colnames(matrix) <- source
    (this$.tsdb)$writeTimeSeries(matrix)
    return(TRUE)
})

method("getDataByName","TSDataLoader", function(this, container='tsdb', tickerName, source=NULL, field=NULL, start=NULL, end=NULL, ...){
	needs(container='character?', tickerName='character', source='character?', field='character?')

	switch(container,
	#TSDB
		tsdb = return(TimeSeriesDB$retrieveOneTimeSeriesByName(name=tickerName, data.source=source, start=start, end=end)),
	#SystemDB
		systemdb = {failIf(is.null(field),'Field cannot be NULL for systemdb request')
					tickerData <- Symbol(tickerName)$series()[,field]},
	#File
		{filedata <- TimeSeriesFile$readTimeSeries(container)
		tickerData <- filedata[[tickerName,source]]}
	)
	if (!is.null(start)) tickerData <- tickerData[index(tickerData) >= strptime(as.POSIXct(start),"%Y-%m-%d")]
	if (!is.null(end)) tickerData <- tickerData[index(tickerData) <= as.POSIXct(squish(format(as.POSIXct(end),"%Y-%m-%d"),"23:59:59"))]
	return(tickerData)
})

method("getDataByAttributeList", "TSDataLoader", function(this, container='tsdb', attributeList, source, arrangeBy, start=NULL, end=NULL, ...){
	needs(container='character?',source='character?',arrangeBy='character?')
	
	if (container == 'tsdb'){
		tsdb <- TimeSeriesDB()
		return(tsdb$retrieveTimeSeriesByAttributeList(attributeList, source, start=start, end=end, arrange.by=arrangeBy))
	}
	else{
		failIf((is.null(arrangeBy) && (!is.null(start) || !is.null(end))),'To use start/end date with file container, arrangeBy cannot be NULL')
		filedata <- TimeSeriesFile$readTimeSeries(container)
		if (!is.null(arrangeBy)) filedata <- TimeSeriesDB$arrangeByAttributes(filedata,arrangeBy)
		for (element in names(filedata)){
			if (!is.null(start)) filedata[[element]] <- filedata[[element]][index(filedata[[element]]) >= strptime(as.POSIXct(start),"%Y-%m-%d")]
			if (!is.null(end)) filedata[[element]] <- filedata[[element]][index(filedata[[element]]) <= as.POSIXct(squish(format(as.POSIXct(end),"%Y-%m-%d"),"23:59:59"))]
		}
		return(filedata)
	}
})