## Test file for the IRS object
library(QFFixedIncome)

testSpecificIRS <- function()
{
    # tests bad inputs
    
    IRSsample <- SpecificIRS()
    
    shouldBomb(IRSsample$setTermsSpecific(currency = "us", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "op", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = FALSE, quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = 1,instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irsswap",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = FALSE,spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = FALSE,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = 1,payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = 1,payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "sem",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = 30,dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = 30,
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = -1,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "p",coupon = 0.05, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = FALSE, effDate = "2005-01-01", matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = 2, matDate = "2010-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = FALSE,relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2000-01-01",relevantFinancialCenters=c("nyb","lnb")))
    shouldBomb(IRSsample$setTermsSpecific(currency = "usd", quoteType = "open", quoteSide = "ask", quoteConvention = "rate",instrument = "irs",
        indexFloat = "libor",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",payFreqFixed = "semi-annual",rateBasisFloat = "simple interest",
        rateBasisFixed = "simple interest",dayCountFloat = "30/360",dayCountFixed = "30/360",
        notional = 1000000,direction = "pay",coupon = 0.05, effDate = "2005-01-01", matDate = "2000-01-01",relevantFinancialCenters=c("No Dice")))
    # tests good inputs
        
    IRSsample <- SpecificIRS()
    IRSsample$setDefault(direction = "pay",coupon = 0.05,effDate = "2005-01-01", matDate = "2010-01-01")

    checkEquals("usd",IRSsample$.currency)
    checkEquals("close",IRSsample$.quoteType)
    checkEquals("mid",IRSsample$.quoteSide)
    checkEquals("rate",IRSsample$.quoteConvention)
    checkEquals("irs",IRSsample$.instrument)
    checkEquals("libor 3m",IRSsample$.indexFloat)
    checkEquals(0,IRSsample$.spreadFloat)
    checkEquals("quarterly",IRSsample$.resetFreqFloat)
    checkEquals("quarterly",IRSsample$.payFreqFloat)
    checkEquals("semi-annual",IRSsample$.payFreqFixed)
    checkEquals("simple interest",IRSsample$.rateBasisFloat)
    checkEquals("simple interest",IRSsample$.rateBasisFixed)
    checkEquals("act/360",IRSsample$.dayCountFloat)
    checkEquals("30/360",IRSsample$.dayCountFixed)
    checkEquals(TRUE,IRSsample$.isDefined)
    checkEquals("pay",IRSsample$getDirection())
    checkEquals(1000000,IRSsample$getNotional())
    checkEquals(0.05,IRSsample$.coupon)
    checkEquals(c("2005-01-01"),as.character(IRSsample$getEffectiveDate()))
    checkEquals(c("2010-01-01"),as.character(IRSsample$getMaturityDate()))
    checkEquals(TRUE,IRSsample$.isDefinedSpecific)
    checkEquals(c("nyb","lnb"),IRSsample$getRelevantFinancialCenters())
    
    IRSsample$setDefaultUsingTenorAndForwardStart(direction = "pay",coupon = 0.05,tradeDate= "2005-01-01", tenor = "10y", forwardStart= "1y")
    checkEquals("usd",IRSsample$.currency)
    checkEquals("close",IRSsample$.quoteType)
    checkEquals("mid",IRSsample$.quoteSide)
    checkEquals("rate",IRSsample$.quoteConvention)
    checkEquals("irs",IRSsample$.instrument)
    checkEquals("libor 3m",IRSsample$.indexFloat)
    checkEquals(0,IRSsample$.spreadFloat)
    checkEquals("quarterly",IRSsample$.resetFreqFloat)
    checkEquals("quarterly",IRSsample$.payFreqFloat)
    checkEquals("semi-annual",IRSsample$.payFreqFixed)
    checkEquals("simple interest",IRSsample$.rateBasisFloat)
    checkEquals("simple interest",IRSsample$.rateBasisFixed)
    checkEquals("act/360",IRSsample$.dayCountFloat)
    checkEquals("30/360",IRSsample$.dayCountFixed)
    checkEquals(TRUE,IRSsample$.isDefined)
    checkEquals("pay",IRSsample$getDirection())
    checkEquals(1000000,IRSsample$getNotional())
    checkEquals(0.05,IRSsample$.coupon)
    checkEquals(c("2006-01-04"),as.character(IRSsample$getEffectiveDate()))
    checkEquals(c("2016-01-04"),as.character(IRSsample$getMaturityDate()))
    checkEquals(TRUE,IRSsample$.isDefinedSpecific)
    checkEquals(c("nyb","lnb"),IRSsample$getRelevantFinancialCenters())
    
    
    
    rm(IRSsample)
}
