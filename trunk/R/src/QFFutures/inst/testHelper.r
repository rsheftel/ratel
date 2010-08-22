getTestZoo <- function(name){
	test.data <- data.frame(read.csv(system.file("testdata/ModifiedFuturesBuilder",name, package = "QFFutures"), sep = ",", header = TRUE))
	test.data <- zoo(test.data[,-1],as.POSIXct(test.data[,1]))    
	getZooDataFrame(test.data)
}

getRawDataZoo <- function(name){
	rawData <- data.frame(read.csv(system.file("testdata/ModifiedFuturesBuilder",name, package = "QFFutures"), sep = ",", header = TRUE))
	getZooDataFrame(zoo(rawData[,-1],rawData[,1]),colnames(rawData)[-1])
}