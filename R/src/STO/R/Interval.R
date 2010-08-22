constructor("Interval", function(name = NULL, firstOfInterval = function() fail("no function supplied to calculate beginning of interval")) {
    this <- extend(RObject(), "Interval", .units = NULL, .numUnits = NULL, .name = name, .firstOfInterval = firstOfInterval)
    constructorNeeds(this, name = "character", firstOfInterval = "function")
    if(inStaticConstructor(this)) return(this)

    if(name %in% c("yearly", "monthly", "weekly", "daily", "tick")) {
        this$.units <- Interval$.unitsMapping()[[name]]
        this$.numUnits <- 1
    } else if(length(grep("minute$", name, perl = TRUE)) == 1) {
        this$.units <- Interval$.unitsMapping()[["minute"]]
        num <- regexpr("^\\d+", name, perl = TRUE)
        if(num == -1) this$.numUnits <- 1
        else this$.numUnits <- as.numeric(substr(name, num, attr(num, "match.length")))
    } else {
        fail("unknown interval: ", name)
    }
    this
})

method(".unitsMapping", "Interval", function(this, ...) {
    c(tick = 1, minute = 2, daily = 3, weekly = 4, monthly = 5, yearly = 6)
})

method("greaterEqual", "Interval", function(this, that, ...) {
    needs(that = "Interval")
    this$.units > that$.units || (this$.units == that$.units && this$.numUnits >= that$.numUnits)
})

method("as.character", "Interval", function(this, ...) {
    this$.name
})

.setDateParts <- function(...) {
    settings <- list(...)
    settings$isdst <- -1
    function(d) {
        lt <- as.POSIXlt(d)
        if(!is.null(settings$wday))
            lt <- as.POSIXlt(match.fun("-.POSIXt")(lt, as.difftime(lt$wday - settings$wday, units="days")))
        for(part in names(settings))
            lt[[part]] <- settings[[part]]
        as.POSIXct(lt)
    }
}

Interval$MONTHLY <- Interval("monthly", .setDateParts(mday=1, hour=0, min=0, sec=0))
Interval$YEARLY <- Interval("yearly", .setDateParts(mon=0, mday=1, hour=0, min=0, sec=0))
Interval$DAILY <- Interval("daily", .setDateParts(hour=0, min=0, sec=0))
Interval$WEEKLY <- Interval("weekly", .setDateParts(wday=1, hour=0, min=0, sec=0))

method("collapse", "Interval", function(this, z, aggregationFunc, ...) {
    needs(z="zoo", aggregationFunc="function")
    myAggregate(z, this$.firstOfInterval, aggregationFunc, ...)
})

myAggregate <- function(x, by, FUN)
{
    needs(x = "zoo", by="function", FUN="function")
    my.unique <- function(x) x[MATCH(x, x) == seq(length = length(x))]
    my.sort <- function(x) x[order(x)]
    by <- by(index(x))
    checkLength(index(x), length(by))
    df <- aggregate(coredata(x), list(by), FUN)
    df <- df[, -1]
    zoo(df, my.sort(my.unique(by)))
}
