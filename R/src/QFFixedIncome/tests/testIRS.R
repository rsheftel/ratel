## Test file for the IRS object
library(QFFixedIncome)

testIRS <- function()
{
    # tests bad inputs
    
    IRSsample <- IRS()
    
    shouldBomb(IRSsample$setTerms(currency = "us", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "op", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = FALSE, quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = 1,instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irsswap",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = FALSE,spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = FALSE,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = 1,payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = 1,payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "sem",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good ",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = 30,dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = 30,busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = 30,busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="No Dice",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="No Dice",relevantFinancialCenters=c("nyb","lnb")))            
    shouldBomb(IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("No Dice")))
    # tests good inputs
        
    IRSsample <- IRS()
    IRSsample$setTerms(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",busDayConvFixed="Previous Good",busDayConvFloat="Previous Good",relevantFinancialCenters=c("nyb","lnb"))

    checkEquals("usd",IRSsample$.currency)
    checkEquals("open",IRSsample$.quoteType)
    checkEquals("ask",IRSsample$.quoteSide)
    checkEquals("rate",IRSsample$.quoteConvention)
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
    
    rm(IRSsample)
}
