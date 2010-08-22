# System Class
# 
# Author: rsheftel
###############################################################################

constructor("System", function(system=NULL){
	this <- extend(RObject(), "System", .system=system)
	constructorNeeds(this, system="character")
	if (inStaticConstructor(this)) return(this)
	failIf(!SystemDB$alreadyExists('System','Name',system),squish('System does not exist: ',system))
	return(this)
})

method("pvNames", "System", function(this, ...){
	return(SystemDB$pvNamesForSystem(this$.system))	
})

method("parameters", "System", function(this, ...){
	return(SystemDB$parameterNames(this$.system))	
})

method("addPV", "System", function(this, pvName, parameterList=NULL, asOfDate, interval, version, bloombergTag=NULL, commitToDB=FALSE, ...){
	needs(pvName='character', parameterList='list(character)?', asOfDate='character', interval='character', version='character', bloombergTag='character?', commitToDB='logical')
	
	failIf(SystemDB$alreadyExists('ParameterValues', 'Name', pvName), squish('PVName already exists: ',pvName))	
	
	parameters <- this$parameters()
	for (parameter in names(parameterList)){
		failIf(!(parameter %in% parameters), squish('Invalid parameter name: ',parameter))
	}
	
	parameterValues <- NULL
	for (parameter in parameters){
		listValue <- parameterList[[parameter]]
		if(is.null(listValue)){
			listValue <- 'NA'
			print(squish('Parameter not defined in parameterList, will be empty in systemDB: ', parameter))
		}
		parameterValues <- c(parameterValues, as.character(listValue))
	}
	
	sys <- SystemDBManager()
	sys$commitToDB(commitToDB)
	print("Inserting to ParameterValues table...")
	res <- sys$insertParameterValuesTable(system=this$.system, pvName=pvName, parameterNames=parameters, parameterValues=parameterValues, asOfDate=asOfDate)
	failIf((!is.logical(res) && !matches(tempDirectory(),res)), res)
	
	print("Inserting to SystemDetails table...")
	this$.testfile <- sys$insertSystemDetailsTable(systemName=this$.system, version=version, interval=interval, pvName=pvName)
	failIf((!is.logical(this$.testfile) && !matches(tempDirectory(),res)), this$.testfile)
	
	if(!is.null(bloombergTag)){
		print("Inserting to BloombergTags table...")
		res <- sys$insertBloombergTag(SystemDB$systemID(system=this$.system, interval=interval, version=version, pvName=pvName), bloombergTag)
		failIf((!is.logical(res) && !matches(tempDirectory(),res)), res)
	}
})
