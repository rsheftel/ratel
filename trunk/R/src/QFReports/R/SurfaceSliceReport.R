# SurfaceSliceReport Class
###############################################################################

constructor("SurfaceSliceReport", function(	htmlDirectory=NULL, filename=NULL, sto=NULL, imagesDirectory=NULL,
											msiv=NULL, metric=NULL, axes=NULL, aggFunction=mean, metricCube=NULL, filter=NULL){
	
	this <- extend(RObject(), "SurfaceSliceReport", .filename=filename, .sto=sto, .msiv=msiv, .metric=metric, .axes=axes,
													.aggFunction=aggFunction, .metricCube=metricCube, .filter=filter)
												
	constructorNeeds(this, htmlDirectory="character?", filename="character?", sto="STO",imagesDirectory="character?",
							msiv="MSIV", metric="Metric", axes="character", aggFunction="function", metricCube="MetricCube?", filter="Filter?")
	if (inStaticConstructor(this)) return(this)
	this$.dirs$html <- htmlDirectory
	this$.dirs$images <- imagesDirectory
	this$.metricCube <- lazy(this$.metricCube, this$.sto$metrics(this$.msiv), log=FALSE)
	if(is.null(this$.filter)) this$.filter <- RunFilter$with("ALL", this$.sto$parameters()$runs())
	this$.makeFilenames()
	this$.openConnections()
	return(this)
})

#############################################################################################################
#			General Methods
#############################################################################################################
method("addTitle", "SurfaceSliceReport", function(this, ...){
	hwrite(squish("Surface Slice - ",as.character(this$.msiv)), this$.conn, heading=1, center=TRUE, style='font-family: sans-serif', br=TRUE)
})

#############################################################################################################
#			Plot Methods
#############################################################################################################
method("addContourPlot", "SurfaceSliceReport", function(this, zRange=NULL, ...){
	this$.surface <- lazy(this$.surface, this$.sto$surface(	this$.msiv, this$.axes[1], this$.axes[2], this$.metric, this$.aggFunction,
															metric.cube = this$.metricCube, filter=this$.filter), log=FALSE)
	if(isWindows()){png(this$filenames()$contour, width=700, height=700)
		}else{bitmap(this$filenames()$contour, res=150)}
	this$.surface$plotContour(as.character(this$.metric),zRange=zRange)
	dev.off()
	hwriteImage(image.url=this$imageLinkText(this$filenames()$contour), this$.conn)
})

method("addMultiLinePlot", "SurfaceSliceReport", function(this, ...){
	this$.surface <- lazy(this$.surface, this$.sto$surface(	this$.msiv, this$.axes[1], this$.axes[2], this$.metric, this$.aggFunction,
			metric.cube = this$.metricCube, filter=this$.filter), log=FALSE)
	if(isWindows()){png(this$filenames()$multiLine, width=700, height=700)
		}else{bitmap(this$filenames()$multiLine, res=150)}
	this$.surface$plotMultiLine(viewAlong=this$.axes[1])
	dev.off()
	hwrite(as.character(this$.metric), this$.conn, heading=2, center=TRUE, style='font-family: sans-serif', br=TRUE)
	hwriteImage(image.url=this$imageLinkText(this$filenames()$multiLine), this$.conn)
})

#############################################################################################################
#			Grid Methods
#############################################################################################################
method("addDataGrid", "SurfaceSliceReport", function(this, ...){
	this$.surface <- lazy(this$.surface, this$.sto$surface(	this$.msiv, this$.axes[1], this$.axes[2], this$.metric, this$.aggFunction,
			metric.cube = this$.metricCube, filter=this$.filter), log=FALSE)
	data <- t(rev(as.data.frame(this$.surface)))
	data <- cbind(data,rowMeans(data))
	data <- rbind(data,colMeans(data))
	rownames(data)[NROW(data)] <- 'MEAN'
	colnames(data)[NCOL(data)] <- 'MEAN'
	hwrite(squish(as.character(this$.msiv),' : ',as.character(this$.metric)), this$.conn, heading=2, center=TRUE, style='font-family: sans-serif', br=TRUE)
	hwrite(squish('X= ',this$.axes[1],' : Y= ',this$.axes[2]), this$.conn, heading=2, center=TRUE, style='font-family: sans-serif', br=TRUE)
	hwrite(HWriterUtils$dataTable(data, rowNames=TRUE, colNames=TRUE), this$.conn, br=TRUE)
})

#############################################################################################################
#			Support Functions
#############################################################################################################

method(".makeFilenames", "SurfaceSliceReport", function(this, ...){
	this$.filenames <- list()
	if(is.null(this$.dirs$html)) this$.dirs$html <- tempDirectory()
	if(is.null(this$.dirs$images)) this$.dirs$images <- this$.dirs$html
	if(rightStr(this$.dirs$html,1)!='/') this$.dirs$html <- squish(this$.dirs$html,'/')
	if(rightStr(this$.dirs$images,1)!='/') this$.dirs$images <- squish(this$.dirs$images,'/')
	
	if(is.null(this$.filename)) this$.filename <- squish('SurfaceSliceReport',format(Sys.time(),"%Y%m%d_%H%M"),round(abs(rnorm(1)*100000),0))
	if(rightStr(this$.filename,5)=='.html') this$.filename <- leftStr(this$.filename,nchar(this$.filename)-5)
	this$.filenames$html <- squish(this$.dirs$html, this$.filename, '.html')
	this$.filenames$contour <- squish(this$.dirs$images, this$.filename, '_contour.png')
	this$.filenames$multiLine <- squish(this$.dirs$images, this$.filename, '_multiLine.png')
})

method(".openConnections", "SurfaceSliceReport", function(this, ...){
	this$.conn <- openPage(filename=this$.filenames$html, title=this$.filename)
	#file.remove(this$.filenames$pdf)
	#pdf(this$.filenames$pdf)
})

method("closeConnection", "SurfaceSliceReport", function(this, ...){
	closePage(this$.conn)
	try(dev.off(),silent=TRUE)
	this$.conn <- NULL	
})

method("openReport", "SurfaceSliceReport", function(this, ...){
	if(!is.null(this$.conn)) this$closeConnection()
	browseURL(this$filenames()$html)
})

method("filenames", "SurfaceSliceReport",function(this, ...){
	return(this$.filenames)		
})

method("imageLinkText", "SurfaceSliceReport", function(this, fullFilename, ...){
	filename <- basename(fullFilename)
	imagePathLast <- last(strsplit(this$.dirs$images,"/")[[1]])
	htmlPathLast  <- last(strsplit(this$.dirs$html,"/")[[1]])
	if(imagePathLast != htmlPathLast) filename <- squish(imagePathLast,'/',filename)
	return(filename)			
})
