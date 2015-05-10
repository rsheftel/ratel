#Retreive the pararmater space from a sto

#Create the parameter space dataframe
params <- sto$parameters()
paramFrame <- params$data()

paramCount <- length(paramFrame[1,])

#Populate the parameter names
paramSpaceNames <- colnames(paramFrame[2:paramCount])

#Initialize start, end and step
paramSpaceStartValues <- vector('numeric',0)
paramSpaceEndValues <- vector('numeric',0)
paramSpaceStepValues <- vector('numeric',0)
paramSpaceNumRuns <- vector('numeric',0)

for (i in 2:paramCount){
    paramValues <- sort(unique(paramFrame[,i]))
    paramSpaceStartValues <- c(paramSpaceStartValues,min(paramValues))
    paramSpaceEndValues <- c(paramSpaceEndValues,max(paramValues))
    #Step size
    if (length(paramValues) >= 2){
        paramSpaceStepValues <- c(paramSpaceStepValues, paramValues[2] - paramValues[1])
    }else{
        paramSpaceStepValues <- c(paramSpaceStepValues,0)
    }
    paramSpaceNumRuns <- c(paramSpaceNumRuns,length(paramValues))
}

#merge into an array
parameterSpace <- t(rbind(paramSpaceNames,paramSpaceStartValues, paramSpaceEndValues, paramSpaceStepValues,paramSpaceNumRuns))