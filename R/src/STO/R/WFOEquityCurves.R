ec.untilNextOptimization <- function(wfo){
	ranges <- NULL
	endDates <- wfo$schedule()[,"endDate"]
	for(i in 1:wfo$nbSteps()){
		ranges[[i]] <- list()
		if(i < wfo$nbSteps()){
			ranges[[i]] <- Range(endDates[i],endDates[i+1])	
		}else{
			ranges[[i]] <- Range(endDates[i],wfo$endDate())	
		}
	}
	ranges
}

ec.optiSteps <- function(wfo,optiSteps){
	ranges <- NULL
	endDates <- wfo$schedule()[,"endDate"]
	for(i in 1:wfo$nbSteps()){
		ranges[[i]] <- list()
		if(i %in% optiSteps){
			if(i < last(optiSteps)){
				ranges[[i]] <- Range(endDates[i],endDates[optiSteps[match(i,optiSteps)+1]])
			}else{
				ranges[[i]] <- Range(endDates[i],wfo$endDate())
			}
		}else{
			ranges[[i]] <- "Pass"
		}
	}	
	ranges
}