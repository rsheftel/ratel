setConstructorS3("FXAddTriOffset", function(triName=NULL,tsdb=NULL,writeToTSDB=FALSE,...)
{
        this <- extend(RObject(), "FXAddTriOffset",
        .triName = triName,
		.tri = NULL,
		.tsdb = tsdb,
		.writeToTSDB = writeToTSDB
        )
        constructorNeeds(this,tsdb = "TimeSeriesDB")
        if(!inStaticConstructor(this))
        	{
          	this$.triName <- triName
	  }
   this
})

setMethodS3("makeTriPositive","FXAddTriOffset",function(this,...)
{
	if (is.null(this$.tri)) this$loadTri()
	if (this$isTriPositive()) return()
	offsetQuantum <- 100
	minimumTri <- min(coredata(this$.tri))
	offset <- offsetQuantum + offsetQuantum * abs(trunc(minimumTri/offsetQuantum))
	this$addConstantToTri(offset=offset)
	failIf (!this$isTriPositive())
	if (this$.writeToTSDB){
		this$writeTriToDatabase()
		return()
	}
	else return(this$.tri)
})

setMethodS3("loadTri","FXAddTriOffset",function(this,...)
{
	this$.tri <- this$.tsdb$retrieveOneTimeSeriesByName(name=this$.triName, data.source="internal")
})

setMethodS3("addConstantToTri","FXAddTriOffset",function(this,offset=NULL,...)
{
	needs(offset="numeric")
	this$.tri <- this$.tri + offset
})

setMethodS3("isTriPositive","FXAddTriOffset",function(this,...)
{
	if (length(which((this$.tri)<0))>0) return(FALSE)
	else return(TRUE)	
})
setMethodS3("writeTriToDatabase","FXAddTriOffset",function(this,...)
{
		tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(this$.triName, "internal"))
		tsMatrix[[1,1]] <- this$.tri
		fx.data <- this$.tsdb$writeTimeSeries(tsMatrix)
})