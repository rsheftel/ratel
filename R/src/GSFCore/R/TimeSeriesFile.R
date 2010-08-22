library("R.oo")
library("zoo")

constructor("TimeSeriesFile", function() {
    this <- extend(RObject(), "TimeSeriesFile")
})

method("writeTimeSeries", "TimeSeriesFile", function(this, ts.array, file = "", ...) {
    ts.list <- unlist(lapply(
        rownames(ts.array), 
        function(ts) lapply(
            colnames(ts.array), 
            function(ds) ts.array[[ts,ds]] 
        )
    ), recursive = FALSE)
    names(ts.list) <- unlist(lapply(
        rownames(ts.array), 
        function(ts) 
            lapply(colnames(ts.array), 
            function(ds) paste(ts,ds,sep = ":") 
        )
    ) )

    ts.names <- c()
    ts.zoo <- zoo(NULL)
    class(index(ts.zoo)) = c("POSIXt", "POSIXct")  #  Otherwise we get a warning
    for(ts in names(ts.list)) {
        if (!is.null(ts.list[[ts]])) {
            currentIndex <- index(ts.list[[ts]])
            failUnless(
                isType(currentIndex, "POSIXt"), 
                "index of a zoo in ts.array is not date, is ", class(currentIndex), "\n",
                commaSep(head(currentIndex, 20))
            )
            ts.zoo <- merge(ts.zoo, ts.list[[ts]])
            ts.names <- c( ts.names, ts )
        }
    }
    df <- as.data.frame(ts.zoo)
    colnames(df) <- ts.names
    write.csv(df, file = file)
})

method("writeOneTimeSeries", "TimeSeriesFile", function(this, ts, name, data.source, file = "", ...) {
    stopifnot(is(ts, "zoo"))

    ts.array <- array(list(NULL), dim = c(1,1), dimnames = list(c(name), c(data.source)))
    ts.array[[1,1]] <- ts
    TimeSeriesFile$writeTimeSeries(ts.array, file, ...)
})

method("as.ts.array", "TimeSeriesFile", function(this, ts.data, ...) {
    assert(is(ts.data, "data.frame"))
    
    metadata <- strsplit(names(ts.data), ":")
    ts.list <- unlist(unique(lapply(metadata[-1], function(x) x[[1]])))
    ds.list <- unlist(unique(lapply(metadata[-1], function(x) x[[2]])))

    tz <- metadata[1][[1]]
    if(length(tz) == 0 || any(grep("date", tz, ignore.case = TRUE)))
        tz <- ""
    assert(tz == "", "time zone functionality is DISABLED.  All times must be in New York time, and no time zone can be specified.")

    ts.array <- array(list(NULL), dim = c(length(ts.list), length(ds.list)), dimnames = list(ts.list, ds.list))
    for(ts in ts.list) {
        for(ds in ds.list) {
            ts.name <- paste(ts, ds, sep=":")
            if(!is.null(ts.data[, ts.name])) {
                ts.array[[ts, ds]] <- na.omit(zoo(as.matrix(ts.data[, paste(ts, ds, sep=":")]), order.by = as.POSIXct(ts.data[,1], tz = tz)))
                attr(ts.array[[ts, ds]], "na.action") <- NULL  # This busts our tests.  Shouldn't matter
            }
        }
    }

    ts.array
})

method("readTimeSeries", "TimeSeriesFile", function(this, file = stdin(), ...) {
    ts.data <- read.csv(file, check.names = FALSE, stringsAsFactors = FALSE, na.strings = c("NA", "na", "N/A", "n/a", "NULL", "null", "#N/A"))
    if(tolower(first(colnames(ts.data))) == "name")
        ts.data <- this$.convertTimeSeriesPivotedFormat(ts.data)
    if(any(is.na(colnames(ts.data))))
        ts.data <- ts.data[-which(is.na(colnames(ts.data)))]
    tsNames <- lapply(
        strsplit(colnames(ts.data), ':'), 
        function(parts) { ifElse(length(parts) > 1, first(parts), parts) }
    )
    if (any(tsNames == 'NA'))
        ts.data <- ts.data[-which(tsNames == 'NA')]
    TimeSeriesFile$as.ts.array(ts.data)
})

method(".convertTimeSeriesPivotedFormat", "TimeSeriesFile", function(this, ts.data, ...) {
    needs(ts.data="data.frame")
    failUnless(all(tolower(colnames(ts.data)[1:4]) == c("name", "source", "date", "value")), "columns in pivot format must be name, source, date, value.  yours are ", humanString(colnames(ts.data)[1:4]))
    seriesSources <- unique(paste(ts.data$name, ts.data$source, sep=":"))
    dates <- unique(ts.data$date)
    columns <- c(list(dates), lapply(seriesSources, function(x) rep(NA, length(dates))))
    df <- do.call(data.frame, columns)
    colnames(df) <- c("Date", seriesSources)
    rownames(df) <- dates
    for(i in seq_along(first(ts.data))) {
        df[as.character(ts.data[[i, 3]]), paste(ts.data[[i, 1]], ts.data[[i, 2]], sep=":")] <- ts.data[[i, 4]]
    }
    df
})

method("readOneTimeSeries", "TimeSeriesFile", function(this, file = stdin(), ...) {
    result.array <- this$readTimeSeries(file, ...)
    stopifnot(ncol(result.array) == 1 && nrow(result.array) == 1)

    result.array[[1,1]]
})


method("createTimeSeries", "TimeSeriesFile", function(this, tsdb, file, ...) {
    needs(file="character", tsdb="TimeSeriesDB")
    on.exit(JDb$rollback())
    JTimeSeries$createFromFile_by_String(file)
    JDb$commit()
})

method("createTimeSeriesIfNotExists", "TimeSeriesFile", function(this, tsdb, file, ...) {
        needs(file="character", tsdb="TimeSeriesDB")
        on.exit(JDb$rollback())
        JTimeSeries$createFromFile_by_String_boolean(file, TRUE)
        JDb$commit()
    })

