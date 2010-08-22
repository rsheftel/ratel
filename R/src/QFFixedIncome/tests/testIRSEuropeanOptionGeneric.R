library(QFFixedIncome)

testIRSEuropeanOptionGeneric <- function()
{
    expiry <- "2y"
    currency <- "usd"
    quoteSide <-"mid"
    tenor <- "10y"
#   Set up forwards

    irsGeneric <- GenericIRS()
    irsGeneric$setDefault(currency=currency, quoteSide=quoteSide, tenor=tenor)

# check for bad inputs    
    
    shouldBomb(IRSEuropeanOptionGeneric("BusinessTime",expiry)) 
    shouldBomb(IRSEuropeanOptionGeneric(irsGeneric,"none"))
  
#check for good inputs
    
    irsoption <- IRSEuropeanOptionGeneric(GenericIRS = irsGeneric, expiry = expiry)
    
    checkEquals(irsoption$getGenericIRS()$.currency,currency)
    checkEquals(irsoption$getGenericIRS()$.tenor,tenor)
    checkEquals(irsoption$getExpiry(),expiry)
    checkEquals(irsoption$getGenericIRS()$.quoteSide,quoteSide)
    
    
}
    
