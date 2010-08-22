constructor("SystemPairsTradingSystemDBManager", function(
    pairNames = NULL,
    transformationName = NULL,
    versionString = NULL,
	systemDescription = NULL,
	systemName = NULL,
	window = NULL
,...){
    this <- extend(RObject(), "SystemPairsTradingSystemDBManager",   
        .versionString = versionString,
        .transformationName = transformationName,
        .pairNames = pairNames,
		.versionString = versionString,
		.window = window,
		.systemDescription = systemDescription,
		.systemName = systemName,
		.interval = "daily",
		.myName = "Jerome Bourgeois"
    )
    
    if(inStaticConstructor(this))return(this)
	
	this$.versionNumeric = SystemPairsTrading$getVersionNumeric(versionString)	
	this$.systemDBUploadPath  <- squish(dataDirectory(),"/SystemDB_upload/",systemDescription,"/")
	dir.create(this$.systemDBUploadPath,FALSE)
    this
})

method("generateTimeSeriesDataTable", "SystemPairsTradingSystemDBManager", function(this,asOfDate,subSector,triExchange,...)
{
	nCols <- 20
	firstRow <- data.frame(t(rep("SystemDB..Time_series_data",nCols)))
	secondRow <- data.frame("Name","Long_name","SubSector","Country","Type","Exchange","Expiry","OptionFlag","TemplateFlag","HistDaily","HistTick","TodayTick","Live","Description","Owner","AsOf","OD_ExchangeSetting","OD_SettingID","OD_ExchangeSession","OD_SessionID")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
			this$getNames(this$.pairNames[i]),
			this$getLongNames(this$.pairNames[i]),
			subSector,
			"USD",
			this$getTypes(),
			this$getExchange(triExchange),
			this$getExpiries(),
			"FALSE",
			"FALSE",
			"TSDB",
			"NA",
			"NA",
			"Gissing",
			this$getDescription(),
			this$.myName,
			as.character(asOfDate),
			"TRUE",
			"NULL",
			"TRUE",
			"NULL"
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"Time_series_data")
})

method("generateMarketTable", "SystemPairsTradingSystemDBManager", function(this,...)
{
	nCols <- 3
	firstRow <- data.frame(t(rep("SystemDB..Market",nCols)))
	secondRow <- data.frame("Name","Weighting_function","Rebalance_function")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$marketName(this$.pairNames[i]),
				this$weightingFunction(),
				"daily on close"
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"Market")
})

method("generateTSDBTable", "SystemPairsTradingSystemDBManager", function(this,asOfDate,...)
{
	nCols <- 15
	firstRow <- data.frame(t(rep("SystemDB..TSDB",nCols)))
	secondRow <- data.frame("Name","Data_source","Name_open","Name_high","Name_low","Name_close","Name_volume","Name_open_interest","Calculate_method","StartDate","VerifyBy","VerifyDate","Template","Comments","CaptureHighLowFromTicks")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$getNames(this$.pairNames[i]),
				"internal",
				this$tsNames(this$.pairNames[i]),
				this$tsNames(this$.pairNames[i]),
				this$tsNames(this$.pairNames[i]),
				this$tsNames(this$.pairNames[i]),
				"NULL",
				"NULL",
				"OpenIsPriorClose",
				"NULL",
				this$.myName,
				as.character(asOfDate),
				"FALSE",
				"NULL",
				"FALSE"				
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"TSDB")
})


method("generateSymbolMap", "SystemPairsTradingSystemDBManager", function(this,...)
{
	nCols <- 8
	dataFrame <- data.frame("","ADEName","tsdbName","tsdbSource","liveSource","liveTemplate","liveTopic","liveField")
	colnames(dataFrame) <- 1:nCols
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$getNames(this$.pairNames[i]),
				this$getNames(this$.pairNames[i]),
				this$tsNames(this$.pairNames[i]),
				"internal",
				"NULL",
				"NULL",
				"NULL",
				"NULL"
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"SymbolMap")
})

method("generateMSIVTable", "SystemPairsTradingSystemDBManager", function(this,documentation,...)
{
	nCols <- 6
	firstRow <- data.frame(t(rep("SystemDB..MSIV",nCols)))
	secondRow <- data.frame("Name","Market","System","Interval","Version","Documentation")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$msivName(this$.pairNames[i]),
				this$marketName(this$.pairNames[i]),
				this$.systemName,
				this$.interval,
				this$.versionString,
				documentation
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"MSIV")
})

method("generateMSIVBacktestTable", "SystemPairsTradingSystemDBManager", function(this,stoDir,stoId,runDate,startDate,endDate,validationAccept = "True",...)
{
	nCols <- 10
	firstRow <- data.frame(t(rep("SystemDB..MSIVBacktest",nCols)))
	secondRow <- data.frame("MSIV_Name","STOdir","STOid","RunDate","StartDate","EndDate","ValidationAccept","ResultFile","Comment","Owner")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$msivName(this$.pairNames[i]),
				stoDir,
				stoId,
				runDate,
				startDate,
				endDate,
				validationAccept,
				"NULL",
				"NULL",
				this$.myName
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"MSIVBacktest")
})

method("generateMSIVLiveHistoryTable", "SystemPairsTradingSystemDBManager", function(this,pvName,startDate,endDate,...)
{
	nCols <- 4
	firstRow <- data.frame(t(rep("SystemDB..MSIVLiveHistory",nCols)))
	secondRow <- data.frame("MSIV_Name","PV_Name","Start_trading","End_trading")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$msivName(this$.pairNames[i]),
				pvName,
				startDate,
				endDate
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"MSIVLiveHistory")
})

method("generateMSIVParameterValuesTable", "SystemPairsTradingSystemDBManager", function(this,pvName,...)
{
	nCols <- 3
	firstRow <- data.frame(t(rep("SystemDB..MSIVParameterValues",nCols)))
	secondRow <- data.frame("MSIV_Name","PV_Name","LiveWorkspace")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$msivName(this$.pairNames[i]),
				pvName,
				"NULL"
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"MSIVParameterValues")
})

method("generateMSIVPropertyTable", "SystemPairsTradingSystemDBManager", function(this,pvName,property,propertyValue,asOfDate,...)
{
	nCols <- 5
	firstRow <- data.frame(t(rep("SystemDB..MSIVProperty",nCols)))
	secondRow <- data.frame("MSIV_Name","PV_Name","Property","Value","AsOfDate")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$msivName(this$.pairNames[i]),
				pvName,
				property,
				propertyValue,
				asOfDate
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}
	this$writeTable(dataFrame,"MSIVProperty")
})


method("generateParameterValuesTable", "SystemPairsTradingSystemDBManager", function(this,pvName,asOfDate,...)
{
	nCols <- 6
	firstRow <- data.frame(t(rep("SystemDB..ParameterValues",nCols)))
	secondRow <- data.frame("System","Name","Strategy","ParameterName","ParameterValue","AsOfDate")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	newRows <- data.frame(
		this$.systemName,
		pvName,
		this$.systemName,
		this$parameterNames(),
		0,
		asOfDate
	)		
	colnames(newRows) <- 1:nCols
	dataFrame <- rbind(dataFrame,newRows)
	this$writeTable(dataFrame,"ParameterValues")
})


method("generateSystemDetailsTable", "SystemPairsTradingSystemDBManager", function(this,pvName,stoDir,stoId,...)
{
	nCols <- 6
	firstRow <- data.frame(t(rep("SystemDB..SystemDetails",nCols)))
	secondRow <- data.frame("system_name","version","interval","sto_dir","sto_id","pv_name")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	newRows <- data.frame(
			this$.systemName,
			this$.versionString,
			this$.interval,
			stoDir,
			stoId,
			pvName
	)		
	colnames(newRows) <- 1:nCols
	dataFrame <- rbind(dataFrame,newRows)
	this$writeTable(dataFrame,"SystemDetails")
})

method("generateGroupMemberMSIVPVsTable", "SystemPairsTradingSystemDBManager", function(this,pvName,weights,...)
{
	nCols <- 4
	firstRow <- data.frame(t(rep("SystemDB..GroupMemberMSIVPVs",nCols)))
	secondRow <- data.frame("GroupName","MSIV_Name","PV_Name","Weight")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$.systemName,
				this$msivName(this$.pairNames[i]),
				thsi$.myName,
				weights
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}		
	colnames(newRows) <- 1:nCols
	dataFrame <- rbind(dataFrame,newRows)
	this$writeTable(dataFrame,"GroupMemberMSIVPVs")
})

method("generateGissingTable", "SystemPairsTradingSystemDBManager", function(this,asOfDate,...)
{
	nCols <- 6
	firstRow <- data.frame(t(rep("SystemDB..Gissing",nCols)))
	secondRow <- data.frame("Name","Record","Template","VerifiedBy","VerifiedDate","Generic")
	colnames(firstRow) <- 1:nCols
	colnames(secondRow) <- colnames(firstRow)
	dataFrame <- rbind(firstRow,secondRow)
	for(i in 1:NROW(this$.pairNames)){
		newRows <- data.frame(
				this$marketName(this$.pairNames[i]),
				this$marketName(this$.pairNames[i]),
				"MARKETDATA",
				this$.myName,
				asOfDate,
				"NULL"
		)		
		colnames(newRows) <- 1:nCols
		dataFrame <- rbind(dataFrame,newRows)
	}		
	colnames(newRows) <- 1:nCols
	dataFrame <- rbind(dataFrame,newRows)
	this$writeTable(dataFrame,"Gissing")
})


method("writeTable", "SystemPairsTradingSystemDBManager", function(this,dataFrame,tableName,...)			
{
	write.table(
			data.frame(dataFrame),squish(this$.systemDBUploadPath,tableName,".csv"),
			col.names = FALSE,
			row.names = FALSE,
			sep = ","
	)
})

method("marketName", "SystemPairsTradingSystemDBManager", function(this,pairName,...)			
{
	paste("PTT",this$.versionNumeric,".",pairName,sep = "")
})

method("parameterNames", "SystemPairsTradingSystemDBManager", function(this,...)			
{
	c(
		"ATRLength","LeadBars","acfHalfLife","acfLag","acfTrigger","hedgeMax","hedgeMin","hedgeSwitch",
		"maxBarInTrade","nATR","pScoreMin","rSquareMin","scaleMin","startSize","stopMultiple","zScoreMin"
	)
})

method("msivName", "SystemPairsTradingSystemDBManager", function(this,pairName,...)			
{
	paste(this$marketName(pairName),this$.systemName,this$.interval,this$.versionString,sep = "_")
})


method("tsNames", "SystemPairsTradingSystemDBManager", function(this,pairName,...)			
{
	paste(rep(this$marketName(pairName),10),this$.transformationName,this$.versionString,this$getTransformationOutputs(),sep = "_")
})

method("weightingFunction", "SystemPairsTradingSystemDBManager", function(this,...)			
{
	squish(this$.window," trading day regression slope")	
})

method("getNames", "SystemPairsTradingSystemDBManager", function(this,pairName,...)			
{
	prefix <- paste(c("PTC","PTD","PTH","PTI","PTP","PTR","PTS","PTT","PTV","PTZ","PTB"),this$.versionNumeric,sep = "")
	paste(prefix,pairName,sep = ".")	
})

method("getDescription", "SystemPairsTradingSystemDBManager", function(this,...)paste(this$getTransformationOutputs(),"for",this$.systemDescription,sep = " "))

method("getTypes", "SystemPairsTradingSystemDBManager", function(this,...)c(rep("Signal",7),"Index",rep("Signal",3)))

method("getExpiries", "SystemPairsTradingSystemDBManager", function(this,...)c(rep("Constant",7),"ReturnIndex",rep("Constant",3)))

method("getExchange", "SystemPairsTradingSystemDBManager", function(this,triExchange,...)c(rep("OTC",7),triExchange,rep("OTC",3)))

method("getTransformationOutputs", "SystemPairsTradingSystemDBManager", function(this,...){
	c("transaction_cost","delta","hedge","intercept","p_score","r_square","scale","tri","residual","z_score","beta")
})

method("getLongNames", "SystemPairsTradingSystemDBManager", function(this,pairName,...)			
{
	paste(this$getTransformationOutputs(),this$.versionString,pairName,sep = "_")	
})