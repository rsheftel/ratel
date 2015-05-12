
library(xts)
# Mid Pricing with Correlated Assets

diffusionWeight <- function(maturity){
# Linear interpolation from 1 at target to 0 at 25% distance
	
	targetNeighborDistance <- abs(as.numeric(maturity - target$maturity))
	distancePercent <- 1 - (targetNeighborDistance / as.numeric(target$maturity - todayDate))
	return(approx(c(0.75,1),c(0,1),distancePercent,rule=2)$y)
}

sizeWeights <- function(tradeSizes){
#weight the sizing of either a quote or trade	
	
	minSize <- 5
	maxSize <- 50    
	minWeight <- 1
	maxWeight <- 2   #this says that a size of the max is 4x the weight of the min
	return(approx(c(minSize,maxSize),c(minWeight,maxWeight),tradeSizes,rule=2)$y / maxWeight)
}

timeWeights <- function(times, now){
#exponentially weighted time weight
	
	#in real version use business days, not calendar days
	halfLife <- 0.5 #mins
	lambda <- 0.5^(1/halfLife)
	
	timeDifference.mins <- as.numeric(difftime(now,times,units='mins'))
	weights <- c()
	for (x in timeDifference.mins){
		weights <- c(weights, max(lambda^x, 0.00000000001))
	}
	return(weights)
}

filterWeights <- function(securities, actions){
	actualTrades <- (securities=='actual') & (actions=='trade')
	lastActualTrade <- length(actualTrades) - match(TRUE, rev(actualTrades)) + 1 
	filterWeights <- rep(0,length(actualTrades))
	filterWeights[lastActualTrade:length(actualTrades)] <- 1
	return(filterWeights)
}

finalWeights <- function(events, now){
	weightMatrix <- cbind(	sizeWeights(events$tradeSize),
							timeWeights(events$time, now),
							events$diffusionWeight,
							filterWeights(events$security, events$action))
	finalWeights <- c()
	for (x in 1:nrow(weightMatrix)){
		finalWeights <- c(finalWeights,prod(weightMatrix[x,]))
	}
	return(finalWeights/sum(finalWeights))
}		

level <- function(events,now){
	weights <- finalWeights(events, now)
	return(sum(weights * events$level)/sum(weights))
}

showEvents <- function(events, now){
	return(cbind(events, data.frame(sizeWeight=sizeWeights(events$tradeSize),
									timeWeight=timeWeights(events$time, now),
									filterWeight=filterWeights(events$security, events$action),
									finalWeight=finalWeights(events,now)	
			)))
}

showResults <- function(events, now){
	print(now)
	print(level(events, now))
	print(showEvents(events, now))
}

onEvent <- function(events, event){
	#stamp the original time on the record, used for quote decay
	event$origTime <- event$time
	
	if (event$action == 'quote'){
		priorLevel <- level(events, event$time)
		if (priorLevel >= event$ask){
			event$level <- event$ask
		} else if(priorLevel <= event$bid){
			event$level <- event$bid
		} else {
			event$level <- priorLevel
		}
	}
	
	if(is.null(events)){ 
		events <- as.data.frame(event)
	}else{
		events <- rbind(events, as.data.frame(event))
	}
	
	now <- events$time[[length(events$time)]]
	events <- onTime(events, now)
	return(events)
}

quoteDecayToMid <- function(events, now){
	
	halfLife <- 1 #mins
	lambda <- 0.5^(1/halfLife)
	
	#filter for quotes only and do analysis
	#this is bad brute force, but for demo purpose only, a more elegant solution would be trivial
	
	for (x in 1:nrow(events)){
		if (events[x,]$action == 'quote'){
			timeDifference <- as.numeric(difftime(events[x,]$time,events[x,]$origTime,units='mins'))
			decay <- max(lambda^timeDifference, 0.00000000001)
			midLevel <- (events[x,]$bid + events[x,]$ask)/2
			events[x,]$level <- decay*(events[x,]$level) + (1-decay)*(midLevel)
		}
	}
	return(events)
}

onTime <- function(events, now){
	events <- quoteDecayToMid(events, now)
	
	#Keep the last quote refreshed, first the latest on the actual
	filters <- filterWeights(events$security, events$action)
	goodRows <- (filters==1) & (events$security=='actual') & (events$action=='quote')
	
	if(TRUE %in% goodRows){	#there is a live 
		quoteRow <- max(which(goodRows, TRUE))
		events[quoteRow,]$time <- now
	}
	return(events)
}

makeTrace <- function(trace, trace.detail, startTime, endTime, events){
	times <- (index(trace)[index(trace)>=startTime & index(trace)<endTime])
	for (x in 1:length(times)){
		time <- times[[x]]
		events <- onTime(events, time)
		window(trace,time) <- level(events,time)
		trace.detail[[as.character(time)]] <- events
	}
	return(list(trace=trace, trace.detail=trace.detail, events=events))
}

#set up the target information
todayDate <- as.POSIXct('2011-03-01')
target <- list()
target$maturity <- as.POSIXct('2020-11-15')
events <- NULL

#Set up the trace vector
trace.times <- seq(as.POSIXct('2011-03-01 9:00:00'),length=240,by='1 secs')
trace <- zoo(NA, trace.times)
trace.detail <- vector('list', length(trace.times))
names(trace.detail) <- trace.times

#First populate with last night close.... as an 'actual' 'trade'
event <- list()
event$security <- 'actual'
event$action   <- 'trade'
event$bid		<- NA
event$ask		<- NA
event$level		<- 5.5
event$tradeSize <- 50	#pick a size for LNC that makes sense
event$time		<- as.POSIXct('2011-02-01 15:00:00')
event$diffusionWeight <- diffusionWeight(target$maturity)

events <- onEvent(events, event)
now <- as.POSIXct('2011-03-01 9:00:00')
showResults(events,now)

startTime <- as.POSIXct('2011-03-01 9:00:00')
endTime <- as.POSIXct('2011-03-01 9:00:10')

res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events

# Event
event <- list()
event$security <- 'actual'
event$action   <- 'trade'
event$bid		<- NA
event$ask		<- NA
event$level		<- 5.6
event$tradeSize <- 5
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity)

events <- onEvent(events, event)
now <- endTime
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:00:30')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events


#Next event, a trade in a neighbor
event<-list()
event$security <- 'implied'
event$action   <- 'trade'
event$level		<- 6.4
event$bid		<- NA
event$ask		<- NA
event$tradeSize <- 25
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity+31536000)

events <- onEvent(events, event)
now <- endTime
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:01:00')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events


#A quote on this actual security with prior level outside the quote
event<-list()
event$security <- 'actual'
event$action   <- 'quote'
event$level		<- NA
event$bid		<- 7
event$ask		<- 8
event$tradeSize <- 5
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity)

events <- onEvent(events, event)
now <- endTime
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:01:30')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events

#A quote on this actual security with prior level inside the quote
event<-list()
event$security <- 'actual'
event$action   <- 'quote'
event$level		<- NA
event$bid		<- 6.5
event$ask		<- 7.5
event$tradeSize <- 5
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity)

events <- onEvent(events, event)
now <- endTime
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:02:00')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events

#Time progresses, see how it effects the quotes
now <- as.POSIXct('2011-03-01 9:03:30')
showResults(onTime(events, now),now)

#A trade on this actual security
event<-list()
event$security <- 'actual'
event$action   <- 'trade'
event$level		<- 7.5
event$bid		<- NA
event$ask		<- NA
event$tradeSize <- 50
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity)

events <- onEvent(events, event)
now <- endTime
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:02:15')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events

#A quote on neighbor security
event<-list()
event$security <- 'implied'
event$action   <- 'quote'
event$level		<- NA
event$bid		<- 5
event$ask		<- 6
event$tradeSize <- 5
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity+31536000*2)

events <- onEvent(events, event)
now <- endTime
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:02:30')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events

#But then a quote on this security confirms
event<-list()
event$security <- 'actual'
event$action   <- 'quote'
event$level		<- NA
event$bid		<- 5
event$ask		<- 6
event$tradeSize <- 5
event$time		<- endTime
event$diffusionWeight <- diffusionWeight(target$maturity)

events <- onEvent(events, event)
now <- as.POSIXct('2011-03-01 9:05:00')
showResults(events,now)

startTime <- endTime
endTime <- as.POSIXct('2011-03-01 9:04:01')
res <- makeTrace(trace, trace.detail, startTime, endTime, events)
trace <- res$trace
trace.detail <- res$trace.detail
events <- res$events

#Time progresses, see how it effects the quotes
now <- as.POSIXct('2011-03-01 9:05:30')
showResults(onTime(events, now),now)

#Time progresses, see how it effects the quotes
now <- as.POSIXct('2011-03-01 9:06:00')
showResults(onTime(events, now),now)

trace <- as.xts(trace)

edge case:  one sided quote
			a quote in another if the "implied" process changes, then the implied quote needs to update
			
			how to take into account if it was 'bid hit' or 'offer lifted' and the 'follow'
		
			grid of possibilities:
					bid hit, offer on follow
					bid hit, bid returns
					etc..
					