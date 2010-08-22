# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

constructor("HWriterUtils", function(filename = NULL, pdf = TRUE){
	this <- extend(RObject(), "HWriterUtils", .filename = filename, .pdf=pdf)
	constructorNeeds(this, filename ='character?', pdf='logical')
	if(inStaticConstructor(this)) return(this)
	this$.makeFileNames()
	this$.conn <- this$.openConnections()
	return(this)	
})

##############################################################################

method("dataTable", "HWriterUtils", function(static, data, rowNames = TRUE, colNames = TRUE, numDigits = 4, formatNumbers = TRUE, ...){
	needs(data = 'matrix|data.frame|character', rowNames = 'logical', colNames = 'logical', formatNumbers = 'logical')

	if(formatNumbers) data <- static$formatNumbers(data, numDigits)
	return(hwrite(data,
			style		= 'font-family: monospace; border: 1px dotted #999',
			row.names 	= rowNames,
			col.names	= colNames,
			row.style	= list('font-weight:bold; border: 1px dotted #999'),
			col.style	= list('font-weight:bold; border: 1px dotted #999'),
			cellpadding = 3,
			row.bgcolor	= list('lightgreen')
	))		
})

method('listToColumn', 'HWriterUtils', function(static, listData, ...){
	needs(listData = 'list')
	for(element in names(listData)) if(is.null(listData[[element]]))
			listData[[element]] <- 'NULL'
	return(t(data.frame(listData)))	
})

method('formatNumbers', 'HWriterUtils', function(static, x, numDigits = 4, ...){
	return(format(x, scientific = FALSE, big.mark=',',digits = numDigits))	
})

method('formatDollars', 'HWriterUtils', function(static, data, ...){
	return(format(data, nsmall = 0, scientific = FALSE, digits = 1, justify = 'right', big.mark = ','))	
})

method("closeConnection", "HWriterUtils", function(this, ...){
	closePage(this$.conn)
	if(this$.pdf) dev.off()
	this$.conn <- NULL	
})

method("openReport", "HWriterUtils", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$filenames()$html)
})

method("filenames", "HWriterUtils",function(this, ...){
	return(this$.filenames)		
})

method('connection', 'HWriterUtils', function(this, ...){
	if(is.null(this$.conn)) this$.conn <- this$.openConnections()
	return(this$.conn)		
})

method('addLine', 'HWriterUtils', function(this, numLines = 1, ...){
	for(i in 1:numLines) hwrite('', this$connection(), br = TRUE)			
})

############################################################
method(".makeFileNames", "HWriterUtils", function(this, ...){
	this$.filenames <- list()
	if(is.null(this$.filename)) this$.filename <- squish(tempDirectory(),'HWriter',format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0))
	this$.filenames$html <- this$.filename
	if(rightStr(this$.filenames$html,5)!='.html') this$.filenames$html <- squish(this$.filename,'.html')
	this$.filenames$pdf <- squish(leftStr(this$.filenames$html,nchar(this$.filenames$html)-5),'.pdf')
})

method(".openConnections", "HWriterUtils", function(this, ...){
	file.remove(this$.filenames$pdf)
	if(this$.pdf) pdf(this$.filenames$pdf)
	return(openPage(filename=this$.filenames$html, title=this$.filename))
})

hWriterFileMatches <- function(testFilename, benchFilename, deleteFile=FALSE, startLine=10, endLineOffset=2){
	testData <- read.csv(testFilename)
	benchData <- read.csv(benchFilename)
	if (deleteFile) file.remove(testFilename)
	endLine <- NROW(testData)- endLineOffset
	return(checkSame(as.character(benchData[startLine:endLine,]),as.character(testData[startLine:endLine,])))
}

