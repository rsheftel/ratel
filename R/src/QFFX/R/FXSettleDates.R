setConstructorS3("FXSettleDates", function(FXCurr=NULL,tradeDate=NULL,transaction=NULL,...)
{
    this <- extend(RObject(), "FXSettleDates",
        .FXCurr = NULL,
        .tradeDate = NULL,
        .transaction = NULL,
        .spotSettleDates = NULL,
        .settleDate = NULL,
        .expiryDate = NULL
        )
      
      constructorNeeds(this,FXCurr = "FXCurr")
      if(!inStaticConstructor(this))
      {
         this$.FXCurr <- FXCurr    
         transactionList <- FXTenor$getTenors()
         assert(any(transaction == transactionList),paste(transaction,"is not a valid transaction"))
         this$.transaction <- transaction
         this$.tradeDate <- as.POSIXct(tradeDate)
      }
      this
})

setMethodS3("getExpirySettleDate","FXSettleDates",function(this,FXCurr=NULL, tradeDate=NULL, transaction=NULL,holidayList=NULL,...)
{      
 	settleDates <- FXSettleDates(FXCurr,tradeDate,transaction)
## All spot transaction settle T+2, except usd/cad and usd/try which are T+1
    settleOffset <- 2
    if (settleDates$.FXCurr$over() == "usd" && ((settleDates$.FXCurr$under() == "cad") || 
			(settleDates$.FXCurr$under() == "try"))) settleOffset <- 1
    if (is.null(holidayList)) holidayList <- FXHolidayDates$getHolidayDates(settleDates$.FXCurr)
    unit = "d"
    NumUnits <- settleOffset
    settleDates$.expiryDate <- as.POSIXct(tradeDate)
    settleDates$.settleDate <- getFincadDateAdjust(settleDates$.expiryDate,unit,NumUnits,holidayList)
    settleDates$.expiryDate <- as.POSIXct(trunc(settleDates$.expiryDate,"day"))
	settleDates$.settleDate <- as.POSIXct(trunc(settleDates$.settleDate,"day"))     
    
	if (transaction=="spot") 
    {
        temp.list <- list(expiryDate = settleDates$.expiryDate,settleDate = settleDates$.settleDate)
        return(temp.list)
    }
    settleDates$.settleDate <- settleDates$computeSettleDate(settleDates$.settleDate,settleDates$.transaction,holidayList)
    unit = "d"
    NumUnits <- settleOffset
    settleDates$.expiryDate <- getFincadDateAdjust(settleDates$.settleDate,unit,-NumUnits,holidayList)
    temp.list <- list(expiryDate = settleDates$.expiryDate,settleDate = settleDates$.settleDate)
    return(temp.list)     
})

setMethodS3("computeSettleDate","FXSettleDates",function(this,startDate=NULL, transaction=NULL,holidayList=NULL,...)
{
  parse.list <- parseSimpleTenor(transaction)
  unit <- parse.list$unit
  NumUnits <- parse.list$numUnits
  settleDate <- getFincadDateAdjust(startDate,unit,NumUnits,holidayList)
  return(settleDate)
})
 