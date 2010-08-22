
constructor("TBAGrid", function(program = NULL, couponVector = NULL, gridDateTime = NULL)
{
    this <- extend(RObject(), "TBAGrid", .program = program, .couponVector = couponVector, .gridDateTime = gridDateTime)
    if (inStaticConstructor(this)) return(this)
    constructorNeeds (this, program = "character", couponVector = "numeric", gridDateTime = "POSIXct")

    #Set up initial variable
    this$.couponVector = format(this$.couponVector,nsmall=1)
    this$.settleVector = c('1n','2n','3n','4n')
    this$.source = "internal"

    return(this)
})

method("setSettleDatesFromTSDB", "TBAGrid", function(this, ...) {
#Set the settle dates and daysToSettle from TSDB

    tsdb <- TimeSeriesDB()
    #set up settle Dates vector
    this$.settleDates <- rep(NA,length(this$.settleVector))

    for (countSettle in 1:(length(this$.settleVector))){
        countCoupon <- 1
        while ((countCoupon <= length(this$.couponVector)) && is.na(this$.settleDates[countSettle])){
            tickerSettle <- paste(this$.program,this$.couponVector[countCoupon],this$.settleVector[countSettle],'settle_date',sep="_")

            tsdbReturn <- as.numeric(tsdb$retrieveOneTimeSeriesByName(tickerSettle,this$.source, this$.gridDateTime, this$.gridDateTime))
            if(!NROW(tsdbReturn)==0)
                this$.settleDates[countSettle] <- tsdbReturn
            countCoupon <- countCoupon + 1
        }
    }
    this$.settleDates <- as.Date(as.character(this$.settleDates),"%Y%m%d")
    this$.daysToSettle <- as.numeric(this$.settleDates - as.Date(this$.gridDateTime))
   #print(this$.settleDates)
   #print(this$.daysToSettle)
})

method("setPricesFromTSDB", "TBAGrid", function(this, ...) {
#Set up the price matrix from TSDB

    tsdb <- TimeSeriesDB()
    #set up prices matrix
    this$.prices <- matrix(NA, nrow = length(this$.couponVector), ncol = length(this$.settleVector))

    for (countCoupon in 1:length(this$.couponVector)){
        for (countSettle in 1:length(this$.settleVector)){
            tickerPrice <- paste(this$.program,this$.couponVector[countCoupon],this$.settleVector[countSettle],'price',sep="_")

            tsdbReturn <- as.numeric(tsdb$retrieveOneTimeSeriesByName(tickerPrice,this$.source, this$.gridDateTime, this$.gridDateTime))
            if(!NROW(tsdbReturn)==0)
                this$.prices[countCoupon,countSettle] <- tsdbReturn
        }
    }
    #print(this$.prices)
})

method("getNDayForwardPrice", "TBAGrid", function(this, nDay, ...) {
#Return the nDayForward prices from the matrix in the same order as the couponVector

    needs(nDay = "numeric")
    nDayVector <- rep(NA,length(this$.couponVector))
    
    for (countCoupon in 1:length(this$.couponVector)){
        #strip out the NA ros and cols
        goodCols <- (!is.na(this$.daysToSettle) & !is.na(this$.prices[countCoupon,]))
        nDayVector[countCoupon] <- qf.interpolate(nDay, this$.daysToSettle[goodCols], this$.prices[countCoupon,goodCols])
    }
    return(nDayVector)
})

method("getCurrentCoupon", "TBAGrid", function(this, ...){
#returns the current coupon from the grid, uses the CMM method for CC but leaves the value as a MEY

    nDays <- 30
    parPrice <- 100
    delayDaysByProgram <- list(fncl=24, fnci=24, fglmc=14, fgci=14, gnsf=14)
    delayDays <- delayDaysByProgram[[this$.program]]

    couponVector <- as.numeric(this$.couponVector)

    #get the forward prices
    fwdPrice <- this$getNDayForwardPrice(nDays)
    
    #Delay adjust
    adjFwdPrice <- fwdPrice + (couponVector * delayDays/360)
    
    goodCols <- !is.na(adjFwdPrice)
    currentCoupon <- qf.interpolate(parPrice, adjFwdPrice[goodCols], couponVector[goodCols])

    return(currentCoupon)
})

method("setPricesFromMatrix", "TBAGrid", function(this, priceMatrix, couponVector, settleVector, ...){
#Populates the price matrix from a given matrix and coupon and settle vectors
# The grid is couponsXsettles

    needs(priceMatrix = "matrix", couponVector = "numeric", settleVector = "character")

    #set up an NA matrix of size of the object
    this$.prices <- matrix(NA, nrow = length(this$.couponVector), ncol = length(this$.settleVector))

    #find the good
    goodCoupons <- match(as.numeric(this$.couponVector), couponVector)
    goodSettles <- match(this$.settleVector, settleVector)

    this$.prices <- priceMatrix[goodCoupons,goodSettles]
})

method("setSettleDatesFromMatrix", "TBAGrid", function(this, settleDateMatrix, settleVector, ...){
#Populates the settleDate and daysToSettle vectors from a grid

    needs(settleDateMatrix = "matrix", settleVector = "character")
    #set up an NA vector
    this$.settleDates <- rep(NA,length(this$.settleVector))

    #find the good
    goodSettles <- match(this$.settleVector, settleVector)

    for (countSettle in 1:length(this$.settleVector)){
        countCoupon <- 1
        while ((countCoupon <= nrow(settleDateMatrix)) && is.na(this$.settleDates[countSettle])){
            this$.settleDates[countSettle] <- settleDateMatrix[countCoupon, goodSettles[countSettle]]
            countCoupon <- countCoupon + 1
        }
    }
    this$.settleDates <- as.Date(as.character(this$.settleDates),"%Y%m%d")
    this$.daysToSettle <- as.numeric(this$.settleDates - as.Date(this$.gridDateTime))
})

method("writeTSDBuploadFile", "TBAGrid", function(this, uploadMethod='file', path=NULL, ...){
#Generate the upload file to be used by the TSDB csv upload process
#Now modified to allow direct upload to TSDB
	
    #setup the path
	if(is.null(path)) path <- tsdbUploadDirectory()
    
    #45d forward price
    tickerName <- paste(this$.program, this$.couponVector, '45d_price', sep="_")
	tsdbValues <- zoo(t(this$getNDayForwardPrice(45)), this$.gridDateTime)
	uploadZooToTsdb(tsdbValues, tickerName, this$.source, uploadMethod=uploadMethod, uploadFilename=squish('TBA_45d_fwdPx_',this$.program,'_',format(this$.gridDateTime,'%Y%m%d')), uploadPath=path)
		
    #30d forward price
    tickerName <- paste(this$.program, this$.couponVector, '30d_price', sep="_")
	tsdbValues <- zoo(t(this$getNDayForwardPrice(30)), this$.gridDateTime)
	uploadZooToTsdb(tsdbValues, tickerName, this$.source, uploadMethod=uploadMethod, uploadFilename=squish('TBA_30d_fwdPx_',this$.program,'_',format(this$.gridDateTime,'%Y%m%d')), uploadPath=path)
		
    #Current Coupon
    tickerName <- paste(this$.program, 'cc_30d_yield', sep="_")
	tsdbValues <- zoo(t(this$getCurrentCoupon()), this$.gridDateTime)
	uploadZooToTsdb(tsdbValues, tickerName, this$.source, uploadMethod=uploadMethod, uploadFilename=squish('TBA_CC_',this$.program,'_',format(this$.gridDateTime,'%Y%m%d')), uploadPath=path)
})