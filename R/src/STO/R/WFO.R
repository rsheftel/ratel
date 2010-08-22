constructor("WFO", function(name = NULL,msivs = NULL,sto = NULL,startDate = NULL,endDate = NULL) {
	options(scipen = 20)
	this <- extend(RObject(),"WFO",.sto = sto,.name = name,.msivs  = msivs)
	constructorNeeds(this,name = "character", sto = "STO",msivs = "list")
	if(inStaticConstructor(this)) return(this)
	if(!is.null(startDate) & !is.null(endDate)) this$.range <- Range(startDate,endDate)
	else this$.range <- this$getStoRange()
	this$.optimizedPnlCurvesLookup <- Map("character", "list")
	this$.parameterizedMsivsLookup <- Map("character", "list")
	assert(this$.range$.start <= this$.range$.end,"WFO: startDate > endDate!")
	this$setup()
	this$checkEmptyMetrics()	
	this
})

method("msivs", "WFO", function(this,...)this$.msivs)

method("runs", "WFO", function(this,...)lazy(this$.runNumbers,sort(this$.sto$runNumbers()),log = FALSE))

method("curvesFast", "WFO", function(this,...) lazy(this$.stoCurves, this$.sto$curves(...), log=FALSE))

method("curvesSlow", "WFO", function(this,...) this$.sto$curves(...))

method("startDate", "WFO", function(this,...)this$.range$.start)

method("range", "WFO", function(this,...)this$.range)

method("endDate", "WFO", function(this,...)this$.range$.end)

method("metricsFast", "WFO", function(this,...)lazy(this$.stoMetrics, this$.sto$metrics(...), log=FALSE))

method("metricsSlow", "WFO",  function(this,...)this$.sto$metrics(...))

method("msivsCharacter", "WFO", function(this,...) lazy(this$.msivsCharacter,sapply(this$msivs(),function(x)as.character(x)), log=FALSE))

method("ranges", "WFO", function(this,...) {
	schedule <- this$schedule()
	lapply(1:NROW(schedule),function(x){Range(start = schedule[x,"startDate"],end = schedule[x,"endDate"])})	
})

method("getStoRange", "WFO", function(this,...){
	startDates <- NULL
	endDates <- NULL
	for (msiv in this$msivs()){
		firstCurve <- pnl(this$curvesFast()$curve(msiv,1))
		index(firstCurve) <- as.character(as.Date(index(firstCurve)))
		startDates <- c(startDates,first(index(firstCurve)))
		endDates <- c(endDates,last(index(firstCurve)))
	}
	Range(min(startDates),max(endDates))
})

method("checkEmptyMetrics", "WFO", function(this,...){
	metricFolder <- squish(this$.sto$.dir,"/","Metrics")
	if(any(sapply(this$msivs(),function(x){x$hasMetricFile(metricFolder)})))
		throw("Metric folder should be empty to perform WFO!")
})

method("setup", "WFO", function(this,...) {
	this$.dir  <- squish(this$.sto$.dir,"/",this$.name)
	dir.create(this$.dir,FALSE)
	this$.schedulePath  <- squish(this$.dir,"/Schedule.csv")
	this$.portfolioWeightsPath <- squish(this$.dir,"/PortfolioWeights.csv")
	this$.portfolioNamesPath <- squish(this$.dir,"/PortfolioNames.csv")
	this$.optimalRunsPath <- squish(this$.dir,"/OptimalRuns")
	this$.curvesPath <- squish(this$.dir,"/StitchedCurves")
	dir.create(this$.curvesPath,FALSE)
	dir.create(this$.optimalRunsPath,FALSE)
})

method("writeCsv", "WFO", function(this,data,pathCsv,...){
	needs(pathCsv = "character")
	write.table(data,pathCsv,sep = ",",row.names = FALSE)
})

method("writeStitchedCurve", "WFO", function(this,zoo,curveName,...){
	needs(curveName = "character",zoo = "zoo")
	write.table(getZooDataFrame(zoo),squish(this$.curvesPath,"/",curveName,".csv"),sep = ",",row.names = TRUE,col.names = FALSE)
	zoo
})

method("getOptimizedPnlCurves", "WFO", function(this,optimalRunsMethod,...){
	needs(optimalRunsMethod = "character")
	if(!this$.optimizedPnlCurvesLookup$has(optimalRunsMethod))
		this$calcParameterizedMsivs(optimalRunsMethod)
	this$.optimizedPnlCurvesLookup$fetch(optimalRunsMethod)
})

method("readStitchedCurve", "WFO", function(this,curveName,...){
	needs(curveName = "character")
	table <- read.table(squish(this$.curvesPath,"/",curveName,".csv"),sep = ",")
	getZooDataFrame(zoo(table[,2],table[,1]))
})

method("readCsv", "WFO", function(this,pathCsv,...){
	needs(pathCsv = "character")
	read.table(pathCsv,sep = ",",header = TRUE,stringsAsFactors = FALSE)
})

method("updateCsv", "WFO", function(this,data,pathCsv,...){
	needs(pathCsv = "character")
	newData <- this$readCsv(pathCsv)
	for(i in 1:NROW(data))
		for(j in 1:NCOL(data))
			if(!is.na(data[i,j]))newData[i,j] <- data[i,j]
	this$writeCsv(newData,pathCsv)
	newData
})


method("nbSteps", "WFO", function(this,...) NROW(this$schedule()) )

method("steps", "WFO", function(this,...) as.numeric(this$schedule()[,"step"]) )

method("createSchedule", "WFO", function(this,firstDate,freqDays,rollingWindowDays = NULL,...){
	print("Creating Schedule")
	needs(firstDate = "POSIX|character",freqDays = "numeric",rollingWindowDays = "numeric?")
	assert(firstDate < this$endDate()); assert(firstDate > this$startDate())
	startDate <- this$startDate(); endDate <- as.POSIXct(firstDate)	
	schedule <- data.frame(step = 1,startDate = startDate,endDate = endDate)
	if(!is.null(rollingWindowDays))
		if(as.Date(schedule[,"endDate"]) - as.Date(schedule[,"startDate"]) < rollingWindowDays)
			warning(squish("The first optimization window is lower than: ",rollingWindowDays," days!"))
	continue <- TRUE
	while(continue){
		endDate <- Period$days(freqDays)$advance(as.POSIXct(endDate))
		if(is.null(rollingWindowDays)) startDate <- startDate
		else{
			startDate <- Period$days(rollingWindowDays)$rewind(as.POSIXct(endDate))
			assert(startDate > this$startDate())
		}
		if(endDate > this$endDate()) continue <- FALSE
		else{
			schedule <- rbind(
				schedule,
				data.frame(step = last(schedule[,"step"] +1),startDate = startDate,endDate =  endDate)
			)
		}
	}
	this$writeCsv(schedule,this$.schedulePath)
	schedule
})

method("schedule", "WFO", function(this,...){
	failIf(!file.exists(this$.schedulePath),"No Schedule In Memory! Use WFO$createSchedule(...) First!")
	this$readCsv(this$.schedulePath)
})

method("setUpWFOForMSIV", "WFO", function(this,msiv,...){
	needs(msiv = 'MSIV')
	this$calcPortfolioWeights(p.EqualWeighting)
	this$writeCsv(
		data.frame(step = this$steps(),
		portfolioNames = rep(msiv$.market,this$nbSteps()),stringsAsFactors = FALSE),this$.portfolioNamesPath
	)
})

method("calcPortfolioWeights", "WFO", function(this,method,...){
	print("Calculating Portfolio Weights")
	needs(method = "function")
	if(!file.exists(this$.portfolioWeightsPath)){
		weightsData <- data.frame(matrix(nrow = this$nbSteps(),ncol = 1 + length(this$msivs())))
		colnames(weightsData) <- c("step",this$msivsCharacter())
		weightsData[,"step"] <- this$steps()
		this$writeCsv(weightsData,this$.portfolioWeightsPath)
	}
	portfolioWeights <- method(this,...)
	this$updateCsv(portfolioWeights,this$.portfolioWeightsPath)
})

method("portfolioWeights", "WFO", function(this,...){
	failIf(!file.exists(this$.portfolioWeightsPath),"No Portfolio Weights In Memory! Use WFO$calcPortfolioWeights(...) First!")
	this$readCsv(this$.portfolioWeightsPath)
})

method("addPortfolios", "WFO", function(this,...){
	print("Adding WFO Portfolios")
	portfolioWeights <- this$portfolioWeights()
	portfolioNames <- NULL
	portfolioNum <- 0
	for(step in this$steps()){
		newPortfolio <- TRUE
		if(step > 1){
			alreadyAdded <- sapply(1:(step-1),function(x){all(portfolioWeights[x,-1] == portfolioWeights[step,-1])})
			if(any(alreadyAdded))newPortfolio <- FALSE
		}
		if(newPortfolio){
			portfolioNum <- portfolioNum + 1
			portfolioNames <- c(portfolioNames,squish(this$.name,"Port",portfolioNum))
			msivsFiltered <- portfolioWeights[step,-1] != 0
			port <- Portfolio(
				last(portfolioNames),
				msivs = this$msivs()[na.omit(match(colnames(portfolioWeights)[-1],this$msivsCharacter()))],
				weights = as.numeric(portfolioWeights[step,-1][msivsFiltered])
			)
			this$.sto$add(port)
		}else{
			portfolioNames <- c(portfolioNames,unique(portfolioNames[alreadyAdded]))
		}
	}
	portfolioNames <- data.frame(step = this$steps(),portfolioNames = portfolioNames,stringsAsFactors = FALSE)
	this$writeCsv(portfolioNames,this$.portfolioNamesPath)
	portfolioNames
})

method("portfolioNames", "WFO", function(this,...){
	failIf(!file.exists(this$.portfolioNamesPath),"No Portfolio Names In Memory! Use WFO$addPortfolios(...) First!")
	this$readCsv(this$.portfolioNamesPath)
})

method("calcOptimalRuns", "WFO", function(this,optimalRunsMethod,optimalRunsFunction,steps = this$steps(),...){
	print("Calculating Optimal Runs")
	needs(optimalRunsMethod = "character",optimalRunsFunction = "function",steps = "numeric|integer")
	optimalRunsCsvPath <- squish(this$.optimalRunsPath,"/",optimalRunsMethod,".csv")
	if(!file.exists(optimalRunsCsvPath))
		this$writeCsv(data.frame(step = this$steps(),optimalRuns = ""),optimalRunsCsvPath)
	optiRuns <- NULL
	for (opti in steps){
		optiRunsTable <- this$readCsv(optimalRunsCsvPath)
		if(is.na(optiRunsTable[opti,"optimalRuns"])){
			print(squish("Opti ",opti,"/",this$nbSteps()))
			optiRuns <- optimalRunsFunction(this,this$ranges()[[opti]],this$.sto$msiv(this$portfolioNames()[opti,"portfolioNames"]),...)
			optiRunsTable[opti,"optimalRuns"] <- paste(unlist(optiRuns,recursive = FALSE),collapse = ",")
			this$updateCsv(optiRunsTable,optimalRunsCsvPath)
		}
	}
	this$optimalRuns(optimalRunsMethod)
})

method("optimalRuns", "WFO", function(this,optimalRunsMethod,...){
	needs(optimalRunsMethod = "character")
	this$readCsv(squish(this$.optimalRunsPath,"/",optimalRunsMethod,".csv"))
})

method("summary", "WFO", function(this,optimalRunsMethod,...){
	needs(optimalRunsMethod = "character")
	allData <- data.frame(
		this$schedule(),
		portfolioNames = this$portfolioNames()[,"portfolioNames"],
		optimalRuns = this$optimalRuns(optimalRunsMethod)[,-1],
		this$portfolioWeights()[,-1],
		stringsAsFactors = FALSE
	)
	colnames(allData) <- c('step','startDate','endDate','portfolioNames','optimalRuns',colnames(this$portfolioWeights())[-1])
	this$writeCsv(allData,squish(this$.dir,"/summary.csv"))
	allData
})

method("calcParameterizedMsivs", "WFO", function(this,optimalRunsMethod,calcOptimizedPnlCurves = TRUE,...){
	needs(optimalRunsMethod = "character",calcOptimizedPnlCurves = "logical")
	summary <- this$summary(optimalRunsMethod)
	parameterizedMsivsList <- NULL
	for(i in this$steps()){
		parameterizedMsivsList[[i]] <- list()
		runs <- as.numeric(the(strsplit(as.character(summary[i,"optimalRuns"]),",")))
		msivs <- vector("list",NROW(runs))
		for(k in 1:NROW(runs))msivs[[k]] <- this$.sto$msiv(summary[i,"portfolioNames"])
		parameterizedMsivsList[[i]] <- ParameterizedMsivs(
			msivs = msivs,
			weights = rep(1/NROW(runs),NROW(runs)),
			runs = runs
		)
	}
	this$.parameterizedMsivsLookup$set(optimalRunsMethod,parameterizedMsivsList)
	if(calcOptimizedPnlCurves)this$calcOptimizedPnlCurves(optimalRunsMethod,parameterizedMsivsList)
})

method("calcOptimizedPnlCurves", "WFO", function(this,optimalRunsMethod,parameterizedMsivsList,...){
	needs(optimalRunsMethod = "character",parameterizedMsivsList = "list")
	optimizedPnlCurves <- NULL
	ranges <- this$ranges(); curves <- this$curvesSlow()
	for(i in this$steps()){
		optimizedPnlCurves[[i]] <- list()
		optimizedPnlCurves[[i]] <- parameterizedMsivsList[[i]]$curve(curves)$pnl()
	}
	this$.optimizedPnlCurvesLookup$set(optimalRunsMethod,optimizedPnlCurves)
})

method("oneOptimizedEquityCurve", "WFO", function(this,step,optimalRunsMethod,rangeCut = FALSE,...){
	needs(step = "numeric|integer",optimalRunsMethod = "character",rangeCut = "logical")
	optimizedPnlCurves <- this$getOptimizedPnlCurves(optimalRunsMethod)
	if(rangeCut)cumsum(this$ranges()[[step]]$cut(optimizedPnlCurves[[step]]))
	else cumsum(optimizedPnlCurves[[step]])
})

method("equityCurve", "WFO", function(this,curveName,optimalRunsMethod,equityCurveFunction = ec.untilNextOptimization,...){
	needs(curveName = "character",optimalRunsMethod= "character",equityCurveFunction = "function")
	if(file.exists(squish(this$.curvesPath,"/",curveName,".csv")))return(this$readStitchedCurve(curveName))
	this$writeStitchedCurve(this$stitchEquityCurve(optimalRunsMethod,equityCurveFunction),curveName)
})

method("stitchEquityCurve", "WFO", function(this,optimalRunsMethod,equityCurveFunction,...){
	needs(optimalRunsMethod = "character",equityCurveFunction = "function")
	ranges <- equityCurveFunction(this,...)
	optimizedPnlCurves <- this$getOptimizedPnlCurves(optimalRunsMethod)
	ranges <- ranges[as.character(ranges) != "Pass"]
	pnlCurves <- NULL
	for(i in 1:NROW(ranges)) pnlCurves[[i]] <- ranges[[i]]$cut(optimizedPnlCurves[[i]])
	if(NROW(pnlCurves) == 1) cumsum(pnlCurves[[1]])
	else cumsum(stitchZoos(pnlCurves,FALSE,"usePrior")$stitchedZoo)		
})

method("destroy", "WFO", function(class, dir, ...) {
	needs(dir = "character")
	unlink(recursive=TRUE, dir)
})