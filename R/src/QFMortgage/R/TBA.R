constructor("TBA", function() {
    extend(RObject(), "TBA")      
})

method("frontSettle", "TBA", function(static, program, date, ...) {
    needs(program="character", date="POSIXt|character")
    date <- as.POSIXlt(date)
    jdate <- JDates$date_by_String(strftime(date, "%Y/%m/%d"))
    as.POSIXct(JDates$yyyyMmDd_by_Date(JTbaTable$TBA()$frontSettle_by_String_Date(program, jdate)))
})

method("frontNotificationDate", "TBA", function(static, program, date, ...) {
    needs(program="character", date="POSIXt|character")
    date <- as.POSIXlt(date)
    jdate <- JDates$date_by_String(strftime(date, "%Y/%m/%d"))
    as.POSIXct(JDates$yyyyMmDd_by_Date(JTbaTable$TBA()$frontNotificationDate_by_String_Date(program, jdate)))
})

method("couponVector", "TBA", function(static, program, group='all', ...){
	needs(program='character', group='character')
	if (group=='all')
		switch(program,
			fncl  = return(seq(3.5, 8.5, 0.5)),
			fglmc = return(seq(3.5, 8.5, 0.5)),
			gnsf  = return(seq(3.5, 8.5, 0.5)),
			fnci  = return(seq(3.5, 8.0, 0.5)),
			fgci  = return(seq(3.5, 8.0, 0.5)),
			return('Not a valid program')
			)
	if (group=='active')
		switch(program,
			fncl  = return(seq(4.0, 6.5, 0.5)),
			fglmc = return(seq(4.0, 6.5, 0.5)),
			gnsf  = return(seq(4.0, 6.5, 0.5)),
			fnci  = return(seq(4.0, 6.0, 0.5)),
			fgci  = return(seq(4.0, 6.0, 0.5)),
			return('Not a valid program')
			)
})

method("startEndDatesForCoupons", "TBA", function(static, program, couponVector, startDates=NULL, endDates=NULL,...){
#Set up the start and end date
	
	needs(program='character', couponVector="numeric", startDates="POSIXct|character?", endDates="POSIXct|character?")
	
	#Set up the default ranges
	#FNCL
	datesCoupons <- seq(3.5,8.5,0.5)
	datesStart <- as.POSIXct(c('1970-01-01','2008-12-22','2003-06-01','2003-04-01','2000-01-03','1993-08-16','1992-11-20','1992-07-23','1970-01-01','1970-01-01','1970-01-01'))
	datesEnd <- as.POSIXct(c('1970-01-01','2100-12-31','2100-12-31','2100-12-31','2100-12-31','2100-12-31','2100-12-31','2008-12-19','2003-09-01','2003-06-01','2001-12-31'))
	datesFncl <- list(coupons=datesCoupons, startDates=datesStart, endDates=datesEnd)
	
	#FNCI
	datesCoupons <- seq(3.5,8.0,0.5)
	datesStart <- as.POSIXct(c('1970-01-01','2003-04-01','2002-12-05','2002-03-01','1970-01-01','1970-01-01','1970-01-01','1970-01-01','1970-01-01','1970-01-01'))
	datesEnd <- as.POSIXct(c('1970-01-01','2100-12-31','2100-12-31','2100-12-31','2100-12-31','2100-12-31','2004-03-31','2003-06-30','2003-04-30','2001-01-31'))
	datesFnci <- list(coupons=datesCoupons, startDates=datesStart, endDates=datesEnd)
	
	defaultDateRanges <- list(fncl=datesFncl,fnci=datesFnci)
	
	if (is.null(startDates)){
		dateList <- defaultDateRanges[[program]]
		startDates <- dateList$startDates[match(couponVector,dateList$coupons)]
	}
	
	if (is.null(endDates)){
		dateList <- defaultDateRanges[[program]]
		endDates <- dateList$endDates[match(couponVector,dateList$coupons)]
	}
	
	return(list(startDates=startDates, endDates=endDates))
})

method("cut", "TBA", function(static, data.zoo, program, coupons, ...){
	needs(data.zoo="zoo", program='character', coupons='numeric|integer')
	startEndDates <- static$startEndDatesForCoupons('fncl',coupons)
	return(Range(max(startEndDates$startDates), min(startEndDates$endDates))$cut(data.zoo))
})
