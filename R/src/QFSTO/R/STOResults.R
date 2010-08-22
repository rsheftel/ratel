# TODO: Add comment
# 
# Author: rsheftel
###############################################################################


constructor("STOResults", function(systemID.in=NULL, systemID.out=NULL){
	this <- extend(RObject(), "STOResults")
	constructorNeeds(this, systemID.in="numeric|integer", systemID.out="numeric|integer?")
	if (inStaticConstructor(this)) return(this)
	this$.systemID <- list()
	this$.systemID[['in']]  <- systemID.in
	this$.systemID[['out']] <- systemID.out
	this$.loadStoSetup()
	this$.loadSystemDetails()
	this$runDate()
	return(this)
})

method('.loadStoSetup', 'STOResults', function(this, ...){
	this$.stoSetup <- list()
	this$.stoSetup[['in']] <- STOSetup(systemID=this$.systemID[['in']])
	this$.stoSetup[['in']]$loadFromSystemDB(verbose=TRUE)
	this$.stoSetup[['out']] <- NULL
	if(!is.null(this$.systemID[['out']])){
		this$.stoSetup[['out']] <- STOSetup(systemID=this$.systemID[['out']])
		cat('\nLoading stoSetup for out sample ID...\n')
		this$.stoSetup[['out']]$loadFromSystemDB(verbose=TRUE)
	}		
})

method(".loadSystemDetails", 'STOResults', function(this, ...){
	this$.details <- list()
	this$.details[['in']] <- SystemDB$systemDetails(this$.systemID[['in']])
	this$.stoID <- list()
	this$.stoID[['in']] <- this$.details[['in']]$stoID
	this$.stoID[['out']] <- 'NA'
	if (!is.null(this$.systemID[['out']])){
		this$.details[['out']] <- SystemDB$systemDetails(this$.systemID[['out']])
		this$.stoID[['out']] <- this$.details[['out']]$stoID
	}
	this$.runDate <- list()
})

method('stoSetupObject', 'STOResults', function(this, inOut='in', ...){
	return(this$.stoSetup[[inOut]])	
})

method("runDate", "STOResults", function(this, runDate.in='', runDate.out='', ...){
	needs(runDate.in="character", runDate.out='character')
	if((runDate.in=='') && (runDate.out=='')) return(this$.runDate)
	this$.runDate[['in']] <- runDate.in
	this$.runDate[['out']] <- runDate.out
})

method("acceptedPortfolios", "STOResults", function(this, portfolios=NULL, ...){
	needs(portfolios="character?")
	if(is.null(portfolios)) return(this$.portfolios)
	failIf(!all(portfolios %in% this$.stoSetup[['in']]$portfolioNames()),"Portfolio names not valid.")	
	
	makeMarkets <- function(inOut){
		goodBadAll <- list()	
		goodBadAll$all <- unique(unlist(this$.stoSetup[[inOut]]$portfolios()))
		goodBadAll$good <- unique(unlist(lapply(this$.portfolios, function(x) this$.stoSetup[[inOut]]$portfolios()[[x]])))
		goodBadAll$bad <- goodBadAll$all[-match(goodBadAll$good,goodBadAll$all)]
		return(goodBadAll)
	}

	this$.portfolios <- portfolios
	this$.markets <- list()
	this$.markets[['in']] <- makeMarkets('in')
	if(!is.null(this$.stoSetup[['out']])) this$.markets[['out']] <- makeMarkets('out')
})

method('acceptedMarkets', 'STOResults', function(this, ...){
	return(this$.markets)	
})

method("uploadToSystemDB", "STOResults", function(this, commitToDB=FALSE, ...){
	failIf(is.null(this$.runDate[['in']]), 'No run date set, cannot upload!')
	failIf(is.null(this$.markets), "Nothing setup yet.")
	stoMan <- SystemDBManager()
	stoMan$commitToDB(commitToDB)
	res <- list()
	
	backtestTableUpload <- function(inOut, goodBad){
		needs(inOut='character', goodBad='character')
		if(goodBad=='good') acceptReject <- TRUE
		if(goodBad=='bad') acceptReject <- FALSE 
		return(stoMan$updateMSIVBacktestTableResults(	markets=	this$.markets[[inOut]][[goodBad]], 
														systemName=	this$.stoSetup[[inOut]]$system(),
														interval=	this$.stoSetup[[inOut]]$interval(),
														version=	this$.stoSetup[[inOut]]$version(), 
														runDate=	this$.runDate[[inOut]],
														stoID=		this$.stoSetup[[inOut]]$stoID(),
														validationAccept= acceptReject))
	}
	
	for (inOut in c('in','out')){
		if (!is.null(this$.stoSetup[[inOut]])){
			print(squish('Uploading for in/out sample: ',toupper(inOut)))
			print('Updating portfolios that did NOT pass...')
			res[[inOut]]$good <- backtestTableUpload(inOut, 'good')
			print('Updating portfolios that did pass...')
			res[[inOut]]$bad <- backtestTableUpload(inOut, 'bad') 
		}
	}
	
	#Upload the in/out sample stoIDs
	print('Updating stoIDs for MSIVs...')
	res$msivStoIDs <- stoMan$updateMSIVTable(	markets=	unique(c(this$.markets[['in']]$all,this$.markets[['out']]$all)),
												systemName=	this$.stoSetup[['in']]$system(),
												interval=	this$.stoSetup[['in']]$interval(),
												version=	this$.stoSetup[['in']]$version(),
												documentation='',
												inSampleSTOid=	this$.stoID[['in']],
												outSampleSTOid=	this$.stoID[['out']])
	return(res)
})
