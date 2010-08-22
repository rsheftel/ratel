constructor("Range", function(start = NULL, end = NULL) {
    this <- extend(RObject(), "Range", 
        .start = NULL,
        .end = NULL
    )
    constructorNeeds(this, start = "character|POSIXt", end = "character|POSIXt?")
    if(inStaticConstructor(this)) return(this)
    
    if(is.null(end)) end <- start
    this$.start <- as.POSIXct(start)
    this$.end <- as.POSIXct(makeEndInclusive(end))	
    this
})

method("cut", "Range", function(this, z, ...) { 
    needs(z = "zoo")
    z[ index(z) >= this$.start & index(z) <= this$.end ]
})

method("before", "Range", function(static, end, ...) {
    Range("1800/1/1", as.POSIXct(end)-1)
})

method("after", "Range", function(static, start, ...) {
    Range(as.POSIXct(makeEndInclusive(start))+1, "2500/12/31")
})

method("all", "Range", function(static, ...) {
    Range("1800/1/1", "2500/12/31")
})

method("jRange", "Range", function(this, ...) {
    JRange$by_Date_Date(as.JDate(this$.start), as.JDate(this$.end))
})

method("coveredBy", "Range", function(this, dates, ...) {
    needs(dates="POSIXt")
    any(dates < this$.start) && any(dates > this$.end)
})

method("as.character", "Range", function(this, ...) { 
    squish(ymdHuman(this$.start), " to ", ymdHuman(this$.end))
})

makeEndInclusive <- function(end) {
    needs(end="character|POSIXt?")
    if(is.null(end)) return(end)
    
    if(is.character(end) && length(grep(":", end)) == 0)
        return(as.POSIXct(paste(end, "23:59:59")))

    if(inherits(end, "POSIXt")) {
        end.lt <- as.POSIXlt(end)
        if(end.lt$hour == 0 && end.lt$min == 0 && end.lt$sec == 0) {
            end.lt$hour <- 23
            end.lt$min <- 59
            end.lt$sec <- 59
            return(as.POSIXct(end.lt))
        }
    }
    end
}

method("today", "Range", function(static,...) { 
	as.POSIXct(as.character(Sys.Date()))
})

method("ytd", "Range", function(static,relativeTo = static$today(),...) {
	needs(relativeTo = 'POSIXt')
	Range(as.POSIXct(squish(format(relativeTo,"%Y"),'-01-01')),relativeTo)
})

method("mtd", "Range", function(static,relativeTo = static$today(),...) {
	needs(relativeTo = 'POSIXt')
	Range(as.POSIXct(squish(format(relativeTo,"%Y-%m"),'-01')),relativeTo)
})

method("lastNDays", "Range", function(static,nDays,relativeTo = static$today(),...) { 
	needs(relativeTo = 'POSIXt',nDays = 'numeric')			
	Range(Period('days',nDays)$rewind(relativeTo),relativeTo)
})

method("lastNMonths", "Range", function(static,nMonths,relativeTo = static$today(),...) { 
	needs(relativeTo = 'POSIXt',nMonths = 'numeric')			
	Range(Period('months',nMonths)$rewind(relativeTo),relativeTo)
})

method("last20days", "Range", function(static,relativeTo = static$today(),...){ static$lastNDays(20,relativeTo) })

method("lastMonth", "Range", function(static,relativeTo = static$today(),...){ static$lastNMonths(1,relativeTo) })