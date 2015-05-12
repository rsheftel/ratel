## Test file for the IMMDates object
library(QFCredit)



test.maturityFromEffective <- function()
{
    # test bad inputs
    
    dateSample <- IMMDates()
    
    shouldBomb(dateSample$nextOne())
    shouldBomb(dateSample$nextOne(myDate = TRUE))
    
    shouldBomb(dateSample$maturityFromEffective())
    shouldBomb(dateSample$maturityFromEffective(effDate = TRUE))
    shouldBomb(dateSample$maturityFromEffective(effDate = "2006-01-01",tenor = "z"))    
    shouldBomb(dateSample$maturityFromEffective(effDate = "2006-01-01",tenor = -1))
    
    # test good inputs
    
    checkEquals(as.POSIXlt("2011-03-20"),dateSample$maturityFromEffective(effDate = "2006-03-19",tenor = 5))
    checkEquals(as.POSIXlt("2011-03-20"),dateSample$maturityFromEffective(effDate = "2006-03-20",tenor = 5))
    checkEquals(as.POSIXlt("2011-06-20"),dateSample$maturityFromEffective(effDate = "2006-03-21",tenor = 5))
    checkEquals(as.POSIXlt("2012-06-20"),dateSample$maturityFromEffective(effDate = "2006-12-21",tenor = 5.25))
    
    checkEquals(as.POSIXlt("1996-03-20"),dateSample$nextOne(myDate = "1996-03-20"))
    checkEquals(as.POSIXlt("1996-06-20"),dateSample$nextOne(myDate = "1996-03-21"))
    checkEquals(as.POSIXlt("1996-06-20"),dateSample$nextOne(myDate = "1996-03-22"))
	
	
	#  SNAC contracts
	
	checkEquals(as.POSIXlt("2011-03-20"),dateSample$maturityFromEffective(effDate = "2006-01-18",tenor = 5,TRUE)) # 3/19 trade date
	checkEquals(as.POSIXlt("2011-06-20"),dateSample$maturityFromEffective(effDate = "2006-01-19",tenor = 5,TRUE)) # 3/20 trade date
	checkEquals(as.POSIXlt("2011-06-20"),dateSample$maturityFromEffective(effDate = "2006-01-20",tenor = 5,TRUE)) # 3/21 trade date
    
    rm(dateSample)
}