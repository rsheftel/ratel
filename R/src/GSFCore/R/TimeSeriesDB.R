# TODO: Add some kind of configuration object for DB params?

setConstructorS3("TimeSeriesDB", function() {
    extend(RObject(), "TimeSeriesDB")
})

method("retrieveOneTimeSeriesByName", "TimeSeriesDB", function(this, name, data.source = NULL, start = NULL, end = NULL, ...) {
    ts <- this$retrieveTimeSeriesByName(name, data.source, start, end, ...)
    failIf(length(ts) > 1, "too many series named ", name)
    if (length(ts) == 0) {
        failUnless(this$timeSeriesExists(name), "no time series for ", name)         
        return(zoo(c()))
    }
    the(ts)
})

method(".jSeriesList", "TimeSeriesDB", function(class, seriesNames, ...) {
    lapply(seriesNames, function(seriesName) JTimeSeries$series_by_String(seriesName))
})

method("retrieveTimeSeriesByName", "TimeSeriesDB", function(this, name, data.source = NULL, start = NULL, end = NULL, ...) {
    series <- this$.jSeriesList(name)
    series <- series[sapply(series, exists)]
    if(length(series) == 0) return(NULL)

    range <- this$.jRange(start, end)
    if(is.null(data.source)) {
        map <- JTimeSeriesDataTable$observationsMap_by_Range_TimeSeriesArray(range, series)
    } else {
        source <- JDataSource$source_by_String(data.source)
        map <- JTimeSeriesDataTable$observationsMap_by_DataSource_Range_TimeSeriesArray(source, range, series)
    }
    this$.jObservationsMapToArray(map, sapply(series, function(s) s$name()))
})

method(".jRange", "TimeSeriesDB", function(class, start, end, ...) {
    if(is.null(start)) start <- as.POSIXct("1753-01-01")
    if(is.null(end)) end <- as.POSIXct("9999-12-31")
    JRange$range_by_Date_Date(as.JDate(as.POSIXct(start)), as.JDate(as.POSIXct(makeEndInclusive(end))))
})

method(".jObservationsMapToArray", "TimeSeriesDB", function(class, map, series = NULL, ...) {
    omap <- map$as(JObservationsMap())
    if(omap$isEmpty()) return(NULL)
    ts.list <- stringsVector(map$seriesNames())
    if(is.null(series)) series <- ts.list
    else series <- series[series %in% ts.list]
    ds.list <- stringsVector(map$sourceNames())
    result <- array(list(NULL), dim = c(length(ts.list), length(ds.list)), dimnames = list(series, ds.list))
    for(ts in ts.list) {
        for(ds in ds.list) {
            ss <- class$.jSeriesSource(ts,ds)
            if(!omap$has_by_Object(ss)) next;
            obs <- omap$get_by_Object(ss)
            result[[ts, ds]] <- observations.zoo(obs)
        }
    }
    result
})

method("retrieveOneTimeSeriesByAttributeList", "TimeSeriesDB", function(this, attributes, data.source = NULL, start = NULL, end = NULL, ...) {
    result.array <- this$retrieveTimeSeriesByAttributeList(attributes, data.source, start, end, ...)
    assert(ncol(result.array) == 1 && nrow(result.array) == 1)
    result.array[[1,1]]
})

method("retrieveTimeSeriesByAttributeList", "TimeSeriesDB", function(this, attributes, data.source = NULL, start = NULL, end = NULL, arrange.by = NULL, ...) {
    assert(is.null(arrange.by) || (!is.null(data.source) && length(data.source) == 1), "if arrange.by is specified, exactly one data.source must be specified")

    values <- this$.jAttributeValues(attributes)
    if(!JTimeSeries$exists_by_AttributeValues(values)) return(NULL)
    range <- this$.jRange(start, end)
    if(is.null(data.source)) {
        map <- JTimeSeriesDataTable$observationsMap_by_Range_AttributeValues(range, values)
    } else {
        source <- JDataSource$source_by_String(data.source)
        map <- JTimeSeriesDataTable$observationsMap_by_DataSource_Range_AttributeValues(source, range, values)
    }
    result <- this$.jObservationsMapToArray(map)
    if(!is.null(arrange.by) && !is.null(result))
        result <- this$arrangeByAttributes(result, arrange.by)
    result
})

method("lookupTimeSeriesByAttributeList", "TimeSeriesDB", function(this, attributes, ...) {
    jList <- JTimeSeriesTable$TIME_SERIES()$names_by_AttributeValues(this$.jAttributeValues(attributes))
    unlist(japply(jList$iterator(), JString(), toString)) 
})

method(".jSeriesSource", "TimeSeriesDB", function(class, seriesName, sourceName, ...) {
    JTimeSeries$series_by_String(seriesName)$with_by_DataSource(JDataSource$source_by_String(sourceName))
})

method("numObservations", "TimeSeriesDB", function(this, name, data.source, ...) {
    this$.jSeriesSource(name, data.source)$count()
})

method("purgeTimeSeries", "TimeSeriesDB", function(this, name, data.source, ...) {
    on.exit(JDb$rollback())
    this$.jSeriesSource(name, data.source)$purge()
    JDb$commit()
})

method("timeSeriesExists", "TimeSeriesDB", function(this, name, ...) {
    JTimeSeries$series_by_String(name)$exists()
})

method(".deleteTimeSeries", "TimeSeriesDB", private = TRUE, function(this, name, are.you.sure = FALSE, ...) {
    assert(are.you.sure)
    on.exit(JDb$rollback())
    series <- JTimeSeries$series_by_String(name)
    if(!series$exists()) return()
    series$purgeAllData()
    series$delete()
    JDb$commit()
})

method(".jAttributeValues", "TimeSeriesDB", function(class, attributes, ...) {
    valuesList <- lapply(names(attributes), function(attr) JAttribute$attribute_by_String(attr)$value_by_StringArray(as.character(attributes[[attr]])))
    values <- JAttributeValues$values_by_AttributeValueArray(valuesList)
})

method("createTimeSeries", "TimeSeriesDB", function(this, name, attributes, ...) {
    on.exit(JDb$rollback())
    JTimeSeries$series_by_String(name)$create_by_AttributeValues(this$.jAttributeValues(attributes))
    JDb$commit()
})

method("lookupSeriesIDs", "TimeSeriesDB", function(this, tsNames, ...) {
	needs(tsNames = 'character')
	as.numeric(sapply(tsNames,function(x){JTimeSeries$series_by_String(x)$id()}))
})

method("lookupSeriesNames", "TimeSeriesDB", function(this, ids, ...) {
	needs(ids = 'numeric')
	as.character(sapply(ids,function(x){JTimeSeries$series_by_Integer(JInteger$by_int(x))$name()}))
})

method("lookupAttributesForTimeSeries", "TimeSeriesDB", function(this, time.series, attributes = NULL, ...) {
    series <- this$.jSeriesList(time.series)
    series <- series[sapply(series, exists, USE.NAMES=FALSE)]
    valuesList <- lapply(series, function(s) s$attributes())
    if(is.null(attributes)) {
        attrSet <- first(valuesList)$attributes()
        attrSet <- JHashSet$by_Collection(attrSet)
        class(attrSet) <- class(JSet())
        for(values in valuesList[-1])
            attrSet$addAll_by_Collection(values$attributes())
        attributes <- japply(attrSet$iterator(), JAttribute(), name)
    }
    result <- matrix(as.character(NA), nrow=length(series), ncol=length(attributes))
    rownames(result) <- sapply(series, name)
    colnames(result) <- attributes
    for(seriesIndex in seq_along(series)) {
        for(a in attributes) {
            jAttribute <- JAttribute$attribute_by_String(a)
            values <- valuesList[[seriesIndex]]
            if(values$has_by_Attribute(jAttribute)) {
                s <- series[[seriesIndex]]
                result[[s$name(), a]] <- values$get_by_Attribute(jAttribute)$name()
            }
        }
    }
    zoo(result, order.by=rownames(result))
})

method("arrangeByAttributes", "TimeSeriesDB", function(this, ts.array, attributes, ...) {
    assert(ncol(ts.array) == 1)

    ts.attr <- this$lookupAttributesForTimeSeries(rownames(ts.array), attributes)
    dimnames <- sapply(attributes, function(attr.name) unique(ts.attr[, attr.name]), simplify = FALSE)
    dim <- sapply(dimnames, length, USE.NAMES = FALSE)

    result <- array(list(NULL), dim, dimnames)
    for(ts in rownames(ts.array)) {
        coordinates <- matrix(mapply(function(val, dims) match(val, dims), ts.attr[ts, names(dimnames)], dimnames), ncol = length(dim))
        assert(is.null(result[coordinates][[1]]), "only one time series can be in a cell - did you specify enough dimensions?")
        result[coordinates] <- ts.array[ts, 1]
    }

    result
})

method("createAndWriteOneTimeSeriesByName", "TimeSeriesDB", function(this, ts, name, data.source, attributes,...) {
    if(!this$timeSeriesExists(name))this$createTimeSeries(name,attributes)
    this$writeOneTimeSeriesByName(ts,name,data.source)
})

method("writeOneTimeSeriesByName", "TimeSeriesDB", function(this, ts, name, data.source,...) {
    this$writeTimeSeries(array(list(ts), c(1,1), list(name, data.source)))
})

method(".jObservations", "TimeSeriesDB", function(this, z, ...) {
    needs(z="zoo")
    if(!inherits(index(z), "POSIXct")) index(z) <- as.POSIXct(index(z))
    JObservations$by_longArray_doubleArray(millis.from.POSIXct(index(z)), as.vector(z))
})

method("writeTimeSeries", "TimeSeriesDB", function(this, ts.array, ...) {
    on.exit(JDb$rollback())
	numObs <- 0
    for(ts.name in rownames(ts.array)){
        for(ds.name in colnames(ts.array)) {
            if(is.null(ts.array[[ts.name, ds.name]])) next;
			numObs <- numObs + length(ts.array[[ts.name, ds.name]])
		}
	}
	if (numObs == 0) return()
	series <- vector("character", numObs)
	source <- vector("character", numObs)
	timesMillis <- vector("numeric", numObs)
	values <- vector("numeric", numObs)
	count <- 1
    for(ts.name in rownames(ts.array)) {
        for(ds.name in colnames(ts.array)) {
			z <- ts.array[[ts.name, ds.name]]
            if(is.null(z)) next;
			len <- length(z)
            if(len == 0) next;
			range <- count:(count+len-1)
			series[range] <- ts.name
			source[range] <- ds.name			
			timesMillis[range] <- millis.from.POSIXct(as.POSIXct(index(z)))
			values[range] <- as.vector(z)
			count <- count + len
        }
    }
    for(i in 1:(ceiling(numObs/10000))) {
        lower <- (i-1) * 10000 + 1
        upper <- min(lower + 9999, numObs)
	    JTimeSeriesDataTable$writeFromR_by_StringArray_StringArray_longArray_doubleArray(
            series[lower:upper], 
            source[lower:upper], 
            timesMillis[lower:upper], 
            values[lower:upper]
        )
    }
    JDb$commit()
})

dumpSeries <- function(name = NULL, id = NULL) { 
    needs(name="character?", id="numeric?")
    if (!is.null(name)) JTimeSeries$main_by_StringArray(name)
    else JTimeSeries$main_by_StringArray(id)
}

group <- function(name, date) { 
    needs(name="character", date="character|POSIXct")
    date = as.POSIXct(date)
    
    JTimeSeriesGroupStatus$main_by_StringArray(c("-group", name, "-date", ymdHuman(date)))
}