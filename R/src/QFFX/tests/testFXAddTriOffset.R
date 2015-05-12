library("QFFX")
testFXAddTriOffset <- function()
{
    triName <- c("usdjpy_6m_put_tri_2w15d")
	testZoo <- zoo(c(1,-2,3),c(as.POSIXct("2008-11-05"),as.POSIXct("2008-11-06"),as.POSIXct("2008-11-07")))
	secondTestZoo <- zoo(c(105,-106,-10),c(as.POSIXct("2008-11-05"),as.POSIXct("2008-11-06"),as.POSIXct("2008-11-07")))
	tsdb <- TimeSeriesDB()
	addTriOffset <- FXAddTriOffset(triName = triName, tsdb = tsdb, writeToTSDB = FALSE)
	
	addTriOffset$loadTri()
	triValue <- addTriOffset$.tri[3]
	addTriOffset$addConstantToTri(offset = 100)  
	checkSame(addTriOffset$.tri[3],triValue+100)
	
	addTriOffset$.tri <- testZoo
	addTriOffset$addConstantToTri(offset = 100)  
	checkSame(as.numeric(addTriOffset$.tri[3]),103)
	
	addTriOffset$.tri <- testZoo
	checkSame(addTriOffset$isTriPositive(),FALSE)
	
	addTriOffset$makeTriPositive()
	checkSame(as.numeric(addTriOffset$.tri[2]),98)
	
	addTriOffset$.tri <- secondTestZoo
	addTriOffset$makeTriPositive()
	checkSame(as.numeric(addTriOffset$.tri[2]),94)
	
}
    
