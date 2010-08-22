constructor("SystemDB", function() {
    extend(RObject(), "SystemDB")        
})

method("sqlSelect", "SystemDB", function(static, sqlString, ...){
	needs(sqlString='character')
	conn <- SQLConnection()
	conn$init()
	return(conn$select(sqlString))
})

method("sqlInsert", "SystemDB", function(static, sqlString, ...){
	needs(sqlString='character')
	conn <- SQLConnection()
	conn$init()
	return(conn$query(sqlString))
})

method("alreadyExists", "SystemDB", function(this, tableName, fieldName, value, ...){
#Returns TRUE/FALSE if a given value for a given field exists in a table in SystemDB. Any single instance will lead to TRUE	
	needs(tableName='character',fieldName='character',value='character|numeric')
	sqlString <- squish("select count(*) from SystemDB..",tableName," where ",fieldName," = '",value,"'")
	conn <- SQLConnection()
	conn$init()
	return(as.numeric(conn$select(sqlString)) > 0)
})

method("getFieldForName", "SystemDB", function(static, field, table, name, ...){
	needs(field="character", table="character", name="character")
	sqlResult <- static$sqlSelect(squish("select distinct ",field," from SystemDB..",table," where Name = '",name,"'"))
	return(as.character(sqlResult[[1]]))
})

####################################################################################################
#    Market Methods 
####################################################################################################

method("marketsBySystemPV", "SystemDB", function(static, system, interval, version, pv, ...) {
    needs(system="character", interval="character", version="character", pv="character")
    
    siv <- JSiv$by_String_String_String(system, interval, version)
    markets <- JMsivLiveHistory$LIVE()$markets_by_Siv_Pv(
        siv, 
        JPv$by_String(pv)
    )
    unlist(japply(markets$iterator(), JSymbol(), name))
})

method("bigPointValue", "SystemDB", function(static, market, ...) {
    needs(market="character")
    JMarket$contractSize_by_String(market)
})

method("fixedSlippage", "SystemDB", function(static, market, ...) {
    needs(market="character")
    JMarket$by_String(market)$fixedSlippage()
})

method("histDaily", "SystemDB", function(static, market, ...){
	needs(market="character")
	return(static$getFieldForName('HistDaily','Time_series_data',market))
})

method("asciiFilename", "SystemDB", function(static, market, ...){
	needs(market="character")
	sqlResult <- static$getFieldForName('Filename','ASCII',market)
	if (isWindows()) return(sqlResult)
	return(gsub('\\\\','/',substring(sqlResult,9)))		#If is Linux
})

method("rebalanceFunction", "SystemDB", function(static, market, ...){
	needs(market="character")
	return(static$getFieldForName('Rebalance_function','Market',market))
})

method("marketFromMsivs", "SystemDB", function(static, msivs=NULL, ...){
	needs(msivs="character")
	sqlList <- strsplit(msivs,'_')
	sqlList <- lapply(sqlList, function(x) x[1])
	return (unlist(sqlList))
})

method('sectors', 'SystemDB', function(static, markets, ...){
	needs(markets='character')
	marketsString <- squish("'",paste(markets,collapse="','"),"'")
	subSectors <- static$sqlSelect(squish("SELECT Name, SubSector FROM SystemDB..Time_series_data WHERE Name in (",marketsString,")"))
	subSectorsString <- squish("'",paste(unique(as.character(subSectors$SubSector)),collapse="','"),"'")
	topSectors <- static$sqlSelect(squish("SELECT SubSector, Sector FROM SystemDB..SubSectors WHERE SubSector in (",subSectorsString,")"))
	return(merge(subSectors,topSectors,all=TRUE)[c('Name','SubSector','Sector')])
})

####################################################################################################
#    Futures and Continuous Methods
####################################################################################################

method("getContinuousContracts", "SystemDB", function(static,.subset = 'all',...){
	assert(.subset %in% c('gfut','all','ts'))
	likeStr <- '%.%'	
	if(.subset == 'gfut') likeStr <- '%.GFUT.%'
	if(.subset == 'ts') likeStr <- '%.TS.%'
	sqlResult <- static$sqlSelect(
		squish("select Name from SystemDB..Time_series_data where Name Like '",likeStr,"' and Type = 'Future' and Expiry = 'Continuous' and SubSector <> 'TestSubSector' and NOT(Description Like 'Test%') and NOT(Name Like '%Old')"))
	return(as.character(sqlResult[[1]]))
})

method("deleteMarketTicker", "SystemDB", function(static,market,...){
	conn <- SQLConnection()
	conn$init()
	conn$query(squish("delete from SystemDB..MarketTickers where Market = '",market,"'"))			
})

method("insertMarketTicker", "SystemDB", function(static,market,bloomberg,yellowKey,tsdb,timeStamp,bloombergRoot,ricRoot,...){
	needs(market = 'character',bloomberg = 'character',tsdb = 'character',timeStamp = 'character',
	yellowKey= 'character',bloombergRoot = 'character',ricRoot = 'character')
	conn <- SQLConnection()
	conn$init()	
	conn$query(squish("insert into SystemDB..MarketTickers (Market, Bloomberg, YellowKey, TSDB, Timestamp,BloombergRoot,RICRoot) values ('",market,"','",bloomberg,"','",yellowKey,"','",tsdb,"','",timeStamp,"','",bloombergRoot,"','",ricRoot,"')"))	
})

method("getFrontBloombergTicker", "SystemDB", function(static,market,...){
	SystemDB$getMarketTickerInfo(market,'Bloomberg')
})

method("getContinuousBloombergSecurity", "SystemDB", function(static,Name,...){	
	sqlResult <- static$sqlSelect(squish("select Security from SystemDB..Bloomberg where Name = '",Name,"'"))
	as.character(sqlResult[[1]])
})

method("getFrontTSDBTicker", "SystemDB", function(static,market,...){
	SystemDB$getMarketTickerInfo(market,'TSDB')
})

method("getLastMarketUpdate", "SystemDB", function(static,market,...){
	SystemDB$getMarketTickerInfo(market,'Timestamp')
})

method("getYellowKey", "SystemDB", function(static,market,...){
	SystemDB$getMarketTickerInfo(market,'YellowKey')
})

method("getBloombergRoot", "SystemDB", function(this,market,...){
	SystemDB$getMarketTickerInfo(market,'BloombergRoot')	
})

method("getRICRoot", "SystemDB", function(this,market,...){
	SystemDB$getMarketTickerInfo(market,'RICRoot')	
})

method("getMarketTickerInfo", "SystemDB", function(static,market,field,...){
	assert(field %in% c('Bloomberg','TSDB','Timestamp','YellowKey','BloombergRoot','RICRoot'))
	sqlResult <- static$sqlSelect(squish("select ",field," from SystemDB..MarketTickers where Market = '",market,"'"))
	as.character(sqlResult[[1]])
})

####################################################################################################
#    System Methods 
####################################################################################################

method("systemID", "SystemDB", function(static, system, interval, version, stoDir='NA', stoID='NA', pvName='NA', ...){
	needs(system="character", interval="character", version="character", stoDir="character", stoID="character", pvName="character")
	fields <- squish("system_name = '",system,"' AND version='",version,"' AND interval = '",interval,"'")
	fields <- c(fields,squish(" AND sto_dir = '",stoDir,"'"))
	fields <- c(fields,squish(" AND sto_id = '",stoID,"'"))
	fields <- c(fields,squish(" AND pv_name = '",pvName,"'")) 
	sqlResult <- static$sqlSelect(squish("select distinct id from SystemDB..SystemDetails where ",fields))
	return(as.numeric(sqlResult[[1]]))
})

method("strategyClass", "SystemDB", function(static, strategy, ...){
	needs(strategy="character")
	sqlResult <- static$sqlSelect(squish("select distinct Class from SystemDB..Strategy where Name='",strategy,"'"))
	return(as.character(sqlResult[[1]]))
})

method("systemQClassName", "SystemDB", function(static, system, ...){
	needs(system="character")
	sqlResult <- static$sqlSelect(squish("select distinct QClassName from SystemDB..System where Name='",system,"'"))
	return(as.character(sqlResult[[1]]))
})

method("msivBacktestMarkets", "SystemDB", function(static, stoID=NULL, ...){
	needs(stoID="character")
	sqlResult <- static$sqlSelect(squish("select MSIV_Name from SystemDB..MSIVBacktest where STOid='",stoID,"'"))
	return(SystemDB$marketFromMsivs(as.vector(sqlResult$MSIV_Name)))
})

method("msivBacktest", "SystemDB", function(static, stoID=NULL, ...){
	needs(stoID="character")
	sqlResult <- static$sqlSelect(squish("select StartDate,EndDate from SystemDB..MSIVBacktest where STOid='",stoID,"'"))
	return(data.frame(	market=		SystemDB$msivBacktestMarkets(stoID),
						startDate=	sqlResult$StartDate,
						endDate=	sqlResult$EndDate))				
})

method("backtestPortfolios", "SystemDB", function(static, stoID=NULL, ...){
	needs(stoID="character")
	sqlResult <- static$sqlSelect(squish("select PortfolioName,MSIV_Name from SystemDB..PortfolioBacktest where STOid='",stoID,"'"))
	
	portfolioNames <- unique(as.character(sqlResult$PortfolioName))
	result <- list()
	for (portfolioName in portfolioNames){
		msivs <- as.character(sqlResult[portfolioName == as.character(sqlResult$PortfolioName),'MSIV_Name'])
		markets <- SystemDB$marketFromMsivs(msivs)
		result <- appendSlowly(result,markets)
	}
	names(result) <- portfolioNames
	return(result)
})

method("systemDetails", "SystemDB", function(static, systemID=NULL, makeFilenameNative=FALSE, ...){
	needs(systemID="numeric|integer", makeFilenameNative="logical")
	sqlResult <- static$sqlSelect(squish("select * from SystemDB..SystemDetails where id='",systemID,"'"))
	resultList <- (list(system			= as.character(sqlResult$system_name),
						version			= as.character(sqlResult$version),
						interval		= as.character(sqlResult$interval),
						pvName			= as.character(sqlResult$pv_name),
						stoDirectory	= as.character(sqlResult$sto_dir),
						stoID			= as.character(sqlResult$sto_id)))
	if(makeFilenameNative) resultList$stoDirectory <- makeFilenameNative(resultList$stoDirectory)
	return(resultList)
})

method("pvNamesForSystem", "SystemDB", function(static, system=NULL, ...){
	needs(system="character")
	sqlResult <- static$sqlSelect(squish("SELECT DISTINCT Name FROM SystemDB..ParameterValues WHERE Strategy='",system,"'"))
	return(as.vector(sqlResult$Name))
})

method("parameterNames", "SystemDB", function(static, system=NULL, ...){
	needs(system="character")
	sqlResult <- static$sqlSelect(squish("SELECT ParameterName FROM SystemDB..StrategyParameterNames WHERE Strategy='",system,"'"))
	return(as.vector(sqlResult$ParameterName))
})

method('msivsForPV', "SystemDB", function(static, pvName=NULL, ...){
	needs(pvName="character")
	sqlResult <- static$sqlSelect(squish("SELECT MSIV_Name FROM SystemDB..MSIVParameterValues WHERE PV_Name='",pvName,"'"))
	return(as.vector(sqlResult$MSIV_Name))
})

method('bloombergTag', "SystemDB", function(static, systemID, ...){
	needs(systemID="numeric|integer")
	sqlResult <- static$sqlSelect(squish("select Tag from SystemDB..BloombergTags where systemId='",systemID,"'"))
	if(NROW(sqlResult) == 0) return(NULL)
	return(as.character(sqlResult[[1]]))
})

method('liveHistoryStartEndDates', "SystemDB", function(static, msiv, pvName, ...){
	needs(msiv="character", pvName="character")
	sqlResult <- static$sqlSelect(squish("select Start_trading, End_trading from SystemDB..MSIVLiveHistory where MSIV_Name='",msiv,"' AND PV_Name='",pvName,"'"))
	if(NROW(sqlResult) == 0) return(NULL)
	return(sqlResult)
})

method("tradingRangeForLiveMSIVPV", "SystemDB", function(static,msivName,pvName,...){
	needs(msivName="character",pvName="character")
	sqlResult <- static$sqlSelect(squish("select Start_trading, End_trading from SystemDB..MSIVLiveHistory where MSIV_Name='",msivName,"' and PV_Name = '",pvName,"'"))
	if(NROW(sqlResult) == 0) return(NULL)
	if(is.na(sqlResult[[2]])) sqlResult[[2]] <- as.character(Sys.Date())	
	return(Range(as.POSIXct(sqlResult[[1]]),as.POSIXct(sqlResult[[2]])))
})

method('parameterValues', "SystemDB", function(static, system, pvName, parameter, ...){
	needs(system='character', pvName='character', parameter='character')
	sqlResult <- static$sqlSelect(squish("select ParameterValue, AsOfDate from SystemDB..ParameterValues where System='",system,
											"' and Name='",pvName,"' and ParameterName='",parameter,"'"))
	return(sqlResult)	
})

method('lastParameterValue', "SystemDB", function(static, system, pvName, parameter, ...){
	needs(system='character', pvName='character', parameter='character')
	resChar <- JParameterValuesTable$VALUES()$param_by_String_String_String(system, pvName, parameter)
	if (!is.na(as.numeric(resChar))) return(as.numeric(resChar))
	return(resChar)
})

method('sizingParameter', "SystemDB", function(this, system, ...){
	needs(system='character')
	JStrategyParameters$NAMES()$sizingParameter_by_String(system)
})

####################################################################################################
#    Portfolio Group Methods 
####################################################################################################

method("memberGroupNamesForGroup", "SystemDB", function(static,groupName,...){
	needs(groupName="character")
	sqlResult <- static$sqlSelect(squish("select MemberGroupName from SystemDB..GroupMemberGroups where GroupName='",groupName,"'"))
	if(NROW(sqlResult) == 0) return(NULL)
	return(as.character(sqlResult[[1]]))
})

method("memberGroupMSIVPVsForGroup", "SystemDB", function(static, groupName, pvName, ...){
	needs(groupName="character", pvName='character')
	sqlResult <- static$sqlSelect(squish("select MSIV_Name from SystemDB..GroupMemberMSIVPVs where GroupName='",groupName,"' AND PV_Name='",pvName,"'"))
	if(NROW(sqlResult) == 0) return(NULL)
	return(as.character(sqlResult[[1]]))
})

method('marketHistory', "SystemDB", function(static, market, ...){
	needs(market="character")
	sqlResult <- static$sqlSelect(squish("select StartDate, EndDate from SystemDB..MarketHistory where Market='",market,"'"))
	if(NROW(sqlResult) == 0) return(NULL)
	return(sqlResult)
})
