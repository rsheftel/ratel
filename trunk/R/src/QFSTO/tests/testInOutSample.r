# InOutSample class test
# 
# Author: rsheftel
###############################################################################

library(QFSTO)
systemID.in  <- 179030
systemID.out <- 196963
testdata <- squish(system.file("testdata", package="QFSTO"),'/InOutSample/')

test.constructor <- function(){
	checkInherits(InOutSample(123,456),"InOutSample")
	shouldBomb(InOutSample())
	shouldBomb(InOutSample('123','456'))
}

test.loadRunGroups <- function(){
	ios <- InOutSample(systemID.in=systemID.in, systemID.out=systemID.out)
	ios$loadRunGroups('in')
	checkInherits(ios$runGroups('in'),'RunGroups')
}

test.individualReports <- function(){
	ios <- InOutSample(systemID.in=systemID.in, systemID.out=systemID.out)
	ios$loadRunGroups('in')
	ios$generateReports()	
}

test.runAll <- function(){
	checkInherits(InOutSample(systemID.in=systemID.in, systemID.out=systemID.out, source='in'),"InOutSample")
	#InOutSample(systemID.in=systemID.in, systemID.out=systemID.out, source='in')$generateReports()
}
