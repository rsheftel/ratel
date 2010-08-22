# ExposureReport Class
# 
# Author: rsheftel
###############################################################################

constructor("ExposureReport", function(exposure=NULL, filename=NULL){
	this <- extend(RObject(), "ExposureReport", .exposure = exposure, .filename=filename)
	constructorNeeds(this, exposure="Exposure", filename="character?")
	if (inStaticConstructor(this)) return(this)
	if(is.null(filename)) filename <- squish(tempDirectory(),'Exposure_',format(Sys.time(),'%Y%m%d_%H%M%S'))
	this$.hw <- HWriterUtils(filename=filename, pdf=TRUE)
	this$.conn <- this$.hw$connection()
	return(this)
})

method('filenames', 'ExposureReport', function(this, ...){
	return(this$.hw$filenames())	
})

method('closeConnection', 'ExposureReport', function(this, ...){
	this$.hw$closeConnection()
})

method('openReport', 'ExposureReport', function(this, ...){
	this$.hw$openReport()
})

method('addTitle', 'ExposureReport', function(this, ...){
	hwrite("Exposure Report", this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)	
	hwrite(this$.exposure$groupName(), this$.conn, heading=2, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

method('addMetricFrame', 'ExposureReport', function(this, aggregationLevels=NULL, weights=NULL, ...){
	mf <- this$.exposure$metricFrame(aggregationLevels, weights, ...)
	hwrite("Metric Frame", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(squish("Aggregation Levels: ",paste(aggregationLevels,collapse=', ')), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	if(is.null(weights)) weights <- 'NULL'
	hwrite(squish("Weights: ",weights), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	hwrite(as.character(this$.exposure$range()), this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(mf, rowNames=FALSE, colNames=TRUE), this$.conn, br=FALSE)	
})

method('addCorrelations', 'ExposureReport', function(this, aggregationLevels=NULL, weights=NULL, ...){
	cg <- this$.exposure$correlations(aggregationLevels, weights, ...)
	hwrite("Correlations - 5d Change", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	hwrite(squish("Aggregation Levels: ",paste(aggregationLevels,collapse=', ')), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	if(is.null(weights)) weights <- 'NULL'
	hwrite(squish("Weights: ",weights), this$.conn, heading=3, center=FALSE, style='font-family: sans-serif')
	hwrite(as.character(this$.exposure$range()), this$.conn, heading=4, center=FALSE, style='font-family: sans-serif')
	hwrite(HWriterUtils$dataTable(cg, rowNames=TRUE, colNames=TRUE), this$.conn, br=FALSE)	
})
