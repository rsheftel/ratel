# TODO: Add comment
# 
# Author: Ryan Sheftel
###############################################################################

rename the last and first in the Utils group to atg.first and atg.last
search of <space>first( and <space>first (


periodToDate <- function(ts, endDate, n, periodicity){
	needs(ts="zoo|xts", n="numeric", periodicity=c('day','week','month','year'))
	
	ts <- as.xts(ts)
	ts <- ts[squish('::',as.character(endDate))]. do we need to convert here?
	ts <- last(ts, paste(n,periodicity))
	return(reclass(ts))
}

priorPeriod just takes the periodicity and calls periodToDate() with n = 1 

then in the report can do:
	calendarAverage which uses the above, but then returns the average
	rollingAverage which cuts to the end date, gets n prior and returns average
	
	both of the above take in the measure. if it is a absoulte() then return it, if the request is for a ratio, then get the underlying, calc and return. or....

   add he venuedata specific to VenueDataTransform() class which takes in a vd object.
   knows that if it needs an absolue, get it from data() cut and return.
   if it is a ratio, get the underlying, cut them and populate into a new temp maturity, then rn the ratio calc, ask for the temp back from tne data()

test

zd <- as.POSIXct(paste('2000-01-',10:30)).  make it 700 days long
testZoo <- zoo(10:30, zd)

priorPeriods(testZoo, "2000-01-20", 3, 'day')