## Commonly used functions for FX.

GetFXPrecedenceCross <- function(cross=NULL,...)
{
  temp.fx <- ParseCross(cross)
  precedence <- GetFXPrecedencePairs(temp.fx$over, temp.fx$under)
  return(precedence)
}

GetFXPrecedencePairs <- function(over=NULL, under=NULL,...)
{
  needs(over="character")
  needs(under="character") 
  assert(nchar(over)==3)
  assert(nchar(under)==3)   
  
  res <- select(paste("select * from ccy where ccy_name in ('",over,"', '",under,"')",sep=""))
  precedence = list(over = res[res[,"ccy_name"]==over,]$precedence, under = res[res[,"ccy_name"]==under,]$precedence)
  return(precedence)
}

IsPrecedenceCorrectCross <- function(cross=NULL,...)
{
  precedence <- GetFXPrecedenceCross(cross)
  if (precedence$over < precedence$under) return(TRUE)
  return(FALSE)
}

ReturnCorrectFXPair <- function(firstCcy=NULL, secondCcy=NULL)
{
  needs(firstCcy="character")
  needs(secondCcy="character") 
  assert(nchar(firstCcy)==3)
  assert(nchar(secondCcy)==3)   
  assert(!(firstCcy == secondCcy))
  
  res <- GetFXPrecedencePairs(firstCcy,secondCcy)
  if (res$over < res$under)
  {
    fxCurr <- FXCurr$setByPairs(firstCcy,secondCcy)
    return(fxCurr)
   }
  else
  {
    fxCurr <- FXCurr$setByPairs(secondCcy,firstCcy)
    return(fxCurr)
    }
}
 

IsPrecedenceCorrectPairs <- function(over=NULL, under=NULL,...)
{
   needs(over="character")
   needs(under="character") 
   assert(nchar(over)==3)
   assert(nchar(under)==3)
   
   precedence <- GetFXPrecedencePairs(over,under,)
   if (precedence$over < precedence$under) return(TRUE)
  
  return(FALSE)
}

ParseCross <- function(cross = NULL,...)
{
    needs(cross="character")
    over<-strsplit(cross,"/")[[1]][1]
    under<-strsplit(cross,"/")[[1]][2]
    assert(nchar(over)==3)
    assert(nchar(under)==3)   
    temp.list <- list(over = over, under = under, cross = cross)
    return(temp.list)
}

GetCurrencies <- function(...)
{
  as.character(select("select ccy_name from ccy order by precedence")[["ccy_name"]])
}

select <- function(query) {
  conn <- SQLConnection()
  conn$init()
  result <- conn$select(query)
  conn$disconnect()
  result
}

writeCurrencyRateDataToFile <- function(tsdb=NULL, FXCurr = NULL,startDate=NULL, endDate=NULL, fileName=NULL)
{
    ticker.list <- c()
    tenorList <- FXTenor$getTenors()
    for (tenor in tenorList)
    {
      ccy.pair <- paste(FXCurr$over(), FXCurr$under(), sep = "")
      time.series.name <- paste(ccy.pair, tenor, "rate", "mid", sep = "_")
      ticker.list <- c(ticker.list,time.series.name)
    }   
    
    data.load <- tsdb$retrieveTimeSeriesByName(name=ticker.list, source="gs", start=startDate, end=endDate)
    fx.data <- TimeSeriesFile$writeTimeSeries(ts.array = data.load, file = fileName)
    
}

getListCurrencyPairs <- function()
{
  ccypairList <- c()
  ccy.list <- GetCurrencies()
  for(i in 1:(length(ccy.list)-1)) {
        for(j in (i+1):length(ccy.list)) {
            ccypairList <- c(ccypairList,squish(ccy.list[[i]],ccy.list[[j]]))
        }
  }
  return(ccypairList)
}

getListActiveCurrencyPairs <- function()
{
  ccypairList <- c()
  ccy.list = as.character(select("select ccy_pair_name from ccy_pair where is_active=\'TRUE\' order by ccy_pair_name")[["ccy_pair_name"]])
  return(ccy.list)
}

getListActiveCurrencyObjects <- function()
{
  mod.ccy.list <- list()
  ccy.list <- getListActiveCurrencyPairs()
  for (i in 1:length(ccy.list))
  {
    over.ccy <- substr(ccy.list[i],1,3)
    under.ccy <- substr(ccy.list[i],4,6)
    fxcurr <-  FXCurr$setByPairs(over = over.ccy, under = under.ccy)
    mod.ccy.list[[i]] <- fxcurr
  }
  return(mod.ccy.list)
}

getListActivelyTradedCurrencies <- function()
{
	mod.ccy.list <- list()
	ccy.list <- getListActiveCurrencyPairs()
	for (i in 1:length(ccy.list))
	{
		over.ccy <- substr(ccy.list[i],1,3)
		under.ccy <- substr(ccy.list[i],4,6)
		fxcurr <-  squish(over.ccy, "/",under.ccy)
		mod.ccy.list <- appendSlowly(mod.ccy.list,fxcurr)
	}
as.character(mod.ccy.list)
}
       
isCrossUSDDenominated <- function(curr=NULL)
{
	needs(curr="FXCurr")
	if (curr$over()=="usd") return(TRUE)
	return(FALSE)
}      

doesCrossContainUSD <- function(curr=NULL){
	needs(curr="FXCurr")
	if ((curr$over()=="usd")||(curr$under()=="usd")) return(TRUE)
	return(FALSE)
}