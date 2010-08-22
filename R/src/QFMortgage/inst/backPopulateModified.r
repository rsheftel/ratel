
#Create and back populate the following:

# Current Coupon
# 30d Fwd Price
# 45d Fwd Price

backPopulateCurrentCoupon <- function(){

    library(QFMortgage)

    programs <- c('fncl','gnsf','fglmc','fnci','fgci')
    cpns30y <- seq(4.5,9.0,0.5)
    cpns15y <- (cpns30y - 0.5)
    cpnsVec <- rbind(cpns30y, cpns30y, cpns30y, cpns15y, cpns15y)
    settleVec <- c('1n','2n','3n','4n')

    if (isWindows()) path <- "V:/TSDB_Upload/Today/"
        else path <- "/data/TSDB_upload/Today/"

    
    for (programCount in 1:length(programs)){
    
        program <- programs[programCount]
        cpns <- cpnsVec[programCount,]
        print(paste('start for ',program))
        
        tbaMatrix <- TBAMatrix(program, cpns)
        tbaMatrix$setHistoryFromTSDB()

        allZoos <- tbaMatrix$getPriceZoo()
        allZoos <- do.call(merge,allZoos)
        allDates <- index(allZoos)

        tsdbZoo <- zoo(NA, allDates)
        print('Made tsdbZoo')
        
        for (dateCount in 1:length(allDates)){
            thisDate <- allDates[dateCount]
            tbaGrid <- TBAGrid(program,cpns,thisDate)
            priceMatrix <- tbaMatrix$getPriceMatrix(thisDate, cpns)
            tbaGrid$setPricesFromMatrix(priceMatrix, cpns, settleVec)
            
            settleMatrix <- tbaMatrix$getSettleDateMatrix(thisDate, cpns)
            tbaGrid$setSettleDatesFromMatrix(settleMatrix, settleVec)
            
            currentCpn <- tbaGrid$getCurrentCoupon()
            tsdbZoo[match(thisDate,index(tsdbZoo))] <- currentCpn
            print(paste(program,thisDate,currentCpn,sep="   "))
        }
        print('done populating zoo')
        
        #write to TSDB upload file
        tickerName <- paste(program, 'cc_30d_yield', sep="_")
        tickerName <- paste(tickerName, 'internal', sep=":")
        tsdbDF <- data.frame(tsdbZoo)
        colnames(tsdbDF) <- tickerName
        write.csv(tsdbDF, squish(path,'history_CC_',program,'.csv'),row.names=TRUE, quote=FALSE)
        print ('done writing zoo')
    }
}

backPopulateFwdPrice <- function(){

    library(QFMortgage)

    programs <- c('fncl','gnsf','fglmc','fnci','fgci')
    cpns30y <- seq(4.5,9.0,0.5)
    cpns15y <- (cpns30y - 0.5)
    cpnsVec <- rbind(cpns30y, cpns30y, cpns30y, cpns15y, cpns15y)
    settleVec <- c('1n','2n','3n','4n')

    if (isWindows()) path <- "V:/TSDB_Upload/Today/"
        else path <- "/data/TSDB_upload/Today/"

     for (nDays in c(30,45)){
        for (programCount in 1:length(programs)){

            program <- programs[programCount]
            cpns <- cpnsVec[programCount,]
            print(paste('start for ',program))

            tbaMatrix <- TBAMatrix(program, cpns)
            tbaMatrix$setHistoryFromTSDB()

            allZoos <- tbaMatrix$getPriceZoo()
            allZoos <- do.call(merge,allZoos)
            allDates <- index(allZoos)

            tsdbZoo <- zoo(t(rep(NA,length(cpns))), allDates)
            print('Made tsdbZoo')

            for (dateCount in 1:length(allDates)){
                thisDate <- allDates[dateCount]
                tbaGrid <- TBAGrid(program,cpns,thisDate)
                priceMatrix <- tbaMatrix$getPriceMatrix(thisDate, cpns)
                tbaGrid$setPricesFromMatrix(priceMatrix, cpns, settleVec)

                settleMatrix <- tbaMatrix$getSettleDateMatrix(thisDate, cpns)
                tbaGrid$setSettleDatesFromMatrix(settleMatrix, settleVec)

                fwdPrices <- tbaGrid$getNDayForwardPrice(nDays)
                tsdbZoo[match(thisDate,index(tsdbZoo)),] <- fwdPrices
                print(paste(program,thisDate,fwdPrices,sep="   "))
            }
            print('done populating zoo')

            #write to TSDB upload file
            tickerName <- paste(program,'_',format(cpns,nsmall=1),'_',nDays, 'd_price', sep="")
            tickerName <- paste(tickerName, 'internal', sep=":")
            tsdbDF <- data.frame(tsdbZoo)
            colnames(tsdbDF) <- tickerName
            write.csv(tsdbDF, squish(path,'history_',nDays,'d_',program,'.csv'),row.names=TRUE, quote=FALSE)
            print ('done writing zoo')
        }
    }
}