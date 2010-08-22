## Test file for the TermStructureUtils class
library("GSFAnalytics")

test.characterToNumericTenor <- function()
{
    checkEquals(c(0.5,1.5,1,2,3,4,5,6,7,8,9,10,12,15,20,25,30,40),
            characterToNumericTenor(tenor = c("6m","18m","1y","2y","3y","4y","5y","6y","7y","8y","9y","10y","12y","15y","20y","25y","30y","40y")))  
    checkEquals(c(0.5,30),characterToNumericTenor(tenor = c("6m","30y"))) 
    checkEquals(c(30,30),characterToNumericTenor(tenor = c("30y","30y"))) 
    shouldBomb(characterToNumericTenor("6.5m"))
}
test.numericToCharacterTenor <- function()
{
    checkEquals(c("6m","18m","1y","2y","3y","4y","5y","6y","7y","8y","9y","10y","12y","15y","20y","25y","30y","40y"),
            numericToCharacterTenor(tenor = c(0.5,1.5,1,2,3,4,5,6,7,8,9,10,12,15,20,25,30,40)))            
    checkEquals(c("6m","30y"),numericToCharacterTenor(tenor = c(0.5,30)))
    checkEquals(c("1y","1y"),numericToCharacterTenor(tenor = c(1,1)))
    shouldBomb(numericToCharacterTenor(6.6))             
}

test.getTermStructureForTimeSeries <- function()
{
    # Check bombs
    
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries()))
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries("irs_usd_rat_tenor_mid",tenorList,source = "internal",startDate = "2007-01-01",endDate = "2007-01-01")))   
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries("irs_usd_rate_mid",tenorList,source = "internal",startDate = "2007-01-01",endDate = "2007-01-01")))
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",TRUE,source = "internal",startDate = "2007-01-01",endDate = "2007-01-01")))    
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",tenorList,source = "i",startDate = "2007-01-01",endDate = "2007-01-01")))   
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",tenorList,source = "internal",startDate = "2008-01-01",endDate = "2007-01-01")))    
    shouldBomb(characterToNumericTenor(getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",tenorList,source = "internal",startDate = TRUE,endDate = "2007-01-01")))   
    
    tenorList <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")

    # NULL Check
    result <- getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",tenorList,source = "internal",startDate = "2007-01-01",endDate = "2007-01-01")
    checkEquals(NULL,result)

    # IRS check
    result <- getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",tenorList,source = "internal",startDate = "2007-05-03",endDate = "2007-05-03")
    target <- getZooDataFrame(zoo(
                    matrix(c(
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342),nrow = 1, ncol = 16),
                    order.by = "2007-05-03 15:00:00"))
    colnames(target) <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")
    checkEquals(target,result)
    
    # CDS check
    tenorList <- c("6m","1y","2y","3y","4y","5y","7y","10y","15y","20y","30y")
    result <- getTermStructureForTimeSeries("gm_snrfor_usd_xr_spread_tenor",tenorList,source = "markit",startDate = "2006-03-01",endDate = "2006-03-01")
    target <- getZooDataFrame(zoo(
                    matrix(c(0.1319827654,0.1356195457,0.1483032511,0.1371213442,0.1292120448,
                        0.1253536151,0.1184347815,0.1140607894,0.1121550235,0.1092537523,0.1117944908),nrow = 1, ncol = 11),
                    order.by = "2006-03-01"))
       
    colnames(target) <- c("6m","1y","2y","3y","4y","5y","7y","10y","15y","20y","30y")
    checkEquals(target,result)
    
    # one tenor
    tenorList <- c("5y")
    result <- getTermStructureForTimeSeries("gm_snrfor_usd_xr_spread_tenor",tenorList,source = "markit",startDate = "2006-03-01",endDate = "2006-03-01")
    target <- getZooDataFrame(zoo(0.1253536151,order.by = "2006-03-01"))
    colnames(target) <- c("5y")
    
    checkEquals(target,result)
}

test.getInterpolatedTermStructure <- function()
{
    tenorList <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")
    numTenorList <- characterToNumericTenor(tenorList)
    
    zooCurve <- getZooDataFrame(zoo(
                    matrix(c(
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342),nrow = 1, ncol = 16),
                    order.by = "2007-05-03 15:00:00"))
    type <- 1
    
    # Check bombs
    
    shouldBomb(getInterpolatedTermStructure())
    shouldBomb(getInterpolatedTermStructure(1,numTenorList,4.5,type))
    shouldBomb(getInterpolatedTermStructure(zooCurve,TRUE,4.5,type))
    shouldBomb(getInterpolatedTermStructure(zooCurve,numTenorList,TRUE,type))
    shouldBomb(getInterpolatedTermStructure(zooCurve,numTenorList,4.5,TRUE))
    shouldBomb(getInterpolatedTermStructure(zooCurve,numTenorList,c(4.5,2),type,TRUE))
    shouldBomb(getInterpolatedTermStructure(zooCurve,numTenorList,matrix(c(4.5,2),ncol = 2),type,TRUE))
    
    # 4.5 year interpolation
    
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,4.5,type)
    target <- getZooDataFrame(zoo(5.05236,order.by = "2007-05-03 15:00:00"))
    colnames(target) <- c(4.5)
    
    checkEquals(target,result)
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,4.5,type,TRUE)
    colnames(target) <- "interpolated"
    checkEquals(target,result)
    
    # 4.5/5.5 year interpolation
    
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4.5,5.5),type)
    target <- getZooDataFrame(zoo(array(c(5.05236,5.07482),dim = c(1,2)),order.by = "2007-05-03 15:00:00"))
    colnames(target) <- c(4.5,5.5)
    
    checkEquals(target,result)
    
    # 4.5/5.5 year interpolation, several dates
    
    zooCurve <- getZooDataFrame(zoo(
                    matrix(c(
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342,
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342),nrow = 2, ncol = 16),
                    order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(zooCurve) <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")     
    
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4.5,5.5),type)
    target <- getZooDataFrame(zoo(array(c(5.149630,5.178685,5.222895,5.270085),dim = c(2,2)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- c(4.5,5.5)
    
    checkEquals(target,result)
    
    # acrossTime = TRUE, several dates
    
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4.5,5.5),type,TRUE)
    target <- getZooDataFrame(zoo(array(c(5.149630,5.270085),dim = c(2,1)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- "interpolated"
    
    checkEquals(target,result)
       
    # 0,0.24,0.25 year interpolation, several dates, libor
    
    zooCurve <- getZooDataFrame(zoo(
                    matrix(c(
                        5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342,
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342),nrow = 2, ncol = 15),
                    order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(zooCurve) <- TermStructure$libor
    
    result <- getInterpolatedTermStructure(zooCurve,characterToNumericTenor(TermStructure$libor),c(0,0.24,0.25),type)
    target <- getZooDataFrame(zoo(array(c(5.04051,5.04224,5.3731496,5.3972908,5.38778,5.40622),dim = c(2,3)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- c(0,0.24,0.25)
    
    checkEquals(target,result)
    
    # several dates, outside range
    
    zooCurve <- getZooDataFrame(zoo(
                    matrix(c(
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342,
                        5.20674,5.10598,5.04051,5.04224,5.06248,5.08716,5.11933,5.14901,
                        5.17993,5.20836,5.26586,5.33181,5.38778,5.40622,5.40592,5.39342),nrow = 2, ncol = 16),
                    order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(zooCurve) <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")  
    
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(1,45),type)
    target <- getZooDataFrame(zoo(array(c(5.20674,5.10598,5.40592,5.39342),dim = c(2,2)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- c(1,45)
    
    checkEquals(target,result)
    checkEquals(
        as.numeric(getInterpolatedTermStructure(zooCurve,numTenorList,c(1,45),type)),        
        as.numeric(getInterpolatedTermStructure(zooCurve,numTenorList,c(1.5,40),type))
    )
    
    # Check NA handling
    
    zooCurve[2,4] <- NA
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4,5),type)
    target <- getZooDataFrame(zoo(array(c(5.11933,5.14776,5.17993,5.20836),dim = c(2,2)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- c(4,5)
    
    checkEquals(target,result)
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4,4),type,TRUE)
    target <- getZooDataFrame(zoo(array(c(5.11933,5.14776),dim = c(2,1)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- "interpolated"
    checkEquals(target,result)
    
    # Check only one value and NAs
    
    zooCurve[1:2,1:16] <- NA
    zooCurve[2,5] <- 5
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4,5),type)
    target <- getZooDataFrame(zoo(array(c(NA,5,NA,5),dim = c(2,2)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- c(4,5)
    
    checkEquals(target,result)
    result <- getInterpolatedTermStructure(zooCurve,numTenorList,c(4,7),type,TRUE)
    target <- getZooDataFrame(zoo(array(c(NA,5),dim = c(2,1)),order.by = c("2007-05-03 15:00:00","2007-05-05 15:00:00")))
    colnames(target) <- "interpolated"
    checkEquals(target,result)
}