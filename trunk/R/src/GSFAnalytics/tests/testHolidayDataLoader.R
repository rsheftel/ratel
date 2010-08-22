## Test file for the HolidayDataLoader object
library("GSFAnalytics")

testHolidayDataLoader <- function()
{
    # tests bad inputs

    holidaySample <- HolidayDataLoader()
    shouldBomb(holidaySample$getHolidays())
    shouldBomb(holidaySample$getHolidays(source = "ficalendar",financialCenter = "nyb",startDate = "1987-09-07",endDate = "1988-01-01"))
    shouldBomb(holidaySample$getHolidays(source = "financialcalendar",financialCenter = "ny",startDate = "1987-09-07",endDate = "1988-01-01"))
    shouldBomb(holidaySample$getHolidays(source = "financialcalendar",financialCenter = "nyb",startDate = TRUE,endDate = "1988-01-01"))
    shouldBomb(holidaySample$getHolidays(source = "financialcalendar",financialCenter = "nyb",startDate = "1987-09-07",endDate = FALSE))
    shouldBomb(holidaySample$getHolidays(source = "financialcalendar",financialCenter = "nyb",startDate = "1987-09-07",endDate = "1980-09-07"))
      
    # tests good inputs

    holidaySample <- HolidayDataLoader()
    d <- holidaySample$getHolidays(source = "financialcalendar",financialCenter = "nyb",startDate = "1987-09-07",endDate = "1988-01-01")     

    target <- as.POSIXct(c("1987-09-07 Eastern Daylight Time","1987-10-12 Eastern Daylight Time",
        "1987-11-11 Eastern Standard Time","1987-11-26 Eastern Standard Time","1987-12-25 Eastern Standard Time","1988-01-01 Eastern Standard Time"))

    checkEquals(target,d)
}

testHolidayDataLoaderHandleParameterLists <- function()
{
    holidaySample <- HolidayDataLoader()
    d <- holidaySample$getHolidays(source = "financialcalendar",financialCenter = c("lnb","nyb"),startDate = "2008-02-14",endDate = "2008-03-25")      
    target <- as.POSIXct(c("2008-02-18","2008-03-21","2008-03-24"))
    checkEquals(target,d)
}


test.HolidayDataLoader.getHolidaysToTenor <- function(){
    
    holidaySample <- HolidayDataLoader()

    result <- holidaySample$getHolidaysToTenor(
        source = "financialcalendar",
        financialCenter = "nyb",
        tenorNumeric = 1,
        startDate = "2007-11-06",
        endDate = "2007-11-07"
    )

    target = holidaySample$getHolidays("financialcalendar","nyb","2007-11-06","2009-11-07")

    checkEquals(result, target)

    result <- holidaySample$getHolidaysToTenor(
        source = "financialcalendar",
        financialCenter = "nyb",
        tenorNumeric = 1,
        startDate = "2007-11-06",
        endDate = NULL
    )

    target = holidaySample$getHolidays("financialcalendar","nyb","2007-11-06",as.Date(Sys.Date()) + 2 * 365)

    checkEquals(result, target)
}


          
