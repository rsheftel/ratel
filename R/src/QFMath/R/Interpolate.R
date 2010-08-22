# Interpolate Class
# 
# Author: rsheftel
###############################################################################


qf.interpolate <- function(searchValue, searchVector, resultVector){
# Returns the interpolated value from the resultVector that
#  is interpolated based on the searchValue in the searchVector
# Both vectors must be sorted with values from min to max
# The extrapolation is done on a flat line constant from the edge value
# NA are handled by removing those columns from the search and result
	
	#remove all columns with NA in search or result vector
	goodCols <- !is.na(searchVector) & !is.na(resultVector)
	searchVector <- searchVector[goodCols]
	resultVector <- resultVector[goodCols]
	
	#Find the high and low index
	indexLow <- ifelse(searchValue <= searchVector[1],
		1,
		max(which(searchVector <= searchValue)))
	indexHigh <- ifelse(searchValue >= searchVector[length(searchVector)],
		length(searchVector),
		min(which(searchVector >= searchValue)))
	
	if(!is.na(indexLow) && !is.na(indexHigh)){
		if(indexLow==indexHigh)
			return(resultVector[indexLow])
		else {
			return(resultVector[indexLow] + (resultVector[indexHigh] - resultVector[indexLow]) *
					((searchValue - searchVector[indexLow])/(searchVector[indexHigh] - searchVector[indexLow])))
		}}
	else
		return(NA)
}

qf.interpolateVector <- function(searchValues, searchVector, resultVector){
	sapply(searchValues, function(x) qf.interpolate(x,searchVector, resultVector))
}

qf.interpolateXY <- function(searchValueY, searchValueX, searchVectorY, searchVectorX, resultMatrix){
#Returns the interpolated value the the resultMatrix that
# is interpolated on the X and Y dimensions
# The Y vector are the rows and the X is the columns, in R standard it is [Y,X] for the matrix element
	
	indexLowX <- ifelse(searchValueX <= searchVectorX[1],
		1,
		max(which(searchVectorX <= searchValueX)))
	indexHighX <- ifelse(searchValueX >= searchVectorX[length(searchVectorX)],
		length(searchVectorX),
		min(which(searchVectorX >= searchValueX)))
	
	indexLowY <- ifelse(searchValueY <= searchVectorY[1],
		1,
		max(which(searchVectorY <= searchValueY)))
	indexHighY <- ifelse(searchValueY >= searchVectorY[length(searchVectorY)],
		length(searchVectorY),
		min(which(searchVectorY >= searchValueY)))
	
	weightHighX <- ifelse(indexLowX == indexHighX, 1,
		(searchValueX - searchVectorX[indexLowX]) / (searchVectorX[indexHighX] - searchVectorX[indexLowX]))
	weightLowX <- (1 - weightHighX)
	
	weightHighY <- ifelse(indexLowY == indexHighY, 1,
		(searchValueY - searchVectorY[indexLowY]) / (searchVectorY[indexHighY] - searchVectorY[indexLowY]))
	weightLowY <- (1 - weightHighY)
	
	result <- (weightHighX * weightHighY) * resultMatrix[indexHighY, indexHighX]
	result <- ((weightHighX * weightLowY) * resultMatrix[indexLowY, indexHighX]) + result
	result <- ((weightLowX * weightHighY) * resultMatrix[indexHighY, indexLowX]) + result
	result <- ((weightLowX * weightLowY)  * resultMatrix[indexLowY, indexLowX]) + result
	
	return(result)
}
