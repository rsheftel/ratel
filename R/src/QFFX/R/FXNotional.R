setConstructorS3("FXNotional", function(notional=NULL, notionalFlag=NULL,...)
{
	#	over is the over currency name
	#	under is the under currency name
	#	1 = 1mm of notional
	
	this <- extend(RObject(), "FXNotional",
		.notional = 100,
        .notionalFlag="over"
      )
     if(!inStaticConstructor(this))
     {
     	if (!is.null(notional)){
        	assert(notional>0)
        	this$.notional <-notional
        }
        if (!is.null(notionalFlag)) {
        	assert(any(notionalFlag==c("over","under")))
          	this$.notionalFlag <- notionalFlag}
        }
      
      this
})

setMethodS3("getNotional","FXNotional",function(this,...)
{
  return(this$.notional)
})

setMethodS3("getNotionalFlag","FXNotional",function(this,...)
{
  return(this$.notionalFlag)
})


