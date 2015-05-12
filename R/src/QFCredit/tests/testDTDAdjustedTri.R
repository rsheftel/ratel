## Test file for the DTDAdjustedTri object

library(QFCredit)

this <- DTDAdjustedTri()

dateList <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03"))
adjustedTri1 <- zoo(c(100,110,102),dateList)
adjustedTri2 <- zoo(c(100,105,101),dateList)
cdsSpreads  <- zoo(c(0.03,0.02,0.04),dateList)
avgIndexSpread <- zoo(c(0.02,0.04,0.02),dateList)

testDTDAdjustedTri.constructor <- function()
{
    checkSame(this$.indexTickers,c("cdx-na-ig","cdx-na-ig-hvol"))
    checkSame(this$.tenor,"5y")
    checkSame(this$.dataSource,"internal")
    checkSame(this$.timeStamp,"15:00:00")
	checkSame(this$.base,100)
}

testDTDAdjustedTri.calcAdjustedCDSTRI <- function()
{     
    cdsTRI <- getZooDataFrame(zoo(c(100,98,96,97),c("2007-05-01","2007-05-02","2007-05-03","2007-05-04")))
    indexTRI <- getZooDataFrame(zoo(c(100,99,98,98.5),c("2007-05-01","2007-05-02","2007-05-03","2007-05-04")))
    cdsDelta <- getZooDataFrame(zoo(c(-0.05,-0.06,-0.05,-0.06),c("2007-05-01","2007-05-02","2007-05-03","2007-05-04")))
    indexDelta <- getZooDataFrame(zoo(c(-0.04,-0.05,-0.04,-0.05),c("2007-05-01","2007-05-02","2007-05-03","2007-05-04")))   
    
    target <- getZooDataFrame(zoo(c(100,99.250,98.450,98.825),c("2007-05-01","2007-05-02","2007-05-03","2007-05-04")))
    colnames(target) <- "TRI"
    checkEquals(target,this$calcAdjustedCDSTRI(cdsTRI,indexTRI,cdsDelta,indexDelta))
}

testDTDAdjustedTri.calcCombinedAdjustedCDSTRI <- function()
{
    junk <- getZooDataFrame(zoo(c(-0.04),c("2007-05-01")))

    shouldBomb(this$calcCombinedAdjustedCDSTRI(junk,adjustedTri2,cdsSpreads,avgIndexSpread))
    shouldBomb(this$calcAdjustedCDSTRI(adjustedTri1,junk,cdsSpreads,avgIndexSpread))
    shouldBomb(this$calcAdjustedCDSTRI(adjustedTri1,adjustedTri2,junk,avgIndexSpread))
    shouldBomb(this$calcAdjustedCDSTRI(adjustedTri1,adjustedTri2,cdsSpreads,junk))

    # method = "standard"

        # Base case
        result <- this$calcCombinedAdjustedCDSTRI(adjustedTri1,adjustedTri2,cdsSpreads,avgIndexSpread)
        target <- getZooDataFrame(zoo(c(100,105,97),dateList))
        colnames(target) <- "TRI"
        checkSame(result,target)          
}