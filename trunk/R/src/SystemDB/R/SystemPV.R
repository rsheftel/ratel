# SystemPV Class
# 
# Author: rsheftel
###############################################################################

constructor("SystemPV", function(systemID=NULL){
	this <- extend(RObject(), "SystemPV", .systemID=systemID)
	constructorNeeds(this, systemID="numeric|integer")
	if (inStaticConstructor(this)) return(this)
	this$commitToDB(FALSE)
	this$.constructFromSystemID()
	return(this)
})

method(".constructFromSystemID", "SystemPV", function(this, ...){
	failIf(!SystemDB$alreadyExists('SystemDetails','id',this$.systemID),squish('SystemID does not exist: ',this$.systemID))
	this$.systemDetails <- SystemDB$systemDetails(systemID=this$.systemID)
	this$.pvName <- systemDetails$pvName 		
	failIf((this$.systemDetails$pvName=='NA'), squish('SystemID does not have a PVName: ', this$.systemID))
})

####################################################################################################
#    Getters
####################################################################################################

method("markets", "SystemPV", function(this, ...){
	return(SystemDB$marketFromMsivs(SystemDB$msivsForPV(this$.systemDetails$pvName)))		
})

####################################################################################################
#    SystemDB Methods
####################################################################################################

method('commitToDB', "SystemPV", function(this, commitToDB=NULL, ...){
	needs(commitToDB='logical?')
	if(is.null(commitToDB)) return(this$.commitToDB)
	this$.commitToDB <- commitToDB	
})

method(".createSystemDBObject", "SystemPV", function(this, ...){
	if (is.null(this$.systemDB)) this$.systemDB <- SystemDBManager()
	this$.systemDB$commitToDB(this$.commitToDB)	
})

method("addMarkets", "SystemPV", function(this, markets, portfolioGroup=NULL, ...){
	needs(markets='character', portfolioGroup='character?')
	this$.createSystemDBObject()
	
	for (market in markets)
		failIf(!SystemDB$alreadyExists('Market','Name',market), squish('Not valid market: ',market))
	
	res <- list()
	print('Attaching MSIVs to PV')
	res$MSIVParamValues <- this$.systemDB$insertMSIVParameterValuesTable(	markets=	markets, 	
																			systemName=	this$.systemDetails$system,
																			interval=	this$.systemDetails$interval,
																			version =	this$.systemDetails$version,
																			pvName  =	this$.systemDetails$pvName)
	if(!is.null(portfolioGroup)){
		print(squish('Inserting MSIVs to PortfolioGroup: ',portfolioGroup))
		res$groupMemberMSIVPVs <- this$.systemDB$insertGroupMemberMSIVPVs(	group=		portfolioGroup, 
																			markets=	markets,
																			system=		this$.systemDetails$system,
																			interval=	this$.systemDetails$interval,
																			version =	this$.systemDetails$version,
																			pvName  =	this$.systemDetails$pvName)
	}									
	return(res)														
})

method("turnMarketsOn", "SystemPV", function(this, markets, startDate, portfolioGroup, ...){
	needs(markets='character', startDate='character', portfolioGroup='character')
	this$.createSystemDBObject()
	failIf(is.null(SystemDB$bloombergTag(this$.systemID)), 'Must have bloomberg tag to make live.')
	
	msivs <- paste(markets, this$.systemDetails$system, this$.systemDetails$interval, this$.systemDetails$version, sep="_")
	groupMemberMSIVs <- SystemDB$memberGroupMSIVPVsForGroup(portfolioGroup, this$.systemDetails$pvName)
	for (msiv in msivs)
		failIf(!(msiv %in% groupMemberMSIVs), squish('Cannot make an MSIV live until it is the portfolioGroup: ',msiv))
	
	print('Inserting MSIVs to MSIVLiveHistory')
	return(this$.systemDB$insertMSIVLiveHistory(	markets =	markets, 	
													system  =	this$.systemDetails$system,
													interval=	this$.systemDetails$interval,
													version =	this$.systemDetails$version,
													pvName  =	this$.systemDetails$pvName,
													startDate=  startDate))
})
