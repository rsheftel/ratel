# CommonMarketFactorsReport Class
#
# Creates an html and pdf report packet from the CommonMarketFactors
# 
# Author: RSheftel
###############################################################################

constructor("CommonMarketFactorsReport", function(outputDirectory = NULL, filenamePrefix=NULL, analysisZoo=NULL){
	this <- extend(RObject(), "CommonMarketFactorsReport", .analysisZoo=analysisZoo, .filenamePrefix=filenamePrefix)
	if (inStaticConstructor(this)) return(this)
	constructorNeeds(this, outputDirectory = "character", filenamePrefix="character", analysisZoo="zoo")
	
	failIf(is.null(colnames(analysisZoo)), "analysisZoo must have column names.")
	this$.filenames$prefix <- squish(outputDirectory, filenamePrefix)
	this$makeFilenames()
	this$.openConnections()
	this$.cmf <- CommonMarketFactors(addStandardFactors = FALSE)
	return(this)
})

method ("makeFilenames", "CommonMarketFactorsReport", function(this, ...){
	this$.filenames$html <- squish(this$.filenames$prefix,'.html')	
})

method ("filename", "CommonMarketFactorsReport", function(this, ...){
	return(this$.filenames$html)	
})

method("commonMarketFactorsObject", "CommonMarketFactorsReport", function(this, ...){
	return(this$.cmf)	
})

method("openReport", "CommonMarketFactorsReport", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$.filenames$html)		
})

method(".openConnections", "CommonMarketFactorsReport", function(this, ...){
	this$.conn <- openPage(filename=this$.filenames$html, title=this$.filename)
})

method("closeConnection", "CommonMarketFactorsReport", function(this, ...){
	closePage(this$.conn)
	this$.conn <- NULL	
})

method("generateReport", "CommonMarketFactorsReport", function(this, ...){
	failIf(is.null(this$.cmf$factors()),"Must load factors before generating report.")
	hwrite(squish("Common Market Factors - ",this$.filenamePrefix), this$.conn, heading=1, center=FALSE, style='font-family: sans-serif')
	for (analysisName in colnames(this$.analysisZoo)){
		analysisVariable <- getZooDataFrame(this$.analysisZoo[,analysisName],analysisName)
		this$.cmf$analysisVariable(analysisVariable, analysisName)
		this$.cmf$stripTimes()
		pdfFilename <- squish(this$.filenames$prefix,'_',analysisName,'.pdf')
		data <- this$.cmf$generateReport(pdfFilename)
		hwrite(analysisName, this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
		hwrite(squish("Graphs for ",analysisName), this$.conn, link=squish('file:///',makeWindowsFilename(pdfFilename)))
		hwrite(HWriterUtils$dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn)
	}
})
