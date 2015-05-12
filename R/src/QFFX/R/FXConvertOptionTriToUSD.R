setConstructorS3("FXConvertOptionTriToUSD", function(...)
{
        this <- extend(RObject(), "FXConvertOptionTriToUSD")
        this
})

setMethodS3("convertLocalCcyTri","FXConvertOptionTriToUSD",function(this,fxCurr,tenor,putCall,rebalPeriod,rebalDelta,...)
{    
    CurrInst <- FXConvertOptionTriToUSD()
	ccy <- paste(fxCurr$over(),fxCurr$under(),sep="")
	tri_type <- paste(rebalPeriod,(rebalDelta*100),"d",sep="")
	tri_tag <- "tri_local_ccy"
	local_ccy_series_name <- paste(ccy,tenor,putCall,tri_tag,tri_type,sep="_")
	
	tri_tag <- "tri"
	target_series_name <- paste(ccy,tenor,putCall,tri_tag,tri_type,sep="_")
	
	result <- FXConvertTriToUSD$convertTriToUSD(localTRI=local_ccy_series_name,targetTRI=target_series_name)
	
})