# Diversity scoring class
# 
# Author: rsheftel
###############################################################################


constructor("Diversity", function(groupName=NULL){
	this <- extend(RObject(), "Diversity", .groupName=groupName)
	constructorNeeds(this, groupName="character")
	if (inStaticConstructor(this)) return(this)
	this$.curveGroup <- CurveGroup(this$.groupName) 
	this$.populateMsivpvs()
	return(this)
})

method('as.character','Diversity',function(this, ...){
	return(squish('Diversity: ',this$.groupName))	
})

method('as.data.frame','Diversity', function(this, ...){
	return(data.frame(Diversity=squish('Diversity: ',this$.groupName)))
})

method("childNames", "Diversity", function(this, ...){
	return(this$.curveGroup$childNames())	
})

method("loadMarkets", "Diversity", function(this, ...){
	this$.fullMarketZoo <- NULL
	markets <- this$.curveGroup$markets()
	for (market in markets){
		print(squish("loading market: ",market))
		this$.fullMarketZoo[[market]] <- Symbol(market)$series()[,'close']
	}
})

method(".populateChildGroupMarkets", "Diversity", function(this, ...){
	this$.child <- NULL
	for (child in this$childNames()){
		if(this$.isGroup(child)){
			childMarkets <- CurveGroup(child)$markets()
			if (length(childMarkets) > 1){
				this$.child[[child]] <- do.call(merge, sapply(childMarkets, function(x) this$.marketZoo[[x]], simplify = FALSE))
			}else{
				this$.child[[child]] <- getZooDataFrame(this$.marketZoo[[childMarkets]],childMarkets)
			}
		}
		else{
			childName <- this$.marketFromMsivpv(child) 
			this$.child[[child]] <- getZooDataFrame(this$.marketZoo[[childName]],childName)
		}
	}		
})

method("childMarketsZoo", "Diversity", function(this, childName, ...){
	needs(childName="character")
	return(this$.child[[childName]])	
})

method(".populateMsivpvs", "Diversity", function(this, ...){
	msivs <- this$.curveGroup$msivs()
	pvs <- this$.curveGroup$pvs()
	this$.msivpvs <- paste(msivs,pvs,sep=":")
	this$.markets <- this$.curveGroup$markets()
})

method(".isGroup", "Diversity", function(this, groupName, ...){
	needs(groupName='character')
	return(!any(groupName == this$.msivpvs))
})

method(".marketFromMsivpv", "Diversity", function(this, msivpv, ...){
	needs(msivpv='character')
	return(this$.markets[[match(msivpv, this$.msivpvs)]])	
})

method("score", "Diversity", function(this, methodName, range=NULL, ...){
	needs(methodName='character', range='Range?')
	assert((methodName %in% this$availableMethods()),squish('Not a valid method: ',methodName))
	if(is.null(this$.fullMarketZoo)) this$loadMarkets()
	this$.marketZoo <- this$.fullMarketZoo
	if(!is.null(range)) this$.marketZoo <- lapply(this$.fullMarketZoo, function(x) range$cut(x))
	this$.populateChildGroupMarkets()
	return(do.call(squish("calc",methodName), list(this, ...)))
})

###################################################################################################
#	Calculation methods. Each returns a named vector of scores. Each starts with "calc"
###################################################################################################

method('availableMethods', 'Diversity', function(this, ...){
	return(c('TotalCorrelationLevel','TotalCorrelationReturns','PrincipalComponents','Fixed'))	
})

method("calcTotalCorrelationLevel", "Diversity", function(this, ...){
	scores <- NULL
	for (child in this$childNames()){
		scores <- c(scores,this$groupTotalCorrelationLevel(child)) 
	}
	names(scores) <- this$childNames()
	return(scores)			
})

method("groupTotalCorrelationLevel", "Diversity", function(this, groupName, ...){
	needs(groupName="character")
	groupZoo <- this$.child[[groupName]]
	if (NCOL(groupZoo)==1) return(1)
	correlationMatrix <- abs(cor(groupZoo, use="pairwise.complete.obs", method="pearson"))
	correlationMatrix[is.na(correlationMatrix)] <- 1
	totCorrelations <- sum(correlationMatrix) - NCOL(correlationMatrix)
	markets <- NCOL(correlationMatrix) + 0.5* (1-sqrt(1 + 4 * totCorrelations))
	return(markets)		
})

method("calcTotalCorrelationReturns", "Diversity", function(this, lag = 1, ...){
	scores <- NULL
	for (child in this$childNames()){
		scores <- c(scores,this$groupTotalCorrelationReturns(child)) 
	}
	names(scores) <- this$childNames()
	return(scores)			
})

method("groupTotalCorrelationReturns", "Diversity", function(this, groupName, lag = 1, ...){
	needs(groupName="character", lag = 'numeric')
	groupZoo <- this$.child[[groupName]]
	groupZoo <- diff(groupZoo,lag=lag)
	if (NCOL(groupZoo)==1) return(1)
	correlationMatrix <- abs(cor(groupZoo, use="pairwise.complete.obs", method="pearson"))
	correlationMatrix[is.na(correlationMatrix)] <- 1
	totCorrelations <- sum(correlationMatrix) - NCOL(correlationMatrix)
	markets <- NCOL(correlationMatrix) + 0.5* (1-sqrt(1 + 4 * totCorrelations))
	return(markets)		
})

method('calcPrincipalComponents', 'Diversity', function(this, threshold = 0.9, interp = TRUE, changes = FALSE, lag = 1, ...){
	scores <- NULL
	for (child in this$childNames()){
		scores <- c(scores, this$groupPrincipalComponents(child, threshold, interp, changes, lag))
	}		
	names(scores) <- this$childNames()
	return(scores)
})

method('groupPrincipalComponents', 'Diversity', function(this, groupName, threshold = 0.9, interp = TRUE, changes = FALSE, lag = 1, ...){
	needs(threshold = 'numeric', groupName = 'character', interp = 'logical', changes = 'logical')
	groupZoo <- this$.child[[groupName]]
	if(NCOL(groupZoo) == 1) return(1)
	
	groupZoo <- na.locf(groupZoo)	
	# remove columns that are all NA's
	groupZoo <- groupZoo[,which(sd(groupZoo) != 0)]
	if(changes)  groupZoo <- diff(groupZoo, lag = lag)
		
	importanceValues <- summary(prcomp(groupZoo, scale = TRUE))$importance['Cumulative Proportion',]	
	upperBound <- match(TRUE, importanceValues > threshold)
	if(upperBound == 1) return(1)
	if(interp == FALSE) return(upperBound)
	
	qf.interpolate(threshold, c(importanceValues[upperBound - 1], importanceValues[upperBound]), c(upperBound - 1, upperBound))	
})

method('calcFixed', 'Diversity', function(this, weights=NULL, ...){
	needs(weights='integer|numeric|list')
	if(is.list(weights)) weights <- unlist(weights)
	failIf(!all(names(weights) %in% this$childNames()), 'Names in weight vector not valid.')
	failIf(!all(this$childNames() %in% names(weights)), 'Must provide a weight for each child.')
	return(weights)
})
