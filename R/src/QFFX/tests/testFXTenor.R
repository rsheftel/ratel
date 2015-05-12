library("QFFX")
testFXTenor <- function()
{
    
  checkEquals(FXTenor$checkTenor("2y"),TRUE)
  shouldBomb(FXTenor$checkTenor("1.5y"))
  shouldBomb(FXTenor$checkTenor("BusinessTime"))
  shouldBomb(FXTenor$checkTenor(tenor=2))
  checkEquals(FXTenor$checkTenor("1y"),TRUE)
  checkEquals(FXTenor$checkTenor("5y"),TRUE)
  checkEquals(FXTenor$checkTenor("1w"),TRUE)  
    
  }
    
