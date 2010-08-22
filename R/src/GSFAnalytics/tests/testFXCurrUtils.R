library("GSFAnalytics")

#Parse Cross
test.ParseCross <-function(...)
{
    shouldBomb(ParseCross("usdchf"))
    temp.list <- ParseCross("usd/chf")
    checkEquals(temp.list$over,"usd")
    checkEquals(temp.list$under,"chf")
    checkEquals(temp.list$cross,"usd/chf")
}
    
test.GetFXPrecedenceCross <- function(...)
{    
  shouldBomb(GetFXPrecedenceCross("BusinessTime"))
  
  results <- GetFXPrecedenceCross("usd/chf")
#USD == 5
  checkEquals(results$over,5)
#CHF == 7
  checkEquals(results$under,7)   
}

test.GetFXPrecedencePairs <- function(...)
{
  shouldBomb(GetFXPrecedencePairs("BusinessTime"))
  
  results <- GetFXPrecedencePairs("usd","chf")
#USD == 5
  checkEquals(results$over,5)
#CHF == 7
  checkEquals(results$under,7)   
}

test.ReturnCorrectFXPair <- function(...)
{
  shouldBomb(ReturnCorrectFXPair("usd","BusinessTime"))
  shouldBomb(ReturnCorrectFXPair("BusinessTime","jpy"))
  fxCurr <- FXCurr$setByCross("usd/mxn")
  fxExample <- ReturnCorrectFXPair("usd","mxn")
  checkEquals(fxCurr$over(),fxExample$over())
  checkEquals(fxCurr$under(),fxExample$under())
} 
 
 test.IsPrecedenceCorrectCross <- function(...)
 {
  shouldBomb(IsPrecedenceCorrectCross("BusinessTime"))
  checkEquals(IsPrecedenceCorrectCross("usd/chf"),TRUE) 
  checkEquals(IsPrecedenceCorrectCross("usd/eur"),FALSE)
 }
  
 test.IsPrecedenceCorrectPairs <- function(...)
 {
  shouldBomb(IsPrecedenceCorrectPairs("BusinessTime"))
  checkEquals(IsPrecedenceCorrectPairs("usd","chf"),TRUE) 
  checkEquals(IsPrecedenceCorrectPairs("usd","eur"),FALSE)
 }
 
 test.GetCurrencies <- function(...)
 {
  checkTrue(all(c("usd","chf","jpy") %in% GetCurrencies()))
 }
 
test.getListActiveCurrencyPairs <- function(...)
{
  ccy.list <- getListActiveCurrencyPairs()
  subList <- c("usdjpy","audusd","usdmxn")
  for (i in subList){
    checkEquals(any(i==ccy.list[]),TRUE)
  }
}

test.getListActiveCurrencyObjects <- function(...)
{
  ccy.list <- getListActiveCurrencyObjects()
  subList <- list(FXCurr$setByCross("aud/usd"),FXCurr$setByCross("usd/mxn"),FXCurr$setByCross("eur/usd"))
   for (i in 1:length(subList)){
    checkEquals(any(subList[[i]]$cross()==lapply(ccy.list[],cross)),TRUE)
    }
}

test.getListActivelyTradedCurrencies <- function(...)
{
  ccy.list <- getListActivelyTradedCurrencies()
  checkEquals(any(ccy.list=="usd/chf"),TRUE)
  checkEquals(any(ccy.list=="eur/chf"),TRUE)
}

test.isCrossUSDDenominated <- function(...)
{
	shouldBomb(isCrossUSDDenominated(curr="businessTime"))
	checkSame(isCrossUSDDenominated(curr=FXCurr$setByCross("usd/mxn")),TRUE)
	checkSame(isCrossUSDDenominated(curr=FXCurr$setByCross("eur/jpy")),FALSE)
}

test.doesCrossContainUSD <- function(...)
{
	shouldBomb(doesCrossContainUSD(curr="businessTime"))
	checkSame(doesCrossContainUSD(curr=FXCurr$setByCross("usd/mxn")),TRUE)
	checkSame(doesCrossContainUSD(curr=FXCurr$setByCross("eur/usd")),TRUE)
	checkSame(isCrossUSDDenominated(curr=FXCurr$setByCross("eur/jpy")),FALSE)
}

