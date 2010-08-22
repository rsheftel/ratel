constructor("Roll", function(rollMethod = NULL) {
    this <- extend(RObject(), "Roll", .rollMethod = rollMethod)
    constructorNeeds(this,rollMethod = "function")
    this
})

method("rollDates", "Roll", function(this,expiryDates,...) {
    this$.rollMethod(expiryDates,...)
})

daysToExpiry <- function(expiryDates,nbDays = 5,holidayCenter = "nyb",...){
    needs(expiryDates = "character|POSIXt")
    as.POSIXct(unlist(lapply(as.character(expiryDates),function(x){as.character(businessDaysAgo(nbDays,x,holidayCenter))})))
}

calendarDaysToExpiry <- function(expiryDates,nbDays = 5,...){
	needs(expiryDates = "character|POSIXt")
	period <- Period("days",nbDays)
	as.POSIXct(unlist(lapply(as.character(expiryDates),function(x){as.character(period$rewind(as.POSIXct(x)))})))
}