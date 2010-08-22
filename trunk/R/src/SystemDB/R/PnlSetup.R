# PnlSetup Class
# 
# Author: rsheftel
###############################################################################

constructor("PnlSetup", function(){
	this <- extend(RObject(), "PnlSetup")
	constructorNeeds(this)
	if (inStaticConstructor(this)) return(this)
	this$.setupVariables()
	this$commitToDB(FALSE)
	return(this)
})

method('.setupVariables', 'PnlSetup', function(this, ...){
	this$.bloombergTag <- NULL
	this$.reportGroup <- NULL
})

####################################################################################################
#    Setter Getter
####################################################################################################

method('bloombergTag', 'PnlSetup', function(this, tagName=NULL, ...){
	needs(tagName='character?')
	if(is.null(tagName)) return(this$.bloombergTag)
	failIf(!matches('^QF.',tagName), squish("Bad bloomberg tag name, must be of form 'QF.' : ",tagName))
	this$.bloombergTag <- tagName
})

method('reportGroup', 'PnlSetup', function(this, groupName=NULL, ...){
	needs(groupName='character?')
	if(is.null(groupName)) return(this$.reportGroup)
	failIf(!matches('^report_daily_',groupName), squish("Bad report group name, must be of form 'report_daily_' : ",groupName))
	this$.reportGroup <- groupName
})

####################################################################################################
#    Insert Methods
####################################################################################################

method('commitToDB', 'PnlSetup', function(this, commitToDB=NULL, ...){
	needs(commitToDB='logical?')
	if(is.null(commitToDB)) return(this$.commitToDB)
	this$.commitToDB <- commitToDB
})

method('insertPnlSetup', 'PnlSetup', function(this, ...){
	failIf(is.null(this$.bloombergTag), 'Bloomberg Tag not set.')
	failIf(is.null(this$.reportGroup), 'Report Group not set.')
	
	print('Inserting items in PerformaceDB...')
	this$.insertToPerformanceDB()
})

method('.insertToPerformanceDB', 'PnlSetup', function(this, ...){
	this$.insertTag()
	this$.insertToAllTagsGroup()
	this$.insertReportGroup()
	this$.insertTagToReportGroup()
})

method('.insertTag', 'PnlSetup', function(this, ...){
	res <- NULL
	if (!(this$.bloombergTag %in% PerformanceDB$tags())){
		print('Inserting bloomberg tag into Tag table...')
		res <- PerformanceDB$insertTag(this$.bloombergTag, this$commitToDB())
		failIf((!is.logical(res) && !matches(tempDirectory(),res)), res)
	}
	return(res)		
})

method('.insertToAllTagsGroup', 'PnlSetup', function(this, ...){
	allTags <- 'AllTags'
	res <- NULL
	if(!(this$.bloombergTag %in% PerformanceDB$tagsForTagGroup(allTags))){
		print(squish('Inserting bloomberg tag into ',allTags,' TagGroup...'))
		res <- PerformanceDB$insertTagToTagGroup(this$.bloombergTag, allTags, this$commitToDB())
		failIf((!is.logical(res) && !matches(tempDirectory(),res)), res)
	}
	return(res)
})

method('.insertReportGroup', 'PnlSetup', function(this, ...){
	res <- NULL
	if (!(this$.reportGroup %in% PerformanceDB$groups())){
		print('Inserting Report Group into Tag_Group table...')
		res <- PerformanceDB$insertTagGroup(this$.reportGroup, this$commitToDB())
		failIf((!is.logical(res) && !matches(tempDirectory(),res)), res)
	}
	return(res)		
})

method('.insertTagToReportGroup', 'PnlSetup', function(this, ...){
	res <- NULL
	if (!(this$.bloombergTag %in% PerformanceDB$tagsForTagGroup(this$.reportGroup))){
		print(squish('Inserting bloomberg tag into ',this$.reportGroup,' TagGroup...'))
		res <- PerformanceDB$insertTagToTagGroup(this$.bloombergTag, this$.reportGroup, this$commitToDB())
		failIf((!is.logical(res) && !matches(tempDirectory(),res)), res)
	}
	return(res)		
})
