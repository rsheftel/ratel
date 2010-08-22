# STOSetup Class
# 
# Author: rsheftel
###############################################################################

constructor("STOSetup", function(system=NULL, interval=NULL, version=NULL, stoDirectory=NULL, stoID=NULL, systemID=NULL){
	this <- extend(RObject(), "STOSetup", .system=system, .interval=interval, .version=version, .stoDirectory=stoDirectory, 
											.stoID=stoID, .systemID=systemID)
	constructorNeeds(this, system="character?", interval="character?", version="character?", stoDirectory="character?", 
							stoID="character?", systemID="numeric|integer?")
	if (inStaticConstructor(this)) return(this)
	this$.clearAllLists()
	if(!is.null(systemID)) this$.constructFromSystemID()
	else if(rightStr(stoDirectory,1) == "/") this$.stoDirectory = leftStr(this$.stoDirectory,nchar(this$.stoDirectory)-1)
	return(this)
})

method(".constructFromSystemID", "STOSetup", function(this, ...){
	details <- SystemDB$systemDetails(this$.systemID, makeFilenameNative=TRUE)	
	this$.system 		<- details$system
	this$.interval 		<- details$interval
	this$.version		<- details$version
	this$.stoDirectory	<- details$stoDirectory
	this$.stoID			<- details$stoID 
})

method(".clearAllLists", "STOSetup", function(this, ...){
	this$.markets <- NULL	
	this$.portfolios <- list()
	this$.parameters <- NULL
	this$.sto <- NULL
	this$.strategyClass <- NULL
	this$.qClassName <- NULL
	this$.systemDB <- NULL
	this$.commitToDB <- FALSE
	this$.uploadedToDB <- FALSE
	this$.portfolioWeightsMethod <- 'SumToOne'
	this$clearDates()
})

#################################################################################################################
#	Getter only
#################################################################################################################

method("system", "STOSetup", function(this, ...){
	return(this$.system)	
})

method("interval", "STOSetup", function(this, ...){
	return(this$.interval)	
})

method("version", "STOSetup", function(this, ...){
	return(this$.version)	
})

method("stoDirectory", "STOSetup", function(this, ...){
	return(this$.stoDirectory)	
})

method("stoID", "STOSetup", function(this, ...){
	return(this$.stoID)	
})

method("markets", "STOSetup", function(this, ...){
	return(this$.markets)	
})

method("portfolioNames", "STOSetup", function(this, ...){
	return(names(this$.portfolios))	
})

method("portfolios", "STOSetup", function(this, ...){
	return(this$.portfolios)	
})

method("parameters", "STOSetup", function(this, ...){
	return(cbind(this$.parameters,total=cumprod(this$.parameters[,'count']))) 
})

method("systemID", "STOSetup", function(this, ...){
	return(SystemDB$systemID(system=this$.system, version=this$.version, interval=this$.interval, stoDir=this$.stoDirectory, stoID=this$.stoID))
})

method("stoObject", "STOSetup", function(this, ...){
	return(this$.sto)
})

#################################################################################################################
#	Markets
#################################################################################################################

method("addMarkets", "STOSetup", function(this, markets=NULL, filename=NULL, ...){
	needs(markets='character?', filename='character?')
	failIf ((is.null(markets) && is.null(filename)),"Must supply either a vector of markets, or a filename.")
	failIf ((!is.null(markets) && !is.null(filename)),"Can only supply either a vector of markets, or a filename.")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	
	if (!is.null(filename)){
		markets <- read.csv(filename, header=FALSE)
		markets <- as.vector(markets[,1])
	}
	newMarkets <- markets[!(markets %in% this$.markets)]
	this$.markets <- c(this$.markets, newMarkets)
})

method("addPortfolio", "STOSetup", function(this, name=NULL, markets=NULL, ...){
	needs(name='character', markets='character')
	failIf(any(name %in% this$portfolioNames()),"Portfolio already exists.")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	
	priornames <- names(this$.portfolios)
	this$.portfolios <- appendSlowly(this$.portfolios, markets)
	names(this$.portfolios) <- c(priornames, name)
	this$addMarkets(markets)				
})

method("addPortfolios", "STOSetup", function(this, filename=NULL, ...){
	needs(filename="character")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	filedata <- read.csv(filename, header=TRUE)
	portfolioNames <- as.vector(unique(filedata$Portfolio))
	for (portfolioName in portfolioNames){
		markets <- as.vector(filedata[portfolioName==filedata$Portfolio,'Market'])
		this$addPortfolio(portfolioName, markets)
	}
})

method("addMarketsToPortfolio", "STOSetup", function(this, portfolio=NULL, markets=NULL, ...){
	needs(portfolio='character', markets='character')
	failIf(!any(portfolio %in% this$portfolioNames()),"Portfolio does not exist.")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	
	portfolioMarkets <- this$.portfolios[[portfolio]]
	newMarkets <- markets[!(markets %in% portfolioMarkets)]
	this$.portfolios[[portfolio]] <- c(portfolioMarkets, newMarkets)
})

#################################################################################################################
#	Parameters
#################################################################################################################
method("addParameters", "STOSetup", function(this, name=NULL, start=NULL, end=NULL, step=NULL, ...){
	needs(name="character", start="numeric|integer", end="numeric|integer", step="numeric|integer")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	failIf(any(name %in% this$.parameters$name),squish('Parameter already exists : ',name))
	counts <- (end - start)/step + 1
	counts[is.nan(counts)] <- 1
	failIf(any(abs(counts - round(counts,6))>1e-6),"Start to end not an even number of steps") 
	this$.parameters <- rbind(this$.parameters, data.frame(name=name, start=start, end=end, step=step, count=round(counts,6)))
})

method("updateParameter", "STOSetup", function(this, name=NULL, start=NULL, end=NULL, step=NULL, ...){
	needs(name="character", start="numeric|integer?", end="numeric|integer?", step="numeric|integer?")
	failIf(!any(name %in% this$.parameters[,'name']), "Parameter not defined yet, cannot update.")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	
	paramRow <- match(name,this$.parameters[,'name'])
	if(!is.null(start)) this$.parameters[paramRow,'start'] <- start
	if(!is.null(end)) this$.parameters[paramRow,'end'] <- end
	if(!is.null(step)) this$.parameters[paramRow,'step'] <- step		
	counts <- (this$.parameters[paramRow,'end'] - this$.parameters[paramRow,'start']) / this$.parameters[paramRow,'step'] + 1
	failIf(((counts - trunc(counts))!=0),"Start to end not an even number of steps")
	this$.parameters[paramRow,'count'] <- counts
})

#################################################################################################################
#	Dates
#################################################################################################################
method("startEndDates", "STOSetup", function(this, start=NULL, end=NULL, filename=NULL, ...){
	needs(start="character|POSIXct?", end="character|POSIXct?", filename="character?")
		
	if (is.null(start) && is.null(end) && is.null(filename)){
		this$.makeDateVectors()
		return(list(start=this$.startDates, end=this$.endDates))
	}
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	this$.setDates(start,"start")
	this$.setDates(end,"end")
	if (!is.null(filename)){
		filedata <- read.csv(filename, header=TRUE)
		markets <- as.character(as.vector(filedata$Market))
		nameMap <- list(StartDate='start',EndDate='end')
		for (sOrE in c('StartDate','EndDate')){
			dates <- as.vector(filedata[[sOrE]])
			names(dates) <- markets
			this$.setDates(na.omit(dates),nameMap[[sOrE]])
		}
	}
})

method("clearDates", "STOSetup", function(this, ...){
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	this$.marketDates <- list()
		this$.marketDates$start$default <- NA
		this$.marketDates$end$default <- NA
	this$.startDates <- NULL
	this$.endDates <- NULL
})

method(".setDates", "STOSetup", function(this, x, sOrE,...){
	if (is.null(x)) return()
	if (any(is(x)=="POSIXct")) x <- format(x,"%Y%m%d")
	if (is.null(names(x))){
		failIf((length(x) > 1),"Vector of more than one date must be named.")
		this$.marketDates[[sOrE]]$default <- x 
	}else{
		failIf(!all(names(x) %in% this$markets()),"Bad market names.")
		this$.marketDates[[sOrE]]$markets <- this$.addOrReplace(this$.marketDates[[sOrE]]$markets, x)
	}
})

method(".makeDateVectors", "STOSetup", function(this, ...){
	markets <- this$markets()
	this$.startDates <- rep(this$.marketDates$start$default, length(markets))
	this$.endDates   <- rep(this$.marketDates$end$default, length(markets))
	names(this$.startDates) <- markets
	names(this$.endDates)   <- markets
	this$.startDates <- this$.addOrReplace(this$.startDates, this$.marketDates$start$markets)
	this$.endDates 	 <- this$.addOrReplace(this$.endDates, this$.marketDates$end$markets)
})

method(".addOrReplace", "STOSetup", function(this, vector.original, vector.new, ...){
	names.original <- names(vector.original)
	names.new	  <- names(vector.new)
	for (name in names.new){
		if (name %in% names.original){
			vector.original[[name]] <- vector.new[[name]]
		}else{
			priorNames <- names(vector.original)
			vector.original <- c(vector.original, vector.new[[name]])
			names(vector.original) <- c(priorNames, name)
		}
	}
	return(vector.original)
})

#################################################################################################################
#	Portfolio Weights
#################################################################################################################
method("portfolioWeights", "STOSetup", function(this, method=NULL, ...){
	needs(method="character?")
	if (is.null(method)) return(this$.portfolioWeightsMethod)
	failIf(!(method %in% c('SumToOne','EachIsOne')),"Method must be from list c('SumToOne','EachIsOne')")
	failIf(!is.null(this$.sto),"Cannot use method after sto files have been created.")
	failIf(this$.uploadedToDB, "Cannot use method after uploaded to SystemDB.")
	
	this$.portfolioWeightsMethod <- method	
})

#################################################################################################################
#	STO Files
#################################################################################################################
method("createSTOfiles", "STOSetup", function(this, ...){
	failIf(!is.null(this$.sto), "STO files already created, cannot do again.")
	dir.create(this$.stoDirectory, showWarnings=FALSE, recursive=TRUE)
	this$.sto <- STO$create(dir=this$.stoDirectory, id=this$.stoID, msivs=this$.makeMSIVs(), calculateMetrics=FALSE)
	this$.sto$parameters(this$.makeParameterSpace())
	file.remove(squish(this$.stoDirectory,'/',this$.stoID,'/MSIVs.csv'))
	print("STO files created.")
})

method(".makeMSIVs", "STOSetup", function(this, ...){
	siv <- SIV(this$.system, this$.interval, this$.version)
	return(siv$m(this$.markets))	
})

method(".makeParameterSpace", "STOSetup", function(this, ...){
	failIf(is.null(this$.parameters),"No parameters to make ParameterSpace.")
	inputs.df <- as.data.frame(t(this$.parameters[,c('start','end','step')]))
	colnames(inputs.df) <- this$.parameters[,'name']
	return(ParameterSpace(inputs.df))
})

#################################################################################################################
#	SystemDB
#################################################################################################################

method("strategyClass", "STOSetup", function(this, class=NULL, ...){
	needs(class="character?")
	if (is.null(class)) return(this$.strategyClass)
	if (!SystemDBManager$alreadyExists('Class','Name',class)) print(squish('Class does not exist in SystemDB: ',class))
	this$.strategyClass <- class 
})

method("systemQClassName", "STOSetup", function(this, qClassName=NULL, ...){
	needs(qClassName="character?")
	if(is.null(qClassName)) return(this$.qClassName)
	this$.qClassName <- qClassName	
})

method("uploadToSystemDB", "STOSetup", function(this, commitToDB=FALSE, ...){
	failIf(this$.uploadedToDB, "Already uploaded to SystemDB.")
	failIf(SystemDB$alreadyExists('SystemDetails','sto_id',this$.stoID), squish('Cannot upload to SystemDB, stoID already exists: ',this$.stoID))
	
	this$.commitToDB = commitToDB
	this$.uploadedToDB <- TRUE
	
	cat("\n\nInserting Strategy...\n")
	print(this$.uploadStrategy())
	cat("\n\nInserting Parameters...\n")
	print(this$.uploadParameters())
	cat("\n\nInserting System...\n")
	print(this$.uploadSystem())
	cat("\n\nInserting System-Strategy...\n")
	print(this$.uploadSystemStrategy())
	cat("\n\nInserting System Details...\n")
	print(this$.uploadSystemDetails())
	cat("\n\nInserting MSIVs...\n")
	print(this$.uploadMSIVs())
	cat("\n\nInserting MSIV Backtest...\n")
	print(this$.uploadMSIVBacktest())
	cat("\n\nInserting Portfolios...\n")
	print(this$.uploadPortfolioBacktest())
	cat("\n\nDone.\n\n")
})

method(".createSystemDBObject", "STOSetup", function(this, ...){
	if (is.null(this$.systemDB)) this$.systemDB <- SystemDBManager()
	this$.systemDB$commitToDB(this$.commitToDB)	
})

method(".uploadStrategy", "STOSetup", function(this, ...){
	this$.createSystemDBObject()
	failIf(is.null(this$.strategyClass), "Strategy class cannot be NULL.")
	return(this$.systemDB$insertStrategyTable(	strategyName=this$.system, 
												strategyClass=this$.strategyClass, 
												strategyDescription='',
												strategyOwner=''))	
})

method(".uploadParameters", "STOSetup", function(this, ...){
	this$.createSystemDBObject()
	failIf(is.null(this$.parameters), "Parameters cannot be NULL.")
	return(this$.systemDB$insertStrategyParameterNames(	strategyName=this$.system,
														parameterNames=as.vector(this$.parameters$name),
														parameterDescriptions=rep('',length(this$.parameters$name))))
})

method(".uploadSystem","STOSetup", function(this, ...){
	this$.createSystemDBObject()
	failIf(is.null(this$.qClassName),"Must set QClassName to upload System to DB.")
	return(this$.systemDB$insertSystemTable(	systemName=this$.system,
												qClassName=this$.qClassName))
})

method(".uploadSystemStrategy", "STOSetup", function(this, ...){
	this$.createSystemDBObject()
	return(this$.systemDB$insertSystemStrategiesTable(systemName=this$.system, strategyNames=this$.system))
})

method(".uploadSystemDetails", "STOSetup", function(this, ...){
	this$.createSystemDBObject()
	return(this$.systemDB$insertSystemDetailsTable(	systemName=this$.system,
													version=this$.version,
													interval=this$.interval,
													stoDir=this$.stoDirectory,
													stoID=this$.stoID))	
})

method(".uploadMSIVs", "STOSetup", function(this, ...){
	this$.createSystemDBObject()
	failIf(is.null(this$.markets),"No markets defined to upload to MSIV table.")
	return(this$.systemDB$insertMSIVTable(	markets=this$.markets,
											systemName=this$.system,
											interval=this$.interval, 
											version=this$.version))	
})

method(".uploadMSIVBacktest", "STOSetup", function(this, ...){
	this$.createSystemDBObject()		
	failIf(is.null(this$.markets),"No markets defined to upload to MSIV table.")
	msivs <- paste(this$markets(),this$.system,this$.interval,this$.version,sep="_")
	dates <- this$startEndDates()
	return(this$.systemDB$insertMSIVBacktestTable(	msivNames=msivs,
													stoID=this$.stoID,
													stoDir=this$.stoDirectory,
													startDate=as.character(dates$start),
													endDate=as.character(dates$end)))
})

method(".uploadPortfolioBacktest", "STOSetup", function(this, ...){
	this$.createSystemDBObject()
	failIf((length(this$portfolios())==0),"No portfolios defined to upload to PortfolioBacktest table.")
	
	portfolios <- this$portfolios()
	totalResult <- TRUE
	for (portfolio in names(portfolios)){
		markets <- portfolios[[portfolio]]
		msivs <- paste(markets,this$.system,this$.interval,this$.version,sep="_")
		if (this$portfolioWeights()=='SumToOne') weights <- rep(1/length(msivs),length(msivs))
		if (this$portfolioWeights()=='EachIsOne') weights <- rep(1,length(msivs))
		print(squish('Uploading Portfolio : ',portfolio,' : using weight method : ',this$portfolioWeights()))
		result <- this$.systemDB$insertPortfolioBacktest(msivs, weights=weights, 
															stoID=this$.stoID, stoDir=this$.stoDirectory, portfolioName=portfolio)
		print(result)
		if(result!=TRUE) totalResult <- result
	}
	return(totalResult)
})

#################################################################################################################
#	Load from SystemDB
#################################################################################################################

method("loadFromSystemDB", "STOSetup", function(this, verbose=FALSE, ...){
	if(verbose) print("Loading Strategy Class...")
	this$.loadStrategyClass()
	if(verbose) print("Loading QClass Name...")
	this$.loadSystemQClassName()
	if(verbose) print("Loading markets...")
	this$.loadMarkets()
	if(verbose) print("Loading dates...")
	this$.loadDates()
	if(verbose) print("Loading portfolios...")
	this$.loadPortfolios()
	if(verbose) print("Loading STO object & parameters...")
	this$.loadParameters()
})

method(".loadStrategyClass", "STOSetup", function(this, ...){
	this$strategyClass(SystemDB$strategyClass(strategy=this$system()))
})

method(".loadSystemQClassName", "STOSetup", function(this, ...){
	this$systemQClassName(SystemDB$systemQClassName(system=this$system()))
})

method(".loadMarkets", "STOSetup", function(this, ...){
	this$addMarkets(SystemDB$msivBacktestMarkets(stoID=this$stoID()))
})

method(".loadDates", "STOSetup", function(this, ...){
	df <- SystemDB$msivBacktest(stoID=this$stoID())
	dates <- as.POSIXct(df$startDate)
	names(dates) <- as.character(df$market)
	this$.setDates(dates,'start')
	dates <- as.POSIXct(df$endDate)
	names(dates) <- as.character(df$market)
	this$.setDates(dates,'end')		
})

method(".loadPortfolios", "STOSetup", function(this, ...){
	portfolioList <- SystemDB$backtestPortfolios(stoID=this$stoID())
	for (portfolio in names(portfolioList))
		this$addPortfolio(portfolio, markets=portfolioList[[portfolio]])
})

method(".loadParameters", "STOSetup", function(this, ...){
	tempSto <- STO(this$stoDirectory(),this$stoID(),calculateMetrics=FALSE)
	param.list <- tempSto$parameters()$definition()
	for (param in names(param.list)){
		this$addParameters(name=param, start=param.list[[param]][[1]], end=param.list[[param]][[2]], step=param.list[[param]][[3]])
	}
	this$.sto <- tempSto
})
