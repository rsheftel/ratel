setConstructorS3("FXConvertSpotTriToUSD", function(fxCurr=NULL,...)
{
        this <- extend(RObject(), "FXConvertSpotTriToUSD", .fxCurr = fxCurr)
        if(!inStaticConstructor(this))
        {
          this$.fxCurr <- fxCurr
          this$.tsdb <- TimeSeriesDB()
        }
        this
})

setMethodS3("convertLocalCcyTri","FXConvertSpotTriToUSD",function(this,fxCurr,...)
{    
    CurrInst <- FXConvertSpotTriToUSD(fxCurr)
    local_ccy_series_name <- squish(fxCurr$over(),fxCurr$under(),"_spot_tri_local_ccy")
	target_series_name <- squish(fxCurr$over(),fxCurr$under(),"_spot_tri")
	result <- FXConvertTriToUSD$convertTriToUSD(localTRI = local_ccy_series_name, targetTRI = target_series_name)
	return(result)
})

  