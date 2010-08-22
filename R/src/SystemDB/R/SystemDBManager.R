# Collection of helper and wrapper functions for the SystemDb management process
# 
# Author: rsheftel
###############################################################################

constructor("SystemDBManager", function(){
	this <- extend(RObject(), "SystemDBManager")
	if (inStaticConstructor(this)) return(this)
	this$commitToDB(TRUE)
	return(this)
})

method("commitToDB", "SystemDBManager", function(this, commitFlag=NULL, ...){
#set the flag on whether or not to commit things actually into systemDB or just output files for testing
	needs(commitFlag='logical?')	
	if (is.null(commitFlag)) return (this$.commitToDB)
	if (!is.logical(commitFlag)) return('Must be logical argument')
		this$.commitToDB <- commitFlag
})

method("insertToSystemDB", "SystemDBManager", function(this, tableName, fieldNames, values, ...){
#Wrapper of the insert/update function from JavaUtils
	needs(tableName='character', fieldNames='character',values='numeric|character|matrix')
	insertResult <- uploadToDB('SystemDB', tableName, fieldNames, values, this$.commitToDB)
	if(insertResult==FALSE)
		insertResult <- paste('Unable to insert values into SystemDB table',tableName)
		return(insertResult)	
})

method("updateInSystemDB","SystemDBManager", function(this, tableName, fieldNames, keyFieldNames, values, ...){
#Wrapper of the update function in JavaUtils, this will allow for updates using the key field
	needs(tableName='character', fieldNames='character',keyFieldNames='character', values='numeric|character|matrix')
	if(!all(keyFieldNames%in%fieldNames)) return(FALSE)
	for (keyFieldName in keyFieldNames)
		fieldNames <- replace(fieldNames,which(keyFieldName==fieldNames),squish(keyFieldName,'*'))
	updateResult <- uploadToDB('SystemDB', tableName, fieldNames, values, this$.commitToDB)
	if(updateResult==FALSE)
		updateResult <- paste('Unable to update values in SystemDB table',tableName)
		return(updateResult)	
})

method("alreadyExists", "SystemDBManager", function(this, tableName, fieldName, value, ...){
#Returns TRUE/FALSE if a given value for a given field exists in a table in SystemDB. Any single instance will lead to TRUE	
	needs(tableName='character',fieldName='character',value='character|numeric')
	return(SystemDB$alreadyExists(tableName,fieldName,value))
})

method("alreadyExistsMultiKey", "SystemDBManager", function(this, tableName, fieldNames, values, ...){
#Returns TRUE/FALSE if a given value for a given field exists in a table in SystemDB. Any single instance will lead to TRUE	
	needs(tableName='character',fieldNames='character',values='character|numeric')
	keys <- c()
	for (x in 1:length(fieldNames)){
		keys <- squish(keys,squish(" ",fieldNames[[x]]," = '",values[[x]],"' AND")) 
	}
	keys <- leftStr(keys,nchar(keys)-4)
	sqlString <- squish("select count(*) from SystemDB..",tableName," where",keys)
	conn <- SQLConnection()
	conn$init()
	return(as.numeric(conn$select(sqlString)) > 0)
})

####################################################################################################
#    Strategy Tables
####################################################################################################

method("insertStrategyTable","SystemDBManager", function(this, strategyName, strategyClass, strategyDescription, strategyOwner, ...){	
	
	if (this$alreadyExists('Strategy','Name',strategyName)) return(squish('Strategy already exists in SystemDB..Strategy table: ',strategyName))
	if (!this$alreadyExists('Class','Name',strategyClass)) return (squish('Class does not exist in SystemDB..Class table: ',strategyClass))
	
	fieldNames <- c('Name','Class','Description','Owner')			
	values <- c(strategyName, strategyClass, strategyDescription, strategyOwner)
	return(this$insertToSystemDB('Strategy',fieldNames,values))
})

method("insertStrategyParameterNames","SystemDBManager", function(this, strategyName,parameterNames, parameterDescriptions, ...){
	
	if(length(parameterNames) != length(parameterDescriptions)) return(FALSE)
	if (!this$alreadyExists('Strategy','Name',strategyName)) return (squish('Strategy does not exist in SystemDB..Strategy table: ',strategyName))
	
	fieldNames <- c('Strategy','ParameterName','Description')			
	values <- cbind(rep(strategyName,length(parameterNames)),parameterNames,parameterDescriptions)
	return(this$insertToSystemDB('StrategyParameterNames',fieldNames,values))
})

method("insertStrategy","SystemDBManager", function(this, strategyName, strategyClass, strategyDescription, strategyOwner, parameterNames, parameterDescriptions,...){
#Main wrapper method to insert a new strategy	
	insertResult <- this$insertStrategyTable(strategyName, strategyClass, strategyDescription, strategyOwner)
	if(insertResult==TRUE)
		insertResult <-	this$insertStrategyParameterNames(strategyName,parameterNames, parameterDescriptions)
	
	return(insertResult)
})

####################################################################################################
#    System Tables
####################################################################################################

method("insertSystemTable","SystemDBManager", function(this, systemName=NULL,systemDescription="", systemDocumentation="", 
															 systemOwner="", qClassName="", ...){
	needs(systemName='character', systemDescription='character?', systemDocumentation='character?', systemOwner='character?', qClassName='character?')	
	if (this$alreadyExists('System','Name',systemName)) return(squish('System already exists in SystemDB..System table: ',systemName))
	
	fieldNames <- c('Name','Description','Documentation','Owner','QClassName')			
	values <- c(systemName,systemDescription, systemDocumentation, systemOwner, qClassName)
	return(this$insertToSystemDB('System',fieldNames,values))
})

method("insertSystemStrategiesTable","SystemDBManager", function(this, systemName, strategyNames,...){
	if (!this$alreadyExists('System','Name',systemName)) return(squish('System does not exist in SystemDB..System table: ',systemName))
	for (strategyName in strategyNames)
		if (!this$alreadyExists('Strategy','Name',strategyName)) return(squish('Strategy does not exist in SystemDB..Strategy table: ',strategyName))
	
	lenStrategies <- length(strategyNames)
	
	longEntries <- rep(TRUE,lenStrategies)
	longExists	<- rep(TRUE,lenStrategies)
	shortEntries <- rep(TRUE,lenStrategies)
	shortExists	<- rep(TRUE,lenStrategies)
	systemNames <- rep(systemName,lenStrategies)
	
	fieldNames <- c('System','Strategy','LongEntry','LongExit','ShortEntry','ShortExit')			
	values <- cbind(systemNames,strategyNames,longEntries,longExists,shortEntries,shortExists)
	return(this$insertToSystemDB('SystemStrategies',fieldNames,values))
})

method("insertSystemDetailsTable", "SystemDBManager", function(this, systemName=NULL, version=NULL, interval=NULL, stoDir='NA',stoID='NA',pvName='NA',...){
	needs(systemName='character', version='character', interval='character', stoDir='character?',stoID='character?',pvName='character?')
	if (!this$alreadyExists('System','Name',systemName)) return(squish('System does not exist in SystemDB..System table: ',systemName))
	if (!this$alreadyExists('Version','Name',version)) return(squish('Version does not exist in SystemDB..Version table: ',version))
	if (!this$alreadyExists('Interval','Name',interval)) return(squish('Interval does not exist in SystemDB..Interval table: ',interval))
	
	fieldNames <- c('system_name','version','interval','sto_dir','sto_id','pv_name')
	values <- c(systemName, version, interval, stoDir, stoID, pvName)
	if (this$alreadyExistsMultiKey('SystemDetails',fieldNames,values)) return('Record already exists in SystemDB..SystemDetails')	
	return(this$insertToSystemDB('SystemDetails',fieldNames,values))	
})

method("insertSystem","SystemDBManager", function(this, systemName, systemDescription, systemDocumentation=NULL, systemOwner, strategyNames, ...){
#Main wrapper function to insert a new system
		
		insertResult <- this$insertSystemTable (systemName,systemDescription, systemDocumentation, systemOwner)
		if(insertResult==TRUE)
			insertResult <- this$insertSystemStrategiesTable (systemName, strategyNames)
		
		return(insertResult)
})

method("insertStrategyAndSystem","SystemDBManager", function(this, name, class, description, documentation=NULL, owner, parameterNames, parameterDescriptions, ...){
#Main wrapper function to insert a new system-strategy
	
	insertResult <- this$insertStrategy(name, class, description, owner, parameterNames, parameterDescriptions)
	if(insertResult==TRUE)
		insertResult <- this$insertSystem(name,description, documentation, owner, name)
	
	return(insertResult)
})

####################################################################################################
#    MSIV Tables
####################################################################################################

method("insertMSIVTable", "SystemDBManager", function(this, markets, systemName, interval, version, ...){
	needs(markets="character", systemName="character", interval="character", version="character")
	
	if (!this$alreadyExists('Interval','Name',interval)) return (squish('Interval does not exist in SystemDB..Interval table: ',interval))
	if (!this$alreadyExists('Version','Name',version)) return (squish('Version does not exist in SystemDB..Version table: ',version))
	if (!this$alreadyExists('System','Name',systemName)) return(squish('System does not exist in SystemDB..System table: ', systemName))
	
	for (market in markets)
		if(!this$alreadyExists('Market','Name',market)) return(squish('Market does not exist in SystemDB..Markets table: ',market))
	
	msivNames <- paste(markets, systemName, interval, version, sep="_")
	
	notNeeded <- c()
	for (x in 1:length(msivNames))
		if(this$alreadyExists('MSIV','Name',msivNames[x])) {
			print(squish('MSIV already exists in SystemDB..MSIV table: ',msivNames[x]))
			notNeeded <- c(notNeeded,x)
		}
	if(!is.null(notNeeded)){
		markets <- markets[-notNeeded]
		msivNames <- msivNames[-notNeeded]
	}
	
	fieldNames <- c('Market','System','Interval','Version','Name')
	values <- cbind(markets,systemName,interval,version,msivNames)
	
	return(this$insertToSystemDB('MSIV',fieldNames,values))
})

method("updateMSIVTable", "SystemDBManager", function(this, markets, systemName, interval, version, inSampleSTOid='', outSampleSTOid='', ...){
	needs(markets="character", systemName="character", interval="character", version="character", inSampleSTOid='character', outSampleSTOid='character')
	
	if (!this$alreadyExists('Interval','Name',interval)) return (squish('Interval does not exist in SystemDB..Interval table: ',interval))
	if (!this$alreadyExists('Version','Name',version)) return (squish('Version does not exist in SystemDB..Version table: ',version))
	if (!this$alreadyExists('System','Name',systemName)) return(squish('System does not exist in SystemDB..System table: ', systemName))
		
	msivs <- paste(markets, systemName, interval, version, sep="_")
	for (msiv in msivs)
		failIf(!this$alreadyExists('MSIV','Name',msiv), squish('MSIV does not exist in SystemDB..MSIV table: ',msiv))
		
	fieldNames <- c('Market','System','Interval','Version','Name','InSampleSTOid','OutSampleSTOid')
	values <- cbind(markets,systemName,interval,version,msivs, inSampleSTOid, outSampleSTOid)
	
	return(this$updateInSystemDB('MSIV',fieldNames, keyFieldNames=c('Market','System','Interval','Version','Name'), values))
})

method("insertMSIVBacktestTable", "SystemDBManager", function(this, msivNames, runDate='', stoID='', stoDir='', startDate='', endDate='',
																	validationAccept='',...){
	needs(msivNames="character", runDate="character?", stoID="character?", stoDir="character?", startDate="character?", endDate="character?",
			validationAccept="character?")
	for (msiv in msivNames)
		if(!this$alreadyExists('MSIV','Name',msiv)) return(squish('MSIV does not exist in SystemDB..MSIV table: ', msiv))
	
	failIf((!this$checkStringDates(startDate)),"StartDate in bad format, must be character YYYYMMDD")
	failIf((!this$checkStringDates(endDate)),"EndDate in bad format, must be character YYYYMMDD")
	
	fieldNames <- c('MSIV_Name','RunDate','STOid','STOdir','StartDate','EndDate','ValidationAccept')
	values <- cbind(msivNames, runDate, stoID, stoDir, startDate, endDate, validationAccept)
	return(this$insertToSystemDB('MSIVBacktest',fieldNames,values))
})

method("checkStringDates", "SystemDBManager", function(this, dates, ...){
	needs(dates="character?")
	dates <- dates[!(dates=='')]
	if (length(dates)==0) return(TRUE)
	dates <- na.omit(dates)
	if (any(nchar(dates)!=8)) return(FALSE)
	if (any(is.na(as.numeric(dates)))) return(FALSE)
	return(TRUE)
})

method("insertPortfolioBacktest", "SystemDBManager", function(this, msivNames, weights=NULL, stoID='', stoDir='', portfolioName='', ...){
	needs(msivNames="character", stoID="character", stoDir="character", portfolioName="character", weights="numeric|integer")
	for (msiv in msivNames)
		if(!this$alreadyExists('MSIV','Name',msiv)) return(squish('MSIV does not exist in SystemDB..MSIV table: ', msiv))
		
	if (length(weights)==1) weights <- rep(weights,length(msivNames))
	failIf((length(msivNames) != length(weights)),"Weights vector must be same length as msivs or length of one.")
	
	fieldNames <- c('STOdir','STOid','PortfolioName','MSIV_Name','weight')
	values <- cbind(stoDir,stoID,portfolioName,msivNames,weights)
	return(this$insertToSystemDB('PortfolioBacktest',fieldNames,values))
})

method("insertInitialMSIV", "SystemDBManager", function(this, markets, systemName, interval, version, documentation, stoID, stoDir, owner, ...){
#Wrapper for initial MSIV setup prior to STO run
	needs(markets='character', systemName='character',interval='character',version='character')
	
	msivNames <- paste(markets, systemName, interval, version, sep="_")
		
		insertResult <- this$insertMSIVTable(markets, systemName, interval, version, documentation)
		if (insertResult==TRUE)
			this$insertMSIVBacktestTable(msivNames=msivNames, stoID=stoID, stoDir=stoDir, owner=owner)
		return(insertResult)
})

method("updateMSIVBacktestTableResults", "SystemDBManager", function(this, markets, systemName, interval, version, stoID,
																	runDate=NULL, validationAccept=NULL,...){
	needs(markets="character",systemName='character',interval='character',version='character',stoID="character")
	msivNames <- paste(markets, systemName, interval, version, sep="_")
	for (msiv in msivNames)
		if(!this$alreadyExists('MSIV','Name',msiv)) return(squish('MSIV does not exist in SystemDB..MSIV table: ', msiv))
	fieldNames <- c('MSIV_Name','STOid') 
	values <- cbind(msivNames, stoID)
	for (field in c('RunDate','ValidationAccept')){
		fieldVariable <- get(paste(tolower(substring(field, 1,1)), substring(field, 2), sep=""))
		if (!is.null(fieldVariable)){
			fieldNames <- c(fieldNames,field)
			values <- cbind(values,fieldVariable)
		}	
	}
	return(this$updateInSystemDB('MSIVBacktest',fieldNames,c('MSIV_Name','STOid'),values))	
})

####################################################################################################
#    Live Tables
####################################################################################################

method("insertMSIVParameterValuesTable", "SystemDBManager", function(this, markets, systemName, interval, version, pvName, ...){
	needs(markets="character", systemName="character", interval="character", version="character", pvName="character")
	
	if(!this$alreadyExists('ParameterValues','Name',pvName)) return(squish('PVName does not exist in SystemDB..ParameterValues table: ',pvName))		
	msivNames <- paste(markets, systemName, interval, version, sep="_")
	for (msivName in msivNames){
		if(!this$alreadyExists('MSIV','Name',msivName)) return(squish('MSIV does not exist in SystemDB..MSIV table: ',msivName))
		failIf(this$alreadyExistsMultiKey('MSIVParameterValues',c('MSIV_Name','PV_Name'),c(msivName, pvName)), 
												squish('MSIV already exists in PVName: ',msivName))
	}
		
	fieldNames <- c('MSIV_Name','PV_Name')
	values <- cbind(msivNames,pvName)
	return(this$insertToSystemDB('MSIVParameterValues',fieldNames,values))
})

method("insertParameterValuesTable", "SystemDBManager", function(this, system, pvName, parameterNames, parameterValues, asOfDate, ...){
	needs(system='character', pvName='character', parameterNames='character', parameterValues='character', asOfDate='character')
	failIf((length(parameterNames)!=length(parameterValues)), 'Length of parameter names and values must be the same.')
	failIf(!this$alreadyExists('System', 'Name', system), squish('System name not valid: ',system))
	failIf(!this$alreadyExists('Strategy', 'Name', system), squish('System name not valid: ',system))
	
	failIf(!all(parameterNames %in% SystemDB$parameterNames(system)), 'Parameter names not valid for this system.')
	for (parameterName in parameterNames)
		failIf(!this$alreadyExists('StrategyParameterNames', 'ParameterName', parameterName), squish('Invalid parameter: ',parameterName))	

	fieldNames <- c('System','Name','Strategy','ParameterName','ParameterValue','AsOfDate')
	values <- cbind(system, pvName, system, parameterNames, parameterValues, asOfDate)
	return(this$insertToSystemDB('ParameterValues',fieldNames,values))
})

method('insertMSIVLiveHistory', 'SystemDBManager', function(this,  markets, system, interval, version, pvName, startDate, endDate='', ...){
	needs(markets="character", system="character", interval="character", version="character", pvName="character", 
			startDate="character", endDate="character?")
	
	failIf (!this$alreadyExists('Interval','Name',interval), squish('Interval does not exist in SystemDB..Interval table: ',interval))
	failIf (!this$alreadyExists('Version','Name',version), squish('Version does not exist in SystemDB..Version table: ',version))
	failIf (!this$alreadyExists('System','Name',system), squish('System does not exist in SystemDB..System table: ', system))
	
	msivNames <- paste(markets, system, interval, version, sep="_")
	for (msivName in msivNames){
		failIf(!this$alreadyExistsMultiKey('MSIVParameterValues',c('MSIV_Name','PV_Name'),c(msivName, pvName)), 
												squish('Not valid MSIV: ',msivName))
		if (endDate==''){
			endDates <- as.vector(SystemDB$liveHistoryStartEndDates(msivName, pvName)$End_trading)
			if (!is.null(endDates)) failIf(any(is.na(endDates)), squish('MSIV already in on state: ',msivName))
		}
	}
	
	fieldNames <- c('MSIV_Name', 'PV_Name', 'Start_trading', 'End_trading')
	values <- cbind(msivNames, pvName, startDate, endDate)
	return(this$insertToSystemDB('MSIVLiveHistory',fieldNames,values))
})

method("insertBloombergTag", 'SystemDBManager', function(this, systemID, bloombergTag, ...){
	needs(systemID='numeric|integer', bloombergTag='character')
	failIf(!is.null(SystemDB$bloombergTag(systemID)), squish('SystemID already has a bloomberg tag: ',systemID))
	
	systemDetails <- SystemDB$systemDetails(systemID)
	fieldNames <- c('systemId','System','Interval','Version','PV_Name','Tag')
	values <- c(as.character(systemID), systemDetails$system, systemDetails$interval, systemDetails$version, systemDetails$pvName, bloombergTag)
	return(this$insertToSystemDB('BloombergTags',fieldNames,values))	
})

####################################################################################################
#    Portfolio Tables
####################################################################################################

method("insertGroupMemberMSIVPVs", "SystemDBManager", function(this, group, markets, system, interval, version, pvName, weight=1, ...){
	needs(group='character', markets='character', system='character', interval='character', version='character', pvName='character', weight='numeric|integer')

	failIf(!this$alreadyExists('Groups','Name',group), squish('No group: ',group))
	msivNames <- paste(markets, system, interval, version, sep="_")
	for (msiv in msivNames){
		failIf(!this$alreadyExists('MSIV','Name',msiv), squish('MSIV does not exist in SystemDB..MSIV table: ', msiv))
		failIf(this$alreadyExistsMultiKey('GroupMemberMSIVPVs',c('GroupName','MSIV_Name','PV_Name'),c(group,msiv,pvName)), squish('MSIV already exists in group: ', msiv))
	}
	
	fieldNames <- c('GroupName', 'MSIV_Name', 'PV_Name', 'Weight')
	values <- cbind(group, msivNames, pvName, weight)
	return(this$insertToSystemDB('GroupMemberMSIVPVs',fieldNames,values))
})
