setConstructorS3("IRSSlopePayoutRatio", function(currency=NULL,forwardStart=NULL,longTenor=NULL,shortTenor=NULL,startDate=NULL,endDate=NULL,tsdb=NULL,volLookBack=NULL,...)
{
        this <- extend(RObject(), "IRSSlopePayoutRatio",
        .currency = currency,
        .forwardStart = forwardStart,
        .longTenor = longTenor,
        .shortTenor = shortTenor,
        .startDate = startDate,
        .endDate = endDate,
        .tsdb = tsdb,
        .fwdDataLoad = NULL,
        .spotDataLoad = NULL,
        .currentDate = NULL,
        .volLookBack = volLookBack,
        .volScalingFactor = NULL,
        .payoutRatio = NULL        
        )
        
        constructorNeeds(this,tsdb = "TimeSeriesDB",volLookBack = "numeric")
        
        if(!inStaticConstructor(this))
        {
          this$.startDate <- as.POSIXct(startDate)
          this$.endDate <- as.POSIXct(endDate)
          
          assert(startDate <= endDate,paste ("Start date must be before endDate"))
#currently only USD is supported          
          assert(any(currency==c("usd")))
          assert(any(forwardStart==c("1m","2m","3m","6m","1y","2y","3y")))
          
          this$.volScalingFactor <- sqrt(as.numeric(this$getNumberOfDaysInForwardStart()))
          
          NumUnits <- -1.0*as.numeric(this$.volLookBack)
          unit <- "d"
          backDate <- as.POSIXct(getFincadDateAdjust(startDate=startDate,unit=unit,NumUnits=NumUnits,adjRule=1, holidayList=NULL))           
                    
          this$.fwdDataLoad <- this$loadIRSSlopeData(currency = currency,forwardStart=forwardStart,tenorList=c(longTenor,shortTenor),startDate=backDate,
            endDate=endDate,tsdb=tsdb)
          this$.spotDataLoad <- this$loadIRSSlopeData(currency = currency,forwardStart="spot",tenorList=c(longTenor,shortTenor),startDate=backDate,
            endDate=endDate,tsdb=tsdb)
          
          this$.fwdDataLoad <- filter.zoo.byIntersection(this$.fwdDataLoad,this$.spotDataLoad)
          this$.spotDataLoad <- filter.zoo.byIntersection(this$.spotDataLoad,this$.fwdDataLoad)
          
        }
         
         this
})

setMethodS3("getPayoutRatio","IRSSlopePayoutRatio",function(this,...)
{
  return(this$.payoutRatio)
})

setMethodS3("setCurrentDate","IRSSlopePayoutRatio",function(this,newCurrentDate=NULL,...)
{
  if (!is.null(newCurrentDate))
  { 
    this$.currentDate <- as.POSIXct(newCurrentDate)
  }  
  return(this$.currentDate)
})

setMethodS3("getCurrentDate","IRSSlopePayoutRatio",function(this,...)
{
  return(this$.currentDate)
})

setMethodS3("getFwdDataLoad","IRSSlopePayoutRatio",function(this,...)
{
  return(this$.fwdDataLoad)
})

setMethodS3("getSpotDataLoad","IRSSlopePayoutRatio",function(this,...)
{
  return(this$.spotDataLoad)
})

setMethodS3("computePayoutRatio","IRSSlopePayoutRatio",function(this,...)
{
    for (i in (this$.volLookBack+1):length(index(this$getSpotDataLoad()))){  
      this$setCurrentDate(index(this$getSpotDataLoad()[i,]))      
      
      longTenorFwdRate <- as.numeric(this$getFwdDataLoad()[this$getCurrentDate(),this$.longTenor][[1]])
      shortTenorFwdRate <- as.numeric(this$getFwdDataLoad()[this$getCurrentDate(),this$.shortTenor][[1]])
      longTenorSpotRate <-  as.numeric(this$getSpotDataLoad()[this$getCurrentDate(),this$.longTenor][[1]])
      shortTenorSpotRate <-  as.numeric(this$getSpotDataLoad()[this$getCurrentDate(),this$.shortTenor][[1]])
      spot <-  longTenorSpotRate - shortTenorSpotRate
      fwd <-   longTenorFwdRate - shortTenorFwdRate
      rateDiff <- (longTenorSpotRate - shortTenorSpotRate) - (longTenorFwdRate - shortTenorFwdRate)
      vol <- this$computeVol()*(this$.volScalingFactor)            
      temp.payoutRatio <- rateDiff/vol
      temp.zoo <- zoo(temp.payoutRatio,as.character(this$getCurrentDate()))
      this$addPayout(temp.zoo)
      }
    index(this$.payoutRatio) <- as.POSIXct(index(this$.payoutRatio))
})

setMethodS3("computeVol","IRSSlopePayoutRatio",function(this,...)
{
    NumUnits <- -1.0*as.numeric(this$.volLookBack)
    unit <- "d"
    startDate <- as.POSIXct(this$getCurrentDate())
    
    backDate <- getFincadDateAdjust(startDate=startDate,unit=unit,NumUnits=NumUnits,adjRule=1, holidayList=NULL)
    temp.zoo <- this$getSpotDataLoad()[(index(this$getSpotDataLoad())<this$getCurrentDate())&(index(this$getSpotDataLoad())>=backDate)] 
    
    result <- sd(diff(temp.zoo[,2]-temp.zoo[,1]))
    return(result)
})
   
setMethodS3("loadIRSSlopeData","IRSSlopePayoutRatio",function(currency=NULL,forwardStart=NULL,tenorList=NULL,startDate=NULL,endDate=NULL,tsdb=NULL,...)
{
  
  assert(length(tenorList)==2,paste("Need two tenors for this routine"))
  tenors <- c("1y","18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")
  func <- function(x) {assert(any(x==tenors))}
  lapply(tenorList,func)
  longTenor <- tenorList[1]
  shortTenor <- tenorList[2]
  if (!(forwardStart=="spot")) {
    ts.name.long.tenor <- paste("irs",currency,"fwd_rate",paste(forwardStart,longTenor,sep=""),"mid",sep="_")
    ts.name.short.tenor <- paste("irs",currency,"fwd_rate",paste(forwardStart,shortTenor,sep=""),"mid",sep="_")
  }
  else {
    ts.name.long.tenor <- paste("irs",currency,"rate",longTenor,"mid",sep="_")
    ts.name.short.tenor <- paste("irs",currency,"rate",shortTenor,"mid",sep="_")
  }
  
  ts.name.list <- c(ts.name.long.tenor,ts.name.short.tenor)
  result <- getMergedTimeSeries(tsdb=tsdb,tsName=ts.name.list, dataSource="internal", startDate = startDate, endDate = endDate)
  colnames(result) <- tenorList
  result <- strip.times.zoo(result)
  return(result)
})
    
setMethodS3("addPayout","IRSSlopePayoutRatio",function(this,temp.zoo,...)
{
  if (length(this$.payoutRatio) > 0){
    this$.payoutRatio <- rbind(this$.payoutRatio,temp.zoo)
  }
  else
  {
    this$.payoutRatio <- temp.zoo
  }
  return()
})

setMethodS3("getNumberOfDaysInForwardStart","IRSSlopePayoutRatio",function(this,...)
{

# Rough approximation for number of days to scale payout factor
# Ultimately won't matter, just a scaling factor.

  tenor.parse <- parseSimpleTenor(this$.forwardStart)
  first.factor <- (as.numeric(tenor.parse$numUnits))
 
  if (tenor.parse$unit=="m") {
    second.factor <- 21
  }
  else if (tenor.parse$unit=="w") {
    second.factor <- 5
  }
  else if (tenor.parse$unit=="y") {
    second.factor <- 252
  }

return(first.factor*second.factor)
})
          