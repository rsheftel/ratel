# Simple Math Functions
# 
# Author: rsheftel, imcdonald
###############################################################################

roundToNearest <- function(x, n) {
	needs(x = "numeric",n = "numeric")
    round(x / n) * n
}

lowerPartialMoment <- function(x, moment, threshold=0){
	needs(x = 'vector|numeric|integer?', moment = 'numeric', threshold='numeric')
	
	mean(pmax(threshold - x,0)^moment)
}

semiDeviation <- function(x, moment=2, threshold=0, direction='downside'){
	needs(x='vector|numeric|integer?', threshold='numeric', direction='character')
	
	failIf(!any(direction == c('downside','upside')),'Direction must be either "downside" or "upside".')
	
	switch (direction,
			downside = x.subset <- x[x<threshold],
			upside = x.subset <- x[x>threshold])
			
	return((mean((x.subset - threshold)^moment))^(1/moment))
}

