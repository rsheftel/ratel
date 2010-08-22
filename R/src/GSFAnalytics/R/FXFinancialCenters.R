setConstructorS3("FXFinancialCenters", function(...)
{
    this <- extend(RObject(), "FXFinancialCenters",
        .currencyList = NULL
        )

      this$.currencyList <- list("usd"="nyb","gbp"="lnb",
      "aud"="syb","brl"="bxb",
      "cny"="beb","cop"="bob",
      "czk"="prb","dkk"="cob",
      "hkd"="hkb","huf"="bdb",
      "inr"="bmb","idr"="jab",
      "ils"="tab","jpy"="tkb",
      "ars"="bab","myr"="klb",
      "mxn"="mxb","eur"="tgt",
      "nzd"="aub","nok"="osb",
      "pen"="lmb","pln"="wab",
      "rub"="mwb","clp"="sab",
      "sgd"="sib","skk"="btb",
      "sit"="lbb","zar"="job",
      "krw"="seb","sek"="stb",
      "chf"="zub","twd"="tpb",
      "cad"="nca","try"="anb",
      "thb"="bkb","kzt"="ayb",
      "vnd"="???","uah"="???",
      "sar"="???","php"="???", 
      "aed"="???","ron"="???",
	  "isk"="???","ngn"="???",
	  "ghs"="???","two"="???",
	  "cno"="???","ino"="???",
	  "kro"="???"
	  
      )

      this

})
	
setMethodS3("getFinancialCenterGivenCurrency","FXFinancialCenters",function(this,ccy=NULL,...)
{
    currDateInfo <- FXFinancialCenters()
    center <- as.character(currDateInfo$.currencyList[ccy])
    assert((center != "NULL"),paste("Currency ",ccy,"  does not have a financial center"))
    
    return(center)
})

setMethodS3("getListOfFinancialCenters","FXFinancialCenters",function(this,...)
{
  currDateInfo <- FXFinancialCenters()
  return(as.character(currDateInfo$.currencyList[names(currDateInfo$.currencyList)]))
})