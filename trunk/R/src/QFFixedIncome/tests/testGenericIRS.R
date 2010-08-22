library(QFFixedIncome)
## Test file for the IRS object

testGenericIRS <- function()
{
    # tests bad inputs
    
    IRSsample <- GenericIRS()
    
    shouldBomb(IRSsample$setTermsGeneric(currency = "us", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"), tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "op", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = FALSE, quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = 1,instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irsswap",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",
        rateBasisFloat = "simple interest",rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = FALSE,spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = FALSE,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = 1,payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = 1,payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "sem",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = 30,dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
    shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = 30,relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="spot"))
     shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = 30,relevantFinancialCenters = c("No Dice"),tenor = "5y", forwardStart="spot"))
      shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = 5, forwardStart="spot"))
      shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modify This",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y"))
      shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified This",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y"))
      shouldBomb(IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="Not right now"))  
        
    
    # tests good inputs
        
    IRSsample <- GenericIRS()
    IRSsample$setTermsGeneric(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",relevantFinancialCenters = c("nyb","lnb"),tenor = "5y", forwardStart="1y")

    checkEquals("usd",IRSsample$.currency)
    checkEquals("open",IRSsample$.quoteType)
    checkEquals("ask",IRSsample$.quoteSide)
    checkEquals("rate",IRSsample$.quoteConvention)
    checkEquals("irs",IRSsample$.instrument)
    checkEquals("libor",IRSsample$.indexFloat)
    checkEquals(0,IRSsample$.spreadFloat)
    checkEquals("quarterly",IRSsample$.resetFreqFloat)
    checkEquals("quarterly",IRSsample$.payFreqFloat)
    checkEquals("semi-annual",IRSsample$.payFreqFixed)
    checkEquals("simple interest",IRSsample$.rateBasisFloat)
    checkEquals("simple interest",IRSsample$.rateBasisFixed)
    checkEquals("30/360",IRSsample$.dayCountFloat)
    checkEquals("30/360",IRSsample$.dayCountFixed)
    checkEquals(TRUE,IRSsample$.isDefined)
    checkEquals("5y",IRSsample$.tenor)
    checkEquals("1y",IRSsample$.forwardStart)
    checkEquals(TRUE,IRSsample$.isDefinedGeneric)
    
    rm(IRSsample)
}
