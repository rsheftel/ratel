# RunGroups Class
# 
# Author: rsheftel
###############################################################################

constructor("RunGroups", function(stoSetup=NULL, systemID=NULL, loadFromFile=FALSE){
	this <- extend(RObject(), "RunGroups", .stoSetup=stoSetup, .systemID=systemID)
	constructorNeeds(this, stoSetup="STOSetup?", systemID="numeric|integer?")
	if (inStaticConstructor(this)) return(this)
	failIf((!is.null(stoSetup) && !is.null(systemID)),"Can only supply either a STOSetup object or systemID, not both.")
	if(is.null(this$.stoSetup)) this$loadStoDetails() 
	this$loadStoObject()
	this$clearFilters()
	this$clearGroups()
	if(loadFromFile) this$loadFromFile()
	return(this)
})

method("loadStoDetails","RunGroups", function(this, verbose=TRUE, ...){
	this$.stoSetup <- STOSetup(systemID=this$.systemID)
	if(verbose) cat(squish('\nLoading system details from SystemDB for system.id : ',this$.systemID,'\n'))
	this$.stoSetup$loadFromSystemDB(verbose=verbose)
})

method("loadStoObject", "RunGroups", function(this, verbose=TRUE, ...){
	if(verbose) cat("\nLoading the sto object...\n")
	this$.sto <- this$.stoSetup$stoObject()
	if(is.null(this$.sto)) this$.sto <- STO(this$.stoSetup$stoDirectory(), this$.stoSetup$stoID(), calculateMetrics=FALSE)
})

method("stoObject", "RunGroups", function(this, ...){
	return(this$.sto)	
})

method("stoSetupObject", "RunGroups", function(this, ...){
	return(this$.stoSetup)	
})

#################################################################################################################
#	File Methods
#################################################################################################################
method("saveToFile", "RunGroups", function(this, ...){
	dput(this$.groups, squish(this$.stoSetup$stoDirectory(),'/',this$.stoSetup$stoID(),'/RunGroups.dput'))
})

method("loadFromFile", "RunGroups", function(this, systemID=NULL, ...){
	needs(systemID="integer|numeric?")
	stoDir <- squish(this$.stoSetup$stoDirectory(),'/',this$.stoSetup$stoID())   
	if(!is.null(systemID)){
		systemDetails <- SystemDB$systemDetails(systemID, makeFilenameNative=TRUE)
		stoDir <- squish(systemDetails$stoDirectory,'/',systemDetails$stoID) 
	}	
	failIf(!file.exists(squish(stoDir,'/RunGroups.dput')), 'No RunGroups.dput file.')
	this$.groups <- dget(squish(stoDir,'/RunGroups.dput'))
})

#################################################################################################################
#	RunGroup Methods
#################################################################################################################

method("clearGroups", "RunGroups", function(this, ...){
	this$.groups <- list()	
})

method("addGroup", "RunGroups", function(this, name=NULL, portfolios=NULL, runs=NULL, ...){
	needs(name="character", portfolios="character?", runs="integer|numeric?")
	if (is.null(runs)){
		this$.groups[[name]]$runs <- this$filtersRuns()
	}else{
		this$.groups[[name]]$runs <- runs
	}
	if (is.null(portfolios)){
		this$.groups[[name]]$portfolios <- this$.stoSetup$portfolioNames()
	}else{
		this$.groups[[name]]$portfolios <- portfolios
	}
})

method("groupNames", "RunGroups", function(this, ...){
	return(names(this$.groups))	
})

method("group", "RunGroups", function(this, name, ...){
	failIf((!(name %in% this$groupNames())), squish('Group name ',name,' not in list of groups.'))
	return(this$.groups[[name]])		
})

method("removeGroup", "RunGroups", function(this, name, ...){
	failIf((!(name %in% this$groupNames())), squish('Group name ',name,' not in list of groups.'))
	this$.groups[[name]] <- NULL
})

method("groupPortfolios", "RunGroups", function(this, name, portfolios=NULL, replace=TRUE, ...){
	needs(name="character", portfolios="character?", replace="logical")
	failIf((!(name %in% this$groupNames())), squish('Group name ',name,' not in list of groups.'))
	if (is.null(portfolios)) return(this$group(name)$portfolios)
	if (replace) this$.groups[[name]]$portfolios <- portfolios
	if (!replace) this$.groups[[name]]$portfolios <- unique(c(portfolios, this$group(name)$portfolios))
})

#################################################################################################################
#	Filter Methods
#################################################################################################################

method("filters", "RunGroups", function(this, ...){
	return(this$.filters)	
})

method("clearFilters", "RunGroups", function(this, ...){
	this$.filters <- data.frame()
	this$.runFilter <- this$.sto$parameters()$filter(NULL)	
})

method("addFilter", "RunGroups", function(this, parameter=NULL, operator=NULL, value=NULL,...){
	needs(parameter="character", operator="character", value="character")
	failIf((!(parameter %in% this$.stoSetup$parameters()$name)),'Parameter not valid.')
	this$.filters <- rbind(this$.filters,data.frame(parameter=parameter,operator=operator,value=value))	 
	this$.filter()
})

method(".filter", "RunGroups", function(this, ...){
	filter.vector <- NULL
	for (x in 1:NROW(this$.filters)){
		filter.vector <- c(filter.vector,squish(this$.filters[x,'parameter'],this$.filters[x,'operator'],this$.filters[x,'value']))
	} 
	filter <- paste(filter.vector,collapse=" & ")
	this$.runFilter <- eval(parse(text=squish('this$.sto$parameters()$filter(',filter,')')))
})

method("filtersRuns", "RunGroups", function(this, ...){
	return(this$.runFilter$runs())
})
