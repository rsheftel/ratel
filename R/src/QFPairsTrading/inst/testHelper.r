getTestData <- function(name){
	underlyingData <- read.csv(system.file("testdata",name, package = "QFPairsTrading"), sep = ",", header = TRUE)
	zoo(underlyingData[,-1],as.POSIXct(underlyingData[,1]))
}

checkZoos <- function(z1,z2){
	z1 <- zoo(as.numeric(z1),as.POSIXct(as.character(index(z1))))
	z2 <- zoo(as.numeric(z2),as.POSIXct(as.character(index(z2))))
	checkSame2(z1,z2)
}