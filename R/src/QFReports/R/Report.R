# TODO: Add comment
# 
# Author: jbourgeois
###############################################################################

constructor("Report", function(name = NULL,filename=NULL){
	this <- extend(RObject(), "Report", .filename = filename,.name = name)
	constructorNeeds(this, name = 'character',filename="character")
	if (inStaticConstructor(this)) return(this)
	this$.makeFilenames(filename)
	this$.openConnections()
	return(this)
})

method("addTitle", "Report", function(this,title,...){
	hwrite(title, this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

method("addPlotLink", "Report", function(this,title,...){
	hwrite(title, this$.conn, link=squish('file:///',makeWindowsFilename(this$.filenames$pdf)))
})

method("addGroupTags", "Report", function(this,pnlAnalysis,...){
	hwrite("Group Tags", this$.conn, heading=2, center=FALSE, style='font-family: sans-serif')
	for (group in pnlAnalysis$groupNames()){		
		tags <- pnlAnalysis$groupTags(group)
		tags <- data.frame(t(tags))
		rownames(tags) <- group
		hwrite(HWriterUtils$dataTable(tags, rowNames=TRUE, colNames=FALSE), this$.conn, br=FALSE)
	}		
})

method(".makeFilenames", "Report", function(this,...){
	this$.filenames <- list()
	if(is.null(this$.filename)) this$.filename <- squish(tempDirectory(),this$.name,format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0))
	this$.filenames$html <- this$.filename
	if(rightStr(this$.filenames$html,5)!='.html') this$.filenames$html <- squish(this$.filename,'.html')
	this$.filenames$pdf <- squish(leftStr(this$.filenames$html,nchar(this$.filenames$html)-5),'.pdf')
})

method(".openConnections", "Report", function(this, ...){
	this$.conn <- openPage(filename=this$.filenames$html, title=this$.filename)
	file.remove(this$.filenames$pdf)
	pdf(this$.filenames$pdf)
})

method("closeConnection", "Report", function(this, ...){
	closePage(this$.conn)
	dev.off()
	this$.conn <- NULL	
})

method("openReport", "Report", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$.filenames$html)
})

method("filenames", "Report",function(this, ...){
	return(this$.filenames)		
})

method("email", "Report",function(this,subject,runDate,...){
	email <- Mail$notification(subject=squish(subject,format(runDate,'%Y-%m-%d')),
			content=squish('Report - available in ', makeWindowsFilename(this$filenames()$html)))
	email$attachFile(this$filenames()$html)
	email$sendTo('team')	
})