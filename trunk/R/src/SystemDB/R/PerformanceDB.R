# PerformanceDB Class
# 
# Author: RSheftel
###############################################################################

constructor("PerformanceDB", function() {
	extend(RObject(), "PerformanceDB")        
})

method("tags", "PerformanceDB", function(static, ...){
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select("select name from PerformanceDB..Tag")
	return(as.vector(sqlResult$name))		
})

method("sources", "PerformanceDB", function(static, ...){
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select("select name from PerformanceDB..Source")
	return(as.vector(sqlResult$name))		
})

method("groups", "PerformanceDB", function(static, ...){
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select("select name from PerformanceDB..Tag_Group")
	return(as.vector(sqlResult$name))		
})

method("getPositionEquityCurve", "PerformanceDB", function(static, source=NULL, tag=NULL, range=NULL, ...){
	needs(source="character", tag="character", range="Range?")
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select(squish("select endDate, pnl from PerformanceDB..Pnl where tag_id='",
									static$tagID(tag),"' AND source_id='",static$sourceID(source),"'"))
	pnl <- zoo(sqlResult$pnl,as.POSIXct(sqlResult$endDate))
	if(!is.null(range)) pnl <- range$cut(pnl)
	equity <- cumsum(pnl)
	if (NROW(pnl)==0) return(NULL)
	return(ZooCurveLoader$fromEquity(equity,squish(tag,':',source)))
})

method("tagID", "PerformanceDB", function(static, tag=NULL, ...){
	needs(tag="character")
	return(static$.getOneValue(squish("select distinct id from PerformanceDB..Tag where name = '",tag,"'")))
})

method("tagName", "PerformanceDB", function(static, tagID=NULL, ...){
	needs(tagID="numeric")
	return(static$.getOneValue(squish("select distinct name from PerformanceDB..Tag where id = '",tagID,"'")))
})

method("sourceID", "PerformanceDB", function(static, source=NULL, ...){
	needs(source="character")
	return(static$.getOneValue(squish("select distinct id from PerformanceDB..Source where name = '",source,"'")))
})

method("tagGroupID", "PerformanceDB", function(static, tagGroup=NULL, ...){
	needs(tagGroup="character")	
	return(static$.getOneValue(squish("select distinct id from PerformanceDB..Tag_Group where name = '",tagGroup,"'")))
})

method(".getOneValue", "PerformanceDB", function(static, sqlString, ...){
	needs(sqlString="character")
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select(sqlString)
	failIf(NROW(sqlResult)==0, squish("No record found for SQL select: ",sqlString))
	return(as.character(sqlResult[[1]]))
})

method("insertPnL", "PerformanceDB", function(static, startDate=NULL, endDate=NULL, source=NULL, tag=NULL, pnl=NULL, commitToDB=FALSE, update=FALSE, ...){
	needs(startDate="POSIXct", endDate="POSIXct", source="character", tag="character", pnl="numeric|integer", commitToDB="logical?")
	failIf(!(tag %in% static$tags()), squish('Not a valid tag: ',tag))
	failIf(!(source %in% static$sources()), squish('Not a valid source: ',source))
	fieldNames <- c('startDate','endDate','source_id','tag_id','pnl')
	if(update) fieldNames <- c('startDate*','endDate*','source_id*','tag_id*','pnl')
	values <- c(as.numeric(format(startDate,"%Y%m%d")), as.numeric(format(endDate,"%Y%m%d")),
				 static$sourceID(source), static$tagID(tag), pnl)
	insertResult <- uploadToDB('PerformanceDB', 'Pnl', fieldNames, values, commitToDB)
	if(insertResult==FALSE) insertResult <- 'Unable to insert values into PerformanceDB'
	return(insertResult)
})

method("updatePnL","PerformanceDB", function(static, startDate=NULL, endDate=NULL, source=NULL, tag=NULL, pnl=NULL, commitToDB=FALSE, ...){
	needs(startDate="POSIXct", endDate="POSIXct", source="character", tag="character", pnl="numeric|integer", commitToDB="logical?")
	return(static$insertPnL(startDate,endDate,source,tag,pnl,commitToDB,update=TRUE))
})

method("tagsForTagGroup", "PerformanceDB", function(static, tagGroup=NULL, ...){
	needs(tagGroup="character")
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select(squish("select tag_id from PerformanceDB..Tag_Group_Members_Tag where tag_group_id='",
									static$tagGroupID(tagGroup),"'"))
	tagIDs <- sqlResult$tag_id
	return(sapply(tagIDs, function(x) static$tagName(x)))
})

method("investorDataNames", "PerformanceDB", function(static, ...){
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select("select descInvDataDef from PerformanceDB..InvestorDataDefinition")
	return(as.vector(sqlResult$descInvDataDef))		
})

method("investorDataID", "PerformanceDB", function(static, name=NULL, ...){
	needs(name="character")
	return(static$.getOneValue(squish("select distinct invDataDefId from PerformanceDB..InvestorDataDefinition where descInvDataDef = '",name,"'")))
})

method("investorData", "PerformanceDB", function(static, name=NULL, range=NULL, ...){
	needs(name="character", range="Range?")
	conn <- SQLConnection()
	conn$init()
	sqlResult <- conn$select(squish("select date,value from PerformanceDB..InvestorData where invDataDefId='",static$investorDataID(name),"'"))
	resultZoo <- zoo(sqlResult$value,as.POSIXct(sqlResult$date))
	if(!is.null(range)) resultZoo <- range$cut(resultZoo)
	return(resultZoo)
})

method("uploadGlobeOpDailyPnl", "PerformanceDB", function(this,baseDir,uploadDir,archiveDir,fileName,commitToDB = FALSE,update = FALSE,...) {
	needs(baseDir = 'character',uploadDir = 'character',archiveDir = 'character',fileName = 'character')
	uploadDir <- squish(baseDir,'/',uploadDir)
	archiveDir <- squish(baseDir,'/',archiveDir)
	sapply(c(baseDir,uploadDir,archiveDir),requireDirectory)		
	csvFileName <- squish(uploadDir,'/',fileName)
	print(csvFileName)
	if(!file.exists(csvFileName))return(NULL)	
	data <- read.table(csvFileName,sep = ',',header = TRUE,stringsAsFactors = FALSE)
	resFrame <- data.frame(tag = NULL,baseDate = NULL,returnDate = NULL,value = NULL,result = NULL)		
	fileNameStr <- last(unlist(strsplit(csvFileName,'/')))			
	fileNameStr <- unlist(strsplit(unlist(strsplit(fileNameStr,'.csv')),'PNL_'))
	dates <- as.POSIXct(unlist(strsplit(fileNameStr,'_To_')))			
	for(i in 1:NROW(data)){			
		res <- this$insertPnL(first(dates),second(dates),'GlobeOp',data[i,'TAG'],data[i,'DailyPNL'],commitToDB,update)			
		resFrame <- rbind(resFrame, data.frame(tag = data[i,'TAG'],result = res,stringsAsFactors = FALSE))		
	}	
	file.rename(csvFileName, squish(archiveDir,'/',fileName))
	return(resFrame)
})

method('insertTag', 'PerformanceDB', function(static, tag, commitToDB=FALSE, ...){
	needs(tag='character', commitToDB='logical')
	fieldNames <- c('name')
	values <- c(tag)
	insertResult <- uploadToDB('PerformanceDB', 'Tag', fieldNames, values, commitToDB)
	if(insertResult==FALSE) insertResult <- 'Unable to insert values into PerformanceDB'
	return(insertResult)
})

method('insertTagToTagGroup', 'PerformanceDB', function(static, tag, tagGroup, commitToDB=FALSE, ...){
	needs(tag='character', tagGroup='character', commitToDB='logical')
	fieldNames <- c('tag_group_id', 'tag_id')
	values <- c(static$tagGroupID(tagGroup), static$tagID(tag))
	insertResult <- uploadToDB('PerformanceDB', 'Tag_Group_Members_Tag', fieldNames, values, commitToDB)
	if(insertResult==FALSE) insertResult <- 'Unable to insert values into PerformanceDB'
	return(insertResult)
})

method('insertTagGroup', 'PerformanceDB', function(static, tagGroup, commitToDB=FALSE, ...){
	needs(tagGroup='character', commitToDB='logical')
	fieldNames <- c('name')
	values <- c(tagGroup)
	insertResult <- uploadToDB('PerformanceDB', 'Tag_Group', fieldNames, values, commitToDB)
	if(insertResult==FALSE) insertResult <- 'Unable to insert values into PerformanceDB'
	return(insertResult)
})
