stoDirectory <- function() {
    gsub("\\\\", "/", normalizePath(system.file("testdata", package = "STO")))
}

dataDir <- function(dir.name = "Curves") {
    system.file("testdata", dir.name, package = "STO")
}

realDataCube <- function() {
    CurveCube(dataDir())
}

simpleCube <- function() {
    CurveCube(dataDir("SimpleCurves"))
}

raggedDatesCube <- function() {
    CurveCube(dataDir("SimpleCurvesRaggedDates"))
}

siv <- SIV("NDayBreak", "daily", "1")
msivs <- siv$m(c("@TY", "@FV", "@US"))
msivTY <- msivs[[1]]
msivFV <- msivs[[2]]
msivUS <- msivs[[3]]
simpleSiv <- SIV("ABC", "daily", "1")
simpleMsivs <- simpleSiv$m(c("mkt1", "mkt2", "mkt3"))
combined <- Portfolio("combined", simpleMsivs)
raggedMsivs <- simpleSiv$m(c("mkt1", "mkt2", "mkt3", "mkt4"))

destroyWFO <- function() {
	WFO$destroy(squish(stoDirectory(),"/","wfoSTO","/","WFO"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTO","/","Portfolios"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTO","/","MSIVs.csv"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTO","/","CurvesBin","/","CVE_10_Daily_WFOPort1"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTOOneMSIV","/","WFO"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTOOneMSIV","/","Portfolios"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTOOneMSIV","/","MSIVs.csv"))
	WFO$destroy(squish(stoDirectory(),"/","wfoSTOOneMSIV","/","CurvesBin","/","CVE_10_Daily_WFOPort1"))
}

createWFO <- function(startDate = NULL,endDate = NULL) {
	sto <- STO(stoDirectory(),"wfoSTO")
	WFO(name = "WFO",sto = sto,msivs = sto$msivs()[1:2],startDate = startDate,endDate = endDate)
}

createWFOOneMSIV <- function(startDate = NULL,endDate = NULL) {
	sto <- STO(stoDirectory(),"wfoSTOOneMSIV")
	WFO(name = "WFO",sto = sto,msivs = sto$msivs()[1],startDate = startDate,endDate = endDate)
}

Metric$init()