# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

constructor('LiborInterpolator', function(this, start = NULL, end = NULL){
	this <- extend(RObject(), 'LiborInterpolator', .start = start, .end = end)
	if(inStaticConstructor(this)) return(this)
	
	this$.liborTable <- this$loadLiborRates()	
	this		
})

method('liborTable', 'LiborInterpolator', function(this, ...){
	this$.liborTable		
})

method('loadLiborRates', 'LiborInterpolator', function(this, source = 'internal',...){
	getTermStructureForTimeSeries('libor_usd_rate_tenor', TermStructure$libor, startDate = this$.start,
	end = this$.end, source = source, lookFor = 'tenor')	
})

method('buildFincadTable', 'LiborInterpolator', function(this, curveDate, tenors = TermStructure$libor, ccy = 'usd', 
		holidayCenter = 'nyb', holidaySource = 'financialcalendar',...){
	scb <- SwapCurveBuilder(ccy = ccy, holidayCenter = holidayCenter, holidaySource = holidaySource)
	rates <- as.numeric(this$liborTable()[as.POSIXct(paste(curveDate, "15:00:00",sep = " ")),])		
	fincadTable <- scb$buildFincadTable(curveDate, rates, tenors, TRUE)
	daysFromEffDate <- as.numeric(as.Date(fincadTable[,'term_date']) - as.Date(fincadTable[,'eff_date']))
	cbind(fincadTable, daysFromEffDate)
})

method('interpolatedRate', 'LiborInterpolator', function(this, curveDate, expiryDate, maxDaysToSearch = 3, tenors = TermStructure$libor, ccy = 'usd',
		holidayCenter = 'nyb', holidaySource = 'financialcalendar', ...){
		currentCurveDate <- as.POSIXct(curveDate)
		expiryDate <- as.POSIXct(expiryDate)
		output <- NA
		dayShift <- 0
		
		while(is.na(output)){
			if(dayShift > maxDaysToSearch) return(NA)
			fincadTable <- this$buildFincadTable(currentCurveDate, tenors, ccy, holidayCenter, holidaySource)
			daysToExpiry <- as.numeric(as.Date(expiryDate) - as.Date(fincadTable[1,'eff_date']))
			output <- qf.interpolate(daysToExpiry, fincadTable[,'daysFromEffDate'],fincadTable[,'rate'])
			currentCurveDate <- as.POSIXlt(currentCurveDate)
			currentCurveDate$mday <- currentCurveDate$mday + 1
			currentCurveDate <- as.POSIXct(currentCurveDate)
		    dayShift <- dayShift + 1
		}					  
		output		
})
