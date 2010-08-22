## Test file for the LiborTri object
library(QFFixedIncome)

tenor = "on"
startDate = "2008-01-15"
endDate = "2008-01-20"
updateTSDB = FALSE
dates <- as.POSIXct(c("2008-01-16","2008-01-17","2008-01-18"))
dates <- paste(dates,"15:00:00")
this <- LiborTri(ccy = "usd",dataTimeStamp = "15:00:00",unitSett = "d",numUnitsSett = 1,acc = 2)

test.LiborTri.constructor <- function(){
    checkEquals(this$.principal, 100)
    checkEquals(this$.financialCenter, c("lnb","nyb"))   
    checkEquals(this$.holidaySource, "financialcalendar")
    checkEquals(this$.triSource, "internal")
    checkEquals(this$.dataSource, "internal")    
    checkEquals(this$.instrument, "libor")        
    checkEquals(this$.ccy, "usd")        
    checkEquals(this$.dataTimeStamp, "15:00:00")        
    checkEquals(this$.acc, 2)        
    checkEquals(this$.numUnitsSett, 1)        
    checkEquals(this$.unitSett, "d")            
    
    tsdb <- this$.tsdb
    needs(tsdb = "TimeSeriesDB?")
}

test.LiborTri.constructor <- function(){
    result <- this$run(startDate,endDate,tenor,FALSE)
    target <- getZooDataFrame(zoo(c(0.01201216666666667,0.01193925,0.04767366666666667),dates))
    colnames(target) <- colnames(result)
    checkSame(result,target)
    
    shouldBomb(this$run(startDate,endDate,"junk",FALSE))
    shouldBomb(this$run(startDate,TRUE,tenor,FALSE))    
    shouldBomb(this$run(TRUE,endDate,tenor,FALSE))    
}