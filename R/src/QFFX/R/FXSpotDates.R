setConstructorS3("FXSpotDates", function(FXCurr=NULL,tradeDates=NULL,holidayList=NULL,...)
{
    this <- extend(RObject(), "FXSpotDates",
        .FXCurr = NULL,
        .tradeDates = tradeDates,
        .holidayList = holidayList
        )
      
      constructorNeeds(this,FXCurr = "FXCurr")
      if(!inStaticConstructor(this))
      {
         this$.FXCurr <- FXCurr
         this$.tradeDates <- as.POSIXct(this$.tradeDates)    
      }
      this
})

setMethodS3("getSpotSettleDates","FXSpotDates",function(this,FXCurr=NULL, tradeDates=NULL, holidayList=NULL,...)
{      
  
  temp.result <- vector(length=length(tradeDates))
  
  FXSpot <- FXSpotDates(FXCurr,tradeDates,holidayList)
  for (i in 1:length(FXSpot$.tradeDates)) {
       temp.result[i] <- as.character((FXSettleDates$getExpirySettleDate(FXCurr,FXSpot$.tradeDates[i],"spot",this$.holidayList))$settleDate)
   }
   result <- zoo(temp.result,order.by=as.POSIXct(tradeDates))
   return(result)
}) 
  


