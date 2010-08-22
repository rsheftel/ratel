library("STO")

source(system.file("testHelper.r", package = "STO"))

testWFOPlotsConstructor <- function() {
	wfo <- createWFO()
	destroyWFO()
}
