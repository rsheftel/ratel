constructor("BADB", function() {
	extend(RObject(), "BADB")        
})

method("getYellowKeyForBloombergTicker", "BADB", function(static,bloombergTicker,...){
	needs(bloombergTicker = 'character')
	conn <- SQLConnection(); conn$init()
	sqlResult <- conn$select(squish("select BLOOMBERG_SECTOR from BADB..FUTURES_SYMBOL_MAPPING where BLOOMBERG_SYMBOL = '",bloombergTicker,"' and PLATFORM_ID = 'REDI'"))
	sqlResult <- unique(as.character(sqlResult[[1]]))
	failIf(length(sqlResult) == 0,squish('Bloomberg ticker ',bloombergTicker,' not found in BADB.FUTURES_SYMBOL_MAPPING table'))
	failIf(length(sqlResult) > 1,squish('No unique yellow key for Bloomberg ticker ',bloombergTicker,' in BADB.FUTURES_SYMBOL_MAPPING table'))	
	data <- BADB$bloombergYellowKeys()
	data[match(sqlResult,data[,'POMS']),'LongName']
})

method('bloombergYellowKeys', 'BADB', function(static, ...){
	data.frame(
		LongName = c('Index','Comdty','Curncy','Govt'),
		POMS = c('INDX','CMDT','CURR','GOVT')
	,stringsAsFactors = FALSE)
})